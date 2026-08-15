package tech.alomessi.vitra.widget;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import tech.alomessi.vitra.DataRepository;

public class WeatherWidgetProvider extends BaseInfoWidgetProvider {
    private SharedPreferences p(Context c){return c.getSharedPreferences("vitra",Context.MODE_PRIVATE);}
    protected String eyebrow(Context c) { return "WEATHER · "+p(c).getString("city_name","صنعاء · Sana'a"); }
    protected String primary(Context c) { return p(c).getString("weather_temp","—°"); }
    protected String secondary(Context c) { long u=p(c).getLong("weather_updated",0);return p(c).getString("weather_desc","Open Vitra to refresh")+(u==0?" · Open-Meteo":" · "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(u))+" · Open-Meteo"); }
    @Override public void onUpdate(Context c,android.appwidget.AppWidgetManager m,int[] ids){super.onUpdate(c,m,ids);DataRepository.refresh(c,false);}
}
