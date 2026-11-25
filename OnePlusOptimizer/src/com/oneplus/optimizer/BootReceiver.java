package com.oneplus.optimizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(
                DailyAlarmReceiver.PREF_NAME, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(DailyAlarmReceiver.PREF_DAILY_ENABLED, true);

        if (enabled) {
            DailyAlarmReceiver.scheduleNextAlarm(context);
        }
    }
}
