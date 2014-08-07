
package com.bj4.yhh.utilities.floatingwindow;

import com.bj4.yhh.utilities.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
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

    public Context mContext;

    private WindowManager.LayoutParams wmParams;

    private WindowManager wm;

    private boolean mIsWindowShown = false;

    private SharedPreferences mPrefs;

    private LayoutInflater mInflater;

    private FrameLayout mContainer;

    private RelativeLayout mMainParent;

    private TextView mTitle;

    public SharedPreferences getSharedPreferences(Context context) {
        if (mPrefs == null) {
            mPrefs = context.getApplicationContext().getSharedPreferences(PREFS_KEY,
                    Context.MODE_PRIVATE);
        }
        return mPrefs;
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
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainParent = (RelativeLayout)mInflater.inflate(R.layout.floating_window, null);
        mTitle = (TextView)mMainParent.findViewById(R.id.floating_window_title);
        mContainer = (FrameLayout)mMainParent.findViewById(R.id.floating_window_container);
        init();
        addView(mMainParent);
        wm = (WindowManager)mContext.getSystemService("window");
        wmParams = getDefaultWindowManagerParamsSettings();
        wm.addView(this, wmParams);
        mIsWindowShown = true;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

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
        params.height = getSharedPreferences(mContext).getInt(getHeightKey(), 200);
        params.width = getSharedPreferences(mContext).getInt(getWidthKey(), 400);
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
