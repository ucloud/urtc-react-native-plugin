package com.ucloud_plugin.utils;

import android.content.Context;
import android.widget.Toast;

import com.ucloudrtclib.sdkengine.UCloudRtcSdkEnv;

public class ToastUtils {
    public static void shortShow(Context context, String msg) {
        Toast.makeText(UCloudRtcSdkEnv.getApplication().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void longShow(Context context, String msg) {
        Toast.makeText(UCloudRtcSdkEnv.getApplication().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}
