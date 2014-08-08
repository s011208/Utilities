
package com.bj4.yhh.utilities.floatingwindow;

import java.util.ArrayList;

import com.bj4.yhh.utilities.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class FloatingWindow extends FrameLayout {
    private static final String PREFS_KEY = "floating_window_pref";

    private static final String PREFS_KEY_LAST_X_POSITION = "x_position";

    private static final String PREFS_KEY_LAST_Y_POSITION = "y_position";

    private static final String PREFS_KEY_LAST_WIDTH = "width";

    private static final String PREFS_KEY_LAST_HEIGHT = "height";

    public interface FloatingWindowCallback {
        public void onCloseWindow(String classKey);
    }

    private FloatingWindowCallback mCallback;

    public Context mContext;

    private WindowManager.LayoutParams wmParams;

    private WindowManager wm;

    private boolean mIsWindowShown = false;

    private static SharedPreferences sPrefs;

    private LayoutInflater mInflater;

    private FrameLayout mContainer;

    private RelativeLayout mMainParent;

    private TextView mTitle;

    private RelativeLayout mDragableActionBar;

    private TextView mClose, mExtend;

    private int mStatusbarHeight;

    private static ArrayList<String> sWindowOrderList = new ArrayList<String>();

    public void setCallback(FloatingWindowCallback cb) {
        mCallback = cb;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public synchronized static SharedPreferences getSharedPreferences(Context context) {
        if (sPrefs == null) {
            sPrefs = context.getApplicationContext().getSharedPreferences(PREFS_KEY,
                    Context.MODE_PRIVATE);
        }
        return sPrefs;
    }

    public FloatingWindow(Context context) {
        this(context, null);
    }

    public FloatingWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mStatusbarHeight = getStatusBarHeight();
        wm = (WindowManager)mContext.getSystemService("window");
        wmParams = getDefaultWindowManagerParamsSettings();
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainParent = (RelativeLayout)mInflater.inflate(R.layout.floating_window, null);
        mTitle = (TextView)mMainParent.findViewById(R.id.floating_window_title);
        mContainer = (FrameLayout)mMainParent.findViewById(R.id.floating_window_container);
        mClose = (TextView)mMainParent.findViewById(R.id.floating_window_close);
        mExtend = (TextView)mMainParent.findViewById(R.id.floating_window_extend);
        mDragableActionBar = (RelativeLayout)mMainParent.findViewById(R.id.floating_action_bar);
        mExtendTouchListener = new ExtendTouchListener(mContext);
        mExtendTouchListener.setWindowParams(this, wm, wmParams);
        mExtend.setOnTouchListener(mExtendTouchListener);
        mDragWindowTouchListener = new DragWindowTouchListener(mContext);
        mDragWindowTouchListener.setWindowParams(this, wm, wmParams);
        mDragableActionBar.setOnTouchListener(mDragWindowTouchListener);
        mClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onCloseWindow(getClassStringKey());
                    wm.removeView(FloatingWindow.this);
                    String classString = getClassStringKey();
                    sWindowOrderList.remove(classString);
                }
            }
        });
        init();
        addView(mMainParent);
        wm.addView(this, wmParams);
        mIsWindowShown = true;
        String classString = getClassStringKey();
        sWindowOrderList.remove(classString);
        sWindowOrderList.add(0, classString);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            String classString = getClassStringKey();
            if (classString != null && sWindowOrderList.isEmpty() == false
                    && sWindowOrderList.get(0).equals(classString) == false) {
                wmParams.windowAnimations = 0;
                wm.removeView(this);
                wm.addView(this, wmParams);
                wmParams.windowAnimations = android.R.style.Animation_Toast;
                sWindowOrderList.remove(classString);
                sWindowOrderList.add(0, classString);
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public abstract String getClassStringKey();

    private ExtendTouchListener mExtendTouchListener;

    private class ExtendTouchListener implements View.OnTouchListener {
        private WindowManager mWm;

        private WindowManager.LayoutParams mWparams;

        private MotionEvent mDownEvent;

        private View mFloating;

        private Context mContext;

        private int mStartX, mStartY;

        public ExtendTouchListener(Context context) {
            mContext = context;
        }

        public void setWindowParams(View view, WindowManager wm, WindowManager.LayoutParams params) {
            mFloating = view;
            mWm = wm;
            mWparams = params;
        }

        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            final int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownEvent = MotionEvent.obtain(ev);
                    mStartX = mWparams.width;
                    mStartY = mWparams.height;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int)(ev.getRawX() - mDownEvent.getRawX());
                    int deltaY = (int)(ev.getRawY() - mDownEvent.getRawY());
                    mWparams.width = mStartX + deltaX;
                    mWparams.height = mStartY - deltaY;
                    mWm.updateViewLayout(mFloating, mWparams);
                    break;
                case MotionEvent.ACTION_UP:
                    mDownEvent.recycle();
                    float density = mContext.getResources().getDisplayMetrics().density;
                    int width = (int)(mWparams.width / density);
                    int height = (int)(mWparams.height / density);
                    getSharedPreferences(mContext).edit().putInt(getWidthKey(), width).commit();
                    getSharedPreferences(mContext).edit().putInt(getHeightKey(), height).commit();
                    break;
            }
            return true;
        }
    }

    private DragWindowTouchListener mDragWindowTouchListener;

    private class DragWindowTouchListener implements View.OnTouchListener {
        private WindowManager mWm;

        private WindowManager.LayoutParams mWparams;

        private MotionEvent mDownEvent;

        private View mFloating;

        private Context mContext;

        private int mStartX, mStartY;

        public DragWindowTouchListener(Context context) {
            mContext = context;
        }

        public void setWindowParams(View view, WindowManager wm, WindowManager.LayoutParams params) {
            mFloating = view;
            mWm = wm;
            mWparams = params;
        }

        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            final int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownEvent = MotionEvent.obtain(ev);
                    mStartX = mWparams.x;
                    mStartY = mWparams.y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mWparams != null) {
                        int deltaX = (int)(ev.getRawX() - mDownEvent.getRawX());
                        int deltaY = (int)(ev.getRawY() - mDownEvent.getRawY());
                        mWparams.x = mStartX + deltaX;
                        mWparams.y = mStartY + deltaY;
                        mWm.updateViewLayout(mFloating, mWparams);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mDownEvent.recycle();
                    getSharedPreferences(mContext).edit().putInt(getXKey(), mWparams.x).commit();
                    getSharedPreferences(mContext).edit().putInt(getYKey(), mWparams.y).commit();
                    break;
            }
            return true;
        }
    }

    public abstract void init();

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void addFloatingContent(View content) {
        mContainer.addView(content);
    }

    public String getXKey() {
        return PREFS_KEY_LAST_X_POSITION + getClass().toString();
    }

    public String getYKey() {
        return PREFS_KEY_LAST_Y_POSITION + getClass().toString();
    }

    public String getWidthKey() {
        return PREFS_KEY_LAST_WIDTH + getClass().toString();
    }

    public String getHeightKey() {
        return PREFS_KEY_LAST_HEIGHT + getClass().toString();
    }

    public WindowManager.LayoutParams getDefaultWindowManagerParamsSettings() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.x = getSharedPreferences(mContext).getInt(getXKey(), 0);
        params.y = getSharedPreferences(mContext).getInt(getYKey(), 200);
        float density = mContext.getResources().getDisplayMetrics().density;
        params.height = (int)(getSharedPreferences(mContext).getInt(getHeightKey(), 400) * density);
        params.width = (int)(getSharedPreferences(mContext).getInt(getWidthKey(), 200) * density);
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.horizontalWeight = 0;
        params.verticalWeight = 0;
        params.windowAnimations = android.R.style.Animation_Toast;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;

        return params;
    }

    public WindowManager.LayoutParams getWMParams() {
        return wmParams;
    }

    public void showFloatingShortcut() {
        if (mIsWindowShown)
            wm.removeView(this);
        wm.addView(this, wmParams);
    }

}
