
package com.bj4.yhh.utilities.weather;

import com.bj4.yhh.utilities.DatabaseHelper;
import com.bj4.yhh.utilities.UtilitiesApplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class LoadCitiesListService extends Service implements DatabaseHelper.ProgressCallback {
    private boolean mIsLoading = false;

    private int mLoadingProgess = 0;

    private ICitiesLoading.Stub mBinder = new ICitiesLoading.Stub() {

        @Override
        public boolean isLoading() throws RemoteException {
            return mIsLoading;
        }

        @Override
        public int getLoadingProgress() throws RemoteException {
            return mLoadingProgess;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper db = DatabaseHelper.getInstance(LoadCitiesListService.this);
                if (db.hasCitiesTableLoaded() == false) {
                    db.loadCitiesTable(LoadCitiesListService.this);
                }
                mIsLoading = false;
                UtilitiesApplication.sIsCitiesServiceLoading = false;
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void progress(int progress) {
        mIsLoading = true;
        mLoadingProgess = progress;
    }

}
