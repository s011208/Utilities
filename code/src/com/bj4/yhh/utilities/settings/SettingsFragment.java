
package com.bj4.yhh.utilities.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.SettingManager;
import com.bj4.yhh.utilities.fragments.BaseFragment;

public class SettingsFragment extends BaseFragment {

    @Override
    public void init() {
        final Context context = getActivity();
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(R.layout.settings_fragment, null);
        CheckBox cb = (CheckBox)mContentView.findViewById(R.id.settings_enable_ga);
        cb.setChecked(SettingManager.getInstance(context).isEnableGa());
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingManager.getInstance(context).setEnableGa(isChecked);
            }
        });
    }

}
