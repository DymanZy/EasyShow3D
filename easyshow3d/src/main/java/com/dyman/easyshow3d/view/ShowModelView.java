package com.dyman.easyshow3d.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.dyman.easyshow3d.bean.ModelObject;


/**
 * Created by dyman on 16/7/25.
 */
public class ShowModelView extends ModelView{

    private static final String TAG = "ShowModelView";

    public ShowModelView(Context context) {
        this(context, null);
    }

    public ShowModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    /**
     *  触摸事件回调方法，支持动作：单指旋转，双指缩放
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float y = e.getY();
        float x = e.getX();

        switch (e.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                if (touchMode ==TOUCH_NONE && e.getPointerCount() == 1){
                    touchMode = TOUCH_DRAG;
                    mPreviousX = e.getX();
                    mPreviousY = e.getY();
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (e.getPointerCount() >= 2){
                    pinchStartDistance = getPinchDistance(e);
                    if (pinchStartDistance >= 50f){
                        touchMode = TOUCH_ZOOM;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_ZOOM && pinchStartDistance > 0){
                    changeScale = getPinchDistance(e) / pinchStartDistance;
                    wholeScale = changeScale * previousScale;
                } else if(touchMode == TOUCH_DRAG){
                    float dy = y - mPreviousY;//计算触控笔Y位移
                    float dx = x - mPreviousX;//计算触控笔X位移
                    mRenderer.yAngle += dx * TOUCH_SCALE_FACTOR;//设置沿x轴旋转角度
                    mRenderer.zAngle += dy * TOUCH_SCALE_FACTOR;//设置沿z轴旋转角度
                }
                requestRender();
                mPreviousY = y;//记录触控笔位置
                mPreviousX = x;//记录触控笔位置
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (touchMode == TOUCH_ZOOM){
                    touchMode = TOUCH_NONE;
                    previousScale = wholeScale;//记录缩放倍数
                }
                break;

            case MotionEvent.ACTION_UP:
                if (touchMode == TOUCH_DRAG){ touchMode = TOUCH_NONE; }
                break;
        }
        return true;
    }


    /**
     *  计算两指间的距离
     * @param event
     * @return
     */
    private float getPinchDistance(MotionEvent event) {
        float x=0;
        float y=0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
    }

}
