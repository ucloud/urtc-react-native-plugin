package com.ucloud_plugin;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ucloud_plugin.utils.SuperLog;
import com.ucloudrtclib.sdkengine.define.UCloudRtcRenderView;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkScaleType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkSurfaceVideoView;


class RNMyVideoView extends LinearLayout {
    private Context mContext;
    private UCloudRtcRenderView mLocalView;
    private static final String TAG = "RNMyVideoView";
    private static RNMyVideoView mInstance;


    public static RNMyVideoView getInstance(Context context) {
        if (mInstance == null) {
            synchronized (RNMyVideoView.class) {
                if (mInstance == null) {
                    mInstance = new RNMyVideoView(context);
                }
            }
        }
        return mInstance;
    }



    public RNMyVideoView(Context context){
        super(context);
        SuperLog.d(TAG,"RNMyVideoView ctor");
        this.mContext = context;
        mLocalView = new UCloudRtcRenderView(mContext.getApplicationContext());
        mLocalView.init();
        mLocalView.setScaleType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
        mLocalView.setId(R.id.video_view);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,UiHelper.dipToPx(mContext, ViewGroup.LayoutParams.MATCH_PARENT));

        mLocalView.setLayoutParams(params);
        this.removeAllViews();
        this.addView(mLocalView);

    }

    public UCloudRtcRenderView getVideoView(){
        return mLocalView;
    }
    //以下代码修复通过动态 addView 后看不到的问题

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

}
