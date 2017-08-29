package com.dyman.show3dmodel.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.os.Environment;

/**
 * Created by dyman on 16/7/20.
 */
public class SharePreferenceManager {

    private Context context;
    private SharedPreferences sp = null;
    private SharedPreferences.Editor edit;

    public SharePreferenceManager(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("setting", Activity.MODE_PRIVATE);
        edit = sp.edit();
    }


    public void setLastPath(String lastPath) {
        edit.putString("lastPath", lastPath);
        edit.commit();
    }

    public String getLastPath() {
        return sp.getString("lastPath", Environment.getExternalStorageDirectory().getAbsolutePath());
    }


    public void setRenderModel(int model) {
        edit.putInt("renderModel", model);
        edit.commit();
    }

    public int getRenderModel() {
        return sp.getInt("renderModel", GLES20.GL_TRIANGLES);
    }

    /**
     * 画坐标
     * @param b
     */
    public void setDrawLinesEnable(boolean b){
        edit.putBoolean("drawLines", b);
        edit.commit();
    }

    public boolean isDrawLines() {
        return sp.getBoolean("drawLines", false);
    }

    /**
     * 画网格
     * @param b
     */
    public void setDrawGridsEnable(boolean b){
        edit.putBoolean("drawGrids", b);
        edit.commit();
    }

    public boolean isDrawGrids() {
        return sp.getBoolean("drawGrids", false);
    }


    public void setAnalysisWay(boolean b) {
        edit.putBoolean("isSMM", b);
        edit.commit();
    }

    public boolean isSMM() { return sp.getBoolean("isSMM", false); }

}
