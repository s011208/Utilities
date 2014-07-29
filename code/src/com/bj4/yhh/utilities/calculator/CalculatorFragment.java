
package com.bj4.yhh.utilities.calculator;

import com.bj4.yhh.utilities.fragments.BaseFragment;

public class CalculatorFragment extends BaseFragment {

    @Override
    public void init() {
        mContentView = new Calculator(mContext);
    }

}
