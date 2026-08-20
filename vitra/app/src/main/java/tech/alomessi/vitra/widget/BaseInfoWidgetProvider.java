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
import tech.alomessi.vitra.WidgetData;

public abstract class BaseInfoWidgetProvider extends AppWidgetProvider {
    protected abstract String eyebrow(Context context);
    protected abstract String primary(Context context);
    protected abstract String secondary(Context context);

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id);
    }

    protected void update(Context context, AppWidgetManager manager, int id) {
        SharedPreferences prefs = context.getSharedPreferences("vitra", Context.MODE_PRIVATE);
        WidgetData data = WidgetData.find(prefs.getString("widget_style_" + id,
                prefs.getString("pending_widget_style", "digital-simple")));
        int accent = prefs.getInt("widget_accent_" + id, prefs.getInt("accent", 0xff998bff));
        int opacity = prefs.getInt("widget_opacity_" + id, prefs.getInt("opacity_int", 76));
        int darkness = prefs.getInt("widget_darkness_" + id, prefs.getInt("darkness_int", 72));
        int blur = prefs.getInt("widget_blur_" + id, prefs.getInt("blur_int", 18));
        int radius = prefs.getInt("widget_radius_" + id, prefs.getInt("corner_int", 26));
        String background = prefs.getString("widget_background_" + id, prefs.getString("last_background", "Clear"));
        boolean showDate = prefs.getBoolean("widget_show_date_" + id, prefs.getBoolean("last_show_date", true));
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_bitmap);
        views.setImageViewBitmap(R.id.widget_canvas, WidgetArtwork.render(context, data,
                prefs.getBoolean("widget_arabic_" + id, prefs.getBoolean("arabic", true)), accent, opacity, darkness, blur, radius,
                background, showDate));
        Intent launch = new Intent(context, MainActivity.class);
        launch.putExtra(MainActivity.EXTRA_OPEN_WIDGET, data.id);
        PendingIntent click = PendingIntent.getActivity(context, id, launch,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_canvas, click);
        manager.updateAppWidget(id, views);
    }

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int id, android.os.Bundle options) {
        update(context, manager, id);
    }
}
