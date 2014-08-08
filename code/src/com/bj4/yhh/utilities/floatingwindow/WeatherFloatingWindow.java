
package com.bj4.yhh.utilities.floatingwindow;

import com.bj4.yhh.utilities.weather.Weather;

import android.content.Context;
import android.util.AttributeSet;

public class WeatherFloatingWindow extends FloatingWindow {

    public WeatherFloatingWindow(Context context) {
        this(context, null);
    }

    public WeatherFloatingWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherFloatingWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void init() {
        setTitle("Weather");
        addFloatingContent(new Weather(mContext));
    }

    @Override
    public String getClassStringKey() {
        return WeatherFloatingWindow.class.toString();
    }
}
