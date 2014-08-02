
package com.bj4.yhh.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Intent startIntent = new Intent(context, UpdateManagerService.class);
            context.startService(startIntent);
        }
    }

}
