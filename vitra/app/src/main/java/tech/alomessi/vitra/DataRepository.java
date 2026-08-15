package tech.alomessi.vitra;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tech.alomessi.vitra.widget.PrayerWidgetProvider;
import tech.alomessi.vitra.widget.WeatherWidgetProvider;

public final class DataRepository {
    private static final ExecutorService IO=Executors.newSingleThreadExecutor();
    private static volatile long lastRequest;
    public static final String[][] CITIES={
        {"صنعاء · Sana'a","15.3694","44.1910"},{"عدن · Aden","12.7855","45.0187"},
        {"تعز · Taiz","13.5795","44.0209"},{"إب · Ibb","13.9667","44.1833"},
        {"الرياض · Riyadh","24.7136","46.6753"},{"مكة · Makkah","21.3891","39.8579"},
        {"الدمام · Dammam","26.4207","50.0888"},{"الدوحة · Doha","25.2854","51.5310"},
        {"الكويت · Kuwait","29.3759","47.9774"}
    };

    private DataRepository(){}

    public static void refresh(Context context,boolean force){
        long now=System.currentTimeMillis();if(!force&&now-lastRequest<10*60*1000L)return;lastRequest=now;
        Context app=context.getApplicationContext();IO.execute(()->{try{fetchWeather(app);}catch(Exception ignored){}try{fetchPrayer(app);}catch(Exception ignored){}updateWidgets(app);});
    }

    private static void fetchWeather(Context c)throws Exception{
        SharedPreferences p=c.getSharedPreferences("vitra",Context.MODE_PRIVATE);double lat=Double.longBitsToDouble(p.getLong("city_lat",Double.doubleToLongBits(15.3694)));double lon=Double.longBitsToDouble(p.getLong("city_lon",Double.doubleToLongBits(44.1910)));
        String u="https://api.open-meteo.com/v1/forecast?latitude="+lat+"&longitude="+lon+"&current=temperature_2m,weather_code&timezone=auto";JSONObject current=new JSONObject(get(u)).getJSONObject("current");double temp=current.getDouble("temperature_2m");int code=current.getInt("weather_code");
        p.edit().putString("weather_temp",Math.round(temp)+"°").putString("weather_desc",weatherText(code)).putLong("weather_updated",System.currentTimeMillis()).apply();
    }

    private static void fetchPrayer(Context c)throws Exception{
        SharedPreferences p=c.getSharedPreferences("vitra",Context.MODE_PRIVATE);double lat=Double.longBitsToDouble(p.getLong("city_lat",Double.doubleToLongBits(15.3694)));double lon=Double.longBitsToDouble(p.getLong("city_lon",Double.doubleToLongBits(44.1910)));
        String u="https://api.aladhan.com/v1/timings?latitude="+lat+"&longitude="+lon+"&method=4";JSONObject t=new JSONObject(get(u)).getJSONObject("data").getJSONObject("timings");String[] names={"Fajr","Sunrise","Dhuhr","Asr","Maghrib","Isha"};String now=new SimpleDateFormat("HH:mm",Locale.US).format(new Date());String next=names[0],time=t.getString(names[0]).substring(0,5);for(String name:names){String candidate=t.getString(name).substring(0,5);if(candidate.compareTo(now)>0){next=name;time=candidate;break;}}
        SharedPreferences.Editor e=p.edit().putString("prayer_next",next).putString("prayer_time",time).putLong("prayer_updated",System.currentTimeMillis());for(String name:names)e.putString("prayer_"+name.toLowerCase(Locale.ROOT),t.getString(name).substring(0,5));e.apply();
    }

    private static String get(String address)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection();c.setConnectTimeout(9000);c.setReadTimeout(9000);c.setRequestProperty("User-Agent","Vitra/1.0 Android");c.setRequestProperty("Accept","application/json");try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream()))){StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null)b.append(line);return b.toString();}finally{c.disconnect();}}
    private static String weatherText(int code){if(code==0)return"Clear · صافي";if(code<=3)return"Partly cloudy · غائم جزئيًا";if(code<=48)return"Fog · ضباب";if(code<=67)return"Rain · أمطار";if(code<=77)return"Snow · ثلوج";if(code<=82)return"Showers · زخات";return"Thunderstorm · عاصفة";}
    private static void updateWidgets(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);int[] w=m.getAppWidgetIds(new ComponentName(c,WeatherWidgetProvider.class));if(w.length>0)new WeatherWidgetProvider().onUpdate(c,m,w);int[] p=m.getAppWidgetIds(new ComponentName(c,PrayerWidgetProvider.class));if(p.length>0)new PrayerWidgetProvider().onUpdate(c,m,p);}
}
