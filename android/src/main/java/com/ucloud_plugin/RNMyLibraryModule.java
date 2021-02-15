package com.ucloud_plugin;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.ucloud_plugin.constants.UCloudRNEvent;
import com.ucloud_plugin.service.UCloudRtcForeGroundService;
import com.ucloud_plugin.utils.CommonUtils;
import com.ucloud_plugin.utils.PermissionUtils;
import com.ucloud_plugin.utils.SuperLog;
import com.ucloud_plugin.utils.ToastUtils;
import com.ucloud_plugin.view.URTCVideoViewInfo;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEngine;
import com.ucloudrtclib.sdkengine.UCloudRtcSdkEnv;
import com.ucloudrtclib.sdkengine.define.UCloudRtcRenderView;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkAudioDevice;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkAuthInfo;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkLogLevel;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMediaServiceStatus;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMediaType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkMode;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkNetWorkQuality;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkRoomType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkScaleType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStats;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStreamInfo;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStreamRole;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkStreamType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkSurfaceVideoView;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkTrackType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkVideoProfile;
import com.ucloudrtclib.sdkengine.listener.UCloudRtcSdkEventListener;


import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;

public class RNMyLibraryModule extends ReactContextBaseJavaModule {
    private ReactApplicationContext mContext;
    private final String TAG = this.getClass().getSimpleName();
    private boolean isNeedScreenCapture = false;
    private String roomId;
    private String userId;
    private String mToken;
    private String mAppId;
    private Handler mHandler;
    private UCloudRtcSdkEngine sdkEngine;
    public static RNMyVideoView rnMyVideoView;
    //尝试初始化一个UCloudRtcSurfaceVideoView用作渲染流和传入RN进行展示

    public RNMyLibraryModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
        mContext.addActivityEventListener(mActivityEvent);
        initUCloud();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                rnMyVideoView = RNMyVideoView.getInstance(mContext);
                //必须要Looper.prepare
                sdkEngine = UCloudRtcSdkEngine.createEngine(eventListener);
            }
        });
    }
    public RNMyVideoView getVideoView(){
        return rnMyVideoView;
    }
    @NonNull
    @Override
    public String getName() {
        return "RNMyLib";
    }

    /**
     * 初始化SDK
     * @params
     * @params
     */
    private void initUCloud(){
        UCloudRtcSdkEnv.initEnv(mContext.getApplicationContext());
        UCloudRtcSdkEnv.setLogLevel(UCloudRtcSdkLogLevel.UCLOUD_RTC_SDK_LogLevelInfo);

        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        CommonUtils.mItemWidth = (outMetrics.widthPixels - UiHelper.dipToPx(mContext, 15)) / 3;
        CommonUtils.mItemHeight = CommonUtils.mItemWidth;
    }


    @ReactMethod
    public void initWithAppid(String appId, String appKey, int sdkMode,boolean isAutoPub,boolean isAutoSub, Promise promise){
        if(sdkEngine != null){
            SuperLog.d(TAG,"initWithAppid "+ appId + " appKey: "+ appKey + " sdkMode: "+ sdkMode +
                    " isAutoPub:" + isAutoPub + " isAutoSub: "+ isAutoSub);
            UCloudRtcSdkEnv.setTokenSeckey(appKey);
            if(sdkMode == UCloudRtcSdkMode.UCLOUD_RTC_SDK_MODE_TRIVAL.ordinal()){
                UCloudRtcSdkEnv.setSdkMode(UCloudRtcSdkMode.UCLOUD_RTC_SDK_MODE_TRIVAL);
            }else if(sdkMode == UCloudRtcSdkMode.UCLOUD_RTC_SDK_MODE_NORMAL.ordinal()){
                UCloudRtcSdkEnv.setSdkMode(UCloudRtcSdkMode.UCLOUD_RTC_SDK_MODE_NORMAL);
            }
            this.mAppId = appId;
            String initResult = "";
            sdkEngine.setAudioOnlyMode(false) ; // 设置纯音频模式
            sdkEngine.configLocalCameraPublish(true) ; // 设置摄像头是否发布
            sdkEngine.configLocalAudioPublish(true) ; // 设置音频是否发布，用于让sdk判断自动发布的媒体类型
            sdkEngine.configLocalScreenPublish(true) ; // 设置桌面是否发布，作用同上
            sdkEngine.setStreamRole(UCloudRtcSdkStreamRole.UCLOUD_RTC_SDK_STREAM_ROLE_BOTH);// 流权限
            sdkEngine.setAutoPublish(isAutoPub) ; // 是否自动发布
            sdkEngine.setAutoSubscribe(isAutoSub) ;// 是否自动订阅
            sdkEngine.setClassType(UCloudRtcSdkRoomType.UCLOUD_RTC_SDK_ROOM_SMALL);
            UCloudRtcSdkEnv.setWriteToLogCat(true);
            // 摄像头输出等级
            sdkEngine.setVideoProfile(UCloudRtcSdkVideoProfile.matchValue(0)) ;
            initResult += "init finish";
            promise.resolve(initResult);
        }else{
            promise.reject(UCloudRNErrorCode.ENGINE_HAS_DESTROYED + "","engine has been destroyed");
        }
    }

    private Promise mJoinPromise;
    @ReactMethod
    public void joinRoomWithRoomid(String roomId,String userId,String token,Promise promise){
        this.roomId = roomId;
        this.userId = userId;
        this.mToken = token;
        this.mJoinPromise = promise;
        if (isNeedScreenCapture && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //开始申请采集桌面显示的所有内容，在onActivityResult中得到回调
            Activity activity = mContext.getCurrentActivity();
            SuperLog.d(TAG,"request screen capture join room activity = " + activity.getLocalClassName());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(sdkEngine == null){
                        SuperLog.d(TAG,"recreate engine ");
                        sdkEngine = UCloudRtcSdkEngine.createEngine(eventListener);
                    }
                    UCloudRtcSdkEngine.requestScreenCapture(mContext.getCurrentActivity());
                }
            });
        }else{
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(sdkEngine == null){
                        SuperLog.d(TAG,"recreate engine ");
                        sdkEngine = UCloudRtcSdkEngine.createEngine(eventListener);
                    }
                    SuperLog.d(TAG,"join room without screen capture");
                    startJoinChannel();
                }
            });
        }
    }

    @ReactMethod
    public void startForeGroundService(){
        Intent service = new Intent(mContext, UCloudRtcForeGroundService.class);
        mContext.startService(service);
    }

    /**
     * 重写ActivityEventListener监听onActivityResult方法
     * 不然可以在MainActivity中onActivityResult中使用
     * ReactInstanceManager.onActivityResult
     *
     * @params
     *
     */
    private final ActivityEventListener mActivityEvent = new BaseActivityEventListener(){
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (resultCode != RESULT_OK) {
                ToastUtils.shortShow(mContext,"获取桌面采集权限失败");
                return;
            }
            if (requestCode != UCloudRtcSdkEngine.SCREEN_CAPTURE_REQUEST_CODE) {
                ToastUtils.shortShow(mContext,"获取桌面采集权限失败");
                return;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    UCloudRtcSdkEngine.onScreenCaptureResult(data);
                }
            });
            startJoinChannel();
        }
    };
    /**
     * 加入房间
     * @params
     * @params
     *
     */
    private void startJoinChannel(){
        if(sdkEngine != null){
            UCloudRtcSdkAuthInfo info = new UCloudRtcSdkAuthInfo();
            info.setAppId(mAppId);
            info.setToken(mToken);
            info.setRoomId(roomId);
            info.setUId(userId);
            sdkEngine.joinChannel(info);
        }else{
            SuperLog.d(TAG,"startJoinChannel failed for "+ UCloudRNErrorCode.ENGINE_HAS_DESTROYED);
        }
    }

    @ReactMethod
    public void subscribeRemoteStream(ReadableMap remoteStreamInfo){
        //订阅远程流
        if(sdkEngine != null){
            SuperLog.d(TAG,"this is subscribeRemoteStream " + remoteStreamInfo);
            if(remoteStreamInfo != null){
                UCloudRtcSdkStreamInfo info = new UCloudRtcSdkStreamInfo();
                info.setUid(remoteStreamInfo.getString("uId"));
                info.setMediaType(UCloudRtcSdkMediaType.matchValue(remoteStreamInfo.getInt("mediaType")));
                info.setHasAudio(remoteStreamInfo.getBoolean("hasVideo"));
                info.setHasVideo(remoteStreamInfo.getBoolean("hasAudio"));
                info.setMuteVideo(remoteStreamInfo.getBoolean("muteVideo"));
                info.setMuteAudio(remoteStreamInfo.getBoolean("muteAudio"));
                sdkEngine.subscribe(info);
            }else{
                SuperLog.d(TAG,"subscribeRemoteStream failed for "+ UCloudRNErrorCode.ARG_INVALID);
            }
        }else{
            SuperLog.d(TAG,"subscribeRemoteStream failed for "+ UCloudRNErrorCode.ENGINE_HAS_DESTROYED);
        }
    }
    @ReactMethod
    public void unSubscribeRemoteStream(){
        if(sdkEngine != null){
            //取消订阅远程流
            SuperLog.d(TAG,"this is unSubscribeRemoteStream");
            UCloudRtcSdkStreamInfo info = new UCloudRtcSdkStreamInfo();
            info.setUid(userId);
            info.setMediaType(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
            sdkEngine.subscribe(info);
        }else{
            SuperLog.d(TAG,"unSubscribeRemoteStream failed for "+ UCloudRNErrorCode.ENGINE_HAS_DESTROYED);
        }
    }
    @ReactMethod
    public void publishLocalStreamWithCameraEnable(boolean isOpenCamera){
        if(!PermissionUtils.hasPermissions(mContext,Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ToastUtils.shortShow(mContext,"相机或存储权限未开启");
        }
        if(sdkEngine != null){
            //发布本地流
            sdkEngine.publish(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, isOpenCamera,true);
        }else{
            SuperLog.d(TAG,"unSubscribeRemoteStream failed for "+ UCloudRNErrorCode.ENGINE_HAS_DESTROYED);
        }

    }
    @ReactMethod
    public void unPublishLocalStream(){
        if(sdkEngine != null){
            //取消发布本地流
            SuperLog.e("RNMyLibraryModule","this is unPublishLocalStream");
            sdkEngine.unPublish(UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
        }else{
            SuperLog.d(TAG,"unPublishLocalStream failed for "+ UCloudRNErrorCode.ENGINE_HAS_DESTROYED);
        }
    }

    @ReactMethod
    public void startRecordLocalStreamWithType(int type){
        //录制本地视频
        //类型  1 音频 2 视频 3 音频+视频
        SuperLog.d(TAG,"this is startRecordLocalStreamWithType");
    }
    @ReactMethod
    public void stopRecordLocalStream(){
        //停止录制
        SuperLog.d(TAG,"this is stopRecordLocalStream");
    }
    @ReactMethod
    public void leaveRoom(){
        if(sdkEngine != null){
            //离开房间
            SuperLog.d(TAG,"this is leaveRoom");
            sdkEngine.leaveChannel();
        }else{
            SuperLog.d(TAG,"leave room failed for "+ UCloudRNErrorCode.ENGINE_HAS_DESTROYED);
        }
    }
    /****************************************************************/
    @TargetApi(21)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(mContext, "获取桌面采集权限失败",
                    Toast.LENGTH_LONG).show() ;
            return;
        }
        if (requestCode != UCloudRtcSdkEngine.SCREEN_CAPTURE_REQUEST_CODE) {
            Toast.makeText(mContext, "获取桌面采集权限失败",
                    Toast.LENGTH_LONG).show() ;
            return;
        }
        UCloudRtcSdkEngine.onScreenCaptureResult(data);
        startJoinChannel();
    }

    private WritableMap combineInfo(Object info){
        WritableMap params = Arguments.createMap();
        if(info instanceof UCloudRtcSdkStreamInfo){
            UCloudRtcSdkStreamInfo streamInfo = (UCloudRtcSdkStreamInfo)info;
            params.putString("uId",streamInfo.getUId());
            params.putInt("mediaType",streamInfo.getMediaType().ordinal());
            params.putBoolean("hasVideo",streamInfo.isHasVideo());
            params.putBoolean("hasAudio",streamInfo.isHasAudio());
            params.putBoolean("muteVideo",streamInfo.isMuteVideo());
            params.putBoolean("muteAudio",streamInfo.isMuteAudio());
        }
        return params;
    }

    private void sendEvent(String eventName, WritableMap params) {
        if(mContext != null){
            SuperLog.d(TAG,"send event = " + eventName + "params "+ params);
            mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    UCloudRtcSdkEventListener eventListener = new UCloudRtcSdkEventListener() {

        @Override
        public void onServerDisconnect() {
            //
        }

        @Override
        public void onJoinRoomResult(int i, String s, String s1) {
            SuperLog.d(TAG,"JOIN = " + i + ", msg = " + s + ", msg1 = " + s1);
            if(i == 0 && mJoinPromise != null){
                mJoinPromise.resolve("onJoinRoomResult "+ i + " msg: "+ s);
            }
            WritableMap params = Arguments.createMap();
            params.putInt("code", i);
            params.putString("msg", s);
            sendEvent(UCloudRNEvent.EVENT_JOIN_ROOM,params);
        }

        @Override
        public void onLeaveRoomResult(int i, String s, String s1) {
            SuperLog.d(TAG,"LEAVE = " + i + ", msg = " + s + ", msg1 = " + s1);
            WritableMap params = Arguments.createMap();
            params.putInt("code", i);
            params.putString("msg", s);
            sendEvent(UCloudRNEvent.EVENT_LEAVE_ROOM,params);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    UCloudRtcSdkEngine.destory();
                    sdkEngine = null;
                }
            });

        }

        @Override
        public void onRejoiningRoom(String s) {

        }

        @Override
        public void onRejoinRoomResult(String s) {

        }

        @Override
        public void onLocalPublish(int i, String s, UCloudRtcSdkStreamInfo info) {
            //发布流
            SuperLog.d(TAG,"onLocalUnPublish received code: "+ i + " info: "+ info);
            mContext.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(sdkEngine != null){
//                        rnMyVideoView.setBackgroundColor(Color.TRANSPARENT);
//                        UCloudRtcSdkSurfaceVideoView localrenderview = new UCloudRtcSdkSurfaceVideoView(mContext);
//                        localrenderview.init(true);
//                        localrenderview.setId(R.id.video_view);
//                        localrenderview.getSurfaceView().setNeedFullScreen(false);
//                        info.setHasVideo(true);
//                        info.setHasAudio(true);
//                        info.setHasData(true);
//                        //两块不同的View分别渲染本地流和媒体流
//                        //防止本地和媒体同时展示在一个View上面
//                        sdkEngine.renderLocalView(info,localrenderview,UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT,null);
                        WritableMap params = combineInfo(info);
                        params.putInt("code",i);
                        params.putString("msg",s);
                        sendEvent(UCloudRNEvent.EVENT_PUBLISH,params);
                    }else{
                        SuperLog.d(TAG,"onLocalUnPublish ignored for : " + UCloudRNErrorCode.ENGINE_HAS_DESTROYED);
                    }
                }
            });
        }

        @Override
        public void onLocalUnPublish(int i, String s, UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {
            //取消发布媒体流
            SuperLog.d(TAG,"onLocalUnPublish");
            WritableMap params = combineInfo(uCloudRtcSdkStreamInfo);
            params.putInt("code",i);
            params.putString("msg",s);
            sendEvent(UCloudRNEvent.EVENT_UN_PUBLISH,params);
        }

        @Override
        public void onRemoteUserJoin(String s) {

        }

        @Override
        public void onRemoteUserLeave(String s, int i) {

        }

        @Override
        public void onRemotePublish(UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {
            SuperLog.d(TAG,"onRemotePublish received info: "+ uCloudRtcSdkStreamInfo);
            WritableMap params = combineInfo(uCloudRtcSdkStreamInfo);
            sendEvent(UCloudRNEvent.EVENT_REMOTE_PUBLISH,params);
        }

        @Override
        public void onRemoteUnPublish(UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {
            SuperLog.d(TAG,"onRemoteUnPublish "+ uCloudRtcSdkStreamInfo);
            WritableMap params = combineInfo(uCloudRtcSdkStreamInfo);
            sendEvent(UCloudRNEvent.EVENT_REMOTE_UN_PUBLISH,params);
        }

        @Override
        public void onSubscribeResult(int i, String s, UCloudRtcSdkStreamInfo info) {
            SuperLog.d(TAG,"onSubscribeResult received code:" + i + " msg: "+ s + "info: "+ info);
            //订阅媒体流结果
            mContext.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(sdkEngine != null){
                        if(info.isHasVideo()){
                            //进行媒体流渲染
                            sdkEngine.startRemoteView(info, rnMyVideoView.getVideoView(),UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT,null);
                        }else{
                            SuperLog.d(TAG,"onSubscribeResult only has audio ,do not render");
                        }
                    }else{
                        SuperLog.d(TAG,"onSubscribeResult ignored for : " + UCloudRNErrorCode.ENGINE_HAS_DESTROYED);
                    }
                }
            });
        }

        @Override
        public void onUnSubscribeResult(int i, String s, UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {
            //取消媒体订阅流

        }

        @Override
        public void onLocalStreamMuteRsp(int i, String s, UCloudRtcSdkMediaType uCloudRtcSdkMediaType, UCloudRtcSdkTrackType uCloudRtcSdkTrackType, boolean b) {

        }

        @Override
        public void onRemoteStreamMuteRsp(int i, String s, String s1, UCloudRtcSdkMediaType uCloudRtcSdkMediaType, UCloudRtcSdkTrackType uCloudRtcSdkTrackType, boolean b) {

        }

        @Override
        public void onRemoteTrackNotify(String s, UCloudRtcSdkMediaType uCloudRtcSdkMediaType, UCloudRtcSdkTrackType uCloudRtcSdkTrackType, boolean b) {

        }

        @Override
        public void onSendRTCStats(UCloudRtcSdkStats uCloudRtcSdkStats) {

        }

        @Override
        public void onRemoteRTCStats(UCloudRtcSdkStats uCloudRtcSdkStats) {

        }

        @Override
        public void onLocalAudioLevel(int i) {

        }

        @Override
        public void onRemoteAudioLevel(String s, int i) {

        }

        @Override
        public void onKickoff(int i) {

        }

        @Override
        public void onWarning(int i) {

        }

        @Override
        public void onError(int i) {

        }

        @Override
        public void onRecordStart(int i, String s) {

        }

        @Override
        public void onRecordStop(int i) {

        }

        @Override
        public void onQueryMix(int i, String s, int i1, String s1, String s2) {

        }

        @Override
        public void onRecordStatusNotify(UCloudRtcSdkMediaServiceStatus uCloudRtcSdkMediaServiceStatus, int i, String s, String s1, String s2, String s3, String s4) {

        }

        @Override
        public void onRelayStatusNotify(UCloudRtcSdkMediaServiceStatus uCloudRtcSdkMediaServiceStatus, int i, String s, String s1, String s2, String s3, String[] strings) {

        }

        @Override
        public void onAddStreams(int i, String s) {

        }

        @Override
        public void onDelStreams(int i, String s) {

        }

        @Override
        public void onLogOffUsers(int i, String s) {

        }

        @Override
        public void onMsgNotify(int i, String s) {

        }

        @Override
        public void onLogOffNotify(int i, String s) {

        }

        @Override
        public void onServerBroadCastMsg(String s, String s1) {

        }

        @Override
        public void onAudioDeviceChanged(UCloudRtcSdkAudioDevice uCloudRtcSdkAudioDevice) {

        }

        @Override
        public void onPeerLostConnection(int i, UCloudRtcSdkStreamInfo uCloudRtcSdkStreamInfo) {

        }

        @Override
        public void onNetWorkQuality(String s, UCloudRtcSdkStreamType uCloudRtcSdkStreamType, UCloudRtcSdkMediaType uCloudRtcSdkMediaType, UCloudRtcSdkNetWorkQuality uCloudRtcSdkNetWorkQuality) {

        }

        @Override
        public void onAudioFileFinish() {

        }
    };


}

