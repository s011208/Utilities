
package com.bj4.yhh.utilities.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bj4.yhh.utilities.DatabaseHelper;
import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.SettingManager;
import com.bj4.yhh.utilities.UtilitiesApplication;
import com.bj4.yhh.utilities.weather.Weather;
import com.bj4.yhh.utilities.weather.WeatherData;
import com.bj4.yhh.utilities.weather.WeatherService;
import com.bj4.yhh.utilities.weather.WeatherWoeId;

public class Utils {
    @SuppressWarnings("deprecation")
    public static String parseOnInternet(String url) {
        URL u;
        InputStream is = null;
        DataInputStream dis;
        String s;
        StringBuilder sb = new StringBuilder();
        try {
            u = new URL(url);
            is = u.openStream();
            dis = new DataInputStream(new BufferedInputStream(is));
            while ((s = dis.readLine()) != null) {
                sb.append(s);
            }
        } catch (Exception e) {
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ioe) {
            }
        }
        return sb.toString();
    }

    public static String readFromFile(String filePath) {
        String ret = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            ret = sb.toString();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean writeToFile(final String filePath, final String data) {
        Writer writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),
                    "utf-8"));
            writer.write(data);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static WeatherData parseWeatherData(Context context, long woeid) {
        WeatherData rtn = null;
        String city = null, country = null, sWind = null, humidity = null, visibility = null, rise = null, set = null, currentTemp = null, currentText = null;
        WeatherData.WeatherForecast f0 = null, f1 = null, f2 = null, f3 = null, f4 = null;
        int currentCode;
        final boolean usingC = SettingManager.getInstance(context).isUsingC();
        File file = new File(context.getFilesDir().getAbsolutePath() + File.separator
                + WeatherService.FOLDER_NAME + File.separator + woeid);
        if (file.exists()) {
            String data = Utils.readFromFile(file.getAbsolutePath());
            try {
                JSONObject channel = new JSONObject(data).getJSONObject("query")
                        .getJSONObject("results").getJSONObject("channel");
                JSONObject location = channel.getJSONObject("location");
                city = location.getString("city");
                country = location.getString("country");
                JSONObject wind = channel.getJSONObject("wind");
                sWind = wind.getString("speed");
                JSONObject atmosphere = channel.getJSONObject("atmosphere");
                humidity = atmosphere.getString("humidity");
                visibility = atmosphere.getString("visibility");
                JSONObject astronomy = channel.getJSONObject("astronomy");
                rise = astronomy.getString("sunrise");
                set = astronomy.getString("sunset");
                JSONObject condition = channel.getJSONObject("item").getJSONObject("condition");
                currentTemp = getCorrectTemp(usingC, condition.getString("temp"));
                currentText = condition.getString("text");
                currentCode = condition.getInt("code");
                JSONArray forecast = channel.getJSONObject("item").getJSONArray("forecast");
                for (int i = 0; i < forecast.length(); i++) {
                    JSONObject f = forecast.getJSONObject(i);
                    if (i == 0) {
                        f0 = new WeatherData.WeatherForecast(f.getString("day"), getCorrectTemp(
                                usingC, f.getString("high")), getCorrectTemp(usingC,
                                f.getString("low")), f.getInt("code"));
                    } else if (i == 1) {
                        f1 = new WeatherData.WeatherForecast(f.getString("day"), getCorrectTemp(
                                usingC, f.getString("high")), getCorrectTemp(usingC,
                                f.getString("low")), f.getInt("code"));
                    } else if (i == 2) {
                        f2 = new WeatherData.WeatherForecast(f.getString("day"), getCorrectTemp(
                                usingC, f.getString("high")), getCorrectTemp(usingC,
                                f.getString("low")), f.getInt("code"));
                    } else if (i == 3) {
                        f3 = new WeatherData.WeatherForecast(f.getString("day"), getCorrectTemp(
                                usingC, f.getString("high")), getCorrectTemp(usingC,
                                f.getString("low")), f.getInt("code"));
                    } else if (i == 4) {
                        f4 = new WeatherData.WeatherForecast(f.getString("day"), getCorrectTemp(
                                usingC, f.getString("high")), getCorrectTemp(usingC,
                                f.getString("low")), f.getInt("code"));
                    }
                }
                rtn = new WeatherData(city, country, sWind, humidity, visibility, rise, set,
                        currentTemp, currentText, currentCode, f0, f1, f2, f3, f4);
                UtilitiesApplication.sWeatherDataCache.put(woeid, rtn);
            } catch (JSONException e) {
            }
        } else {
            // request to parse
            Intent parse = new Intent(context, WeatherService.class);
            parse.putExtra(WeatherService.INTENT_KEY_WOEID, woeid);
            context.startService(parse);
        }
        return rtn;
    }

    public static void forcedReloadWeatherDataCache(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (UtilitiesApplication.sWeatherDataCache) {
                    UtilitiesApplication.sWeatherDataCache.evictAll();
                    ArrayList<WeatherWoeId> woeids = DatabaseHelper.getInstance(context)
                            .getWeatherWoeid();
                    for (WeatherWoeId woeid : woeids) {
                        parseWeatherData(context, woeid.mWoeid);
                    }
                }
                context.sendBroadcast(new Intent(Weather.INTENT_ON_DATA_UPDATE));
            }
        }).start();
    }

    private static String getCorrectTemp(boolean usingC, String temp) {
        if (usingC) {
            float tempF = Float.valueOf(temp);
            int tempC = (int)Math.round((tempF - 32) * (5 / 9f));
            return String.valueOf(tempC);
        } else {
            return temp;
        }
    }

    public static int getWeatherIcon(int weatherCode) {
        int rtn = 3200;
        switch (weatherCode) {
            case 0:
                rtn = R.drawable.w00;
                break;
            case 1:
                rtn = R.drawable.w01;
                break;
            case 2:
                rtn = R.drawable.w02;
                break;
            case 3:
                rtn = R.drawable.w03;
                break;
            case 4:
                rtn = R.drawable.w04;
                break;
            case 5:
                rtn = R.drawable.w05;
                break;
            case 6:
                rtn = R.drawable.w06;
                break;
            case 7:
                rtn = R.drawable.w07;
                break;
            case 8:
                rtn = R.drawable.w08;
                break;
            case 9:
                rtn = R.drawable.w09;
                break;
            case 10:
                rtn = R.drawable.w10;
                break;
            case 11:
                rtn = R.drawable.w11;
                break;
            case 12:
                rtn = R.drawable.w12;
                break;
            case 13:
                rtn = R.drawable.w13;
                break;
            case 14:
                rtn = R.drawable.w14;
                break;
            case 15:
                rtn = R.drawable.w15;
                break;
            case 16:
                rtn = R.drawable.w16;
                break;
            case 17:
                rtn = R.drawable.w17;
                break;
            case 18:
                rtn = R.drawable.w18;
                break;
            case 19:
                rtn = R.drawable.w19;
                break;
            case 20:
                rtn = R.drawable.w20;
                break;
            case 21:
                rtn = R.drawable.w21;
                break;
            case 22:
                rtn = R.drawable.w22;
                break;
            case 23:
                rtn = R.drawable.w23;
                break;
            case 24:
                rtn = R.drawable.w24;
                break;
            case 25:
                rtn = R.drawable.w25;
                break;
            case 26:
                rtn = R.drawable.w26;
                break;
            case 27:
                rtn = R.drawable.w27;
                break;
            case 28:
                rtn = R.drawable.w28;
                break;
            case 29:
                rtn = R.drawable.w29;
                break;
            case 30:
                rtn = R.drawable.w30;
                break;
            case 31:
                rtn = R.drawable.w31;
                break;
            case 32:
                rtn = R.drawable.w32;
                break;
            case 33:
                rtn = R.drawable.w33;
                break;
            case 34:
                rtn = R.drawable.w34;
                break;
            case 35:
                rtn = R.drawable.w35;
                break;
            case 36:
                rtn = R.drawable.w35;
                break;
            case 37:
                rtn = R.drawable.w37;
                break;
            case 38:
                rtn = R.drawable.w38;
                break;
            case 39:
                rtn = R.drawable.w39;
                break;
            case 40:
                rtn = R.drawable.w40;
                break;
            case 41:
                rtn = R.drawable.w41;
                break;
            case 42:
                rtn = R.drawable.w42;
                break;
            case 43:
                rtn = R.drawable.w43;
                break;
            case 44:
                rtn = R.drawable.w44;
                break;
            case 45:
                rtn = R.drawable.w45;
                break;
            case 46:
                rtn = R.drawable.w46;
                break;
            case 47:
                rtn = R.drawable.w47;
                break;
            default:
                rtn = R.drawable.wna;
                break;
        }
        return rtn;
    }
}
