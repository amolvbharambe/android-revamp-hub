package com.oneplus.optimizer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {

    public static final String EXTRA_BOOT_AUTO_OPTIMIZE = "extra_boot_auto_optimize";
    private static final String CHANNEL_ID = "boot_auto_optimize";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(
                DailyAlarmReceiver.PREF_NAME, Context.MODE_PRIVATE);
        boolean dailyEnabled = prefs.getBoolean(DailyAlarmReceiver.PREF_DAILY_ENABLED, true);
        if (dailyEnabled) {
            DailyAlarmReceiver.scheduleNextAlarm(context);
        }

        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                && TaskConfig.isBootAutoOptimizeEnabled(context)
                && TaskConfig.claimBootLaunchOnce(context)) {
            launchAppAfterBoot(context);
        }
    }

    private void launchAppAfterBoot(Context context) {
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activityIntent.putExtra(EXTRA_BOOT_AUTO_OPTIMIZE, true);

        boolean canOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
        try {
            if (canOverlay) {
                context.startActivity(activityIntent);
            }
        } catch (Throwable ignored) {}

        // Always post a fallback actionable notification so reboot optimization runs even if keyguard delayed startActivity
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Reboot Optimization Launch", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Launches Android Revamp Hub after phone reboot to run default optimizations");
            nm.createNotificationChannel(ch);
        }

        PendingIntent pi = PendingIntent.getActivity(
                context, 3099, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new android.app.Notification.Builder(context, CHANNEL_ID)
                : new android.app.Notification.Builder(context);

        b.setContentTitle("Phone Rebooted — Master Optimization Ready")
         .setContentText("Tap to run your Default Master Optimization tasks.")
         .setSmallIcon(R.drawable.ic_launcher)
         .setContentIntent(pi)
         .setAutoCancel(true);

        if (!canOverlay) {
            b.setFullScreenIntent(pi, false);
        }

        try {
            nm.notify(3099, b.build());
        } catch (Throwable ignored) {}
    }
}
