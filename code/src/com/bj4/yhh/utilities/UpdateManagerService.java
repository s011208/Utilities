
package com.bj4.yhh.utilities;

import com.bj4.yhh.utilities.weather.WeatherService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class UpdateManagerService extends Service implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG = "UpdateManagerService";

    public static final String UPDATE_TYPE = "update_type";

    public static final int UPDATE_NONE = -2;

    public static final int UPDATE_ALL = -1;

    public static final int UPDATE_TYPE_WEATHER = 0;

    public static final int UPDATE_TYPE_MUSIC = 1;

    private static final int HOUR = 1000 * 60 * 60;

    private int mUpdateWeatherInterval = 3 * HOUR;

    private Handler mHandler = new Handler();

    // location update
    private static final long DEFAULT_UPDATE_LOCATION_INTERVAL = 10 * 60 * 1000;

    private LocationClient mLocationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = new LocationClient(this, this, this);
        scheduleUpdate(UPDATE_ALL);
        mHandler.post(mLocationClientConnectionRunnable);
    }

    private Runnable mLocationClientConnectionRunnable = new Runnable() {

        @Override
        public void run() {
            if (mLocationClient != null && mLocationClient.isConnected() == false)
                mLocationClient.connect();
            mHandler.postDelayed(mLocationClientConnectionRunnable,
                    DEFAULT_UPDATE_LOCATION_INTERVAL);
        }
    };

    @Override
    public void onDestroy() {
        if (mLocationClient != null)
            mLocationClient.disconnect();
    }

    private void scheduleUpdate(final int type) {
        switch (type) {
            case UPDATE_TYPE_WEATHER:
                mHandler.post(mUpdateWeatherHandler);
                break;
            case UPDATE_TYPE_MUSIC:
                break;
            case UPDATE_ALL:
                mHandler.post(mUpdateWeatherHandler);
                break;
            case UPDATE_NONE:
                return;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                final int type = extras.getInt(UPDATE_TYPE);
                scheduleUpdate(type);
            }
        }
        return Service.START_STICKY;
    }

    private Runnable mUpdateWeatherHandler = new Runnable() {

        @Override
        public void run() {
            Log.d(TAG, "UpdateWeather");
            Intent intent = new Intent(UpdateManagerService.this, WeatherService.class);
            intent.putExtra(WeatherService.INTENT_UPDATE_ALL, true);
            UpdateManagerService.this.startService(intent);
            mHandler.removeCallbacks(mUpdateWeatherHandler);
            mHandler.postDelayed(mUpdateWeatherHandler, mUpdateWeatherInterval);
        }
    };

    private void updateCurrentLocationData(Location location) {
        if (location == null)
            return;
        float lat = (float)location.getLatitude();
        float lon = (float)location.getLongitude();
        Intent intent = new Intent(this, WeatherService.class);
        intent.putExtra(WeatherService.INTENT_KEY_LAT, lat);
        intent.putExtra(WeatherService.INTENT_KEY_LON, lon);
        intent.putExtra(WeatherService.INTENT_KEY_ADD_INTO_WOEID, false);
        startService(intent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        updateCurrentLocationData(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        updateCurrentLocationData(mLocationClient.getLastLocation());
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(DEFAULT_UPDATE_LOCATION_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationClient.requestLocationUpdates(locationRequest, this);
        mHandler.removeCallbacks(mLocationClientConnectionRunnable);
    }

    @Override
    public void onDisconnected() {
        if (mLocationClient != null && mLocationClient.isConnected())
            mLocationClient.removeLocationUpdates(this);
        mHandler.removeCallbacks(mLocationClientConnectionRunnable);
    }

}
