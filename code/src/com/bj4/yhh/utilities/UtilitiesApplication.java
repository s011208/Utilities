
package com.bj4.yhh.utilities;

import java.util.ArrayList;

import com.bj4.yhh.utilities.listmenu.ListMenuItem;
import com.bj4.yhh.utilities.music.MusicDatabaseHelper;
import com.bj4.yhh.utilities.music.parser.U2BDataParser;
import com.bj4.yhh.utilities.weather.LoadCitiesListService;
import com.bj4.yhh.utilities.weather.WeatherData;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.util.LruCache;
import android.util.SparseArray;

public class UtilitiesApplication extends Application {

    public static final ArrayList<ListMenuItem> LIST_MENU_ITEMS = new ArrayList<ListMenuItem>();

    public static final SparseArray<Integer> FRAGMENT_MATCH_SPARSE_ARRAY = new SparseArray<Integer>();

    public static boolean sIsCitiesServiceLoading = false;

    public static LruCache<Long, WeatherData> sWeatherDataCache = new LruCache<Long, WeatherData>(
            30);

    @Override
    public void onCreate() {
        super.onCreate();
        refreshListMenuItem();
        initU2BDataParser();
        loadCitiesListIfNeeded();
    }

    private void loadCitiesListIfNeeded() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        if (db.hasCitiesTableLoaded() == false) {
            sIsCitiesServiceLoading = true;
            startService(new Intent(this, LoadCitiesListService.class));
        }
    }

    private void initU2BDataParser() {
        U2BDataParser parser = U2BDataParser.getInstance(this);
        MusicDatabaseHelper musicDb = MusicDatabaseHelper.getInstance(this);
        musicDb.addCallback(parser);
    }

    private void refreshListMenuItem() {
        LIST_MENU_ITEMS.clear();
        LIST_MENU_ITEMS.addAll(DatabaseHelper.getInstance(this).getListMenu());
        FRAGMENT_MATCH_SPARSE_ARRAY.clear();
        Resources r = getResources();
        String calculator = r.getString(R.string.item_calculator);
        String weather = r.getString(R.string.item_weather);
        String music = r.getString(R.string.item_music);
        for (int i = 0; i < LIST_MENU_ITEMS.size(); i++) {
            ListMenuItem item = LIST_MENU_ITEMS.get(i);
            if (calculator.equals(item.mContent)) {
                FRAGMENT_MATCH_SPARSE_ARRAY.put(i, MainActivity.FRAGMENT_CALCULATOR);
            } else if (weather.equals(item.mContent)) {
                FRAGMENT_MATCH_SPARSE_ARRAY.put(i, MainActivity.FRAGMENT_WEATHER);
            } else if (music.equals(item.mContent)) {
                FRAGMENT_MATCH_SPARSE_ARRAY.put(i, MainActivity.FRAGMENT_MUSIC);
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
