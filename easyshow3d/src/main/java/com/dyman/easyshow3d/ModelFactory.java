package com.dyman.easyshow3d;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.dyman.easyshow3d.bean.ModelObject;
import com.dyman.easyshow3d.bean.ObjObject;
import com.dyman.easyshow3d.bean.ObjProObject;
import com.dyman.easyshow3d.bean.StlObject;
import com.dyman.easyshow3d.imp.ModelLoaderListener;
import com.dyman.easyshow3d.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * Created by dyman on 2017/9/13.
 */

public class ModelFactory {
    private static final String TAG = "ModelFactory";

    private static byte[] modelBytes = null;
    private static String modelType;
    private static ModelObject modelObject;

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
            listener.loaderFailure();
            return;
        }

        modelObject = decodeByteArray(context, modelBytes, listener);
    }


    public static void multiTest(Context context, String filePath, ModelLoaderListener listener) {
        modelType = FileUtils.getType(filePath).toLowerCase();
        File file = new File(filePath);
        try {
            modelBytes = FileUtils.getFileBytes(context, Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (modelBytes == null) {
            listener.loaderFailure();
            return;
        }

        if (modelType.equals("obj")) {
            modelObject = new ObjProObject(modelBytes, context, ModelObject.DRAW_MODEL, listener);
        } else {
            Log.e(TAG, "multiTest:  " + FileUtils.getName(filePath) + " file type must be .obj");
        }
    }


    private static ModelObject decodeByteArray(Context context, byte[] data, ModelLoaderListener listener) {

        ModelObject modelObject = null;

        if (modelType.equals("obj")) {
            modelObject = new ObjObject(data, context, ModelObject.DRAW_MODEL, listener);
        } else if (modelType.equals("stl")) {
            modelObject = new StlObject(data, context, ModelObject.DRAW_MODEL, listener);
        } else if (modelType.equals("3ds")) {
            Log.e(TAG, " can't handle 3ds model");
            listener.loaderFailure();
        } else {
            Log.e(TAG, "model type error!");
            listener.loaderFailure();
        }

        return modelObject;
    }


    public static void cancelDecode() {
        if (modelObject != null) {
            modelObject.cancelTask();
        } else {
            throw new NullPointerException("ModelObject was null, can't call cancelDecode()! please call decodeFile() first.");
        }
    }


}
