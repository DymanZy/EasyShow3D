package com.dyman.easyshow3d.thread;

import com.dyman.easyshow3d.bean.ObjProObject;
import com.dyman.easyshow3d.utils.Normal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by dyman on 18/5/17.
 */

public class FaceThread extends Thread {


    private static final String TAG = "FaceThread";

    /** 定义解析的起始点 */
    private int start;
    /** 定义解析的结束点 */
    private int end;
    /** obj文件的数据 */
    private ArrayList<String[]> fLines;
    /** 当前线程的ID */
    private int threadID;
    /** 解析完成的回调 */
    private IAnalysisFinishCallback finishCallback;

    private float[] alv;
    private ObjProObject objModel;

    //顶点组装面索引列表--根据面的信息从文件中加载
    private ArrayList<Integer> alFaceIndex=new ArrayList<Integer>();
    //结果顶点坐标列表--按面组织好
    private ArrayList<Float> alvResult=new ArrayList<Float>();
    //平均前各个索引对应的点的法向量集合Map
    //此HashMap的key为点的索引， value为点所在的各个面的法向量的集合
    private HashMap<Integer,HashSet<Normal>> hmn=new HashMap<Integer,HashSet<Normal>>();


    private long beginTime;
    private boolean isFinish = false;


    public FaceThread(int threadID, ArrayList<String[]> fLines, int start, int end, float[] alv, ObjProObject
            objModel, IAnalysisFinishCallback finishCallback) {
        this.threadID = threadID;
        this.fLines = fLines;
        this.start = start;
        this.end = end;
        this.alv = alv;
        this.finishCallback = finishCallback;
        this.objModel = objModel;
        this.beginTime = System.currentTimeMillis();
    }


    @Override
    public void run() {
        ArrayList<Float> normalsList = new ArrayList<>();

        for (int i = start; i < end; i++) {
            String[] tempsa = fLines.get(i);
            if (tempsa[0].trim().equals("f")) {
                int[] index = new int[3];

                //计算第0个顶点的索引，并获取此顶点的XYZ三个坐标
                index[0]=Integer.parseInt(tempsa[1].split("/")[0])-1;
                float x0=alv[3*index[0]];
                float y0=alv[3*index[0]+1];
                float z0=alv[3*index[0]+2];
                alvResult.add(x0);
                alvResult.add(y0);
                alvResult.add(z0);
                objModel.adjustMaxMin(x0, y0, z0);

                //计算第1个顶点的索引，并获取此顶点的XYZ三个坐标
                index[1]=Integer.parseInt(tempsa[2].split("/")[0])-1;
                float x1=alv[3*index[1]];
                float y1=alv[3*index[1]+1];
                float z1=alv[3*index[1]+2];
                alvResult.add(x1);
                alvResult.add(y1);
                alvResult.add(z1);
                objModel.adjustMaxMin(x1, y1, z1);

                //计算第2个顶点的索引，并获取此顶点的XYZ三个坐标
                index[2]=Integer.parseInt(tempsa[3].split("/")[0])-1;
                float x2=alv[3*index[2]];
                float y2=alv[3*index[2]+1];
                float z2=alv[3*index[2]+2];
                alvResult.add(x2);
                alvResult.add(y2);
                alvResult.add(z2);
                objModel.adjustMaxMin(x2, y2, z2);

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

            if ((i - start) % 100 == 0) {
                finishCallback.faceProgressUpdate(threadID, i - start);
            }
        }


        //生成顶点数组
//        alvResult


        //生成法向量数组
        for(Integer i:alFaceIndex) {
            //根据当前点的索引从Map中取出一个法向量的集合
            HashSet<Normal> hsn=hmn.get(i);
            //求出平均法向量
            float[] tn=Normal.getAverage(hsn);
            //将计算出的平均法向量存放到法向量数组中
            normalsList.add(tn[0]);
            normalsList.add(tn[1]);
            normalsList.add(tn[2]);
        }
        isFinish = true;
        finishCallback.alvFaceFinish(threadID, start, alvResult, normalsList);
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


    public boolean isFinish() {
        return isFinish;
    }


}