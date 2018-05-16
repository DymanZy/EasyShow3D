package com.dyman.easyshow3d.thread;

import java.util.ArrayList;

/**
 * Created by dyman on 18/5/17.
 */

public interface IAnalysisFinishCallback {

    void alvFinish(int threadID, int index, ArrayList<Float> alvList);
    void alvFaceFinish(int threadID, int index, ArrayList<Float> verticesList, ArrayList<Float> normalsList);
}