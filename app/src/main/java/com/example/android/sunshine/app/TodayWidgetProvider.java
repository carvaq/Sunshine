package com.example.android.sunshine.app;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Caro Vaquero
 * Date: 21.10.2016
 * Project: Sunshine
 */

public class TodayWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_UPDATE_DATA = "com.example.android.sunshine.app.TodayWidgetProvider.ACTION_UPDATE_DATA";
    private static final String TAG = TodayWidgetProvider.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE_DATA.equals(intent.getAction())) {
            context.startService(new Intent(context, TodayWidgetIntentService.class));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }
}
