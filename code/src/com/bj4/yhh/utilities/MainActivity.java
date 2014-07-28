
package com.bj4.yhh.utilities;

import com.bj4.yhh.utilities.calculator.CalculatorFragment;
import com.bj4.yhh.utilities.fragments.BaseFragment;
import com.bj4.yhh.utilities.listmenu.ListMenu;
import com.bj4.yhh.utilities.listmenu.ListMenu.OnListMenuSelectedCallback;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity implements OnListMenuSelectedCallback {

    private static final int EXPAND_LIST_MENU_ANIMATION_DURATION = 300;

    public static final int FRAGMENT_CALCULATOR = 0;

    private RelativeLayout mActionBar, mListMenu;

    private FrameLayout mMainContainer;

    private ValueAnimator mExpandListMenuAnimator, mDarkenMainContainerAnimator;

    private boolean mIsListMenuExpanded = false;

    private int mListMenuWidth;

    private ListMenu mListMenuItem;

    private String[] mListMenuItemContent;

    private TextView mActionBarTitle;

    private BaseFragment mCalculatorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        init();
        initListMenuAnimations();
        switchFragment(FRAGMENT_CALCULATOR, false);
    }

    private synchronized CalculatorFragment getCalculatorFragment() {
        if (mCalculatorFragment == null) {
            mCalculatorFragment = new CalculatorFragment();
        }
        return (CalculatorFragment)mCalculatorFragment;
    }

    public void switchFragment(int targetFragment, boolean animated) {
        Fragment fragment = getCalculatorFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        switch (targetFragment) {
            case FRAGMENT_CALCULATOR:
                fragment = getCalculatorFragment();
                break;
        }
        transaction.replace(R.id.main_container, fragment).commit();
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
        mListMenuItemContent = getResources().getStringArray(R.array.list_menu_item);
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
        mActionBarTitle.setText(mListMenuItemContent[0]);
        mListMenu = (RelativeLayout)findViewById(R.id.list_menu);
        mListMenu.setTranslationX(-mListMenuWidth);
        mMainContainer = (FrameLayout)findViewById(R.id.main_container);
        mMainContainer.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mIsListMenuExpanded) {
                    collapseListMenu();
                    return true;
                } else {
                    return v.onTouchEvent(event);
                }
            }
        });
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
        mActionBarTitle.setText(mListMenuItemContent[index]);
        collapseListMenu();
    }
}
