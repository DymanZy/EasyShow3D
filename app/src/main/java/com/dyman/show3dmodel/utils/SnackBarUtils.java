package com.dyman.show3dmodel.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by dyman on 16/8/21.
 */
public class SnackBarUtils {

    private static final int COLOR_DANGER = 0xffa94442;//red
    private static final int COLOR_SUCCESS = 0xff3c763d;//green
    private static final int COLOR_INFO = 0xff0b80b9;//blue
    private static final int COLOR_WARNING = 0xffdee011;//yellow

    private static final int ACTION_COLOR = 0xffe0e0e0;//white

    private Snackbar mSnackbar;


    private SnackBarUtils(Snackbar snackbar) {
        this.mSnackbar = snackbar;
    }


    public static SnackBarUtils makeShort(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        return new SnackBarUtils(snackbar);
    }


    public static SnackBarUtils makeLong(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        return new SnackBarUtils(snackbar);
    }


    private View getSnackBarLayout(Snackbar snackbar) {
        if (snackbar != null) {
            return snackbar.getView();
        }
        return null;

    }


    private Snackbar setSnackBarBackColor(int colorId) {
        View snackBarView = getSnackBarLayout(mSnackbar);
        if (snackBarView != null) {
            snackBarView.setBackgroundColor(colorId);
        }
        return mSnackbar;
    }


    public void info() {
        setSnackBarBackColor(COLOR_INFO);
        show();
    }

    public void info(String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(COLOR_INFO);
        show(actionText, listener);
    }

    public void warning() {
        setSnackBarBackColor(COLOR_WARNING);
        show();
    }

    public void warning(String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(COLOR_WARNING);
        show(actionText, listener);
    }

    public void danger() {
        setSnackBarBackColor(COLOR_DANGER);
        show();
    }

    public void danger(String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(COLOR_DANGER);
        show(actionText, listener);
    }

    public void confirm() {
        setSnackBarBackColor(COLOR_SUCCESS);
        show();
    }

    public void confirm(String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(COLOR_SUCCESS);
        show(actionText, listener);
    }


    public void show() {
        mSnackbar.show();
    }

    public void show(String actionText, View.OnClickListener listener) {
        mSnackbar.setActionTextColor(ACTION_COLOR);
        mSnackbar.setAction(actionText, listener).show();
    }

}
