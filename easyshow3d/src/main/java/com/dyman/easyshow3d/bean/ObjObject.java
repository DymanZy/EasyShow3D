package com.dyman.easyshow3d.bean;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;


import com.dyman.easyshow3d.imp.ModelLoaderListener;
import com.dyman.easyshow3d.utils.Normal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.dyman.easyshow3d.utils.LoadUtil.getCrossProduct;
import static com.dyman.easyshow3d.utils.LoadUtil.vectorNormal;

/**
 * Created by dyman on 16/7/25.
 */
public class ObjObject extends ModelObject{

    private static final String TAG = "ObjObject";

    private ModelLoaderListener listener;

    //原始顶点坐标列表--直接从obj文件中加载
    ArrayList<Float> alv=new ArrayList<Float>();
    //顶点组装面索引列表--根据面的信息从文件中加载
    ArrayList<Integer> alFaceIndex=new ArrayList<Integer>();
    //结果顶点坐标列表--按面组织好
    ArrayList<Float> alvResult=new ArrayList<Float>();
    //平均前各个索引对应的点的法向量集合Map
    //此HashMap的key为点的索引， value为点所在的各个面的法向量的集合
    HashMap<Integer,HashSet<Normal>> hmn=new HashMap<Integer,HashSet<Normal>>();
    float[] vertices;
    float[] normals;
    AsyncTask<byte[], Integer, float[]> task;
    int totalLines = 0;


    public ObjObject(byte[] objByte, Context context, int drawMode, ModelLoaderListener listener) {
        this.modelType = "obj";

        this.drawWay = drawMode;
        this.listener = listener;
        parseModel(objByte, context);
    }


    /**
     * 初始化顶点坐标与着色数据的方法(Obj和Stl的不同)
     * @param vertices
     * @param normals
     */
    @Override
    public void initVertexData(float[] vertices, float[] normals) {

        //顶点坐标数据的初始化================begin============================
        vCount=vertices.length/3;

        //创建顶点坐标数据缓冲
        //vertices.length*4是因为一个整数四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为Float型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点坐标数据的初始化================end============================

        //顶点法向量数据的初始化================begin============================
        ByteBuffer cbb = ByteBuffer.allocateDirect(normals.length*4);
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mNormalBuffer = cbb.asFloatBuffer();//转换为Float型缓冲
        mNormalBuffer.put(normals);//向缓冲区中放入顶点法向量数据
        mNormalBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点着色数据的初始化================end============================
    }

    /**
     *  解析obj模型
     * @param data
     * @param context
     */
    @Override
    public void parseModel(byte[] data, Context context) {

        alv = new ArrayList<>();
        alFaceIndex = new ArrayList<>();
        alvResult = new ArrayList<>();
        hmn = new HashMap<>();

        task = new AsyncTask<byte[], Integer, float[]>() {
            @Override
            protected float[] doInBackground(byte[]... objBytes) {
                listener.loadBegin();

                String objText = new String(objBytes[0]);
                String[] objLines = objText.split("\n");
                totalLines = objLines.length;

                String line;
                String[] tempsa, tempsa1;

                for (int i = 0, len = totalLines; i < len; i++){
                    
                    if(isCancelled()){
                        break;
                    }
                    
                    line = objLines[i];
                    tempsa = line.split("[ ]+");

                    if (tempsa.length > 4 && !tempsa[4].trim().equals("")) {
                        putPointAndFace(tempsa);
                        tempsa1 = new String[] {tempsa[0], tempsa[1], tempsa[3], tempsa[4]};
                        putPointAndFace(tempsa1);
                    } else {
                        putPointAndFace(tempsa);
                    }

                    if (i % (totalLines / 100) == 0){
                        publishProgress(i);
                    }
                }

                return new float[0];
            }

            private void putPointAndFace(String[] tempsa) {
                if (tempsa[0].trim().equals("v")){  //此为顶点坐标

                    alv.add(Float.parseFloat(tempsa[1]));
                    alv.add(Float.parseFloat(tempsa[2]));
                    alv.add(Float.parseFloat(tempsa[3]));

                } else if(tempsa[0].trim().equals("f")){    //此为三角形面
                    int[] index = new int[3];
                    //计算第0个顶点的索引，并获取此顶点的XYZ三个坐标
                    index[0]=Integer.parseInt(tempsa[1].split("/")[0])-1;
                    float x0=alv.get(3*index[0]);
                    float y0=alv.get(3*index[0]+1);
                    float z0=alv.get(3*index[0]+2);
                    alvResult.add(x0);
                    alvResult.add(y0);
                    alvResult.add(z0);
                    adjustMaxMin(x0, y0, z0);

                    //计算第1个顶点的索引，并获取此顶点的XYZ三个坐标
                    index[1]=Integer.parseInt(tempsa[2].split("/")[0])-1;
                    float x1=alv.get(3*index[1]);
                    float y1=alv.get(3*index[1]+1);
                    float z1=alv.get(3*index[1]+2);
                    alvResult.add(x1);
                    alvResult.add(y1);
                    alvResult.add(z1);
                    adjustMaxMin(x1, y1, z1);

                    //计算第2个顶点的索引，并获取此顶点的XYZ三个坐标
                    index[2]=Integer.parseInt(tempsa[3].split("/")[0])-1;
                    float x2=alv.get(3*index[2]);
                    float y2=alv.get(3*index[2]+1);
                    float z2=alv.get(3*index[2]+2);
                    alvResult.add(x2);
                    alvResult.add(y2);
                    alvResult.add(z2);
                    adjustMaxMin(x2, y2, z2);

                    //记录此面的顶点索引
                    alFaceIndex.add(index[0]);
                    alFaceIndex.add(index[1]);
                    alFaceIndex.add(index[2]);

                    //通过三角形面两个边向量0-1，0-2求叉积得到此面的法向量
                    //求0号点到1号点的向量
                    float vxa=x1-x0;
                    float vya=y1-y0;
                    float vza=z1-z0;
                    //求0号点到2号点的向量
                    float vxb=x2-x0;
                    float vyb=y2-y0;
                    float vzb=z2-z0;
                    //通过求两个向量的叉积计算法向量
                    float[] vNormal=vectorNormal(getCrossProduct(
                            vxa,vya,vza,vxb,vyb,vzb
                    ));

                    for(int tempInxex:index) {//记录每个索引点的法向量到平均前各个索引对应的点的法向量集合组成的Map中
                        //获取当前索引对应点的法向量集合
                        HashSet<Normal> hsn=hmn.get(tempInxex);
                        if(hsn==null) {//若集合不存在则创建
                            hsn=new HashSet<Normal>();
                        }
                        //将此点的法向量添加到集合中
                        //由于Normal类重写了equals方法，因此同样的法向量不会重复出现在此点
                        //对应的法向量集合中
                        hsn.add(new Normal(vNormal[0],vNormal[1],vNormal[2]));
                        //将集合放进HsahMap中
                        hmn.put(tempInxex, hsn);
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {

                if(isCancelled()){
                    return;
                }

                float progress = (float) values[0] / (float) totalLines;
                DecimalFormat df = new DecimalFormat("#.00");
                listener.loadedUpdate(Float.valueOf(df.format(progress)));
            }

            @Override
            protected void onPostExecute(float[] floats) {
                //生成顶点数组
                int size=alvResult.size();
                vertices=new float[size];
                for(int i=0;i<size;i++) {
                    vertices[i]=alvResult.get(i);
                }

                //生成法向量数组
                normals = new float[alFaceIndex.size()*3];
                int c=0;
                for(Integer i:alFaceIndex) {
                    //根据当前点的索引从Map中取出一个法向量的集合
                    HashSet<Normal> hsn=hmn.get(i);
                    //求出平均法向量
                    float[] tn=Normal.getAverage(hsn);
                    //将计算出的平均法向量存放到法向量数组中
                    normals[c++]=tn[0];
                    normals[c++]=tn[1];
                    normals[c++]=tn[2];
                }

                initVertexData(vertices,normals);
                listener.loadedFinish(ObjObject.this);
            }
        };

        try{
            task.execute(data);
        } catch (Exception e){
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
