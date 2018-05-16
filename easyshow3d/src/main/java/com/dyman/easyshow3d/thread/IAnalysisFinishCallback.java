package com.dyman.easyshow3d.thread;

/**
 * Created by dyman on 18/5/17.
 */

public interface IAnalysisFinishCallback {
    void verticeProgressUpdate(int threadID, int nums);
    void faceProgressUpdate(int threadID, int nums);
    void alvFinish();
    void alvFaceFinish();
}