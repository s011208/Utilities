
package com.bj4.yhh.utilities.weather;

import com.bj4.yhh.utilities.fragments.BaseFragment;

public class WeatherFragment extends BaseFragment {

    @Override
    public void init() {
        mContentView = new Weather(mContext);
    }

}
