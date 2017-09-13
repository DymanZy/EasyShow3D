package com.dyman.easyshow3d;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.dyman.easyshow3d.bean.ModelObject;
import com.dyman.easyshow3d.bean.ObjObject;
import com.dyman.easyshow3d.bean.StlObject;
import com.dyman.easyshow3d.imp.ModelLoaderListener;
import com.dyman.easyshow3d.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by dyman on 2017/9/13.
 */

public class ModelFactory {
    private static final String TAG = "ModelFactory";

    private static byte[] modelBytes = null;
    private static String modelType;

    public static void decodeFile(Context context, String filePath, ModelLoaderListener listener) {

        if (FileUtils.isNullString(filePath)) {
            throw new IllegalArgumentException("filePath can't be null!");
        }

        modelType = FileUtils.getType(filePath).toLowerCase();
        File file = new File(filePath);
        try {
            modelBytes = FileUtils.getFileBytes(context, Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (modelBytes == null) {
            Log.e(TAG, "decode model file failure!");
        }

        decodeByteArray(context, modelBytes, listener);
    }


    private static ModelObject decodeByteArray(Context context, byte[] data, ModelLoaderListener listener) {

        ModelObject modelObject = null;

        if (modelType.equals("obj")) {
            modelObject = new ObjObject(data, context, ModelObject.DRAW_MODEL, listener);
        } else if (modelType.equals("stl")) {
            modelObject = new StlObject(data, context, ModelObject.DRAW_MODEL, listener);
        } else if (modelType.equals("3ds")) {
            Log.e(TAG, " can't handle 3ds model");
        } else {
            Log.e(TAG, "model type error!");
        }

        return modelObject;
    }


}
