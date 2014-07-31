
package com.bj4.yhh.utilities.weather;

import com.bj4.yhh.utilities.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class WeatherRemoveDialog extends DialogFragment {
    public interface Callback {
        public void onPositiveClick();
    }

    private WeatherData mWData;

    private Callback mCallback;

    private void init(WeatherData data, Callback cb) {
        mWData = data;
        mCallback = cb;
    }

    public static WeatherRemoveDialog getNewInstance(WeatherData data, Callback cb) {
        WeatherRemoveDialog newInstance = new WeatherRemoveDialog();
        newInstance.init(data, cb);
        return newInstance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setCustomTitle(null)
                .setMessage(
                        getActivity().getResources().getString(R.string.remove) + " "
                                + mWData.mCity + ", " + mWData.mCountry)
                .setPositiveButton(R.string.ok,
                        new android.content.DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mCallback != null) {
                                    mCallback.onPositiveClick();
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new android.content.DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(true).create();
    }
}
