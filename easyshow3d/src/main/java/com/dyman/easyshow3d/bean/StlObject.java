package com.dyman.easyshow3d.bean;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import com.dyman.easyshow3d.R;
import com.dyman.easyshow3d.imp.ModelLoaderListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.dyman.easyshow3d.utils.LoadUtil.adjust_coordinate;

/**
 * Created by dyman on 16/7/25.
 */
public class StlObject extends ModelObject {

    private static final String TAG = "StlObject";

    private byte[] stlBytes = null;
    List<Float> normalList;
    private int vertext_size = 0;
    //优化使用的数组
    private float[] normal_array = null;
    private float[] vertex_array = null;

    private ModelLoaderListener listener;
    private AsyncTask<byte[], Integer, float[]> task;
    private int totalLines = 0;


    public StlObject(byte[] stlBytes, Context context, int drawMode, ModelLoaderListener listener) {
        this.modelType = "stl";

        this.stlBytes = stlBytes;
        this.listener = listener;
        this.drawWay = drawMode;
        parseModel(stlBytes, context);
    }


    /**
     * 初始化顶点坐标和着色数据的方法
     */
    @Override
    public void initVertexData(float[] vertices, float[] normals) {
        vCount = vertices.length / 3;

        ByteBuffer normal = ByteBuffer.allocateDirect(normals.length * 4);
        normal.order(ByteOrder.nativeOrder());
        mNormalBuffer = normal.asFloatBuffer();
        mNormalBuffer.put(normals);
        mNormalBuffer.position(0);

        //=================矫正中心点坐标========================
        float center_x=(maxX+minX)/2;
        float center_y=(maxY+minY)/2;
        float center_z=(maxZ+minZ)/2;

        for(int i=0;i<vertext_size*3;i++){
            adjust_coordinate(vertices,i*3,center_x);
            adjust_coordinate(vertices,i*3+1,center_y);
            adjust_coordinate(vertices,i*3+2,center_z);
        }

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);//操作系统直接分配Buffer,访问速度更快
        vbb.order(ByteOrder.nativeOrder());//统一根据设备设置ByteOrder
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);//游标回置

    }


    /**
     *  读出模型的三角面片个数
     *      byte[] 转 int
     * @param bytes
     * @param offset
     * @return
     */
    private int getIntWithLittleEndian(byte[] bytes, int offset) {
        return (0xff & stlBytes[offset]) | ((0xff & stlBytes[offset + 1]) << 8) | ((0xff & stlBytes[offset + 2]) << 16) | ((0xff & stlBytes[offset + 3]) << 24);
    }


    private boolean isText(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0x0a || b == 0x0d || b == 0x09) {//对应 回车键 和 tab键
                // white spaces
                continue;
            }
            if (b < 0x20 || (0xff & b) >= 0x80) {
                // control codes
                return false;
            }
        }
        return true;
    }


    /**
     *  模型解析
     * @param data
     * @param context
     */
    @Override
    public void parseModel(byte[] data, final Context context) {
        listener.loadBegin();

        maxX = Float.MIN_VALUE;
        maxY = Float.MIN_VALUE;
        maxZ = Float.MIN_VALUE;
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        minZ = Float.MAX_VALUE;

        normalList = new ArrayList<Float>();


        task = new AsyncTask<byte[], Integer, float[]>() {

            //  ASCII文件格式
            float[] processText(String stlText) throws Exception {
                normalList.clear();

                String[] stlLines = stlText.split("\n");
                vertext_size = (stlLines.length-2)/7;//每个facet(表示一个三角形数据)由7行数据组成， 再整体减去首句和末句。
                vertex_array = new float[vertext_size*9];//vertext_size为三角形个数，每个顶点3个坐标
                normal_array = new float[vertext_size*9];
                totalLines = stlLines.length;

                int normal_num = 0;
                int vertex_num = 0;
                for (int i = 0; i < totalLines; i++) {
                    String string = stlLines[i].trim();
                    if (string.startsWith("facet normal ")) {
                        string = string.replaceFirst("facet normal ", "");
                        String[] normalValue = string.split(" ");
                        for(int n = 0; n < 3;n++){   //为什么要循环3次？是为了和三角形的三个顶点配对？
                            normal_array[normal_num++] = Float.parseFloat(normalValue[0]);
                            normal_array[normal_num++] = Float.parseFloat(normalValue[1]);
                            normal_array[normal_num++] = Float.parseFloat(normalValue[2]);
                        }
                    }
                    if (string.startsWith("vertex ")) {
                        string = string.replaceFirst("vertex ", "");
                        String[] vertexValue = string.split(" ");
                        float x = Float.parseFloat(vertexValue[0]);
                        float y = Float.parseFloat(vertexValue[1]);
                        float z = Float.parseFloat(vertexValue[2]);
                        adjustMaxMin(x, y, z);
                        vertex_array[vertex_num++] = x;
                        vertex_array[vertex_num++] = y;
                        vertex_array[vertex_num++] = z;
                    }

                    if (i % (stlLines.length / 50) == 0) {
                        publishProgress(i);
                    }
                }
                return vertex_array;
            }

            //二进制格式  80+4+i*50
            float[] processBinary(byte[] stlBytes) throws Exception {

                vertext_size = getIntWithLittleEndian(stlBytes, 80);;
                vertex_array = new float[vertext_size*9];
                normal_array = new float[vertext_size*9];

                totalLines = vertext_size;

                for (int i = 0; i < totalLines; i++) {
                    for(int n = 0; n < 3; n++){
                        normal_array[i*9+n*3] = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50));
                        normal_array[i*9+n*3+1] = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 4));
                        normal_array[i*9+n*3+2] = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 8));
                    }
                    float x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 12));
                    float y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 16));
                    float z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 20));
                    adjustMaxMin(x, y, z);
                    vertex_array[i*9] = x;
                    vertex_array[i*9+1] = y;
                    vertex_array[i*9+2] = z;

                    x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 24));
                    y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 28));
                    z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 32));
                    adjustMaxMin(x, y, z);
                    vertex_array[i*9+3] = x;
                    vertex_array[i*9+4] = y;
                    vertex_array[i*9+5] = z;

                    x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 36));
                    y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 40));
                    z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 44));
                    adjustMaxMin(x, y, z);
                    vertex_array[i*9+6] = x;
                    vertex_array[i*9+7] = y;
                    vertex_array[i*9+8] = z;

                    if (i % (totalLines / 100) == 0) {
                        publishProgress(i);
                    }
                }

                return vertex_array;
            }

            @Override
            protected float[] doInBackground(byte[]... stlBytes) {
                float[] processResult = null;
                try {
                    if (isText(stlBytes[0])) {
                        Log.i("StlObject", "trying text...");
                        processResult = processText(new String(stlBytes[0]));
                    } else {
                        Log.i("StlObject", "trying binary...");
                        processResult = processBinary(stlBytes[0]);
                    }
                } catch (Exception e) {
                }
                if (processResult != null && processResult.length > 0 && normal_array != null && normal_array.length > 0) {
                    return processResult;
                }

                return processResult;
            }

            @Override
            public void onProgressUpdate(Integer... values) {
                float progress = (float) values[0] / (float) totalLines;
                DecimalFormat df = new DecimalFormat("#.00");
                float f = Float.valueOf(df.format(progress));
                listener.loadedUpdate(f);
            }

            @Override
            protected void onPostExecute(float[] vertexList) {

                if (normal_array.length < 1 || vertex_array.length < 1) {
                    Toast.makeText(context, context.getString(R.string.easy_show_error_fetch_data), Toast.LENGTH_LONG).show();
                    return;
                }

                listener.loadedFinish(StlObject.this);
                initVertexData(vertex_array, normal_array);

            }
        };

        try {
            task.execute(stlBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void cancelTask() {
        if (task != null && !task.isCancelled()) {
            if (task.cancel(true)) {
                Log.e(TAG, "model's analysis task already cancel!");
                if (listener != null) {
                    listener.loaderCancel();
                }
            }
        }
    }
}
