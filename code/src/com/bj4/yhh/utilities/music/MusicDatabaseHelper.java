
package com.bj4.yhh.utilities.music;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.util.Log;

public class MusicDatabaseHelper extends SQLiteOpenHelper {

    public interface Callback {
        public void datasetChanged();
    }

    private ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "music.db";

    private static final String TAG = "MusicDatabaseHelper";

    private SQLiteDatabase mDb;

    private Context mContext;

    private static MusicDatabaseHelper sInstance;

    public synchronized static MusicDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MusicDatabaseHelper(context);
        }
        return sInstance;
    }

    public static final String MUSIC_DATA_TABLE = "music_data";

    public static final String MUSIC_DATA_TYPE = "music_type";

    public static final String MUSIC_DATA_ARTIST = "artist";

    public static final String MSUIC_DATA_MUSIC = "music";

    public static final String MSUIC_DATA_VIDEO_PATH = "video_path";

    public static final String MSUIC_DATA_RTSP_H = "rtsp_h";

    public static final String MSUIC_DATA_VIDEO_ID = "video_id";

    public static final String MUSIC_DATA_ID = "_id";

    public void addCallback(Callback cb) {
        if (mCallbacks.contains(cb) == false) {
            mCallbacks.add(cb);
        }
    }

    public void removeCallback(Callback cb) {
        mCallbacks.remove(cb);
    }

    private void createMusicDataTable() {
        getDatabase().execSQL(
                "CREATE TABLE IF NOT EXISTS " + MUSIC_DATA_TABLE + " ( " + MUSIC_DATA_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MUSIC_DATA_ARTIST + " TEXT, "
                        + MUSIC_DATA_TYPE + " TEXT, " + MSUIC_DATA_MUSIC + " TEXT, "
                        + MSUIC_DATA_VIDEO_PATH + " TEXT, " + MSUIC_DATA_RTSP_H + " TEXT, "
                        + MSUIC_DATA_VIDEO_ID + " TEXT)");
        SQLiteStatement state = getDatabase().compileStatement(
                "select count(*) from " + MUSIC_DATA_TABLE);
        if (state != null) {
            try {
                if (state.simpleQueryForLong() == 0) {
                    mContext.startService(new Intent(mContext, MusicParseService.class));
                }
            } finally {
                state.close();
            }
        }
    }

    public ArrayList<String> getMusicTypeList() {
        ArrayList<String> rtn = new ArrayList<String>();
        Cursor data = getDatabase().rawQuery(
                "select distinct " + MUSIC_DATA_TYPE + " from " + MUSIC_DATA_TABLE, null);
        if (data != null) {
            try {
                while (data.moveToNext()) {
                    rtn.add(data.getString(0));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }

    public ArrayList<MusicData> getMusicData() {
        ArrayList<MusicData> rtn = new ArrayList<MusicData>();
        Cursor data = getDatabase().query(MUSIC_DATA_TABLE, null, null, null, null, null, null);
        if (data != null) {
            try {
                int idIndex = data.getColumnIndex(MUSIC_DATA_ID);
                int artistIndex = data.getColumnIndex(MUSIC_DATA_ARTIST);
                int typeIndex = data.getColumnIndex(MUSIC_DATA_TYPE);
                int musicIndex = data.getColumnIndex(MSUIC_DATA_MUSIC);
                while (data.moveToNext()) {
                    rtn.add(new MusicData(data.getInt(idIndex), data.getString(typeIndex), data
                            .getString(artistIndex), data.getString(musicIndex)));
                }
            } finally {
                data.close();
            }
        }
        return rtn;
    }

    public void updateMusicData(ArrayList<MusicData> data) {
        Iterator<MusicData> iter = data.iterator();
        while (iter.hasNext()) {
            MusicData music = iter.next();
            if (music.mRTSP != null && music.mVideoId != null && music.mVideoPath != null) {
                ContentValues cv = new ContentValues();
                cv.put(MSUIC_DATA_RTSP_H, music.mRTSP);
                cv.put(MSUIC_DATA_VIDEO_ID, music.mVideoId);
                cv.put(MSUIC_DATA_VIDEO_PATH, music.mVideoPath);
                getDatabase().update(MUSIC_DATA_TABLE, cv, MUSIC_DATA_ID + "=" + music.mId, null);
            }
        }
        getDatabase().delete(MUSIC_DATA_TABLE, MSUIC_DATA_RTSP_H + " is null", null);
    }

    public synchronized void addMusicData(ArrayList<ContentValues> cvs,
            ArrayList<String> musicTypeList, boolean notify) {
        for (String musicType : musicTypeList) {
            getDatabase().delete(MUSIC_DATA_TABLE, MUSIC_DATA_TYPE + "='" + musicType + "'", null);
        }
        try {
            getDatabase().beginTransaction();
            for (ContentValues cv : cvs) {
                getDatabase().replaceOrThrow(MUSIC_DATA_TABLE, null, cv);
            }
            getDatabase().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getDatabase().endTransaction();
        }
        if (notify) {
            for (Callback cb : mCallbacks) {
                cb.datasetChanged();
            }
        }
    }

    private MusicDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getDatabase().execSQL("PRAGMA synchronous = 1");
            setWriteAheadLoggingEnabled(true);
        }
        createMusicDataTable();
    }

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

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
}
