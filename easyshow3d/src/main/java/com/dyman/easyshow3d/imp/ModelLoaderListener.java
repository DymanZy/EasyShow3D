package com.dyman.easyshow3d.imp;

import com.dyman.easyshow3d.bean.ModelObject;

/**
 * Created by dyman on 2017/9/13.
 */

public interface ModelLoaderListener {

    void loadBegin();

    void loadedUpdate(float progress);

    void loadedFinish(ModelObject modelObject);

    void loaderCancel();

    void loaderFailure();
}
