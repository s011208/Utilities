
package com.bj4.yhh.utilities.floatingwindow;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.R.id;
import com.bj4.yhh.utilities.R.layout;
import com.bj4.yhh.utilities.R.string;
import com.bj4.yhh.utilities.fragments.BaseFragment;

public class FloatingWindowOption extends BaseFragment {

    private ListView mFloatingList;

    private FloatingListAdapter mFloatingListAdapter;

    private ArrayList<String> mFloatingWindows = new ArrayList<String>();

    @Override
    public void init() {
        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(R.layout.floating_widow_option_fragment, null);
        mFloatingList = (ListView)mContentView.findViewById(R.id.floating_item_list);
        final String itemWeather = mContext.getString(R.string.item_weather);
        final String itemCalculator = mContext.getString(R.string.item_calculator);
        mFloatingWindows.add(itemWeather);
        mFloatingWindows.add(itemCalculator);
        mFloatingList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (itemWeather.equals(mFloatingListAdapter.getItem(position))) {
                    Intent start = new Intent(mContext, FloatingWindowService.class);
                    start.putExtra(FloatingWindowService.INTENT_START_WINDOW,
                            FloatingWindowService.INTENT_WINDOW_TYPE_WEATHER);
                    mContext.startService(start);
                } else if (itemCalculator.equals(mFloatingListAdapter.getItem(position))) {
                    Intent start = new Intent(mContext, FloatingWindowService.class);
                    start.putExtra(FloatingWindowService.INTENT_START_WINDOW,
                            FloatingWindowService.INTENT_WINDOW_TYPE_CALCULATOR);
                    mContext.startService(start);
                }
            }
        });
        mFloatingListAdapter = new FloatingListAdapter(mFloatingWindows, inflater);
        mFloatingList.setAdapter(mFloatingListAdapter);
    }

    public static class FloatingListAdapter extends BaseAdapter {
        private ArrayList<String> mFloatingWindows;

        private LayoutInflater mInflater;

        public FloatingListAdapter(ArrayList<String> data, LayoutInflater inflater) {
            mFloatingWindows = data;
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return mFloatingWindows.size();
        }

        @Override
        public String getItem(int position) {
            return mFloatingWindows.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup vGroup) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.floating_widow_option_list_item, null);
                holder = new ViewHolder();
                holder.content = (TextView)convertView
                        .findViewById(R.id.floating_window_data_list_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.content.setText(getItem(position));
            return convertView;
        }

        public static class ViewHolder {
            TextView content;
        }
    }
}
