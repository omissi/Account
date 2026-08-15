package tech.alomessi.vitra.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.provider.SearchRecentSuggestions;
import android.widget.RemoteViews;

import tech.alomessi.vitra.R;

public class SearchWidgetProvider extends BaseInfoWidgetProvider {
    protected String eyebrow(Context c) { return "VITRA SEARCH"; }
    protected String primary(Context c) { return "Search the web"; }
    protected String secondary(Context c) { return "Tap to search · private by design"; }

    @Override
    protected void update(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_info);
        views.setTextViewText(R.id.widget_eyebrow, eyebrow(context));
        views.setTextViewText(R.id.widget_primary, primary(context));
        views.setTextViewText(R.id.widget_secondary, secondary(context));
        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
        search.putExtra(android.app.SearchManager.QUERY, "");
        PendingIntent click = PendingIntent.getActivity(context, id, search,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_primary, click);
        views.setOnClickPendingIntent(R.id.widget_secondary, click);
        manager.updateAppWidget(id, views);
    }
}
