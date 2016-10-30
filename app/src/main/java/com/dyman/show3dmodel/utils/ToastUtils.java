package com.dyman.show3dmodel.utils;

import android.content.Context;
import android.widget.Toast;


/**
 * Toast工具类
 * @author dzy
 * @version 1.00
 */
public class ToastUtils {
	public static Toast toast = null;
	/**
	 * 弹出端时toast
	 * 
	 * @param context
	 *            上下文
	 * @param text
	 *            弹出的信息
	 * */
	public static void showShort(Context context, String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		} else {
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setText(text);
		}
		toast.show();
	}

	/**
	 * 弹出长时toast
	 * 
	 * @param context
	 *            上下文
	 * @param text
	 *            弹出的信息
	 * */
	public static void showLong(Context context, String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		} else {
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setText(text);
		}
		toast.show();
	}
	
	/**
	 * 弹出短时toast
	 * 
	 * @param context
	 *            上下文
	 * @param resourseId
	 *            弹出的信息ID
	 * */
	public static void showShort(Context context, int resourseId) {
		if (toast == null) {
			toast = Toast.makeText(context, resourseId, Toast.LENGTH_SHORT);
		} else {
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setText(resourseId);
		}
		toast.show();
	}
	
	/**
	 * 弹出长时toast
	 * 
	 * @param context
	 *            上下文
	 * @param resourseId
	 *            弹出的信息ID
	 * */
	public static void showLong(Context context, int resourseId) {
		if (toast == null) {
			toast = Toast.makeText(context, resourseId, Toast.LENGTH_LONG);
		} else {
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setText(resourseId);
		}
		toast.show();
	}

}
