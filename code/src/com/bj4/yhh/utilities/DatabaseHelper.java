
package com.bj4.yhh.utilities;

import java.util.ArrayList;

import com.bj4.yhh.utilities.listmenu.ListMenuItem;
import com.bj4.yhh.utilities.weather.WeatherData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "utilities.db";

    private static final String TAG = "DatabaseHelper";

    private SQLiteDatabase mDb;

    private Context mContext;

    private static DatabaseHelper sInstance;

    public synchronized static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context);
        }
        return sInstance;
    }

    // weather +++
    public static final String WEATHER_TABLE = "weather";

    public static final String WEATHER_WOEID = "woeid";

    public static final String WEATHER_ORDER = "weather_order";

    private void createWeatherTable() {
        getDatabase().execSQL(
                "CREATE TABLE IF NOT EXISTS " + WEATHER_TABLE + " ( " + WEATHER_WOEID
                        + " INTEGER PRIMARY KEY, " + WEATHER_ORDER + " INTEGER)");
        if (SettingManager.getInstance(mContext).hasWeatherDataInit() == false) {
            addNewWoeid(20070568l);
            addNewWoeid(22695856l);
            addNewWoeid(12703515l);
            addNewWoeid(2347334l);
            addNewWoeid(20070569l);
            SettingManager.getInstance(mContext).setWeatherDataInit();
        }
    }

    public void addNewWoeid(long id) {
        ContentValues cv = new ContentValues();
        cv.put(WEATHER_WOEID, id);
        cv.put(WEATHER_ORDER, 9999);
        getDatabase().insert(WEATHER_TABLE, null, cv);
    }

    public ArrayList<WeatherData> getWeatherWoeid() {
        ArrayList<WeatherData> rtn = new ArrayList<WeatherData>();
        Cursor data = getDatabase().query(WEATHER_TABLE, null, null, null, null, null,
                WEATHER_ORDER);
        if (data != null) {
            try {
                while (data.moveToNext()) {
                    rtn.add(new WeatherData(data.getLong(data.getColumnIndex(WEATHER_WOEID)), data
                            .getInt(data.getColumnIndex(WEATHER_ORDER))));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }

    // weather ---
    // list menu +++
    public static final String LIST_MENU_TABLE = "list_menu";

    public static final String LIST_MENU_TIMES_COLUMN = "times";

    public static final String LIST_MENU_NAME_COLUMN = "name";

    public static final String LIST_MENU_ID_COLUMN = "_id";

    private void createListMenuTable() {
        getDatabase().execSQL(
                "CREATE TABLE IF NOT EXISTS " + LIST_MENU_TABLE + "(" + LIST_MENU_ID_COLUMN
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + LIST_MENU_TIMES_COLUMN
                        + " INTEGER, " + LIST_MENU_NAME_COLUMN + " TEXT)");
        SQLiteStatement state = getDatabase().compileStatement(
                "select count(*) from " + LIST_MENU_TABLE);
        if (state != null) {
            try {
                long count = state.simpleQueryForLong();
                String[] listMenu = mContext.getResources().getStringArray(R.array.list_menu_item);
                if (count == 0) {
                    for (String item : listMenu) {
                        ContentValues cv = new ContentValues();
                        cv.put(LIST_MENU_NAME_COLUMN, item);
                        cv.put(LIST_MENU_TIMES_COLUMN, 0);
                        getDatabase().insert(LIST_MENU_TABLE, null, cv);
                    }
                } else if (count != listMenu.length) {
                    Cursor data = getDatabase().query(LIST_MENU_TABLE, null, null, null, null,
                            null, null, null);
                    if (data != null) {
                        try {
                            ArrayList<String> goingToAdd = new ArrayList<String>();
                            while (data.moveToNext()) {
                                String txt = data.getString(data
                                        .getColumnIndex(LIST_MENU_NAME_COLUMN));
                                for (String item : listMenu) {
                                    if (txt.equals(item)) {
                                        goingToAdd.add(txt);
                                        break;
                                    }
                                }
                            }
                            for (String item : goingToAdd) {
                                ContentValues cv = new ContentValues();
                                cv.put(LIST_MENU_NAME_COLUMN, item);
                                cv.put(LIST_MENU_TIMES_COLUMN, 0);
                                getDatabase().insert(LIST_MENU_TABLE, null, cv);
                            }
                        } finally {
                            data.close();
                        }
                    }
                }
            } finally {
                state.close();
            }
        }

    }

    public void addListMenuCount(ListMenuItem item) {
        ContentValues cv = new ContentValues();
        cv.put(LIST_MENU_TIMES_COLUMN, item.mCount);
        getDatabase().update(LIST_MENU_TABLE, cv, LIST_MENU_ID_COLUMN + "=" + item.mId, null);
    }

    public ArrayList<ListMenuItem> getListMenu() {
        ArrayList<ListMenuItem> rtn = new ArrayList<ListMenuItem>();
        Cursor data = getDatabase().query(LIST_MENU_TABLE, null, null, null, null, null,
                LIST_MENU_TIMES_COLUMN + " desc");
        if (data != null) {
            try {
                while (data.moveToNext()) {
                    int id = data.getInt(data.getColumnIndex(LIST_MENU_ID_COLUMN));
                    String content = data.getString(data.getColumnIndex(LIST_MENU_NAME_COLUMN));
                    long count = data.getLong(data.getColumnIndex(LIST_MENU_TIMES_COLUMN));
                    rtn.add(new ListMenuItem(id, content, count));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }

    // list menu ---
    private SQLiteDatabase getDatabase() {
        if ((mDb == null) || (mDb != null && mDb.isOpen() == false)) {
            try {
                mDb = getWritableDatabase();
            } catch (SQLiteFullException e) {
                Log.w(TAG, "SQLiteFullException", e);
            } catch (SQLiteException e) {
                Log.w(TAG, "SQLiteException", e);
            } catch (Exception e) {
                Log.w(TAG, "Exception", e);
            }
        }
        return mDb;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getDatabase().execSQL("PRAGMA synchronous = 1");
            setWriteAheadLoggingEnabled(true);
        }
        createListMenuTable();
        createWeatherTable();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
