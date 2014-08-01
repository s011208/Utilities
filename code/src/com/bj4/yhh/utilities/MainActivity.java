
package com.bj4.yhh.utilities;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.bj4.yhh.utilities.analytics.Analytics;
import com.bj4.yhh.utilities.analytics.flurry.FlurryTracker;
import com.bj4.yhh.utilities.analytics.mixpanel.MixpanelTracker;
import com.bj4.yhh.utilities.calculator.CalculatorFragment;
import com.bj4.yhh.utilities.fragments.BaseFragment;
import com.bj4.yhh.utilities.listmenu.ListMenu;
import com.bj4.yhh.utilities.listmenu.ListMenu.OnListMenuSelectedCallback;
import com.bj4.yhh.utilities.music.MusicFragment;
import com.bj4.yhh.utilities.weather.WeatherFragment;
import com.bj4.yhh.utilities.weather.WeatherOptionDialog;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnListMenuSelectedCallback,
        MainContainer.MainContainerTouchCallback {

    private static final int EXPAND_LIST_MENU_ANIMATION_DURATION = 300;

    public static final int FRAGMENT_CALCULATOR = 0;

    public static final int FRAGMENT_WEATHER = 1;

    public static final int FRAGMENT_MUSIC = 2;

    private int mCurrentFragment = -1;

    private RelativeLayout mActionBar, mListMenu;

    private FrameLayout mMainContainer;

    private ValueAnimator mExpandListMenuAnimator, mDarkenMainContainerAnimator;

    private boolean mIsListMenuExpanded = false;

    private int mListMenuWidth;

    private ListMenu mListMenuItem;

    private TextView mActionBarTitle;

    private BaseFragment mCalculatorFragment, mWeatherFragment, mMusicFragment;

    private ImageView mOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        MixpanelTracker.getTracker(this); // init
        init();
        initListMenuAnimations();
        switchFragment(UtilitiesApplication.FRAGMENT_MATCH_SPARSE_ARRAY.get(0), false);
    }

    public void onDestroy() {
        MixpanelTracker.getTracker(this).flush(); // send data while onDestroy
        super.onDestroy();
    }

    public void onStart() {
        super.onStart();
        FlurryTracker.startSession(this);
    }

    public void onStop() {
        FlurryTracker.endSession(this);
        super.onStop();
    }

    private synchronized MusicFragment getMusicFragment() {
        if (mMusicFragment == null) {
            mMusicFragment = new MusicFragment();
        }
        return (MusicFragment)mMusicFragment;
    }

    private synchronized WeatherFragment getWeatherFragment() {
        if (mWeatherFragment == null) {
            mWeatherFragment = new WeatherFragment();
        }
        return (WeatherFragment)mWeatherFragment;
    }

    private synchronized CalculatorFragment getCalculatorFragment() {
        if (mCalculatorFragment == null) {
            mCalculatorFragment = new CalculatorFragment();
        }
        return (CalculatorFragment)mCalculatorFragment;
    }

    public void switchFragment(int targetFragment, boolean animated) {
        if (mCurrentFragment == targetFragment) {
            return;
        }
        Fragment fragment = getCalculatorFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        HashMap<String, String> flurryTrackMap = new HashMap<String, String>();
        switch (targetFragment) {
            case FRAGMENT_CALCULATOR:
                fragment = getCalculatorFragment();
                mOption.setVisibility(View.GONE);
                MixpanelTracker.getTracker(this).track(Analytics.ViewingFragment.EVENT,
                        Analytics.ViewingFragment.VIEWING_FRAGMENT,
                        Analytics.ViewingFragment.FRAGMENT_CALCULATOR);
                flurryTrackMap.put(Analytics.ViewingFragment.VIEWING_FRAGMENT,
                        Analytics.ViewingFragment.FRAGMENT_CALCULATOR);
                FlurryTracker.getInstance().track(Analytics.ViewingFragment.EVENT, flurryTrackMap);
                break;
            case FRAGMENT_WEATHER:
                fragment = getWeatherFragment();
                mOption.setVisibility(View.VISIBLE);
                MixpanelTracker.getTracker(this).track(Analytics.ViewingFragment.EVENT,
                        Analytics.ViewingFragment.VIEWING_FRAGMENT,
                        Analytics.ViewingFragment.FRAGMENT_WEATHER);
                flurryTrackMap.put(Analytics.ViewingFragment.VIEWING_FRAGMENT,
                        Analytics.ViewingFragment.FRAGMENT_WEATHER);
                FlurryTracker.getInstance().track(Analytics.ViewingFragment.EVENT, flurryTrackMap);
                break;
            case FRAGMENT_MUSIC:
                fragment = getMusicFragment();
                mOption.setVisibility(View.GONE);
                MixpanelTracker.getTracker(this).track(Analytics.ViewingFragment.EVENT,
                        Analytics.ViewingFragment.VIEWING_FRAGMENT,
                        Analytics.ViewingFragment.FRAGMENT_MUSIC);
                flurryTrackMap.put(Analytics.ViewingFragment.VIEWING_FRAGMENT,
                        Analytics.ViewingFragment.FRAGMENT_MUSIC);
                FlurryTracker.getInstance().track(Analytics.ViewingFragment.EVENT, flurryTrackMap);
                break;
            default:
                return;
        }
        mCurrentFragment = targetFragment;
        transaction.setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out)
                .replace(R.id.main_container, fragment).commit();
    }

    public void onBackPressed() {
        if (mIsListMenuExpanded) {
            collapseListMenu();
        } else {
            super.onBackPressed();
        }
    }

    private void expandListMenu() {
        mExpandListMenuAnimator.start();
        mDarkenMainContainerAnimator.start();
        mIsListMenuExpanded = true;
    }

    private void collapseListMenu() {
        mExpandListMenuAnimator.reverse();
        mDarkenMainContainerAnimator.reverse();
        mIsListMenuExpanded = false;
    }

    private void init() {
        mListMenuWidth = (int)getResources().getDimension(R.dimen.list_menu_width);
        mActionBar = (RelativeLayout)findViewById(R.id.action_bar);
        mActionBar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsListMenuExpanded == false) {
                    expandListMenu();
                } else {
                    collapseListMenu();
                }
            }
        });
        mActionBarTitle = (TextView)findViewById(R.id.action_bar_title);
        mActionBarTitle.setText(UtilitiesApplication.LIST_MENU_ITEMS.get(0).mContent);
        mOption = (ImageView)findViewById(R.id.action_bar_option);
        mOption.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentFragment) {
                    case FRAGMENT_CALCULATOR:
                        break;
                    case FRAGMENT_WEATHER:
                        WeatherOptionDialog dialog = WeatherOptionDialog.getNewInstance();
                        dialog.show(getFragmentManager(), "WeatherOptionDialog");
                        break;
                    case FRAGMENT_MUSIC:
                        break;
                    default:
                        return;
                }
            }
        });
        mListMenu = (RelativeLayout)findViewById(R.id.list_menu);
        mListMenu.setTranslationX(-mListMenuWidth);
        mMainContainer = (FrameLayout)findViewById(R.id.main_container);
        ((MainContainer)mMainContainer).setCallback(this);
        mListMenuItem = (ListMenu)findViewById(R.id.list_menu_item);
        mListMenuItem.setCallback(this);
    }

    private void initListMenuAnimations() {
        mExpandListMenuAnimator = ValueAnimator.ofFloat(-mListMenuWidth, 0);
        mExpandListMenuAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mListMenu.setTranslationX((Float)animation.getAnimatedValue());
            }
        });
        mExpandListMenuAnimator.setDuration(EXPAND_LIST_MENU_ANIMATION_DURATION);
        mDarkenMainContainerAnimator = ValueAnimator.ofFloat(0, 0.3f);
        mDarkenMainContainerAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ColorDrawable cd = new ColorDrawable(Color.argb(
                        (int)((Float)animation.getAnimatedValue() * 255), 0, 0, 0));
                mMainContainer.setForeground(cd);
            }
        });
        mDarkenMainContainerAnimator.setDuration(EXPAND_LIST_MENU_ANIMATION_DURATION);
    }

    @Override
    public void OnListMenuSelected(int index) {
        mActionBarTitle.setText(UtilitiesApplication.LIST_MENU_ITEMS.get(index).mContent);
        collapseListMenu();
        Integer fragmentIndex = UtilitiesApplication.FRAGMENT_MATCH_SPARSE_ARRAY.get(index);
        if (fragmentIndex != null) {
            switchFragment(UtilitiesApplication.FRAGMENT_MATCH_SPARSE_ARRAY.get(index), true);
        }
    }

    @Override
    public boolean onInterceptTouchEvent() {
        if (mIsListMenuExpanded) {
            collapseListMenu();
            return true;
        }
        return false;
    }
}
