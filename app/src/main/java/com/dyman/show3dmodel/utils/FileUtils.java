package com.dyman.show3dmodel.utils;

import com.dyman.show3dmodel.R;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;

/**
 * Created by dyman on 16/7/23.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 检查文件是否为空
     */
    public static boolean isNullString(String str) {
        if (str == null || str.equals(""))
            return true;
        return false;
    }


    /**
     * 获取文件名
     * @param filePathName
     * @return 没有返回“”
     */
    public static String getName(String filePathName) {
        try {
            return filePathName.substring(filePathName.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     *  获取文件名(无后缀)
     * @param filePathName
     * @return
     */
    public static String getNameNoPostfix(String filePathName) {
        try {
            return filePathName.substring(filePathName.lastIndexOf('/') + 1,
                    filePathName.lastIndexOf('.'));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     *  获取文件类型
     * @param filePathName
     * @return
     */
    public static String getType(String filePathName) {
        try {
            return filePathName.substring(filePathName.lastIndexOf('.') + 1).toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    public static int getImageFromType(String fileType) {
        if (fileType.toLowerCase().equals("stl")) {
            return R.mipmap.ic_file_stl;
        } else if (fileType.toLowerCase().equals("3ds")) {
            return R.mipmap.ic_file_ds;
        } else if (fileType.toLowerCase().equals("obj")) {
            return R.mipmap.ic_file_obj;
        } else if (fileType.toLowerCase().equals("dir")) {
            return R.mipmap.ic_folder;
        }
        return 0;
    }

    /**
     *  获取文件大小(B)
     * @param filePathName
     * @return
     */
    public static long getSize(String filePathName) {
        if (isNullString(filePathName))
            return 0;
        File file = new File(filePathName);
        if (file.isFile())
            return file.length();
        return 0;
    }

    /**
     *  获取文件（夹）大小
     * @param filepath
     * @return
     */
    public static long getFileOrFileDirSize(String filepath){

        File file = new File(filepath);
        long blockSize = 0;
        try {
            if(file.isDirectory()){
                blockSize = getFileDirSize(file);
            }else{
                blockSize = getSize(filepath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return blockSize;
    }

    /**
     *  获取文件夹大小
     * @param file
     * @return
     */
    private static long getFileDirSize(File file) {
        long size = 0;
        File flist[] = file.listFiles();
        for (int i = 0, len = flist.length; i < len; i++) {
            if (flist[i].isDirectory()) {
                size += getFileDirSize(flist[i]);
            }else{
                size += flist[i].length();
            }
        }
        return size;
    }

    /**
     *  文件大小单位转换
     * @param fileSize
     * @return
     */
    public static String fileSizeTransfer(long fileSize) {
        String mFileSize;
        DecimalFormat df = new DecimalFormat("######0.00");
        double size = (double) fileSize;
        if (size > 1024 * 1024 * 1024) {
            size = size / (1024 * 1024 * 1024);
            mFileSize = df.format(size) + " G";
        } else if (size > 1024 * 1024) {
            size = size / (1024 * 1024);
            mFileSize = df.format(size) + " MB";
        } else if (size > 1024) {
            size = size / 1024;
            mFileSize = df.format(size) + " KB";
        } else {
            mFileSize = df.format(size) + " B";
        }

        return mFileSize;
    }


    /**
     *  删除文件
     * @param filePathName
     */
    public static void delete(String filePathName) {
        if (isNullString(filePathName))
            return;
        File file = new File(filePathName);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    /**
     *  删除文件夹（递归）
     * @param file  文件名
     * @param isDeleteDir 是否删除主目录
     */
    public static void deleteDir(File file, boolean isDeleteDir) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                deleteDir(childFiles[i], true);
            }
            if (isDeleteDir){
			    file.delete();
            }
        }
    }

    /**
     *  创建文件目录
     * @param path
     */
    public static void createDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    /**
     *  获取文件的MD5值
     * @param filepath
     * @return
     */
    public static String getFileMD5(String filepath) {
        File f = new File(filepath);
        if (!f.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;

        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(f);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }


}
