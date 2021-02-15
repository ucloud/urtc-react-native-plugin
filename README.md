# react-native-urtc

## 安装使用

install with yarn:

```
yarn add react-native-urtc
```

Either way, then link with:

```
react-native link react-native-urtc
```

run in android 

```
react-native run-android
```

upgrade

```
yarn upgrade
```



 ## React-Native 调用

 ```
 import UCloudRtc from 'react-native-urtc';
 import UCloudView from './component/videoView/UCloudView';
 ```
## API
#### 初始化 initWithAppid
```
appid: appid,
appKey: appKey,
sdkMode: 0正式模式  1测试模式    
autoPub: true 开启自动发布 false 关闭
autoSub：true 开启自动订阅 false 关闭
UCloudRtc.initWithAppid(appid, appKey, sdkMode, autoPub,autoSub).then(res => {
    console.log('收到回调', res);
  }).catch(err => {
    console.log('捕获异常', err);
  });
```
#### 加入房间 joinRoomWithRoomid
```
UCloudRtc.joinRoomWithRoomid(roomId, userId, token).then(res => {
    console.log('收到回调', res);
  }).catch(err => {
    console.log('捕获异常', err);
  });
```
#### 离开房间
```
UCloudRtc.leaveRoom();
```
#### 订阅远程流
 ```
 UCloudRtc.subscribeRemoteStream()
 ```
#### 取消订阅远程流
 ```
 UCloudRtc.unSubscribeRemoteStream()
 ```
#### 发布本地流
 ```
 flag: 是否开启摄像头 true-推送音视频流 false-仅推送音频
 UCloudRtc.publishLocalStreamWithCameraEnable(flag)
 ```
#### 取消发布本地流
 ```
 UCloudRtc.unPublishLocalStream()
 ```
#### 录制音视频
 ```
 UCloudRtc.startRecordLocalStreamWithType()
 ```
#### 停止录制
 ```
 UCloudRtc.stopRecordLocalStream()
 ```

### 事件监听
```
//事件列表 0.1.7
EVENT_JOIN_ROOM = "event_joinRoom";
EVENT_LEAVE_ROOM = "event_leaveRoom";
EVENT_PUBLISH = "event_publish";
EVENT_UN_PUBLISH = "event_unPublish";
EVENT_SUB = "event_subscribe";
EVENT_UN_SUB = "event_unSubscribe";
EVENT_REMOTE_PUBLISH = "event_remotePublish";
EVENT_REMOTE_UN_PUBLISH = "event_remoteUnPublish";

const UCloudRtcEventEmitter = new NativeEventEmitter(UCloudRtc);
UCloudRtcEventEmitter.addListener('event_joinRoom', args => {
    console.log('事件event_joinRoom', args);
});
```