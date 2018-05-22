package com.dyman.easyshow3d.thread;

import android.util.Log;

import com.dyman.easyshow3d.bean.ObjProObject;
import com.dyman.easyshow3d.imp.ModelLoaderListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by dyman on 2016/11/13.
 */

public class AnalysisThreadHelper {

    private static final String TAG = "ATHelper";
    /** 解析顶点数据的线程组 */
    private VerticesThread[] vThreads;
    /** 待处理的顶点数据 */
    private ArrayList<String[]> vLines = new ArrayList<>();
    /** 解析面数据的线程组 */
    private FaceThread[] fThreads;
    /** 待处理的面数据 */
    private ArrayList<String[]> fLines = new ArrayList<>();

    private float[] alv;
    private float[] vertices;
    private float[] normals;


    private int threadNum = 3;
    private ObjProObject objModel;
    private int vLineNum = 0;
    private int fLineNum = 0;

    private ModelLoaderListener listener;

    public AnalysisThreadHelper(int threadNum, ObjProObject objModel, ModelLoaderListener listener) {
        this.threadNum = threadNum;
        this.objModel = objModel;
        this.listener = listener;

        vThreads = new VerticesThread[threadNum];
        fThreads = new FaceThread[threadNum];
    }



    /**
     *  传入3D文件，先开三条子线程解析顶点数据，再开三条子线程解析面数据
     *  最后通过回调返回解析结果
     */
    public void analysis(byte[] objByte) {
        //TODO 要将此方法改为异步执行
        listener.loadBegin();
        vLineNum = 0;
        fLineNum = 0;
        String line;
        String[] tempsa, tempsa1;

        // 1. 先将原数据处理分为顶点数据和面数据（这里很耗时间）
        String objText = new String(objByte);
        String[] objLines = objText.split("\n");
        for (int i = 0, len = objLines.length; i < len; i++) {
            line = objLines[i];
            tempsa = line.split("[ ]+");
            if (tempsa.length > 4 && !tempsa[4].trim().equals("")) {
                pushModelData(tempsa);
                tempsa1 = new String[] {tempsa[0], tempsa[1], tempsa[3], tempsa[4]};
                pushModelData(tempsa1);
            } else {
                pushModelData(tempsa);
            }
        }
        alv = new float[vLineNum*3];
        vertices = new float[fLineNum*9];
        normals = new float[fLineNum*9];
        // 2. 开启线程组解析顶点数据
        int vAveLength = vLineNum/3;
        for (int i = 0; i < threadNum; i++) {
            if (i == (threadNum-1)) {
                vThreads[i] = new VerticesThread(i, vLines, alv, i*vAveLength, vLineNum, finishCallback);
            } else {
                vThreads[i] = new VerticesThread(i, vLines, alv, i*vAveLength, (i+1)*vAveLength-1, finishCallback);
            }
            vThreads[i].start();
        }
    }

    private void pushModelData(String[] tempsa) {
        if (tempsa[0].trim().equals("v")) {
            vLineNum++;
            vLines.add(tempsa);
        } else if (tempsa[0].trim().equals("f")) {
            fLineNum++;
            fLines.add(tempsa);
        }
    }

    /**
     *  解析结果的回调，分顶点解析的回调和面解析的回调
     */
    private IAnalysisFinishCallback finishCallback = new IAnalysisFinishCallback() {
        float temp, progress;
        int[] vThreadNums = new int[threadNum];
        int[] fThreadNums = new int[threadNum];
        DecimalFormat df = new DecimalFormat("#.00");

        @Override
        public void verticeProgressUpdate(int threadID, int nums) {
            vThreadNums[threadID] = nums;
            temp = 0;
            for (int num : vThreadNums) {
                temp += num;
            }
            temp = temp / (float) (vLineNum + fLineNum);
            temp = Float.valueOf(df.format(temp));
            if (temp - progress > 0.01) {
                progress = temp;
                listener.loadedUpdate(progress);
            }
        }

        @Override
        public void faceProgressUpdate(int threadID, int nums) {
            fThreadNums[threadID] = nums;
            temp = 0;
            for (int num : fThreadNums) {
                temp += num;
            }
            temp = (temp + vLineNum) / (float) (vLineNum + fLineNum);
            temp = Float.valueOf(df.format(temp));
            if (temp - progress > 0.01) {
                progress = temp;
                listener.loadedUpdate(progress);
            }
        }

        @Override
        public void alvFinish() {

            if (vThreadAllFinish()) {
                //顶点解析完成， 开始解析三角面

                // 3. 开始解析面数据
                int fAveLength = fLineNum/3;
                for (int i = 0; i < threadNum; i++) {
                    if (i == (threadNum-1)) {
                        fThreads[i] = new FaceThread(i, fLines, vertices, normals, i*fAveLength, fLineNum,
                                alv, objModel, finishCallback);
                    } else {
                        fThreads[i] = new FaceThread(i, fLines, vertices, normals, i*fAveLength, (i+1)*fAveLength-1,
                                alv, objModel, finishCallback);
                    }
                    fThreads[i].start();
                }
            }
        }

        @Override
        public void alvFaceFinish() {

            if (fThreadAllFinish()) {
                // 全部解析完成, 返回verticesList和faceList

                // 4.初始化顶点坐标与着色数据, 通过回调通知View显示模型
                objModel.initVertexData(vertices,normals);
                listener.loadedFinish(objModel);
            }
        }
    };


    synchronized private boolean vThreadAllFinish() {
        for (VerticesThread thread : vThreads) {
            if (!thread.isFinish()) {
                return false;
            }
        }
        return true;
    }


    synchronized private boolean fThreadAllFinish() {
        for (FaceThread thread : fThreads) {
            if (!thread.isFinish()) return false;
        }
        return true;
    }

}