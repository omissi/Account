package tech.alomessi.vitra.widget;

import android.content.Context;
import android.content.SharedPreferences;
import tech.alomessi.vitra.DataRepository;

public class PrayerWidgetProvider extends BaseInfoWidgetProvider {
    private SharedPreferences p(Context c){return c.getSharedPreferences("vitra",Context.MODE_PRIVATE);}
    protected String eyebrow(Context c) { return "PRAYER · "+p(c).getString("city_name","صنعاء · Sana'a"); }
    protected String primary(Context c) { return p(c).getString("prayer_next","Prayer")+"  "+p(c).getString("prayer_time","—:—"); }
    protected String secondary(Context c) { return p(c).getLong("prayer_updated",0)==0?"Open Vitra to refresh · Aladhan":"Aladhan · cached for your selected city"; }
    @Override public void onUpdate(Context c,android.appwidget.AppWidgetManager m,int[] ids){super.onUpdate(c,m,ids);DataRepository.refresh(c,false);}
}
