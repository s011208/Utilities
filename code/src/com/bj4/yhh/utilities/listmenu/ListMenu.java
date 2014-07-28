
package com.bj4.yhh.utilities.listmenu;

import com.bj4.yhh.utilities.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListMenu extends ListView {
    public interface OnListMenuSelectedCallback {
        public void OnListMenuSelected(int index);
    }

    private OnListMenuSelectedCallback mCallback;

    public ListMenu(Context context) {
        this(context, null);
    }

    public ListMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        final ListMenuAdapter listAdapter = new ListMenuAdapter(context);
        setAdapter(listAdapter);
        setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCallback != null) {
                    mCallback.OnListMenuSelected(position);
                    listAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void setCallback(OnListMenuSelectedCallback cb) {
        mCallback = cb;
    }

    public static class ListMenuAdapter extends BaseAdapter {

        private final String[] mData;

        private LayoutInflater mInflater;

        public ListMenuAdapter(Context context) {
            mData = context.getResources().getStringArray(R.array.list_menu_item);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mData.length;
        }

        @Override
        public String getItem(int position) {
            return mData[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_menu_items, null);
                holder = new ViewHolder();
                holder.mTxt = (TextView)convertView.findViewById(R.id.list_menu_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.mTxt.setText(getItem(position));
            return convertView;
        }

        public static class ViewHolder {
            TextView mTxt;
        }
    }
}
