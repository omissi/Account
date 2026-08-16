package tech.alomessi.vitra.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import tech.alomessi.vitra.MainActivity;
import tech.alomessi.vitra.R;

public class ClockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            SharedPreferences prefs=context.getSharedPreferences("vitra",Context.MODE_PRIVATE);
            String selected=prefs.getString("widget_style_"+id,prefs.getString("pending_widget_style","digital-simple"));
            boolean analog=selected.startsWith("clock-");
            RemoteViews views = new RemoteViews(context.getPackageName(), analog?R.layout.widget_bitmap:R.layout.widget_time);
            if(analog){int accent=prefs.getInt("widget_accent_"+id,prefs.getInt("accent",0xff9587ff));int style=0;for(int i=0;i<6;i++)if(selected.equals(new String[]{"clock-classic","clock-modern","clock-minimal","clock-fill","clock-orbit","clock-prism"}[i]))style=i;views.setImageViewBitmap(R.id.widget_canvas,WidgetArtwork.analog(accent,style));}
            else WidgetStyle.apply(context,views,id);
            PendingIntent open = PendingIntent.getActivity(context, id, new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(analog?R.id.widget_canvas:R.id.widget_primary, open);
            if(!analog)views.setOnClickPendingIntent(R.id.widget_secondary, open);
            manager.updateAppWidget(id, views);
        }
    }
}
