package tech.alomessi.vitra.widget;

import android.content.Context;

public class WeatherWidgetProvider extends BaseInfoWidgetProvider {
    protected String eyebrow(Context c) { return "WEATHER · OFFLINE"; }
    protected String primary(Context c) { return "—°"; }
    protected String secondary(Context c) { return "Open Vitra to set your location"; }
}
