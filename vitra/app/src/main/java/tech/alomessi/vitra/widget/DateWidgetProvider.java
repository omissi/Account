package tech.alomessi.vitra.widget;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateWidgetProvider extends BaseInfoWidgetProvider {
    protected String eyebrow(Context c) { return new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date()).toUpperCase(); }
    protected String primary(Context c) { return new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()); }
    protected String secondary(Context c) { return new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()); }
}
