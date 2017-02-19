package com.dyman.show3dmodel.bean;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.dyman.show3dmodel.manager.SharePreferenceManager;
import com.dyman.show3dmodel.utils.Normal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by dyman on 16/7/25.
 */
public class ObjProObject extends ModelObject{

    private static final String TAG = "ObjObject";

    private byte[] objByte = null;
    IFinishCallBack finishCallBack;

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
    ProgressDialog progressDialog;


    public ObjProObject(byte[] objByte, Context context, int drawMode, IFinishCallBack finishCallBack) {
        this.modelType = "obj";

        this.objByte = objByte;
        this.drawWay = drawMode;
        this.finishCallBack = finishCallBack;
        sp = new SharePreferenceManager(context);
        processOBJ(objByte, context);
    }


    /**
     * 初始化顶点坐标与着色数据的方法(Obj和Stl的不同)
     * @param vertices
     * @param normals
     */
    public void initVertexData(float[] vertices,float[] normals) {
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
     * 求两个向量的叉积
     */
    public static float[] getCrossProduct(float x1,float y1,float z1,float x2,float y2,float z2) {
        //求出两个矢量叉积矢量在XYZ轴的分量ABC
        float A=y1*z2-y2*z1;
        float B=z1*x2-z2*x1;
        float C=x1*y2-x2*y1;

        return new float[]{A,B,C};
    }

    /**
     *  向量规格化
     */
    public static float[] vectorNormal(float[] vector) {
        //求向量的模
        float module=(float)Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]+vector[2]*vector[2]);
        return new float[]{vector[0]/module,vector[1]/module,vector[2]/module};
    }


    /**
     *  解析obj模型
     * @param objBytes
     * @param context
     */
    private boolean processOBJ(byte[] objBytes, final Context context){

        alv=new ArrayList<Float>();
        alFaceIndex=new ArrayList<Integer>();
        alvResult=new ArrayList<Float>();
        hmn=new HashMap<Integer,HashSet<Normal>>();

        progressDialog = prepareProgressDialog(context, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i(TAG, "onClick: ----------------------- click cancel button on progress dialog");
                if (task != null && !task.isCancelled()) {
                    if (task.cancel(true)) {
                        Log.i(TAG, "onClick: ----------------------- cancel task success~!");
                        finishCallBack.cancelLoadModel();
                    }
                }
            }
        });

        task = new AsyncTask<byte[], Integer, float[]>() {
            @Override
            protected float[] doInBackground(byte[]... objBytes) {

                String objText = new String(objBytes[0]);
                String[] objLines = objText.split("\n");
                int totalLines = objLines.length;
                progressDialog.setMax(totalLines);

                for (int i = 0, len = totalLines; i < len; i++){
                    String line = objLines[i];
                    line = line.trim();
                    String[] tempsa = line.split("[ ]+");
                    if (tempsa[0].trim().equals("v")){  //此为顶点坐标
                        alv.add(Float.parseFloat(tempsa[1]));
                        alv.add(Float.parseFloat(tempsa[2]));
                        alv.add(Float.parseFloat(tempsa[3]));

                    } else if(tempsa[0].trim().equals("f")){    //此为三角形面
                        int[] index = new int[3];
                        if (tempsa.length == 5) {
                            index[0]=Integer.parseInt(tempsa[1].split("/")[0])-1;
                            index[1]=Integer.parseInt(tempsa[2].split("/")[0])-1;
                            index[2]=Integer.parseInt(tempsa[3].split("/")[0])-1;
                            dealFaceTri(index);
                            index[0]=Integer.parseInt(tempsa[2].split("/")[0])-1;
                            index[1]=Integer.parseInt(tempsa[3].split("/")[0])-1;
                            index[2]=Integer.parseInt(tempsa[4].split("/")[0])-1;
                            dealFaceTri(index);
                        } else {
                            index[0]=Integer.parseInt(tempsa[1].split("/")[0])-1;
                            index[1]=Integer.parseInt(tempsa[2].split("/")[0])-1;
                            index[2]=Integer.parseInt(tempsa[3].split("/")[0])-1;
                            dealFaceTri(index);
                        }

                    }

                    if (i % (totalLines / 100) == 0){
                        publishProgress(i);
                    }
                }

                return new float[0];
            }


            private void dealFaceTri(int[] index) {
                //计算第0个顶点的索引，并获取此顶点的XYZ三个坐标
                float x0=alv.get(3*index[0]);
                float y0=alv.get(3*index[0]+1);
                float z0=alv.get(3*index[0]+2);
                alvResult.add(x0);
                alvResult.add(y0);
                alvResult.add(z0);
                adjustMaxMin(x0, y0, z0);

                //计算第1个顶点的索引，并获取此顶点的XYZ三个坐标
                float x1=alv.get(3*index[1]);
                float y1=alv.get(3*index[1]+1);
                float z1=alv.get(3*index[1]+2);
                alvResult.add(x1);
                alvResult.add(y1);
                alvResult.add(z1);
                adjustMaxMin(x1, y1, z1);

                //计算第2个顶点的索引，并获取此顶点的XYZ三个坐标
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


            @Override
            protected void onProgressUpdate(Integer... values) {
                progressDialog.setProgress(values[0]);
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

                finishCallBack.readModelFinish();
                progressDialog.dismiss();

                Log.i(TAG, "------------------------------vertices.size = " + vertices.length);
                Log.i(TAG, "------------------------------normals.size = " + normals.length);
                Log.i(TAG, "------------------------------length_x = " + (maxX-minX));
                Log.i(TAG, "------------------------------length_y = " + (maxY-minY));
                Log.i(TAG, "------------------------------length_z = " + (maxZ-minZ));
            }
        };

        try{
            task.execute(objBytes);
        } catch (Exception e){
            return false;
        }
        return true;
    }


}
