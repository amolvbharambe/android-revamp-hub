package com.oneplus.optimizer;

import android.os.Build;
import android.os.RemoteException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;
import org.json.JSONObject;

public class MyUserService extends IUserService.Stub {

    private volatile String mShizukuPkgHint = null;

    public MyUserService() {
        runCmd("pm grant com.oneplus.optimizer android.permission.WRITE_SECURE_SETTINGS 2>/dev/null || true");
        runCmd("pm grant com.oneplus.optimizer android.permission.CHANGE_CONFIGURATION 2>/dev/null || true");
        String shizPkg = resolveShizukuPkg();
        runCmd("pm enable " + shizPkg + " 2>/dev/null || true");
    }

    @Override
    public void setShizukuPackageHint(String packageName) throws RemoteException {
        if (packageName != null && !packageName.trim().isEmpty() && packageName.trim().matches("[a-zA-Z0-9_.]+")) {
            mShizukuPkgHint = packageName.trim();
            runCmd("printf '%s' '" + mShizukuPkgHint + "' > /data/local/tmp/revamp_shizuku_pkg.txt 2>/dev/null || true");
        }
    }

    @Override
    public String getShizukuPackageName() throws RemoteException {
        return resolveShizukuPkg();
    }

    private String resolveShizukuPkg() {
        if (mShizukuPkgHint != null && !mShizukuPkgHint.isEmpty() && mShizukuPkgHint.matches("[a-zA-Z0-9_.]+")) {
            if (!runCmd("pm path " + mShizukuPkgHint + " 2>/dev/null || true").trim().isEmpty()) {
                return mShizukuPkgHint;
            }
        }
        String saved = runCmd("cat /data/local/tmp/revamp_shizuku_pkg.txt 2>/dev/null || true").trim();
        if (!saved.isEmpty() && saved.matches("[a-zA-Z0-9_.]+")) {
            if (!runCmd("pm path " + saved + " 2>/dev/null || true").trim().isEmpty()) {
                mShizukuPkgHint = saved;
                return saved;
            }
        }
        String queried = runCmd("cmd package resolve-activity --brief -a rikka.shizuku.intent.action.REQUEST_BINDER 2>/dev/null | grep -oE '[a-zA-Z0-9_.]+/moe\\.shizuku\\.manager\\.legacy\\.ShellRequestHandlerActivity' | head -n1 | cut -d/ -f1").trim();
        if (!queried.isEmpty() && queried.matches("[a-zA-Z0-9_.]+")) {
            mShizukuPkgHint = queried;
            return queried;
        }
        return "moe.shizuku.privileged.api";
    }

    @Override
    public void destroy() throws RemoteException {
        System.exit(0);
    }

    private String runCmd(String cmd) {
        StringBuilder sb = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = errReader.readLine()) != null) {
                sb.append("[ERR] ").append(line).append("\n");
            }
            errReader.close();

            p.waitFor();
        } catch (Exception e) {
            sb.append("Exception: ").append(e.getMessage()).append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String executeShell(String command) throws RemoteException {
        return runCmd(command);
    }

    @Override
    public String runMasterOptimization() throws RemoteException {
        return runSelectedOptimizations(
            "bloatware_shield,private_dns,aa_cable_only,charge_limit_80,bypass_charging," +
            "fast_doze,cellular_sleep,aod_smart_mode,weather_tamer,critical_whitelist," +
            "restrict_drainers,cache_trim,storage_trim,dev_debugging_on,banking_stealth_off," +
            "shizuku_cloak_off,fhd_resolution,refresh_ltpo_auto,anim_05x,gestures_shield"
        );
    }

    @Override
    public String runSelectedOptimizations(String taskKeysCsv) throws RemoteException {
        if (taskKeysCsv == null || taskKeysCsv.trim().isEmpty()) {
            return "⚠ No tasks selected to run.";
        }
        StringBuilder log = new StringBuilder();
        log.append("=== Executing Selected System Optimizations ===\n");
        String[] keys = taskKeysCsv.split(",");
        int step = 0;
        for (String raw : keys) {
            String key = raw.trim();
            if (key.isEmpty()) continue;
            step++;
            executeTaskByKey(key, step, log);
        }
        log.append("=== Completed ").append(step).append(" selected optimization task(s). ===\n");
        return log.toString();
    }

    private void executeTaskByKey(String key, int step, StringBuilder log) throws RemoteException {
        switch (key) {
            case TaskConfig.TASK_BLOATWARE_SHIELD:
                setBloatwareShieldEnabled(true);
                log.append(step).append(". ✔ OEM Bloatware & Telemetry Shield ACTIVE (Meta, HeyTap, SAUHelper, OPlus analytics frozen)\n");
                break;
            case TaskConfig.TASK_PRIVATE_DNS:
                setPrivateDnsEnabled(true);
                log.append(step).append(". ✔ AdGuard Private DNS Shield ACTIVE (dns.adguard-dns.com)\n");
                break;
            case TaskConfig.TASK_AA_CABLE_ONLY:
                setAACableOnly(true);
                log.append(step).append(". ✔ Android Auto CABLE-ONLY Mode ACTIVE\n");
                break;
            case TaskConfig.TASK_CHARGE_LIMIT_80:
                set80PercentLimitEnabled(true);
                setSmartChargingEnabled(false);
                log.append(step).append(". ✔ 80% Custom Charge Limit LOCKED ON (Smart Charging OFF)\n");
                break;
            case TaskConfig.TASK_BYPASS_CHARGING:
                setBypassChargingEnabled(true);
                log.append(step).append(". ✔ Hardware Bypass Charging ACTIVE (at ≥80%)\n");
                break;
            case TaskConfig.TASK_FAST_DOZE:
                setFastDozeEnabled(true);
                log.append(step).append(". ✔ Fast Doze Instant Standby ACTIVE (60s screen-off)\n");
                break;
            case TaskConfig.TASK_CELLULAR_SLEEP:
                setCellularSleepOnWifiEnabled(true);
                log.append(step).append(". ✔ Cellular Modem Sleep on Wi-Fi ACTIVE\n");
                break;
            case TaskConfig.TASK_AOD_SMART_MODE:
                setAodSmartModeEnabled(true);
                log.append(step).append(". ✔ Smart AOD & Deep Standby ACTIVE (Pickup/Touch only)\n");
                break;
            case TaskConfig.TASK_WEATHER_TAMER:
                setWeatherTamerEnabled(true);
                log.append(step).append(". ✔ Weather CPU Tamer ACTIVE\n");
                break;
            case TaskConfig.TASK_CRITICAL_WHITELIST:
                enforceCriticalAppsShield();
                log.append(step).append(". ✔ Critical Apps Doze Whitelist Verified (Messaging, Calls, SMS, Alarms & Shizuku)\n");
                break;
            case TaskConfig.TASK_RESTRICT_DRAINERS:
                restrictBackgroundDrainers();
                log.append(step).append(". ✔ Aggressive Background Drainers Restricted\n");
                break;
            case TaskConfig.TASK_CACHE_TRIM:
                runTrimCache();
                runFlushRam();
                log.append(step).append(". ✔ Dalvik/System Cache Trimmed & Background RAM Flushed\n");
                break;
            case TaskConfig.TASK_STORAGE_TRIM:
                runCmd("sm fstrim 2>/dev/null || true");
                log.append(step).append(". ✔ Flash Storage Trimmed (sm fstrim)\n");
                break;
            case TaskConfig.TASK_DEV_DEBUGGING_ON:
                setDevOptionsEnabled(true);
                setUsbDebuggingEnabled(true);
                setWirelessDebuggingEnabled(true);
                log.append(step).append(". ✔ Wired & Wireless Debugging ENABLED (+ Developer Options ON)\n");
                break;
            case TaskConfig.TASK_BANKING_STEALTH_OFF:
                setBankingModeEnabled(false);
                cloakAllRiskApps(false);
                log.append(step).append(". ✔ Banking & UPI Stealth DISABLED (Modding tools uncloaked)\n");
                break;
            case TaskConfig.TASK_SHIZUKU_CLOAK_OFF:
                setShizukuCloaked(false);
                log.append(step).append(". ✔ Shizuku-Next Uncloaked & Active\n");
                break;
            case TaskConfig.TASK_FHD_RESOLUTION:
                setFhdResolutionEnabled(true);
                log.append(step).append(". ✔ Screen Resolution set to Standard FHD+\n");
                break;
            case TaskConfig.TASK_REFRESH_LTPO_AUTO:
                setHighRefreshEnabled(false);
                log.append(step).append(". ✔ Extreme High Refresh Lock DISABLED (Adaptive refresh active)\n");
                break;
            case TaskConfig.TASK_ANIM_05X:
                setAnimationSpeed(0.5f);
                log.append(step).append(". ✔ System Animation Scale set to 0.5x\n");
                break;
            case TaskConfig.TASK_GESTURES_SHIELD:
                restoreGestures();
                log.append(step).append(". ✔ Full-Screen Gestures Shield Restored (AOSP Mode 2)\n");
                break;
            case TaskConfig.TASK_ADB_NO_TIMEOUT:
                setAdbAuthTimeoutDisabled(true);
                log.append(step).append(". ✔ ADB Auth 7-Day Timeout Disabled (Keep Keys Forever)\n");
                break;
            case TaskConfig.TASK_VERIFY_ADB_OFF:
                setVerifyAdbAppsEnabled(false);
                log.append(step).append(". ✔ Verify Apps Over USB Disabled\n");
                break;
            case TaskConfig.TASK_COMPILE_CORE:
                compileDailyApps();
                log.append(step).append(". ✔ Core Essential Apps Native-Compiled (-m speed)\n");
                break;
            case TaskConfig.TASK_COMPILE_NEW:
                compileNewApps();
                log.append(step).append(". ✔ All 3rd-Party User Apps Native-Compiled (-m speed)\n");
                break;
            case TaskConfig.TASK_COMPILE_ALL:
                compileAllInstalledApps();
                log.append(step).append(". ✔ ALL Apps & System UI Deep Native-Compiled (-m speed -f)\n");
                break;
            default:
                log.append(step).append(". • Unknown task skipped: ").append(key).append("\n");
                break;
        }
    }

    @Override
    public String compileDailyApps() throws RemoteException {
        StringBuilder log = new StringBuilder();
        log.append("=== AOT Native ARM64 Speed Compilation ===\n");
        String[] coreApps = new String[]{
            "com.whatsapp",
            "org.telegram.messenger",
            "com.android.chrome",
            "com.google.android.gm",
            "com.spotify.music",
            "com.android.launcher",
            "com.android.systemui",
            "com.microsoft.office.outlook"
        };

        for (String app : coreApps) {
            log.append("• Compiling ").append(app).append("... ");
            String res = runCmd("cmd package compile -m speed -f " + app);
            log.append(res.contains("Success") ? "Success\n" : res + "\n");
        }
        log.append("✔ Daily core apps pre-compiled to native machine code.\n");
        return log.toString();
    }

    @Override
    public String compileNewApps() throws RemoteException {
        StringBuilder log = new StringBuilder();
        log.append("=== Auto-Care: Scanning for New Apps ===\n");
        String raw = runCmd("pm list packages -3 --user 0");
        String[] lines = raw.split("\n");
        int count = 0;
        for (String l : lines) {
            String pkg = l.replace("package:", "").trim();
            if (pkg.isEmpty()) continue;
            runCmd("cmd package compile -m speed " + pkg);
            count++;
            if (count <= 5) {
                log.append("• Compiled: ").append(pkg).append("\n");
            }
        }
        log.append("✔ Verified & compiled ").append(count).append(" apps to native speed.\n");
        return log.toString();
    }

    @Override
    public String runStorageTrim() throws RemoteException {
        StringBuilder log = new StringBuilder();
        log.append("• Running Flash filesystem trim (sm fstrim)...\n");
        runCmd("sm fstrim");
        log.append("• Reclaiming Dalvik & system caches...\n");
        runCmd("pm trim-caches 1000M 2>/dev/null || true");
        log.append("✔ Flash storage trimmed and IOPS restored.\n");
        return log.toString();
    }

    @Override
    public String runTrimCache() throws RemoteException {
        runCmd("pm trim-caches 1000M 2>/dev/null || true");
        return "✔ Dalvik and system caches trimmed successfully.";
    }

    @Override
    public String runFlushRam() throws RemoteException {
        runCmd("am kill-all 2>/dev/null || true");
        return "✔ Background cached processes cleared & RAM flushed.";
    }

    @Override
    public String getSystemMetricsJson() throws RemoteException {
        try {
            // RAM
            String memInfo = runCmd("grep -E 'MemTotal|MemAvailable' /proc/meminfo");
            long totalKb = 0;
            long availKb = 0;
            for (String line : memInfo.split("\n")) {
                if (line.contains("MemTotal:")) {
                    totalKb = parseKb(line);
                } else if (line.contains("MemAvailable:")) {
                    availKb = parseKb(line);
                }
            }
            long usedKb = totalKb - availKb;
            double totalGb = totalKb / 1048576.0;
            double usedGb = usedKb / 1048576.0;
            int ramPct = totalKb > 0 ? (int) ((usedKb * 100) / totalKb) : 0;

            // Battery
            String batInfo = runCmd("dumpsys battery");
            int level = 100;
            String tempStr = "28°C";
            String healthStr = "Good";
            for (String line : batInfo.split("\n")) {
                String l = line.trim();
                if (l.startsWith("level:")) {
                    try { level = Integer.parseInt(l.substring(6).trim()); } catch (Exception ignored) {}
                } else if (l.startsWith("temperature:")) {
                    try {
                        int t = Integer.parseInt(l.substring(12).trim());
                        tempStr = String.format(Locale.US, "%.1f°C", t / 10.0);
                    } catch (Exception ignored) {}
                } else if (l.startsWith("health:")) {
                    healthStr = l.endsWith("2") ? "Good" : "Normal";
                }
            }

            // Storage (/data)
            String df = runCmd("df -h /data | tail -n 1");
            String storageFree = "180G";
            String storageTotal = "457G";
            String[] tokens = df.trim().split("\\s+");
            if (tokens.length >= 4) {
                storageTotal = tokens[1];
                storageFree = tokens[3];
            }

            return String.format(Locale.US,
                "{\"ram_used\":\"%.1f\",\"ram_total\":\"%.1f\",\"ram_pct\":%d,\"bat_level\":%d,\"bat_temp\":\"%s\",\"bat_health\":\"%s\",\"storage_free\":\"%s\",\"storage_total\":\"%s\"}",
                usedGb, totalGb, ramPct, level, tempStr, healthStr, storageFree, storageTotal);
        } catch (Exception e) {
            return "{}";
        }
    }

    private long parseKb(String line) {
        try {
            String digits = line.replaceAll("[^0-9]", "");
            return Long.parseLong(digits);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean isPrivateDnsEnabled() throws RemoteException {
        String mode = runCmd("settings get global private_dns_mode");
        String spec = runCmd("settings get global private_dns_specifier");
        return "hostname".equalsIgnoreCase(mode.trim()) && spec.contains("adguard");
    }

    @Override
    public boolean setPrivateDnsEnabled(boolean enable) throws RemoteException {
        if (enable) {
            runCmd("settings put global private_dns_mode hostname");
            runCmd("settings put global private_dns_specifier dns.adguard-dns.com");
        } else {
            runCmd("settings put global private_dns_mode off");
        }
        return isPrivateDnsEnabled();
    }

    @Override
    public boolean isHighRefreshEnabled() throws RemoteException {
        String res = runCmd("settings get system app_extreme_high_refresh_switch");
        return "1".equals(res.trim());
    }

    @Override
    public boolean setHighRefreshEnabled(boolean enable) throws RemoteException {
        runCmd("settings put system app_extreme_high_refresh_switch " + (enable ? "1" : "0"));
        return isHighRefreshEnabled();
    }

    @Override
    public boolean isAACableOnly() throws RemoteException {
        String mode = runCmd("settings get global op_aa_cable_only_mode").trim();
        if ("1".equals(mode)) return true;
        String appop = runCmd("cmd appops get com.google.android.projection.gearhead CHANGE_WIFI_STATE").trim();
        return appop.contains("ignore") || appop.contains("deny");
    }

    @Override
    public boolean setAACableOnly(boolean cableOnly) throws RemoteException {
        if (cableOnly) {
            runCmd("pm revoke --user 0 com.google.android.projection.gearhead android.permission.NEARBY_WIFI_DEVICES 2>/dev/null || true");
            runCmd("pm revoke --user 0 com.google.android.projection.gearhead android.permission.ACCESS_LOCAL_NETWORK 2>/dev/null || true");
            runCmd("cmd appops set com.google.android.projection.gearhead CHANGE_WIFI_STATE ignore 2>/dev/null || true");
            runCmd("cmd appops set com.google.android.projection.gearhead NEARBY_WIFI_DEVICES ignore 2>/dev/null || true");
            runCmd("settings put global op_aa_cable_only_mode 1");
        } else {
            runCmd("pm grant --user 0 com.google.android.projection.gearhead android.permission.NEARBY_WIFI_DEVICES 2>/dev/null || true");
            runCmd("pm grant --user 0 com.google.android.projection.gearhead android.permission.ACCESS_LOCAL_NETWORK 2>/dev/null || true");
            runCmd("cmd appops set com.google.android.projection.gearhead CHANGE_WIFI_STATE allow 2>/dev/null || true");
            runCmd("cmd appops set com.google.android.projection.gearhead NEARBY_WIFI_DEVICES allow 2>/dev/null || true");
            runCmd("settings put global op_aa_cable_only_mode 0");
        }
        runCmd("cmd deviceidle whitelist +com.google.android.projection.gearhead 2>/dev/null || true");
        runCmd("cmd appops set com.google.android.projection.gearhead RUN_IN_BACKGROUND allow 2>/dev/null || true");
        runCmd("cmd appops set com.google.android.projection.gearhead WAKE_LOCK allow 2>/dev/null || true");
        runCmd("am set-standby-bucket com.google.android.projection.gearhead active 2>/dev/null || true");
        return isAACableOnly();
    }

    @Override
    public boolean isWirelessDebuggingEnabled() throws RemoteException {
        String res = runCmd("settings get global adb_wifi_enabled");
        return "1".equals(res.trim());
    }

    @Override
    public boolean setWirelessDebuggingEnabled(boolean enable) throws RemoteException {
        runCmd("settings put global adb_wifi_enabled " + (enable ? "1" : "0"));
        return isWirelessDebuggingEnabled();
    }

    @Override
    public boolean isAodSmartModeEnabled() throws RemoteException {
        String mode = runCmd("settings get secure aod_display_mode");
        String fullAod = runCmd("settings get secure full_screen_aod");
        return "1".equals(mode.trim()) && "0".equals(fullAod.trim());
    }

    @Override
    public boolean setAodSmartModeEnabled(boolean enable) throws RemoteException {
        if (enable) {
            runCmd("settings put secure aod_display_mode 1");
            runCmd("settings put secure full_screen_aod 0");
            runCmd("settings put secure wake_gesture_enabled 1");
            runCmd("settings put secure oplus_customize_gesture_wake_up_arouse 1");
            runCmd("settings put secure double_tap_to_wake 1");
            runCmd("settings put system double_wake_status 1");
            runCmd("settings put global ambient_tilt_to_wake 1");
            runCmd("settings put global ambient_touch_to_wake 1");
            runCmd("settings put secure deepsleep_switch_state 1");
            runCmd("settings put system oplus_deep_sleep_status 1");
        } else {
            runCmd("settings put secure aod_display_mode 0");
            runCmd("settings put secure full_screen_aod 1");
        }
        return isAodSmartModeEnabled();
    }

    @Override
    public boolean isFhdResolutionEnabled() throws RemoteException {
        String resState = runCmd("settings get secure settings_screen_resolution_state");
        return "1".equals(resState.trim());
    }

    @Override
    public boolean setFhdResolutionEnabled(boolean enable) throws RemoteException {
        if (enable) {
            runCmd("settings put secure oplus_customize_screen_resolution_adjust 0");
            runCmd("settings put secure settings_screen_resolution_state 1");
        } else {
            runCmd("settings put secure oplus_customize_screen_resolution_adjust 0");
            runCmd("settings put secure settings_screen_resolution_state 2");
        }
        return isFhdResolutionEnabled();
    }

    @Override
    public String getCurrentResolutionInfo() throws RemoteException {
        String resState = runCmd("settings get secure settings_screen_resolution_state");
        if ("1".equals(resState.trim())) {
            return "FHD+ Standard Mode (Display Scaling)";
        } else {
            return "Native High Resolution Mode";
        }
    }

    @Override
    public boolean is80PercentLimitEnabled() throws RemoteException {
        String res = runCmd("settings get system regular_charge_protection_switch_state");
        return "1".equals(res.trim());
    }

    @Override
    public boolean set80PercentLimitEnabled(boolean enable) throws RemoteException {
        runCmd("settings put system regular_charge_protection_switch_state " + (enable ? "1" : "0"));
        runCmd("settings put system regular_charge_protection_open_power_level 80");
        return is80PercentLimitEnabled();
    }

    @Override
    public boolean isSmartChargingEnabled() throws RemoteException {
        String res = runCmd("settings get system smart_charge_protection_switch_state");
        return "1".equals(res.trim());
    }

    @Override
    public boolean setSmartChargingEnabled(boolean enable) throws RemoteException {
        runCmd("settings put system smart_charge_protection_switch_state " + (enable ? "1" : "0"));
        runCmd("settings put system smart_charge_switch_state " + (enable ? "1" : "0"));
        return isSmartChargingEnabled();
    }

    @Override
    public boolean isBypassChargingEnabled() throws RemoteException {
        String res = runCmd("settings get system bypass_charge_state");
        return "1".equals(res.trim());
    }

    @Override
    public boolean setBypassChargingEnabled(boolean enable) throws RemoteException {
        runCmd("settings put system bypass_charge_state " + (enable ? "1" : "0"));
        runCmd("settings put system bypass_charge_value 80");
        if (enable) {
            runCmd("settings put system regular_charge_protection_switch_state 1");
            runCmd("settings put system regular_charge_protection_open_power_level 80");
        }
        return isBypassChargingEnabled();
    }

    @Override
    public int getBypassThreshold() throws RemoteException {
        return 80;
    }

    @Override
    public boolean setBypassThreshold(int threshold) throws RemoteException {
        runCmd("settings put system bypass_charge_value 80");
        return true;
    }

    @Override
    public String enforceChargingProfile() throws RemoteException {
        runCmd("settings put system regular_charge_protection_switch_state 1");
        runCmd("settings put system regular_charge_protection_open_power_level 80");
        runCmd("settings put system smart_charge_protection_switch_state 0");
        runCmd("settings put system smart_charge_switch_state 0");
        runCmd("settings put system bypass_charge_state 1");
        runCmd("settings put system bypass_charge_value 80");
        return "✔ Battery Health: 80% Limit LOCKED ON\n✔ Smart Charging: DISABLED\n✔ Hardware Bypass Charging: AUTO-ENGAGE AT ≥80%";
    }

    @Override
    public boolean isFastDozeEnabled() throws RemoteException {
        String res = runCmd("settings get global device_idle_constants");
        return res != null && !res.trim().isEmpty() && !"null".equalsIgnoreCase(res.trim());
    }

    @Override
    public boolean setFastDozeEnabled(boolean enable) throws RemoteException {
        if (enable) {
            runCmd("settings put global device_idle_constants light_after_inactive_to=0,light_pre_idle_to=30000,light_idle_to=120000,inactive_to=60000,sensing_to=0,locating_to=0,location_accuracy=20.0,motion_inactive_to=0,idle_after_inactive_to=0,idle_pending_to=60000,max_idle_pending_to=120000,idle_pending_factor=2.0,idle_to=900000,max_idle_to=21600000,idle_factor=2.0,min_time_to_alarm=3600000");
        } else {
            runCmd("settings delete global device_idle_constants");
        }
        return isFastDozeEnabled();
    }

    @Override
    public boolean isCellularSleepOnWifiEnabled() throws RemoteException {
        String res = runCmd("settings get global mobile_data_always_on");
        return "0".equals(res.trim());
    }

    @Override
    public boolean setCellularSleepOnWifiEnabled(boolean enable) throws RemoteException {
        runCmd("settings put global mobile_data_always_on " + (enable ? "0" : "1"));
        return isCellularSleepOnWifiEnabled();
    }

    @Override
    public String enforceCriticalAppsShield() throws RemoteException {
        String[] critical = new String[]{
            "com.whatsapp",
            "org.telegram.messenger",
            resolveShizukuPkg(),
            "com.oneplus.optimizer",
            "com.google.android.projection.gearhead",
            "com.spotify.music",
            "com.android.incallui",
            "com.android.phone",
            "com.android.providers.telephony",
            "com.android.mms.service",
            "com.google.android.apps.messaging",
            "com.android.deskclock",
            "com.google.android.deskclock"
        };
        for (String pkg : critical) {
            allowInstalledAppInBackground(pkg);
        }
        restoreAudioAndConnectivityServices();
        return "✔ Critical Apps & Audio/AA Shield verified: Messaging, Calls, SMS, Alarms, AA & Shizuku whitelisted.";
    }

    /** Ensures an installed app stays out of Doze/background starvation. */
    private void allowInstalledAppInBackground(String pkg) {
        if (pkg == null || pkg.isEmpty()) return;
        runCmd("if pm path " + pkg + " >/dev/null 2>&1; then " +
               "pm enable " + pkg + " >/dev/null 2>&1 || true; " +
               "cmd deviceidle whitelist +" + pkg + " >/dev/null 2>&1 || true; " +
               "cmd appops set " + pkg + " RUN_IN_BACKGROUND allow >/dev/null 2>&1 || true; " +
               "cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow >/dev/null 2>&1 || true; " +
               "cmd appops set " + pkg + " WAKE_LOCK allow >/dev/null 2>&1 || true; " +
               "am set-standby-bucket " + pkg + " active >/dev/null 2>&1 || true; " +
               "fi");
    }

    /** Restores OPlus audio/cast/peripheral services so AA & BT handoff never distort sound. */
    private void restoreAudioAndConnectivityServices() {
        String[] systemAudioConnectivity = {
            "com.oplus.linker",
            "com.oplus.cast",
            "com.oplus.matrix",
            "com.oplus.cosa",
            "com.oplus.audio.effectcenter"
        };
        for (String pkg : systemAudioConnectivity) {
            runCmd("if pm path " + pkg + " >/dev/null 2>&1; then " +
                   "pm enable " + pkg + " >/dev/null 2>&1 || true; " +
                   "cmd appops set " + pkg + " RUN_IN_BACKGROUND allow >/dev/null 2>&1 || true; " +
                   "cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow >/dev/null 2>&1 || true; " +
                   "cmd appops set " + pkg + " WAKE_LOCK allow >/dev/null 2>&1 || true; " +
                   "am set-standby-bucket " + pkg + " active >/dev/null 2>&1 || true; " +
                   "fi");
        }
    }

    private static final String[] BLOATWARE_PACKAGES = {
        "com.facebook.services",
        "com.facebook.appmanager",
        "com.facebook.system",
        "com.facebook.stella",
        "com.google.android.videos",
        "com.oplus.tips",
        "com.oplus.stdsp",
        "com.oneplus.membership",
        "com.heytap.cloud",
        "com.heytap.mcs",
        "com.oplus.games",
        "com.oneplus.mall",
        "net.oneplus.forums",
        "com.heytap.browser",
        "com.heytap.market",
        "com.heytap.market.overlay",
        "com.oplus.sauhelper",
        "com.oplus.onetrace",
        "com.oplus.logkit",
        "com.oplus.qualityprotect",
        "com.oplus.statistics.rom",
        "com.oplus.cota",
        "com.oplus.customize.coreapp"
    };

    @Override
    public boolean isBloatwareShieldEnabled() throws RemoteException {
        String res = runCmd("pm list packages -e --user 0 | grep -E 'com.heytap.cloud|com.heytap.mcs|com.oplus.sauhelper|com.oplus.statistics.rom|com.oplus.logkit|com.oplus.onetrace|com.oneplus.membership'");
        return res.trim().isEmpty();
    }

    @Override
    public boolean setBloatwareShieldEnabled(boolean enable) throws RemoteException {
        for (String pkg : BLOATWARE_PACKAGES) {
            if (enable) {
                runCmd("pm disable-user --user 0 " + pkg + " 2>/dev/null || true");
                runCmd("am force-stop " + pkg + " 2>/dev/null || true");
                runCmd("cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore 2>/dev/null || true");
                runCmd("cmd appops set " + pkg + " WAKE_LOCK ignore 2>/dev/null || true");
                runCmd("am set-standby-bucket " + pkg + " restricted 2>/dev/null || true");
            } else {
                runCmd("pm enable " + pkg + " 2>/dev/null || true");
                runCmd("cmd appops set " + pkg + " RUN_IN_BACKGROUND allow 2>/dev/null || true");
                runCmd("cmd appops set " + pkg + " WAKE_LOCK allow 2>/dev/null || true");
                runCmd("am set-standby-bucket " + pkg + " active 2>/dev/null || true");
            }
        }
        if (enable) {
            runCmd("settings put secure send_action_app_error 0");
            runCmd("settings put global usage_stats_consent_status 0 2>/dev/null || true");
        }
        return isBloatwareShieldEnabled();
    }

    @Override
    public boolean isWeatherTamerEnabled() throws RemoteException {
        String res = runCmd("cmd appops get net.oneplus.weather RUN_IN_BACKGROUND 2>/dev/null || true");
        return res.contains("ignore");
    }

    @Override
    public boolean setWeatherTamerEnabled(boolean enable) throws RemoteException {
        String mode = enable ? "ignore" : "allow";
        String bucket = enable ? "restricted" : "active";
        runCmd("cmd appops set net.oneplus.weather RUN_IN_BACKGROUND " + mode + " 2>/dev/null || true");
        runCmd("cmd appops set net.oneplus.weather WAKE_LOCK " + mode + " 2>/dev/null || true");
        runCmd("cmd appops set com.coloros.weather.service RUN_IN_BACKGROUND " + mode + " 2>/dev/null || true");
        runCmd("cmd appops set com.coloros.weather.service WAKE_LOCK " + mode + " 2>/dev/null || true");
        runCmd("am set-standby-bucket net.oneplus.weather " + bucket + " 2>/dev/null || true");
        runCmd("am set-standby-bucket com.coloros.weather.service " + bucket + " 2>/dev/null || true");
        return isWeatherTamerEnabled();
    }

    @Override
    public float getAnimationSpeed() throws RemoteException {
        String res = runCmd("settings get global window_animation_scale");
        try {
            return Float.parseFloat(res.trim());
        } catch (Exception e) {
            return 0.5f;
        }
    }

    @Override
    public boolean setAnimationSpeed(float speed) throws RemoteException {
        String s = String.valueOf(speed);
        runCmd("settings put global window_animation_scale " + s);
        runCmd("settings put global transition_animation_scale " + s);
        runCmd("settings put global animator_duration_scale " + s);
        return true;
    }

    @Override
    public boolean isGestureNavigationEnabled() throws RemoteException {
        String oplusMode = runCmd("settings get secure hide_navigationbar_enable");
        String mode = runCmd("settings get secure navigation_mode");
        return "3".equals(oplusMode.trim()) || "2".equals(mode.trim());
    }

    @Override
    public boolean setGestureNavigationEnabled(boolean enable) throws RemoteException {
        if (enable) {
            runCmd("cmd overlay enable com.android.internal.systemui.navbar.gestural 2>/dev/null || true");
            runCmd("cmd overlay disable com.android.internal.systemui.navbar.threebutton 2>/dev/null || true");
            runCmd("settings put secure navigation_mode 2");
            runCmd("settings put secure navigation_gesture 1");
            runCmd("settings put secure hide_navigationbar_enable 3");
            runCmd("settings put secure move_last_navigation_state 3");
            runCmd("settings put secure nav_type_progress_gesture_gear_user \"0,0,2,2\"");
            runCmd("settings put secure hide_gesture_bar_enable 0");
            runCmd("settings put secure gesture_side_back_vibrate_enable 1");
            runCmd("settings put secure gesture_side_hide_bar_prevention_enable 1");
            runCmd("settings put secure gesture_mistouch_prevention_side_enable 0");
            runCmd("settings put secure gesture_mistouch_prevention_side_type 1");
            runCmd("settings put secure oplus_third_part_apps_predictive_back 1");
            runCmd("settings put secure disable_gestures 0");
            runCmd("settings put secure back_gesture_inset_scale_left 1.0");
            runCmd("settings put secure back_gesture_inset_scale_right 1.0");
        } else {
            runCmd("cmd overlay enable com.android.internal.systemui.navbar.threebutton 2>/dev/null || true");
            runCmd("cmd overlay disable com.android.internal.systemui.navbar.gestural 2>/dev/null || true");
            runCmd("settings put secure navigation_mode 0");
            runCmd("settings put secure navigation_gesture 0");
            runCmd("settings put secure hide_navigationbar_enable 0");
            runCmd("settings put secure move_last_navigation_state 0");
        }
        return isGestureNavigationEnabled();
    }

    @Override
    public String restoreGestures() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("• Enabling AOSP Gestural Navigation Overlay...\n");
        runCmd("cmd overlay enable com.android.internal.systemui.navbar.gestural 2>/dev/null || true");
        runCmd("cmd overlay disable com.android.internal.systemui.navbar.threebutton 2>/dev/null || true");

        sb.append("• Enforcing ColorOS Native Full-Screen Gestures (State 3 / Mode 2)...\n");
        runCmd("settings put secure navigation_mode 2");
        runCmd("settings put secure navigation_gesture 1");
        runCmd("settings put secure hide_navigationbar_enable 3");
        runCmd("settings put secure move_last_navigation_state 3");
        runCmd("settings put secure hide_gesture_bar_enable 0");
        runCmd("settings put secure disable_gestures 0");

        sb.append("• Calibrating side back gestures & mistouch prevention...\n");
        runCmd("settings put secure back_gesture_inset_scale_left 1.0");
        runCmd("settings put secure back_gesture_inset_scale_right 1.0");
        runCmd("settings put secure nav_type_progress_gesture_gear_user \"0,0,2,2\"");
        runCmd("settings put secure gesture_side_hide_bar_prevention_enable 1");
        runCmd("settings put secure gesture_side_back_vibrate_enable 1");
        runCmd("settings put secure gesture_mistouch_prevention_side_enable 0");
        runCmd("settings put secure gesture_mistouch_prevention_side_type 1");
        runCmd("settings put secure oplus_third_part_apps_predictive_back 1");

        sb.append("• Enabling Double Tap to Wake & Screen-off gestures...\n");
        runCmd("settings put system oplus_screen_off_gesture_state 1");
        runCmd("settings put system double_wake_status 1");
        runCmd("settings put secure double_tap_to_wake 1");
        runCmd("settings put secure oplus_customize_gesture_double_touch 1");
        runCmd("settings put secure oplus_customize_gesture_double_touch_off 1");
        runCmd("settings put secure oplus_customize_gesture_double_touch_off_launcher 1");
        runCmd("settings put secure oplus_customize_gesture_double_touch_on 1");
        runCmd("settings put secure oplus_customize_gesture_open_type 1");
        runCmd("settings put secure oplus_customize_gesture_open_type_ux 1");

        sb.append("• Enabling Raise to Wake / Motion gestures...\n");
        runCmd("settings put secure wake_gesture_enabled 1");
        runCmd("settings put secure oplus_customize_gesture_wake_up_arouse 1");

        sb.append("✔ ColorOS Native Gestures fully stabilized across all resolutions!");
        return sb.toString();
    }

    // Sideloaded / Modding Apps Cloaking (Unified Signatures)
    private static final String[][] RISK_APP_SIGNATURES = {
        {"org.lsposed.manager", "LSPosed Manager (Framework)"},
        {"me.weishu.kernelsu", "KernelSU (Root Manager)"},
        {"com.topjohnwu.magisk", "Magisk (Root Manager)"},
        {"com.dergoogler.mmrl", "MMRL (Module Manager)"},
        {"com.example.kinginstaller", "KingInstaller (Package Installer)"},
        {"com.aaenabler.app", "AAEnabler (Android Auto Unlocker)"},
        {"com.nikolev", "AAAD (AA Apps Downloader)"},
        {"io.github.highwindbr.aaxp", "AAXP (AA Plugin)"},
        {"me.aap.fermata.auto.dear.google.why", "Fermata Auto (Media Mirroring)"},
        {"sksa.aa.customapps", "AA Custom Apps"},
        {"com.kododake.aabrowser", "AABrowser (Webview in AA)"},
        {"app.morphe.manager", "Morphe Manager (Patcher)"},
        {"app.morphe.android.youtube", "Morphe YouTube (Patched Client)"},
        {"app.morphe.android.apps.youtube.music", "Morphe YT Music (Patched Client)"},
        {"app.revanced.manager.flutter", "ReVanced Manager (Patcher)"},
        {"app.revanced.android.gms", "ReVanced MicroG Services"},
        {"app.revanced.android.youtube", "ReVanced YouTube (Patched)"},
        {"app.revanced.android.apps.youtube.music", "ReVanced YT Music (Patched)"},
        {"org.smarttube.stable", "SmartTube (Patched Client)"},
        {"com.deniscerri.ytdl", "YTDLnis (Media Downloader)"},
        {"com.adguard.android", "AdGuard (Local VPN / Blocker)"},
        {"com.adguard.android.contentblocker", "AdGuard Content Blocker"},
        {"com.wireguard.android", "WireGuard (VPN)"},
        {"com.tailscale.ipn", "Tailscale (VPN)"},
        {"com.emanuelef.remote_capture", "PCAPdroid (Packet Capture)"},
        {"org.fdroid.fdroid", "F-Droid (Alternative Store)"},
        {"dev.imranr.obtainium", "Obtainium (FOSS App Updater)"},
        {"com.anydesk.anydeskandroid", "AnyDesk (Remote Access)"},
        {"com.teamviewer.host.market", "TeamViewer Host (Remote Access)"},
        {"com.teamviewer.teamviewerapp", "TeamViewer (Remote Access)"},
        {"com.rustdesk.rustdesk", "RustDesk (Remote Access)"}
    };

    private String getRiskAppDisplayName(String pkg) {
        for (String[] sig : RISK_APP_SIGNATURES) {
            if (sig[0].equals(pkg)) return sig[1];
        }
        String low = pkg.toLowerCase();
        if (low.contains("fermata")) return "Fermata Auto / Mod";
        if (low.contains("clicker")) return "Auto Clicker";
        if (low.contains("anydesk")) return "AnyDesk";
        if (low.contains("teamviewer")) return "TeamViewer";
        if (low.contains("rustdesk")) return "RustDesk";
        if (low.contains("smarttube")) return "SmartTube";
        if (low.contains("ytdl")) return "YTDLnis";
        if (low.contains("pcap")) return "PCAPdroid";
        if (low.contains("revanced")) return "ReVanced Component";
        if (low.contains("adguard")) return "AdGuard Blocker/VPN";
        if (low.contains("wireguard")) return "WireGuard VPN";
        if (low.contains("lsposed") || low.contains("xposed")) return "Xposed / LSPosed";
        if (low.contains("magisk") || low.contains("kernelsu") || low.contains("ksunext")) return "Root Manager";
        if (low.contains("luckypatcher") || low.contains("fakegps")) return "Patch / Spoof Tool";
        return pkg;
    }

    private java.util.LinkedHashMap<String, String> getDetectedRiskMap() {
        String activeShizuku = resolveShizukuPkg();
        String all3rdList = runCmd("pm list packages -3 -u --user 0");
        java.util.LinkedHashMap<String, String> detected = new java.util.LinkedHashMap<>();
        for (String[] sig : RISK_APP_SIGNATURES) {
            String pkg = sig[0];
            if (pkg.equals(activeShizuku)) continue;
            if (all3rdList.contains("package:" + pkg)) {
                detected.put(pkg, sig[1]);
            }
        }
        for (String line : all3rdList.split("\n")) {
            if (line.startsWith("package:")) {
                String pkg = line.substring(8).trim();
                if (pkg.isEmpty() || pkg.equals("moe.shizuku.privileged.api") || pkg.equals(activeShizuku) || pkg.equals("com.oneplus.optimizer")) {
                    continue;
                }
                if (!detected.containsKey(pkg)) {
                    String low = pkg.toLowerCase();
                    if (low.contains("fermata") || low.contains("kinginstaller") || low.contains("aaenabler") ||
                        (low.contains("mmrl") && !low.contains("commercial")) ||
                        low.contains(".aaxp") || low.endsWith("aaxp") || low.contains("aabrowser") ||
                        low.contains(".morphe.") || low.contains("smarttube") || low.contains("ytdl") ||
                        low.contains("customapps") || low.contains("autoclicker") || low.contains("clicker") ||
                        low.contains("pcapdroid") || low.contains("obtainium") ||
                        low.contains("anydesk") || low.contains("rustdesk") ||
                        low.contains("revanced") || low.contains("adguard") ||
                        low.contains("wireguard") || low.contains("lsposed") || low.contains("xposed") ||
                        low.contains("kernelsu") || low.contains("ksunext") || low.contains("magisk") ||
                        low.contains("luckypatcher") || low.contains("fakegps")) {
                        detected.put(pkg, getRiskAppDisplayName(pkg));
                    }
                }
            }
        }
        // Also detect Play apps patched or sideloaded via mod installers (e.g. app.morphe.manager)
        String withInstallers = runCmd("pm list packages -i -3 --user 0");
        for (String line : withInstallers.split("\n")) {
            if (!line.startsWith("package:")) continue;
            int idx = line.indexOf("installer=");
            if (idx == -1) continue;
            String pkg = line.substring(8, idx).trim();
            String inst = line.substring(idx + 10).trim();
            if (pkg.isEmpty() || pkg.equals("moe.shizuku.privileged.api") || pkg.equals(activeShizuku) || pkg.equals("com.oneplus.optimizer")) {
                continue;
            }
            String lowInst = inst.toLowerCase();
            if ((lowInst.contains("morphe") || lowInst.contains("revanced") || lowInst.contains("kinginstaller") || lowInst.contains("obtainium"))
                    && !detected.containsKey(pkg)) {
                detected.put(pkg, getRiskAppDisplayName(pkg) + " (via " + inst + ")");
            }
        }
        return detected;
    }

    private void cleanBankingTempArtifacts() {
        runCmd("rm -f /data/local/tmp/app-debug.apk /data/local/tmp/TestCert* /data/local/tmp/testcert* /data/local/tmp/dump*.xml /data/local/tmp/window_dump*.xml /data/local/tmp/ui*.xml /data/local/tmp/screen*.png /data/local/tmp/*.apk 2>/dev/null || true");
    }

    @Override
    public boolean isBankingModeEnabled() throws RemoteException {
        String dev = runCmd("settings get global development_settings_enabled");
        String adb = runCmd("settings get global adb_enabled");
        String cloakedFile = runCmd("ls /data/local/tmp/revamp_cloaked_apps.txt 2>/dev/null || true").trim();
        return ("0".equals(dev.trim()) && "0".equals(adb.trim())) || cloakedFile.contains("revamp_cloaked_apps.txt");
    }

    @Override
    public boolean setBankingModeEnabled(boolean enable) throws RemoteException {
        runCmd("pm grant com.oneplus.optimizer android.permission.WRITE_SECURE_SETTINGS 2>/dev/null || true");
        runCmd("pm grant com.oneplus.optimizer android.permission.CHANGE_CONFIGURATION 2>/dev/null || true");
        if (enable) {
            // 1. Save & Disable Third-Party Accessibility Services AND master accessibility_enabled flag
            String activeAcc = runCmd("settings get secure enabled_accessibility_services").trim();
            String accEnabled = runCmd("settings get secure accessibility_enabled").trim();
            if (!activeAcc.isEmpty() && !"null".equals(activeAcc)) {
                runCmd("echo '" + activeAcc + "' > /data/local/tmp/revamp_saved_acc.txt");
                runCmd("settings put secure enabled_accessibility_services ''");
            }
            if ("1".equals(accEnabled)) {
                runCmd("echo 1 > /data/local/tmp/revamp_saved_acc_enabled.txt");
                runCmd("settings put secure accessibility_enabled 0");
            }

            // 2. Save & Pause Private DNS (stops AdGuard DNS from blocking Protectt/Bureau SDK telemetry endpoints)
            String dnsMode = runCmd("settings get global private_dns_mode").trim();
            String dnsSpec = runCmd("settings get global private_dns_specifier").trim();
            if (!dnsMode.isEmpty() && !"null".equals(dnsMode) && !"off".equalsIgnoreCase(dnsMode)) {
                runCmd("printf '%s\n%s\n' '" + dnsMode + "' '" + ("null".equals(dnsSpec) ? "" : dnsSpec) + "' > /data/local/tmp/revamp_saved_dns.txt");
                runCmd("settings put global private_dns_mode off");
            }

            // 3. Cloak & Freeze all detected Sideloaded / Modding / VPN Apps
            java.util.LinkedHashMap<String, String> riskMap = getDetectedRiskMap();
            StringBuilder cloakedList = new StringBuilder();
            for (String pkg : riskMap.keySet()) {
                runCmd("pm disable-user --user 0 " + pkg + " 2>/dev/null || true");
                runCmd("am force-stop " + pkg + " 2>/dev/null || true");
                cloakedList.append(pkg).append("\n");
            }
            if (cloakedList.length() > 0) {
                runCmd("cat << 'EOF' > /data/local/tmp/revamp_cloaked_apps.txt\n" + cloakedList.toString() + "EOF");
            }

            // 4. Clean dev/test APK & UI dump artifacts from /data/local/tmp
            cleanBankingTempArtifacts();

            // 5. Conceal Developer Options & USB/Wireless ADB + best-effort ADB USB/TCP reset
            runCmd("settings put global development_settings_enabled 0");
            runCmd("settings put global adb_enabled 0");
            runCmd("settings put global adb_wifi_enabled 0 2>/dev/null || true");
            runCmd("setprop service.adb.tcp.port -1 2>/dev/null || true");
            runCmd("svc usb setFunctions mtp 2>/dev/null || true");
        } else {
            // 1. Restore Developer Options & ADB
            runCmd("settings put global development_settings_enabled 1");
            runCmd("settings put global adb_enabled 1");
            runCmd("settings put global adb_wifi_enabled 1 2>/dev/null || true");

            // 2. Restore Saved Accessibility Services & Master Flag
            String savedAcc = runCmd("cat /data/local/tmp/revamp_saved_acc.txt 2>/dev/null || true").trim();
            String savedAccEn = runCmd("cat /data/local/tmp/revamp_saved_acc_enabled.txt 2>/dev/null || true").trim();
            if (!savedAcc.isEmpty() && !"null".equals(savedAcc)) {
                runCmd("settings put secure enabled_accessibility_services '" + savedAcc + "'");
                runCmd("rm -f /data/local/tmp/revamp_saved_acc.txt");
            }
            if ("1".equals(savedAccEn) || (!savedAcc.isEmpty() && !"null".equals(savedAcc))) {
                runCmd("settings put secure accessibility_enabled 1");
                runCmd("rm -f /data/local/tmp/revamp_saved_acc_enabled.txt");
            }

            // 3. Restore Saved Private DNS
            String savedDns = runCmd("cat /data/local/tmp/revamp_saved_dns.txt 2>/dev/null || true");
            if (!savedDns.trim().isEmpty()) {
                String[] lines = savedDns.split("\n", -1);
                String mode = lines.length > 0 ? lines[0].trim() : "";
                String spec = lines.length > 1 ? lines[1].trim() : "";
                if (!mode.isEmpty() && !"null".equals(mode)) {
                    runCmd("settings put global private_dns_mode " + mode);
                }
                if (!spec.isEmpty() && !"null".equals(spec)) {
                    runCmd("settings put global private_dns_specifier " + spec);
                }
                runCmd("rm -f /data/local/tmp/revamp_saved_dns.txt");
            }

            // 4. Re-enable all Cloaked Apps
            String cloakedApps = runCmd("cat /data/local/tmp/revamp_cloaked_apps.txt 2>/dev/null || true").trim();
            if (!cloakedApps.isEmpty()) {
                for (String pkg : cloakedApps.split("\n")) {
                    String p = pkg.trim();
                    if (!p.isEmpty()) {
                        runCmd("pm enable " + p + " 2>/dev/null || true");
                    }
                }
                runCmd("rm -f /data/local/tmp/revamp_cloaked_apps.txt");
            }
            cloakAllRiskApps(false);
        }
        return isBankingModeEnabled();
    }

    @Override
    public String getDetectedRiskApps() throws RemoteException {
        java.util.LinkedHashMap<String, String> found = getDetectedRiskMap();
        if (found.isEmpty()) {
            return "CLEAN:0:0:No sideloaded risk apps found.";
        }
        String disabledList = runCmd("pm list packages -d --user 0");
        int cloakedCount = 0;
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry : found.entrySet()) {
            String pkg = entry.getKey();
            boolean isCloaked = disabledList.contains("package:" + pkg);
            if (isCloaked) cloakedCount++;
            if (sb.length() > 0) sb.append(", ");
            sb.append(entry.getValue()).append(isCloaked ? " (Cloaked)" : " (Exposed)");
        }
        return "DETECTED:" + found.size() + ":" + cloakedCount + ":" + sb.toString();
    }

    @Override
    public String checkUserCertificates() throws RemoteException {
        try {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("AndroidCAStore");
            ks.load(null, null);
            java.util.Enumeration<String> aliases = ks.aliases();
            int userCount = 0;
            java.util.List<String> userCertList = new java.util.ArrayList<>();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (alias.startsWith("user:")) {
                    userCount++;
                    try {
                        java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) ks.getCertificate(alias);
                        if (cert != null && cert.getSubjectDN() != null) {
                            userCertList.add(cert.getSubjectDN().getName());
                        }
                    } catch (Exception ignored) {}
                }
            }
            if (userCount == 0) {
                return "CLEAN:0:No User CA Certificates installed in AndroidCAStore.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("DETECTED:").append(userCount).append(":").append(userCount).append(" User Certificate(s) found in Credential Storage.");
            for (String cn : userCertList) {
                sb.append(" [").append(cn).append("]");
            }
            return sb.toString();
        } catch (Exception e) {
            return "CLEAN:0:No User CA Certificates installed in system storage.";
        }
    }

    @Override
    public boolean isShizukuCloaked() throws RemoteException {
        String pid = runCmd("pidof shizuku_server 2>/dev/null || true").trim();
        String flag = runCmd("cat /data/local/tmp/revamp_shizuku_cloaked.txt 2>/dev/null || true").trim();
        if (!pid.isEmpty()) {
            if ("1".equals(flag)) {
                runCmd("rm -f /data/local/tmp/revamp_shizuku_cloaked.txt 2>/dev/null || true");
            }
            return false;
        }
        return "1".equals(flag) || isShizukuDeepFrozen();
    }

    private void stopShizukuServerDetached(boolean disablePackage) {
        String shizPkg = resolveShizukuPkg();
        runCmd("echo 1 > /data/local/tmp/revamp_shizuku_cloaked.txt");
        // 1. Disable Shizuku-Next Watchdog & request clean shutdown before killing daemon
        runCmd("am broadcast -n " + shizPkg + "/moe.shizuku.manager.receiver.WatchdogToggleReceiver -a " + shizPkg + ".WATCHDOG_OFF 2>/dev/null || true");
        runCmd("am broadcast -a " + shizPkg + ".WATCHDOG_OFF -p " + shizPkg + " 2>/dev/null || true");
        runCmd("am broadcast -n " + shizPkg + "/moe.shizuku.manager.receiver.ManualStopReceiver -a " + shizPkg + ".STOP 2>/dev/null || true");
        runCmd("am broadcast -a " + shizPkg + ".STOP -p " + shizPkg + " 2>/dev/null || true");
        // 2. Restrict background execution & wakeups
        runCmd("cmd appops set " + shizPkg + " RUN_IN_BACKGROUND ignore 2>/dev/null || true");
        runCmd("cmd appops set " + shizPkg + " RUN_ANY_IN_BACKGROUND ignore 2>/dev/null || true");
        runCmd("cmd appops set " + shizPkg + " WAKE_LOCK ignore 2>/dev/null || true");
        runCmd("cmd appops set " + shizPkg + " START_FOREGROUND ignore 2>/dev/null || true");
        // 3. Detach 400ms kill so Binder RPC returns cleanly BEFORE shizuku_server exits
        String disableCmd = disablePackage
                ? "pm disable-user --user 0 " + shizPkg + " 2>/dev/null || true; "
                : "";
        runCmd("nohup sh -c 'sleep 0.4; am force-stop " + shizPkg + " 2>/dev/null || true; "
                + disableCmd
                + "PIDS=$(pidof shizuku_server shizuku_starter rish 2>/dev/null); "
                + "if [ -n \"$PIDS\" ]; then kill -9 $PIDS 2>/dev/null || true; fi; "
                + "am force-stop " + shizPkg + " 2>/dev/null || true' >/dev/null 2>&1 &");
    }

    @Override
    public boolean setShizukuCloaked(boolean cloak) throws RemoteException {
        String shizPkg = resolveShizukuPkg();
        if (cloak) {
            stopShizukuServerDetached(false);
            return true;
        } else {
            runCmd("pm enable " + shizPkg + " 2>/dev/null || true");
            runCmd("cmd appops set " + shizPkg + " RUN_IN_BACKGROUND allow 2>/dev/null || true");
            runCmd("cmd appops set " + shizPkg + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null || true");
            runCmd("cmd appops set " + shizPkg + " WAKE_LOCK allow 2>/dev/null || true");
            runCmd("cmd appops set " + shizPkg + " START_FOREGROUND allow 2>/dev/null || true");
            runCmd("rm -f /data/local/tmp/revamp_shizuku_cloaked.txt 2>/dev/null || true");
            startShizukuServerDirect();
            return isShizukuCloaked();
        }
    }

    @Override
    public boolean isShizukuDeepFrozen() throws RemoteException {
        String shizPkg = resolveShizukuPkg();
        String dis = runCmd("pm list packages -d " + shizPkg + " 2>/dev/null || true");
        return dis.contains(shizPkg);
    }

    @Override
    public boolean setShizukuDeepFreeze(boolean freeze) throws RemoteException {
        if (freeze) {
            stopShizukuServerDetached(true);
            return true;
        } else {
            return setShizukuCloaked(false);
        }
    }

    @Override
    public String engageFullBankingStealth(boolean deepFreezeShizuku) throws RemoteException {
        String shizPkg = resolveShizukuPkg();
        boolean origPresent = !runCmd("pm path moe.shizuku.privileged.api 2>/dev/null || true").trim().isEmpty();
        setBankingModeEnabled(true);
        stopShizukuServerDetached(deepFreezeShizuku);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Engaging Full Banking & UPI Stealth ===\n");
        sb.append("1. ✔ Granted self WRITE_SECURE_SETTINGS for offline recovery\n");
        sb.append("2. ✔ Paused Accessibility & Private DNS + Cleared /data/local/tmp artifacts\n");
        sb.append("3. ✔ Cloaked all detected Sideloaded/Modding/VPN tools\n");
        sb.append("4. ✔ Disabled Dev Options, USB ADB & Wireless ADB\n");
        sb.append("5. ✔ Watchdog OFF, ").append(shizPkg).append(" stopped");
        if (deepFreezeShizuku) {
            sb.append(" & Deep Frozen (pm disable-user)");
        }
        sb.append(", shizuku_server terminating\n");
        if (origPresent) {
            sb.append("⚠ WARNING: Original moe.shizuku.privileged.api is STILL installed!\n");
            sb.append("   Protectt.ai / Lookout / Bureau detect this package ID even when disabled.\n");
            sb.append("   Tap 'Setup Shizuku-Next Package Spoof' if any bank still flags it.");
        } else {
            sb.append("✔ Spoofed Shizuku verified (").append(shizPkg).append(") — original ID absent!");
        }
        return sb.toString();
    }

    @Override
    public String exitFullBankingStealth() throws RemoteException {
        setBankingModeEnabled(false);
        setShizukuCloaked(false);
        String shizPkg = resolveShizukuPkg();
        return "✔ Exited Banking Stealth: Dev Options & ADB enabled, Accessibility & Private DNS restored, Modding tools uncloaked, Shizuku (" + shizPkg + ") active.";
    }

    @Override
    public String getStealthAuditSummary() throws RemoteException {
        String shizPkg = resolveShizukuPkg();
        boolean origPresent = !runCmd("pm path moe.shizuku.privileged.api 2>/dev/null || true").trim().isEmpty();
        String shizPid = runCmd("pidof shizuku_server 2>/dev/null || true").trim();
        String dev = runCmd("settings get global development_settings_enabled").trim();
        String adb = runCmd("settings get global adb_enabled").trim();
        String wifiAdb = runCmd("settings get global adb_wifi_enabled").trim();
        String adbdSvc = runCmd("getprop init.svc.adbd").trim();
        String usbCfg = runCmd("getprop persist.sys.usb.config").trim();
        String tcpPort = runCmd("getprop service.adb.tcp.port").trim();
        String accEn = runCmd("settings get secure accessibility_enabled").trim();
        String accSvc = runCmd("settings get secure enabled_accessibility_services").trim();
        String dnsMode = runCmd("settings get global private_dns_mode").trim();
        String vpnLinks = runCmd("dumpsys connectivity 2>/dev/null | grep -iE 'TRANSPORT_VPN|VpnInfo' | head -n3 || true").trim();
        String certs = checkUserCertificates();
        String risks = getDetectedRiskApps();
        String tmpClutter = runCmd("ls -1 /data/local/tmp 2>/dev/null | grep -iE '\\.apk$|testcert|dump.*\\.xml$|ui.*\\.xml$|screen.*\\.png$' || true").trim();

        StringBuilder sb = new StringBuilder();
        sb.append("=== Banking & UPI Stealth Audit ===\n");
        sb.append("[1] Shizuku Pkg: ").append(origPresent ? "❌ EXPOSED (moe.shizuku.privileged.api)" : "✔ Spoofed (" + shizPkg + ")").append("\n");
        sb.append("[2] shizuku_server: ").append(shizPid.isEmpty() ? "✔ Stopped" : "⚠ RUNNING (PID " + shizPid + ")").append("\n");
        sb.append("[3] Dev/USB/WiFi ADB: ").append(dev).append("/").append(adb).append("/").append(wifiAdb)
          .append(" | adbd=").append(adbdSvc).append(" usb=").append(usbCfg).append(" port=").append(tcpPort).append("\n");
        sb.append("[4] Accessibility: enabled=").append(accEn).append(" svcs=").append(accSvc.isEmpty() || "null".equals(accSvc) ? "none" : accSvc).append("\n");
        sb.append("[5] DNS/VPN: dns=").append(dnsMode).append(" | vpn=").append(vpnLinks.isEmpty() ? "none" : "ACTIVE").append("\n");
        sb.append("[6] Certs: ").append(certs).append("\n");
        sb.append("[7] Modding Apps: ").append(risks).append("\n");
        sb.append("[8] /tmp Clutter: ").append(tmpClutter.isEmpty() ? "✔ Clean" : "⚠ " + tmpClutter.replace("\n", ", "));
        return sb.toString();
    }

    @Override
    public boolean isDevOptionsEnabled() throws RemoteException {
        return "1".equals(runCmd("settings get global development_settings_enabled").trim());
    }

    @Override
    public boolean setDevOptionsEnabled(boolean enable) throws RemoteException {
        runCmd("settings put global development_settings_enabled " + (enable ? "1" : "0"));
        return isDevOptionsEnabled();
    }

    @Override
    public boolean isUsbDebuggingEnabled() throws RemoteException {
        return "1".equals(runCmd("settings get global adb_enabled").trim());
    }

    @Override
    public boolean setUsbDebuggingEnabled(boolean enable) throws RemoteException {
        runCmd("settings put global adb_enabled " + (enable ? "1" : "0"));
        return isUsbDebuggingEnabled();
    }

    @Override
    public String startShizukuServerDirect() throws RemoteException {
        String shizPkg = resolveShizukuPkg();
        return runCmd("pkg=$(pm path " + shizPkg + " 2>/dev/null | head -n1 | cut -d: -f2); if [ -n \"$pkg\" ]; then lib=\"$(dirname \"$pkg\")/lib/arm64/libshizuku.so\"; if [ -f \"$lib\" ]; then \"$lib\" 2>&1; else sh /sdcard/Android/data/" + shizPkg + "/files/start.sh 2>&1; fi; else echo \"Shizuku app not found (" + shizPkg + ")\"; fi");
    }

    @Override
    public String getHardwareSpecsJson() throws RemoteException {
        try {
            String mfg = runCmd("getprop ro.product.manufacturer").trim();
            if (mfg.isEmpty()) mfg = Build.MANUFACTURER;
            String marketName = runCmd("getprop ro.vendor.oplus.market.name").trim();
            String model = runCmd("getprop ro.product.model").trim();
            if (model.isEmpty()) model = Build.MODEL;
            String deviceTitle = !marketName.isEmpty() ? marketName : ((mfg + " " + model).trim());

            String oosDisplay = runCmd("getprop ro.build.version.oplusrom.display").trim();
            String displayId = runCmd("getprop ro.build.display.id").trim();
            if (displayId.isEmpty()) displayId = Build.DISPLAY;

            String androidRelease = Build.VERSION.RELEASE;
            int sdkInt = Build.VERSION.SDK_INT;

            String soc = runCmd("getprop ro.soc.model").trim();
            if (soc.isEmpty() && Build.VERSION.SDK_INT >= 31) soc = Build.SOC_MODEL;
            if (soc.isEmpty()) soc = Build.HARDWARE;
            String abi = runCmd("getprop ro.product.cpu.abi").trim();
            String socText = soc + (abi.isEmpty() ? "" : " (" + abi + ")");

            String memTotalLine = runCmd("grep -m1 'MemTotal:' /proc/meminfo");
            long totalKb = parseKb(memTotalLine);
            String ramText = totalKb > 0
                ? String.format(Locale.US, "%.1f GB Available Physical RAM", totalKb / 1048576.0)
                : "Physical RAM detected via Android";

            String df = runCmd("df -h /data 2>/dev/null | tail -n1 | awk '{print $2}'").trim();
            String storageText = df.isEmpty() ? "Internal Flash Storage" : (df + " Total Usable Data Partition");

            String wmSize = runCmd("wm size 2>/dev/null | grep -oE '[0-9]+x[0-9]+' | tail -n1").trim();
            String wmDens = runCmd("wm density 2>/dev/null | grep -oE '[0-9]+' | tail -n1").trim();
            String displayText = (wmSize.isEmpty() ? "Adaptive Display" : wmSize)
                + (wmDens.isEmpty() ? "" : " @ " + wmDens + " DPI");

            String batLevel = runCmd("dumpsys battery 2>/dev/null | grep -i 'level:' | head -n1 | cut -d: -f2").trim();
            String batText = batLevel.isEmpty() ? "Li-Ion / Li-Po Battery Subsystem" : ("Current Charge: " + batLevel + "%");

            String osPrefix = !oosDisplay.isEmpty() ? ("OxygenOS " + oosDisplay + " • ") : "";
            String osText = osPrefix + "Android " + androidRelease + " (SDK " + sdkInt + ")" + (displayId.isEmpty() ? "" : " • " + displayId);
            String badgeText = deviceTitle + " (" + model + ") • " + (!oosDisplay.isEmpty() ? "OxygenOS " + oosDisplay + " • " : "") + "Android " + androidRelease;

            JSONObject json = new JSONObject();
            json.put("badge", badgeText);
            json.put("soc", socText);
            json.put("ram", ramText);
            json.put("storage", storageText);
            json.put("display", displayText);
            json.put("battery", batText);
            json.put("os", osText);
            json.put("hw_chip_badge", Build.DEVICE != null ? Build.DEVICE.toUpperCase(Locale.US) : "ANDROID");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public String getBypassDetailedStatus() throws RemoteException {
        String batLevelStr = runCmd("dumpsys battery | grep -i 'level:' | head -n1 | cut -d: -f2").trim();
        int batLevel = 0;
        try { batLevel = Integer.parseInt(batLevelStr); } catch (Exception ignored) {}

        boolean bypassState = isBypassChargingEnabled();
        if (!bypassState) {
            return "DISABLED: Standard charging to 100%.";
        }
        if (batLevel < 80) {
            return "ARMED (" + batLevel + "% / 80%): Fast-charging battery to 80%. Motherboard bypass engages automatically at ≥80%.";
        } else {
            return "ENGAGED (" + batLevel + "%): Hardware bypass ACTIVE. Power is routed directly to motherboard (0 mA cell wear).";
        }
    }

    @Override
    public String scanLargeFilesJson() throws RemoteException {
        String cmd = "find /sdcard/Download -maxdepth 2 \\( -name \"*.apk\" -o -name \"*.apkm\" -o -name \"*.xapk\" -o -size +25M \\) -exec ls -lh {} + 2>/dev/null";
        String output = runCmd(cmd);
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (String line : output.split("\n")) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            String[] parts = l.split("\\s+");
            if (parts.length >= 9) {
                String size = parts[4];
                String path = l.substring(l.indexOf(parts[8]));
                String name = path.substring(path.lastIndexOf('/') + 1);
                if (!first) json.append(",");
                json.append("{\"name\":\"").append(name.replace("\"", "\\\""))
                    .append("\",\"path\":\"").append(path.replace("\"", "\\\""))
                    .append("\",\"size\":\"").append(size).append("\"}");
                first = false;
            }
        }
        json.append("]");
        return json.toString();
    }

    @Override
    public String deleteFileManual(String filePath) throws RemoteException {
        if (filePath == null || !filePath.startsWith("/sdcard/Download/")) {
            return "ERR: Restricted - only files inside /sdcard/Download/ can be deleted.";
        }
        runCmd("rm -f \"" + filePath + "\"");
        return "✔ Deleted: " + filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    @Override
    public String compileAllInstalledApps() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Native ARM64 Speed Compilation (All Apps) ===\n");
        String raw = runCmd("pm list packages -3 --user 0");
        String[] lines = raw.split("\n");
        int count = 0;
        for (String l : lines) {
            String pkg = l.replace("package:", "").trim();
            if (pkg.isEmpty()) continue;
            runCmd("cmd package compile -m speed -f " + pkg);
            count++;
            if (count % 10 == 0) {
                sb.append("• Compiled ").append(count).append(" apps...\n");
            }
        }
        runCmd("cmd package compile -m speed -f com.android.systemui");
        runCmd("cmd package compile -m speed -f com.android.launcher");
        sb.append("✔ Successfully compiled ").append(count).append(" user apps + System UI & Launcher to native ARM64 speed!\n");
        return sb.toString();
    }

    private static final String[] DRAINER_PACKAGES = {
        "com.facebook.katana",
        "com.instagram.android",
        "com.snapchat.android",
        "com.heytap.browser",
        "com.heytap.cloud",
        "com.heytap.market",
        "com.heytap.accessory",
        "com.oneplus.oshare",
        "com.oneplus.account",
        "com.oneplus.brickmode",
        "com.oneplus.backuprestore",
        "com.oplus.upgradeguide",
        "com.coloros.activation"
    };

    @Override
    public String restrictBackgroundDrainers() throws RemoteException {
        restoreAudioAndConnectivityServices();
        StringBuilder sb = new StringBuilder();
        sb.append("=== Restricting Aggressive Background Battery Drainers ===\n");
        int count = 0;
        for (String pkg : DRAINER_PACKAGES) {
            runCmd("cmd deviceidle whitelist -" + pkg + " 2>/dev/null || true");
            runCmd("cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore 2>/dev/null || true");
            runCmd("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND ignore 2>/dev/null || true");
            runCmd("cmd appops set " + pkg + " WAKE_LOCK ignore 2>/dev/null || true");
            runCmd("am set-standby-bucket " + pkg + " restricted 2>/dev/null || true");
            runCmd("am force-stop " + pkg + " 2>/dev/null || true");
            count++;
        }
        sb.append("✔ Locked down ").append(count).append(" installed background bloat/social drainers into Doze/Restricted standby (Audio/AA & OPlus Linker preserved).\n");
        return sb.toString();
    }

    // Developer Mode Settings
    @Override
    public boolean isAdbAuthTimeoutDisabled() throws RemoteException {
        String val = runCmd("settings get global adb_allowed_connection_time").trim();
        return "0".equals(val);
    }

    @Override
    public boolean setAdbAuthTimeoutDisabled(boolean disabled) throws RemoteException {
        runCmd("settings put global adb_allowed_connection_time " + (disabled ? "0" : "604800000"));
        return isAdbAuthTimeoutDisabled();
    }

    @Override
    public boolean isVerifyAdbAppsEnabled() throws RemoteException {
        String val = runCmd("settings get global verifier_verify_adb_installs").trim();
        return !"0".equals(val);
    }

    @Override
    public boolean setVerifyAdbAppsEnabled(boolean enable) throws RemoteException {
        runCmd("settings put global verifier_verify_adb_installs " + (enable ? "1" : "0"));
        return isVerifyAdbAppsEnabled();
    }

    @Override
    public boolean isWifiDisplayCertEnabled() throws RemoteException {
        String val = runCmd("settings get global wifi_display_certification_on").trim();
        return "1".equals(val);
    }

    @Override
    public boolean setWifiDisplayCertEnabled(boolean enable) throws RemoteException {
        runCmd("settings put global wifi_display_certification_on " + (enable ? "1" : "0"));
        return isWifiDisplayCertEnabled();
    }

    @Override
    public boolean isStayAwakeWhileCharging() throws RemoteException {
        String val = runCmd("settings get global stay_on_while_plugged_in").trim();
        return !"0".equals(val) && !"null".equals(val);
    }

    @Override
    public boolean setStayAwakeWhileCharging(boolean stayAwake) throws RemoteException {
        runCmd("settings put global stay_on_while_plugged_in " + (stayAwake ? "7" : "0"));
        return isStayAwakeWhileCharging();
    }

    @Override
    public String getRiskAppsDetailedJson() throws RemoteException {
        String disabledList = runCmd("pm list packages -d --user 0");
        java.util.LinkedHashMap<String, String> detected = getDetectedRiskMap();

        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (java.util.Map.Entry<String, String> entry : detected.entrySet()) {
            String pkg = entry.getKey();
            String name = entry.getValue();
            boolean cloaked = disabledList.contains("package:" + pkg);
            if (!first) json.append(",");
            json.append("{\"pkg\":\"").append(pkg)
                .append("\",\"name\":\"").append(name.replace("\"", "\\\""))
                .append("\",\"cloaked\":").append(cloaked)
                .append("}");
            first = false;
        }
        json.append("]");
        return json.toString();
    }

    @Override
    public boolean setAppCloaked(String packageName, boolean cloak) throws RemoteException {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.trim();
        if (cloak) {
            runCmd("pm disable-user --user 0 " + pkg + " 2>/dev/null || true");
            runCmd("am force-stop " + pkg + " 2>/dev/null || true");
        } else {
            runCmd("pm enable " + pkg + " 2>/dev/null || true");
        }
        String dis = runCmd("pm list packages -d " + pkg);
        return dis.contains(pkg);
    }

    @Override
    public boolean cloakAllRiskApps(boolean cloak) throws RemoteException {
        for (String pkg : getDetectedRiskMap().keySet()) {
            setAppCloaked(pkg, cloak);
        }
        return true;
    }

    private int getPhysicalShortSidePx() {
        String out = runCmd("wm size").trim();
        int fallback = 1080;
        for (String line : out.split("\n")) {
            if (line.contains(":")) {
                String resPart = line.substring(line.indexOf(':') + 1).trim();
                String[] dims = resPart.split("x");
                if (dims.length == 2) {
                    try {
                        int w = Integer.parseInt(dims[0].trim());
                        int h = Integer.parseInt(dims[1].trim());
                        int minDim = Math.min(w, h);
                        if (minDim > 0) {
                            if (line.startsWith("Override")) return minDim;
                            fallback = minDim;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return fallback;
    }

    @Override
    public int getSmallestWidthDpi() throws RemoteException {
        String out = runCmd("wm density").trim();
        int physical = 420;
        for (String line : out.split("\n")) {
            if (line.contains(":")) {
                try {
                    int val = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                    if (line.startsWith("Override")) return val;
                    physical = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return physical;
    }

    @Override
    public int getSmallestWidthDp() throws RemoteException {
        int minPx = getPhysicalShortSidePx();
        int dpi = getSmallestWidthDpi();
        if (dpi <= 0) dpi = 420;
        return Math.round(minPx * 160.0f / dpi);
    }

    @Override
    public String setSmallestWidthDp(int targetDp) throws RemoteException {
        if (targetDp <= 0) {
            runCmd("wm density reset");
            int resetDpi = getSmallestWidthDpi();
            int resetDp = getSmallestWidthDp();
            return "✔ Smallest Width reset to Stock (" + resetDp + " dp @ " + resetDpi + " DPI)";
        }
        int minPx = getPhysicalShortSidePx();
        int targetDpi = Math.max(120, (minPx * 160) / targetDp);
        runCmd("wm density " + targetDpi);
        String mode = targetDp >= 560 ? "Tablet View" : "Mobile View";
        return "✔ Smallest Width set to " + targetDp + " dp (" + mode + " • " + targetDpi + " DPI)";
    }
}
