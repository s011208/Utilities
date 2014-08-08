
package com.bj4.yhh.utilities.floatingwindow;

import com.bj4.yhh.utilities.R;
import com.bj4.yhh.utilities.calculator.Calculator;
import com.bj4.yhh.utilities.weather.Weather;

import android.content.Context;
import android.util.AttributeSet;

public class CalculatorFloatingWindow extends FloatingWindow {

    public CalculatorFloatingWindow(Context context) {
        this(context, null);
    }

    public CalculatorFloatingWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalculatorFloatingWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void init() {
        setTitle(mContext.getString(R.string.item_calculator));
        addFloatingContent(new Calculator(mContext));
    }

    @Override
    public String getClassStringKey() {
        return CalculatorFloatingWindow.class.toString();
    }
}
