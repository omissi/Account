package tech.alomessi.vitra.widget;

import android.content.Context;

public class PrayerWidgetProvider extends BaseInfoWidgetProvider {
    protected String eyebrow(Context c) { return "PRAYER · الصلاة"; }
    protected String primary(Context c) { return "المغرب  —:—"; }
    protected String secondary(Context c) { return "افتح Vitra لتحديد المدينة وطريقة الحساب"; }
}
