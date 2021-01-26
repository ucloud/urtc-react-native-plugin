package com.ucloud_plugin;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkScaleType;
import com.ucloudrtclib.sdkengine.define.UCloudRtcSdkSurfaceVideoView;


public class RNLocalView extends LinearLayout {
    private Context mContext;
    private  UCloudRtcSdkSurfaceVideoView mLocalView;

    private static RNLocalView mInstance;


    public static RNLocalView getInstance(Context context) {
        if (mInstance == null) {
            synchronized (RNLocalView.class) {
                if (mInstance == null) {
                    mInstance = new RNLocalView(context);
                }
            }
        }
        return mInstance;
    }



    public RNLocalView(Context context){
        super(context);
        this.mContext = context;
        mLocalView = new UCloudRtcSdkSurfaceVideoView(mContext.getApplicationContext());
        mLocalView.init(true);
        mLocalView.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
        mLocalView.setId(R.id.video_view);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,UiHelper.dipToPx(mContext, ViewGroup.LayoutParams.MATCH_PARENT));

        mLocalView.setLayoutParams(params);
        this.addView(mLocalView);

    }

    public UCloudRtcSdkSurfaceVideoView getVideoView(){
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
