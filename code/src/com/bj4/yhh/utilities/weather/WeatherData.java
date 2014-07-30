
package com.bj4.yhh.utilities.weather;

public class WeatherData {
    public static class WeatherForecast {
        public String mDay, mHigh, mLow;

        public int mCode;

        public WeatherForecast(String day, String h, String l, int code) {
            mDay = day;
            mHigh = h;
            mLow = l;
            mCode = code;
        }
    }

    public String mCity, mCountry, mWind, mHumidity, mVisibility, mSunrise, mSunset, mCurrentTemp,
            mCurrentCondi;
    
    public int mCurrentCode;

    public WeatherForecast mF0, mF1, mF2, mF3, mF4;

    public WeatherData(String city, String country, String wind, String humidity,
            String visibility, String rise, String set, String currentTemp, String currentCondi, int currentCode,
            WeatherForecast f0, WeatherForecast f1, WeatherForecast f2, WeatherForecast f3,
            WeatherForecast f4) {
        mF0 = f0;
        mF1 = f1;
        mF2 = f2;
        mF3 = f3;
        mF4 = f4;
        mCity = city;
        mCurrentCondi = currentCondi;
        mCurrentCode = currentCode;
        mVisibility = visibility;
        mSunrise = rise;
        mSunset = set;
        mCurrentTemp = currentTemp;
        mCountry = country;
        mWind = wind;
        mHumidity = humidity;
    }
}
