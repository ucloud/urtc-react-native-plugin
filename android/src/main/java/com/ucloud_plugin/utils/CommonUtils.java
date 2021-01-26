package com.ucloud_plugin.utils;


import androidx.annotation.IntDef;

public class CommonUtils {

    public static int mItemWidth;
    public static int mItemHeight;
    public static int videoprofilesel = 1;

    public static final int camera_capture_mode = 1;
    public static final int audio_capture_mode = 2;
    public static final int screen_capture_mode = 3;
    public static final int screen_Audio_mode = 4;
    public static final int multi_capture_mode = 5;

    public static final int AUTO_MODE = 0;
    public static final int MANUAL_MODE = 1;

    public static final String videoprofile = "videoprofile";
    public static final String capture_mode = "capturemode";
    public static final String PUBLISH_MODE = "PUBLISH_MODE";
    public static final String SCRIBE_MODE = "SCRIBE_MODE";
    public static final String SDK_STREAM_ROLE = "SDK_STREAM_ROLE";
    public static final String SDK_CLASS_TYPE = "SDK_CLASS_TYPE";
    public static final String SDK_SUPPORT_MIX = "SDK_SUPPORT_MIX";
    public static final String SDK_IS_LOOP = "SDK_IS_LOOP";
    public static final String SDK_MIX_FILE_PATH = "SDK_MIX_FILE_PATH";

    public static final String APPID_KEY = "";
    public static final String APP_ID = "";

    @IntDef({AUTO_MODE, MANUAL_MODE})
    public @interface  PubScribeMode {}


}

