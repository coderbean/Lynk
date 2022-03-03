package cn.edu.fjnu.musicdemo;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Objects;

/**
 * Implementation of App Widget functionality.
 */
public class MusicAppWidget extends AppWidgetProvider {

    public static final String TAG = "MusicAppWidget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.d(TAG, "onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (Objects.equals(intent.getAction(), "cn.edu.fjnu.musicdemo.widget.REFRESH")) {
            Log.d(TAG, "onReceive");
            String getTrackName = intent.getStringExtra("getTrackName");
            if (getTrackName != null) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_app_widget);
                remoteViews.setTextViewText(R.id.appwidget_text, getTrackName);
                AppWidgetManager awm = AppWidgetManager.getInstance(context);
                awm.updateAppWidget(new ComponentName(context, MusicAppWidget.class), remoteViews);
            }
        }
    }
}