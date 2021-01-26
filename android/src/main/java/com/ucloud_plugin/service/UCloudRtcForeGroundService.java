package com.ucloud_plugin.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.ucloud_plugin.R;

/**
 * @author ciel
 * @create 2020/3/11
 * @Describe
 */
public class UCloudRtcForeGroundService extends Service {

    private static final String TAG = "UCloudFGService";
    private static final String ID="channel_1";
    private static final String NAME="UCloudRTC";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= 26) {
            Log.d(TAG, "start ForeGround service");
            setForeground();
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @TargetApi(26)
    private void setForeground(){
        NotificationManager manager=(NotificationManager)getSystemService (NOTIFICATION_SERVICE);
        NotificationChannel channel=new NotificationChannel (ID,NAME,NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel (channel);
        Notification notification=new Notification.Builder (this,ID)
                .setContentTitle ("urtc recording")
                .setContentText ("ucloud record foreground service")
                .setSmallIcon (R.mipmap.u)
                .setLargeIcon (BitmapFactory.decodeResource (getResources (),R.mipmap.ic_launcher))
                .build ();
        startForeground (1,notification);
    }
}
