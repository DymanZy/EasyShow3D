package com.dyman.show3dmodel.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.dyman.show3dmodel.R;
import com.dyman.show3dmodel.manager.SharePreferenceManager;


/**
 * Created by dyman on 16/7/23.
 */
public class DialogUtils {

    public static void showAlerDialog(Context context, String message, DialogInterface.OnClickListener onClickListener ){
        showAlertDialog(context, null, message, onClickListener);
    }


    public static void showAlertDialog(Context context, String title, String message,
                                DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title==null ? "提示": title);
        builder.setMessage(message);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("确认", onClickListener);
        builder.show();
    }


    public static Dialog showSettingDialog(final Activity ctx) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_show_setting, null);
        final Dialog dialog = new Dialog(ctx, R.style.noBackgroundDialog);
        Switch lineSwitch = (Switch) v.findViewById(R.id.showLines_switch_dialog_show_setting);
        Switch gridSwitch = (Switch) v.findViewById(R.id.showGrids_switch_dialog_show_setting);
        final SharePreferenceManager sp = new SharePreferenceManager(ctx);
        lineSwitch.setChecked(sp.isDrawLines());
        gridSwitch.setChecked(sp.isDrawGrids());

        lineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sp.setDrawLinesEnable(b);
            }
        });
        gridSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sp.setDrawGridsEnable(b);
            }
        });

        v = setLayoutPadding(ctx, v, 30);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(v);

        return dialog;
    }


    public static Dialog renderSettingDialog(final Activity ctx) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_renderer_setting, null);
        final Dialog dialog = new Dialog(ctx, R.style.noBackgroundDialog);
        TextView lineRender = (TextView) v.findViewById(R.id.lineRender_dialog_renderer);
        TextView triRender = (TextView) v.findViewById(R.id.trianglesRender_dialog_renderer);
        final SharePreferenceManager sp = new SharePreferenceManager(ctx);
        lineRender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.setRenderModel(GLES20.GL_LINE_LOOP);
                dismissDialog(ctx, dialog);
            }
        });

        triRender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.setRenderModel(GLES20.GL_TRIANGLES);
                dismissDialog(ctx, dialog);
            }
        });


        v = setLayoutPadding(ctx, v, 50);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(v);

        return  dialog;
    }

    private static View setLayoutPadding(Activity ctx, View v, int padding){
        DisplayMetrics metrics = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels - padding*2;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, WindowManager.LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(params);
        return v;
    }


    /**
     * 隐藏dialog，加了context生命判断，避免窗口句柄泄漏.
     *
     * @param ctx    dialog依赖的activity
     * @param dialog 欲隐藏的dialog
     */
    public static void dismissDialog(Activity ctx, Dialog dialog) {
        if (dialog != null && dialog.isShowing() && ctx != null
                && !ctx.isFinishing())
            dialog.dismiss();
    }

    public interface MyDialogOnClick{
        public void modeChange(int newSetting);
    }


}
