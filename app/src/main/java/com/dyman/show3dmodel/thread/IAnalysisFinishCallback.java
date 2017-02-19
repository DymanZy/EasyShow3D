package com.dyman.show3dmodel.thread;

import java.util.ArrayList;

/**
 * Created by dyman on 2016/11/13.
 */

public interface IAnalysisFinishCallback {

    void alvFinish(int threadID, int index, ArrayList<Float> alvList);
    void alvFaceFinish(int threadID, int index, ArrayList<Float> verticesList, ArrayList<Float> normalsList);
}
