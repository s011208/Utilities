
package com.bj4.yhh.utilities;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ToastHelper {

    public static final Toast makeToast(Context context, int type) {
        Toast rtn = new Toast(context);
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View parent = inflater.inflate(R.layout.toast_update_view, null);
        ImageView img = (ImageView)parent.findViewById(R.id.toast_icon);
        TextView txt = (TextView)parent.findViewById(R.id.toast_text);
        switch (type) {
        // case TOAST_TYPE_DEVELOPER_OPENED_HINT:
        // img.setImageResource(R.drawable.toast_icon_alert);
        // txt.setText(R.string.toast_developer_mode_opened_hint);
        // break;
        }
        rtn.setView(parent);
        rtn.setDuration(Toast.LENGTH_SHORT);
        rtn.setGravity(Gravity.BOTTOM, 0,
                (int)context.getResources().getDimension(R.dimen.updating_toast_y_offset));
        return rtn;
    }
}
