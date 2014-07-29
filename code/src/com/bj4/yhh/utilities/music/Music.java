
package com.bj4.yhh.utilities.music;

import java.util.ArrayList;

import com.bj4.yhh.utilities.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Music extends FrameLayout {

    public static final String INTENT_ON_DATA_UPDATE = "com.bj4.yhh.utilities.music.on_data_update";

    private Context mContext;

    private MusicListAdapter mAdapter;

    public Music(Context context) {
        this(context, null);
    }

    public Music(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Music(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public void updateContent() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void init() {
        View contentView = ((LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.music, null);
        ListView musicList = (ListView)contentView.findViewById(R.id.music_data_list);
        mAdapter = new MusicListAdapter(mContext);
        musicList.setAdapter(mAdapter);
        addView(contentView);
    }

    public static class MusicListAdapter extends BaseAdapter {
        private Context mContext;

        private ArrayList<String> mData = new ArrayList<String>();

        private LayoutInflater mInflater;

        public MusicListAdapter(Context context) {
            mContext = context;
            mInflater = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            init();
        }

        public void notifyDataSetChanged() {
            init();
            super.notifyDataSetChanged();
        }

        private void init() {
            mData = MusicDatabaseHelper.getInstance(mContext).getMusicTypeList();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public String getItem(int position) {
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
                convertView = mInflater.inflate(R.layout.music_item, null);
                holder = new ViewHolder();
                holder.mType = (TextView)convertView.findViewById(R.id.music_data_list_item);
                holder.mState = (TextView)convertView.findViewById(R.id.music_data_list_item_state);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.mType.setText(getItem(position));
            holder.mState.setText("Start");
            return convertView;
        }

        public static class ViewHolder {
            TextView mType, mState;
        }

    }
}
