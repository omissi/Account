package tech.alomessi.vitra.widget;

import android.content.Context;

/** Configurable provider for all analog and digital clock presets. */
public class ClockWidgetProvider extends BaseInfoWidgetProvider {
    @Override protected String eyebrow(Context context) { return "VITRA CLOCK"; }
    @Override protected String primary(Context context) { return ""; }
    @Override protected String secondary(Context context) { return ""; }
}
