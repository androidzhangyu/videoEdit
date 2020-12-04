package com.forevas.videoeditor.constants;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.forevas.videoeditor.VideoEditorSDK;
import com.forevas.videoeditor.gpufilter.helper.MagicFilterType;

import java.io.File;

/**
 * Created by carden
 */

public class Constants {
    /**
     * 画幅
     */
    public static final int MODE_POR_9_16=0;
    public static final int MODE_POR_1_1=1;
    public static final int MODE_POR_16_9=2;
    /**
     * 屏幕宽高
     */
    public static int screenWidth;
    public static int screenHeight;
    /**
     * 三种画幅的具体显示尺寸
     */
    public static int mode_por_width_9_16;
    public static int mode_por_height_9_16;
    public static int mode_por_width_1_1;
    public static int mode_por_height_1_1;
    public static int mode_por_width_16_9;
    public static int mode_por_height_16_9;

    /**
     * 三种画幅的具体编码尺寸(参考VUE)
     * @param context
     */
    public static final int mode_por_encode_width_9_16=540;
    public static final int mode_por_encode_height_9_16=960;
    public static final int mode_por_encode_width_1_1=540;
    public static final int mode_por_encode_height_1_1=540;
    public static final int mode_por_encode_width_16_9=960;
    public static final int mode_por_encode_height_16_9=540;

    public static void init(Context context) {
        DisplayMetrics mDisplayMetrics = context.getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
        mode_por_width_9_16=screenWidth;
        mode_por_height_9_16=screenHeight;
        mode_por_width_1_1=screenWidth;
        mode_por_height_1_1=screenWidth;
        mode_por_width_16_9=screenWidth;
        mode_por_height_16_9=screenWidth/16*9;
    }

    /**
     * 选取的滤镜
     * 浪漫／温暖／复古／皮古斯／美白／黑水瓶／brannan／素描／1977／弗洛伊德／hefe／哈德森／纳什维尔／冰冷
     */
    public static final MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.ROMANCE,
            MagicFilterType.WARM,
            MagicFilterType.ANTIQUE,
            MagicFilterType.PIXAR,
            MagicFilterType.SKINWHITEN,
            MagicFilterType.INKWELL,
            MagicFilterType.BRANNAN,
            MagicFilterType.SKETCH,
            MagicFilterType.N1977,
            MagicFilterType.FREUD,
            MagicFilterType.HEFE,
            MagicFilterType.HUDSON,
            MagicFilterType.NASHVILLE,
            MagicFilterType.COOL
    };
    /**
     * 用于调试
     */
    /*public static final MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.FAIRYTALE,
            MagicFilterType.SUNRISE,
            MagicFilterType.SUNSET,
            MagicFilterType.WHITECAT,
            MagicFilterType.BLACKCAT,
            MagicFilterType.SKINWHITEN,
            MagicFilterType.HEALTHY,
            MagicFilterType.SWEETS,
            MagicFilterType.ROMANCE,
            MagicFilterType.SAKURA,
            MagicFilterType.WARM,
            MagicFilterType.ANTIQUE,
            MagicFilterType.NOSTALGIA,
            MagicFilterType.CALM,
            MagicFilterType.LATTE,
            MagicFilterType.TENDER,
            MagicFilterType.COOL,
            MagicFilterType.EMERALD,
            MagicFilterType.EVERGREEN,
            MagicFilterType.CRAYON,
            MagicFilterType.SKETCH,
            MagicFilterType.AMARO,
            MagicFilterType.BRANNAN,
            MagicFilterType.BROOKLYN,
            MagicFilterType.EARLYBIRD,
            MagicFilterType.FREUD,
            MagicFilterType.HEFE,
            MagicFilterType.HUDSON,
            MagicFilterType.INKWELL,
            MagicFilterType.KEVIN,
            MagicFilterType.LOMO,
            MagicFilterType.N1977,
            MagicFilterType.NASHVILLE,
            MagicFilterType.PIXAR,
            MagicFilterType.RISE,
            MagicFilterType.SIERRA,
            MagicFilterType.SUTRO,
            MagicFilterType.TOASTER2,
            MagicFilterType.VALENCIA,
            MagicFilterType.WALDEN,
            MagicFilterType.XPROII
    };*/

    /**
     * 在线bgm的地址集合
     */
    public static String[] onlineSongsUrl=new String[]{/**"http://up.mcyt.net/down/47734.mp3","http://up.mcyt.net/down/47720.mp3","http://up.mcyt.net/down/47733.mp3"*/};
    /**
     * 在线bgm的歌名集合
     */
    public static String[] onlineSongsName=new String[]{/**"哑巴","To je zlat posvícení-Musica Bohemica","抖音火曲That Girl-Olly Murs"*/};
    /**
     * 在线bgm的作者集合
     */
    public static String[] onlineSongsAuthor=new String[]{/**"薛之谦","posvícení-Musica Bohemica","Murs"*/};

    public static String getBaseFolder() {

        String baseFolder = Environment.getExternalStorageDirectory() + "/ShortVideo/";
        File f = new File(baseFolder);
        if (!f.exists()) {
            boolean b = f.mkdirs();
            if (!b) {
                baseFolder = VideoEditorSDK.getInstance().getContext().getExternalFilesDir(null).getAbsolutePath() + "/";
            }
        }
        return baseFolder;
    }

    /**
     * 获取录制的文件路径
     * @param
     * @param
     * @return
     */
    public static File createExternalDir(String dir) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory(), dir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                return file;
            } else {
                Toast.makeText(VideoEditorSDK.getInstance().getContext(),"sdcard is not available",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("constants", "ERR: " + e.getMessage());
        }
        return null;
    }




    //获取VideoPath
    public static String getPath(String path, String fileName) {
        String p = getBaseFolder() + path;
        File f = new File(p);
        if (!f.exists() && !f.mkdirs()) {
            return getBaseFolder() + fileName;
        }
        return p + fileName;
    }

}
