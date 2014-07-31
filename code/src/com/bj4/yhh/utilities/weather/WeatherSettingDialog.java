
package com.bj4.yhh.utilities.weather;

import java.util.ArrayList;

import com.bj4.yhh.utilities.DatabaseHelper;
import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.UtilitiesApplication;
import com.bj4.yhh.utilities.util.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WeatherSettingDialog extends DialogFragment {
    private static final String TAG = "WeatherSettingDialog";

    private static ArrayList<String> sAllCities = new ArrayList<String>();

    private View mContentView;

    private Context mContext;

    private WeatherListAdapter mAdapter;

    private FrameLayout mPbar;

    private AutoCompleteTextView mAutoCompleteText;

    public static WeatherSettingDialog getNewInstance() {
        WeatherSettingDialog newInstance = new WeatherSettingDialog();
        return newInstance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initComponents();
        IntentFilter filter = new IntentFilter(Weather.INTENT_ON_DATA_UPDATE);
        filter.addAction(Weather.INTENT_ON_ID_UPDATE);
        if (mContext != null)
            mContext.registerReceiver(mReceiver, filter);
        return new AlertDialog.Builder(getActivity()).setCustomTitle(null).setView(mContentView)
                .setCancelable(true).create();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Weather.INTENT_ON_ID_UPDATE.equals(action)) {
                Bundle extras = intent.getExtras();
                boolean success = false;
                if (extras != null) {
                    success = extras.getBoolean(Weather.INTENT_EXTRAS_ON_ID_UPDATE_RESULT);
                }
                if (success) {
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    // find city
                } else {
                    // not find city
                    if (mPbar != null) {
                        mPbar.setVisibility(View.GONE);
                    }
                }
            } else if (Weather.INTENT_ON_DATA_UPDATE.equals(action)) {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                if (mPbar != null) {
                    mPbar.setVisibility(View.GONE);
                }
            }
        }
    };

    public void onDestroyView() {
        if (mContext != null)
            mContext.unregisterReceiver(mReceiver);
        super.onDestroyView();
    }

    private void initComponents() {
        mContext = getActivity().getApplicationContext();
        mContentView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.weather_setting_dialog, null);
        ListView mWeatherList = (ListView)mContentView.findViewById(R.id.weather_list);
        mAdapter = new WeatherListAdapter(mContext);
        mWeatherList.setAdapter(mAdapter);
        mAutoCompleteText = (AutoCompleteTextView)mContentView
                .findViewById(R.id.weather_list_auto_c_txt);
        refreshAutoCompleteTextContent();
        TextView confirm = (TextView)mContentView.findViewById(R.id.weather_list_add);
        confirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPbar != null) {
                    mPbar.setVisibility(View.VISIBLE);
                }
                Intent intent = new Intent(mContext, WeatherService.class);
                float[] info = DatabaseHelper.getInstance(mContext).getCityInfo(
                        mAutoCompleteText.getText().toString());
                intent.putExtra(WeatherService.INTENT_KEY_LAT, info[0]);
                intent.putExtra(WeatherService.INTENT_KEY_LON, info[1]);
                mContext.startService(intent);
                mAutoCompleteText.setText("");
            }
        });
        mPbar = (FrameLayout)mContentView.findViewById(R.id.progress_bar);
    }

    private void refreshAutoCompleteTextContent() {
        if (sAllCities.isEmpty()) {
            sAllCities = DatabaseHelper.getInstance(mContext).getAllCitiesName();
        }
        ArrayAdapter<String> citiesAdapter = new ArrayAdapter<String>(mContext,
                R.layout.auto_complete_item_view, sAllCities);
        mAutoCompleteText.setAdapter(citiesAdapter);
    }

    public static class WeatherListAdapter extends BaseAdapter {
        private Context mContext;

        private ArrayList<WeatherWoeId> mData;

        private LayoutInflater mInflater;

        public WeatherListAdapter(Context c) {
            mContext = c;
            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            init();
        }

        private void init() {
            mData = DatabaseHelper.getInstance(mContext).getWeatherWoeid();
        }

        public void notifyDataSetChanged() {
            init();
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public WeatherWoeId getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.weather_setting_item, null);
                holder = new ViewHolder();
                holder.mListItem = (TextView)convertView.findViewById(R.id.weather_setting_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            WeatherWoeId woeidData = getItem(position);
            WeatherData wData = UtilitiesApplication.sWeatherDataCache.get(woeidData.mWoeid);
            if (wData == null) {
                holder.mListItem.setText(woeidData.mWoeid + "");
            } else {
                setListItemContent(holder.mListItem, wData);
            }
            return convertView;
        }

        private static void setListItemContent(TextView txt, WeatherData wData) {
            if (txt == null || wData == null) {
                return;
            }
            txt.setText(wData.mCity + ", " + wData.mCountry);
        }

        public static class WeatherDataLoader extends AsyncTask<Void, Void, Void> {
            private TextView mTxt;

            private Context mContext;

            private Long mWoeid;

            private WeatherData mWeatherData;

            public WeatherDataLoader(TextView txt, Context ctx, Long woeid) {
                mTxt = txt;
                mContext = ctx;
                mWoeid = woeid;
            }

            @Override
            protected Void doInBackground(Void... params) {
                mWeatherData = Utils.parseWeatherData(mContext, mWoeid);
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                setListItemContent(mTxt, mWeatherData);
            }
        }

        public static class ViewHolder {
            TextView mListItem;
        }

    }
}
