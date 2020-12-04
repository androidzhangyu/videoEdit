package com.forevas.videoeditor.utils;

import java.math.BigDecimal;

/**
 * Created by carden
 */

public class StorageFormatUtils {
    public static String formatByte(long b){
        if(b<0){
            return "0K";
        }
        BigDecimal filesize = new BigDecimal(b);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1)
            return (returnValue + "M");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "K");
    }
}
