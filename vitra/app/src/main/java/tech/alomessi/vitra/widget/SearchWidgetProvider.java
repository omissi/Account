package tech.alomessi.vitra.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import tech.alomessi.vitra.R;
import tech.alomessi.vitra.WidgetData;

/** Search, app-shortcut and people presets. A tap launches the system search hand-off. */
public class SearchWidgetProvider extends BaseInfoWidgetProvider {
    @Override protected String eyebrow(Context context) { return "VITRA SEARCH"; }
    @Override protected String primary(Context context) { return ""; }
    @Override protected String secondary(Context context) { return ""; }

    @Override protected void update(Context context, AppWidgetManager manager, int id) {
        SharedPreferences prefs=context.getSharedPreferences("vitra",Context.MODE_PRIVATE);
        WidgetData data=WidgetData.find(prefs.getString("widget_style_"+id,prefs.getString("pending_widget_style","search-vibrant")));
        int accent=prefs.getInt("widget_accent_"+id,prefs.getInt("accent",0xff998bff));
        RemoteViews views=new RemoteViews(context.getPackageName(), R.layout.widget_bitmap);
        views.setImageViewBitmap(R.id.widget_canvas,WidgetArtwork.render(context,data,prefs.getBoolean("widget_arabic_"+id,prefs.getBoolean("arabic",true)),accent,
                prefs.getInt("widget_opacity_"+id,prefs.getInt("opacity_int",76)),prefs.getInt("widget_darkness_"+id,prefs.getInt("darkness_int",72)),
                prefs.getInt("widget_blur_"+id,prefs.getInt("blur_int",18)),prefs.getInt("widget_radius_"+id,prefs.getInt("corner_int",26)),
                prefs.getString("widget_background_"+id,prefs.getString("last_background","Clear")),prefs.getBoolean("widget_show_date_"+id,prefs.getBoolean("last_show_date",true))));
        Intent search=new Intent(Intent.ACTION_WEB_SEARCH);
        search.putExtra(android.app.SearchManager.QUERY,"");
        PendingIntent click=PendingIntent.getActivity(context,id,search,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_canvas,click);
        manager.updateAppWidget(id,views);
    }
}
