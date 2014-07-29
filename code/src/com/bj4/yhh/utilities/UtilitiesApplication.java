
package com.bj4.yhh.utilities;

import java.util.ArrayList;

import com.bj4.yhh.utilities.listmenu.ListMenuItem;

import android.app.Application;
import android.content.res.Resources;
import android.util.SparseArray;

public class UtilitiesApplication extends Application {

    public static final ArrayList<ListMenuItem> LIST_MENU_ITEMS = new ArrayList<ListMenuItem>();

    public static final SparseArray<Integer> FRAGMENT_MATCH_SPARSE_ARRAY = new SparseArray<Integer>();

    @Override
    public void onCreate() {
        super.onCreate();
        refreshListMenuItem();
    }

    private void refreshListMenuItem() {
        LIST_MENU_ITEMS.clear();
        LIST_MENU_ITEMS.addAll(DatabaseHelper.getInstance(this).getListMenu());
        FRAGMENT_MATCH_SPARSE_ARRAY.clear();
        Resources r = getResources();
        String calculator = r.getString(R.string.item_calculator);
        String weather = r.getString(R.string.item_weather);
        for (int i = 0; i < LIST_MENU_ITEMS.size(); i++) {
            ListMenuItem item = LIST_MENU_ITEMS.get(i);
            if (calculator.equals(item.mContent)) {
                FRAGMENT_MATCH_SPARSE_ARRAY.put(i, MainActivity.FRAGMENT_CALCULATOR);
            } else if (weather.equals(item.mContent)) {
                FRAGMENT_MATCH_SPARSE_ARRAY.put(i, MainActivity.FRAGMENT_WEATHER);
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
