package com.oneplus.optimizer;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.Calendar;

public class DailyAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY = "com.oneplus.optimizer.ACTION_DAILY_OPT";
    public static final String PREF_NAME = "optimizer_prefs";
    public static final String PREF_DAILY_ENABLED = "pref_daily_autocare";
    private static final String CHANNEL_ID = "daily_autocare";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(PREF_DAILY_ENABLED, true);

        if (!enabled) return;

        // Schedule next day's alarm
        scheduleNextAlarm(context);

        final String tasksCsv = TaskConfig.consumeNextDailyTasksCsv(context);
        PendingResult pendingResult = goAsync();

        new Thread(() -> {
            try {
                if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                    ShizukuManager.getService(context, new ShizukuManager.Callback() {
                        @Override
                        public void onReady(IUserService service) {
                            try {
                                service.runSelectedOptimizations(tasksCsv);
                                postNotification(context, "Daily auto-care complete (" + TaskConfig.parseCsvSet(tasksCsv).size() + " selected tasks ran).");
                            } catch (Exception ignored) {}
                            pendingResult.finish();
                        }

                        @Override
                        public void onError(String message) {
                            pendingResult.finish();
                        }
                    });
                } else {
                    pendingResult.finish();
                }
            } catch (Exception e) {
                pendingResult.finish();
            }
        }).start();
    }

    public static void scheduleNextAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, DailyAlarmReceiver.class);
        intent.setAction(ACTION_DAILY);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int hour = prefs.getInt("pref_daily_hour", 3);
        int minute = prefs.getInt("pref_daily_minute", 0);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }
        } catch (Exception e) {
            try {
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            } catch (Exception ignored) {}
        }
    }

    public static void cancelAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, DailyAlarmReceiver.class);
        intent.setAction(ACTION_DAILY);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }

    private void postNotification(Context context, String message) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Auto-Care Maintenance", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Daily system health & maintenance alerts");
            nm.createNotificationChannel(ch);
        }

        android.app.Notification.Builder b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            b = new android.app.Notification.Builder(context, CHANNEL_ID);
        } else {
            b = new android.app.Notification.Builder(context);
        }

        b.setContentTitle("Android Revamp Hub")
         .setContentText(message)
         .setSmallIcon(R.drawable.ic_launcher)
         .setAutoCancel(true);

        nm.notify(2026, b.build());
    }
}
