package tech.alomessi.vitra.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import tech.alomessi.vitra.MainActivity;
import tech.alomessi.vitra.R;

public abstract class BaseInfoWidgetProvider extends AppWidgetProvider {
    protected abstract String eyebrow(Context context);
    protected abstract String primary(Context context);
    protected abstract String secondary(Context context);

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id);
    }

    protected void update(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_info);
        views.setTextViewText(R.id.widget_eyebrow, eyebrow(context));
        views.setTextViewText(R.id.widget_primary, primary(context));
        views.setTextViewText(R.id.widget_secondary, secondary(context));
        WidgetStyle.apply(context,views,id);
        Intent launch = new Intent(context, MainActivity.class);
        PendingIntent click = PendingIntent.getActivity(context, id, launch,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_primary, click);
        views.setOnClickPendingIntent(R.id.widget_secondary, click);
        views.setOnClickPendingIntent(R.id.widget_eyebrow, click);
        manager.updateAppWidget(id, views);
    }
}
