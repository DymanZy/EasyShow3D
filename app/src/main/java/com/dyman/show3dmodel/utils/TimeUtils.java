package com.dyman.show3dmodel.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by dyman on 16/8/19.
 */
public class TimeUtils {

    private static final String TAG = "TimeUtils";

    /** 将时间格式化 */
    public static String getTimeFormat(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        String t1 = format.format(time);
        return t1;
    }

    public static String timeTransfer(long time) {
        long hour = 0, minute = 0, second = 0, ms = 0;
        if (time > 60 * 60 * 1000 ) {
            ms = time % 1000;
            second = (time / 1000) % 60;
            minute = (time / (60*1000)) % 60;
            hour = (time / (60*60*1000)) % 60;
        } else if (time > 60 * 1000) {
            ms = time % 1000;
            second = (time / 1000) % 60;
            minute = (time / (60*1000)) % 60;
        } else if (time > 1000) {
            ms = time % 1000;
            second = (time / 1000) % 60;
        } else {
            ms = time % 1000;
        }

        return hour + "h" + minute + "m" + second + "s" + ms + "ms";
    }

}
