
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bj4.yhh.utilities.UtilitiesApplication;
import com.bj4.yhh.utilities.weather.WeatherData;
import com.bj4.yhh.utilities.weather.WeatherService;

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
                currentTemp = condition.getString("temp");
                currentText = condition.getString("text");
                currentCode = condition.getInt("code");
                JSONArray forecast = channel.getJSONObject("item").getJSONArray("forecast");
                for (int i = 0; i < forecast.length(); i++) {
                    JSONObject f = forecast.getJSONObject(i);
                    if (i == 0) {
                        f0 = new WeatherData.WeatherForecast(f.getString("day"),
                                f.getString("high"), f.getString("low"), f.getInt("code"));
                    } else if (i == 1) {
                        f1 = new WeatherData.WeatherForecast(f.getString("day"),
                                f.getString("high"), f.getString("low"), f.getInt("code"));
                    } else if (i == 2) {
                        f2 = new WeatherData.WeatherForecast(f.getString("day"),
                                f.getString("high"), f.getString("low"), f.getInt("code"));
                    } else if (i == 3) {
                        f3 = new WeatherData.WeatherForecast(f.getString("day"),
                                f.getString("high"), f.getString("low"), f.getInt("code"));
                    } else if (i == 4) {
                        f4 = new WeatherData.WeatherForecast(f.getString("day"),
                                f.getString("high"), f.getString("low"), f.getInt("code"));
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
}
