
package com.bj4.yhh.utilities.weather;

import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.SettingManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.widget.TextView;

public class WeatherOptionDialog extends DialogFragment {
    private View mContentView;

    public static WeatherOptionDialog getNewInstance() {
        WeatherOptionDialog newInstance = new WeatherOptionDialog();
        return newInstance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initComponents();
        return new AlertDialog.Builder(getActivity()).setCustomTitle(null).setView(mContentView)
                .setCancelable(true).create();
    }

    private void initComponents() {
        final Context context = getActivity().getApplicationContext();
        mContentView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.weather_option_dialog, null);
        TextView settings = (TextView)mContentView.findViewById(R.id.weather_option_setting);
        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                WeatherSettingDialog dialog = WeatherSettingDialog.getNewInstance();
                dialog.show(getFragmentManager(), "WeatherSettingDialog");
            }
        });
        CheckBox simpleView = (CheckBox)mContentView
                .findViewById(R.id.weather_option_using_simple_view);
        simpleView.setChecked(SettingManager.getInstance(context).isWeatherUsingSimpleView());
        simpleView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingManager.getInstance(context).setWeatherUsingSimpleView(isChecked);
            }
        });
        RadioGroup rg = (RadioGroup)mContentView.findViewById(R.id.weather_option_rgroup);
        rg.check(SettingManager.getInstance(context).isUsingC() ? R.id.weather_c : R.id.weather_f);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.weather_f:
                        SettingManager.getInstance(context).setUsingC(false);
                        break;
                    case R.id.weather_c:
                        SettingManager.getInstance(context).setUsingC(true);
                        break;
                }
            }
        });
    }
}
