
package com.bj4.yhh.utilities.weather;

import com.bj4.yhh.utilities.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
        Context context = getActivity().getApplicationContext();
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
    }
}
