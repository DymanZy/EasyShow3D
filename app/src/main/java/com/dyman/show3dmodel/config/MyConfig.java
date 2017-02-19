package com.dyman.show3dmodel.config;

import android.os.Environment;

import java.io.File;

/**
 * Created by dyman on 16/8/21.
 */
public class MyConfig {

    public final static int POST_DELAYED_TIME = 200;

    /**
     *  常用文件目录
     */
    public static final String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    /** QQ文件目录 */
    public static final String QQFilePath = rootPath+ File.separator+"tencent"+File.separator+"QQfile_recv";
    /** 系统下载目录 */
    public static final String SystemDownloadPath = rootPath+File.separator+"Download";
    /** WPS文件目录 */
    public static final String WPSFilePath = rootPath+File.separator+"documents";
    /** 微信文件目录 */
    public static final String WXFilePath = rootPath+File.separator+"tencent"+File.separator+"MicroMsg"+File.separator+"Download";

}
