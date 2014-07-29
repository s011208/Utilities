
package com.bj4.yhh.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MainContainer extends FrameLayout {
    public interface MainContainerTouchCallback {
        public boolean onInterceptTouchEvent();
    }

    private MainContainerTouchCallback mCallback;

    public void setCallback(MainContainerTouchCallback cb) {
        mCallback = cb;
    }

    public MainContainer(Context context) {
        this(context, null);
    }

    public MainContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mCallback != null) {
            return mCallback.onInterceptTouchEvent();
        }
        return false;
    }

}
