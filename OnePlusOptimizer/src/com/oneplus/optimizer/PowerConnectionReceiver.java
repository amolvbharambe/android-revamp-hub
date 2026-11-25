package com.oneplus.optimizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class PowerConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_POWER_CONNECTED.equals(action) || 
            Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_BATTERY_CHANGED.equals(action)) {

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                     status == BatteryManager.BATTERY_STATUS_FULL;
                if (!isCharging || level < 80) {
                    return; // Only enforce bypass charging when charging reaches >= 80%
                }
            }

            // Re-enforce hardcoded charging profile on charger plug-in, boot, and reaching >= 80%
            new Thread(() -> {
                try {
                    if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                        ShizukuManager.getService(context, new ShizukuManager.Callback() {
                            @Override
                            public void onReady(IUserService service) {
                                try {
                                    service.enforceChargingProfile();
                                } catch (Exception ignored) {}
                            }

                            @Override
                            public void onError(String message) {}
                        });
                    }
                } catch (Exception ignored) {}
            }).start();
        }
    }
}
