
package com.bj4.yhh.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.bj4.yhh.utilities.listmenu.ListMenuItem;
import com.bj4.yhh.utilities.weather.WeatherWoeId;

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

    // weather cities table +++
    private static final String TABLE_CITIES_LIST = "cities_list";

    private static final String TABLE_CITIES_LIST_ID = "city_id";

    private static final String TABLE_CITIES_LIST_LON = "city_lon";

    private static final String TABLE_CITIES_LIST_LAT = "city_lat";

    private static final String TABLE_CITIES_LIST_NAME = "city_name";

    private static final String TABLE_CITIES_LIST_NATION = "city_nation";

    public static final int TABLE_CITIES_LIST_NOT_FOUND_DATA = -5000;

    private void createCityListTable() {
        getDatabase().execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_CITIES_LIST + "(" + TABLE_CITIES_LIST_ID
                        + " INTEGER PRIMARY KEY, " + TABLE_CITIES_LIST_LAT + " TEXT, "
                        + TABLE_CITIES_LIST_LON + " TEXT, " + TABLE_CITIES_LIST_NAME + " TEXT, "
                        + TABLE_CITIES_LIST_NATION + " TEXT)");
    }

    public boolean hasCitiesTableLoaded() {
        boolean rtn = false;
        SQLiteStatement state = getDatabase().compileStatement(
                "select count(*) from " + TABLE_CITIES_LIST);
        if (state != null) {
            try {
                if (state.simpleQueryForLong() == 0) {
                    rtn = false;
                } else {
                    rtn = true;
                }
            } finally {
                state.close();
            }
        }
        return rtn;
    }

    public interface ProgressCallback {
        public void progress(int progress);
    }

    public void loadCitiesTable(ProgressCallback cb) {
        android.content.res.AssetManager am = mContext.getAssets();
        try {
            InputStream in = am.open("city_list.txt");
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] data = new byte[2048];
            int count = -1;
            if (cb != null) {
                cb.progress(1);
            }
            while ((count = in.read(data, 0, 2048)) != -1) {
                outStream.write(data, 0, count);
            }
            if (cb != null) {
                cb.progress(30);
            }
            data = null;
            String[] dataArray = new String(outStream.toByteArray(), "ISO-8859-1").split("\n");
            if (dataArray.length <= 0) {
                return;
            }
            getDatabase().beginTransaction();
            final float unit = 70 / (float)dataArray.length;
            int counter = 0;
            try {
                for (String raw : dataArray) {
                    ContentValues cv = new ContentValues();
                    String[] rawArray = raw.split("\t");
                    cv.put(TABLE_CITIES_LIST_ID, rawArray[0]);
                    cv.put(TABLE_CITIES_LIST_LAT, rawArray[2]);
                    cv.put(TABLE_CITIES_LIST_LON, rawArray[3]);
                    cv.put(TABLE_CITIES_LIST_NAME, rawArray[1]);
                    cv.put(TABLE_CITIES_LIST_NATION, rawArray[4]);
                    getDatabase().insertOrThrow(TABLE_CITIES_LIST, null, cv);
                    if (cb != null) {
                        cb.progress((int)(30 + (++counter) * unit));
                    }
                }
                getDatabase().setTransactionSuccessful();
            } finally {
                getDatabase().endTransaction();
                if (cb != null) {
                    cb.progress(100);
                }
                System.gc();
            }
        } catch (IOException e) {
            Log.e(TAG, "failed", e);
        }
    }

    public ArrayList<String> getAllCitiesName() {
        ArrayList<String> rtn = new ArrayList<String>();
        Cursor data = getDatabase().query(TABLE_CITIES_LIST, new String[] {
                TABLE_CITIES_LIST_NAME, TABLE_CITIES_LIST_NATION
        }, null, null, null, null, TABLE_CITIES_LIST_NATION, null);
        if (data != null) {
            while (data.moveToNext()) {
                String name = data.getString(0);
                String nation = data.getString(1);
                rtn.add(name + ", " + nation);
            }
            data.close();
        }
        return rtn;
    }

    public float[] getCityInfo(String cityAndNation) {
        String[] raw = cityAndNation.split(", ");
        float[] rtn = new float[] {
                TABLE_CITIES_LIST_NOT_FOUND_DATA, TABLE_CITIES_LIST_NOT_FOUND_DATA
        };
        if (raw.length == 2) {
            Cursor data = getDatabase().query(
                    TABLE_CITIES_LIST,
                    new String[] {
                            TABLE_CITIES_LIST_LAT, TABLE_CITIES_LIST_LON
                    },
                    TABLE_CITIES_LIST_NAME + "='" + raw[0] + "' and " + TABLE_CITIES_LIST_NATION
                            + "='" + raw[1] + "'", null, null, null, null, null);

            if (data != null) {
                while (data.moveToNext()) {
                    rtn[0] = data.getFloat(0);
                    rtn[1] = data.getFloat(1);
                }
                data.close();
            }
        }
        return rtn;
    }

    // weather cities table ---
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

    public void removeWoeid(long id) {
        getDatabase().delete(WEATHER_TABLE, WEATHER_WOEID + "=" + id, null);
    }

    public ArrayList<WeatherWoeId> getWeatherWoeid() {
        ArrayList<WeatherWoeId> rtn = new ArrayList<WeatherWoeId>();
        Cursor data = getDatabase().query(WEATHER_TABLE, null, null, null, null, null,
                WEATHER_ORDER);
        if (data != null) {
            try {
                while (data.moveToNext()) {
                    rtn.add(new WeatherWoeId(data.getLong(data.getColumnIndex(WEATHER_WOEID)), data
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
        createCityListTable();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
