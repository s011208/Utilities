
package com.bj4.yhh.utilities.analytics;

public class Analytics {

    public static boolean ENABLE_TRACKER = false;

    public static class WidgetSize {
        public static final String EVENT = "widget size";

        public static final String SIZE_ONE_FOUR = "1 * 4";

        public static final String SIZE_TWO_FOUR = "2 * 4";
        
        public static final String FAILED_TO_GET_DATA = "failed to get data";
    }

    public static class TemptureUnit {
        public static final String EVENT = "tempture unit";

        public static final String FAHRENHEIT = "Fahrenheit";

        public static final String CELCIUS = "Celcius";
    }

    public static class ViewingFragment {
        public static final String EVENT = "change fragment";

        public static final String VIEWING_FRAGMENT = "viewing fragment";

        public static final String FRAGMENT_WEATHER = "weather";

        public static final String FRAGMENT_MUSIC = "music";

        public static final String FRAGMENT_SETTINGS = "settings";

        public static final String FRAGMENT_FLOATING_WINDOW_OPTION = "floating window";

        public static final String FRAGMENT_CALCULATOR = "calculator";
    }
}
