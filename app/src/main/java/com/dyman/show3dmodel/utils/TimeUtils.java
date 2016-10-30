package com.dyman.show3dmodel.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dyman on 16/8/19.
 */
public class TimeUtils {

    /** 将时间格式化 */
    public static String getTimeFormat(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        String t1 = format.format(time);
        return t1;
    }

}
