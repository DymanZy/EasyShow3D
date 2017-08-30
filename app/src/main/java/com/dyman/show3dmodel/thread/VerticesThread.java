package com.dyman.show3dmodel.thread;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by dyman on 2016/11/13.
 *
 * obj文件顶点解析线程
 */

public class VerticesThread extends Thread {

    private static final String TAG = "VerticesThread";
    /** 定义解析的起始点 */
    private int start;
    /** 定义解析的结束点 */
    private int end;
    /** obj文件的数据 */
    private ArrayList<String> vLines;
    /** 当前线程的ID */
    private int threadID;
    /** 解析完成的回调 */
    private IAnalysisFinishCallback finishCallback;
    private long beginTime;
    private boolean isFinish = false;


    public VerticesThread(int threadID, ArrayList<String> vLines, int start, int end, IAnalysisFinishCallback finishCallback) {
        this.threadID = threadID;
        this.vLines = vLines;
        this.start = start;
        this.end = end;
        this.finishCallback = finishCallback;
        this.beginTime = System.currentTimeMillis();
    }


    @Override
    public void run() {
        ArrayList<Float> alv = new ArrayList<>();

        for (int i = start; i < end; i++) {
            String line = vLines.get(i);
            String[] tempsa = line.split("[ ]+");
            if (tempsa[0].trim().equals("v")) {
                alv.add(Float.parseFloat(tempsa[1]));
                alv.add(Float.parseFloat(tempsa[2]));
                alv.add(Float.parseFloat(tempsa[3]));
            }
        }
        isFinish = true;
        finishCallback.alvFinish(threadID, start, alv);

    }


    public boolean isFinish() {
        return isFinish;
    }

}
