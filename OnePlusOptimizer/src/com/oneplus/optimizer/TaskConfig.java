package com.oneplus.optimizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared registry of all optimization tasks available for Run Master Optimization,
 * Daily Auto-Care Service (Default vs One-Time), and Reboot Auto-Optimize.
 */
public final class TaskConfig {

    public static final String PREF_NAME = "optimizer_prefs";
    public static final String KEY_MASTER_DEFAULT_CSV = "pref_master_default_tasks_csv";
    public static final String KEY_DAILY_DEFAULT_CSV = "pref_daily_default_tasks_csv";
    public static final String KEY_DAILY_ONETIME_CSV = "pref_daily_onetime_tasks_csv";
    public static final String KEY_BOOT_AUTO_OPTIMIZE = "pref_boot_auto_optimize";
    public static final String KEY_LAST_BOOT_UPTIME_HANDLED = "pref_last_boot_uptime_ms";

    public static final String TASK_CACHE_TRIM = "cache_trim";
    public static final String TASK_STORAGE_TRIM = "storage_trim";
    public static final String TASK_DEV_DEBUGGING_ON = "dev_debugging_on";
    public static final String TASK_BANKING_STEALTH_OFF = "banking_stealth_off";
    public static final String TASK_SHIZUKU_CLOAK_OFF = "shizuku_cloak_off";
    public static final String TASK_PRIVATE_DNS = "private_dns";
    public static final String TASK_AA_CABLE_ONLY = "aa_cable_only";
    public static final String TASK_BLOATWARE_SHIELD = "bloatware_shield";
    public static final String TASK_CHARGE_LIMIT_80 = "charge_limit_80";
    public static final String TASK_BYPASS_CHARGING = "bypass_charging";
    public static final String TASK_FAST_DOZE = "fast_doze";
    public static final String TASK_CELLULAR_SLEEP = "cellular_sleep";
    public static final String TASK_FHD_RESOLUTION = "fhd_resolution";
    public static final String TASK_REFRESH_LTPO_AUTO = "refresh_ltpo_auto";
    public static final String TASK_AOD_SMART_MODE = "aod_smart_mode";
    public static final String TASK_WEATHER_TAMER = "weather_tamer";
    public static final String TASK_ANIM_05X = "anim_05x";
    public static final String TASK_GESTURES_SHIELD = "gestures_shield";
    public static final String TASK_CRITICAL_WHITELIST = "critical_whitelist";
    public static final String TASK_RESTRICT_DRAINERS = "restrict_drainers";
    public static final String TASK_ADB_NO_TIMEOUT = "adb_no_timeout";
    public static final String TASK_VERIFY_ADB_OFF = "verify_adb_off";
    public static final String TASK_COMPILE_CORE = "compile_core";
    public static final String TASK_COMPILE_NEW = "compile_new";
    public static final String TASK_COMPILE_ALL = "compile_all";

    public static final class TaskItem {
        public final String key;
        public final String title;
        public final String summary;
        public final boolean defaultMaster;
        public final boolean defaultDaily;

        public TaskItem(String key, String title, String summary, boolean defaultMaster, boolean defaultDaily) {
            this.key = key;
            this.title = title;
            this.summary = summary;
            this.defaultMaster = defaultMaster;
            this.defaultDaily = defaultDaily;
        }
    }

    public static final TaskItem[] ALL_TASKS = new TaskItem[] {
        new TaskItem(TASK_BLOATWARE_SHIELD, "OEM Bloatware & Telemetry Shield",
            "Freezes Meta/HeyTap/SAUHelper/OPlus telemetry & error reporting", true, true),
        new TaskItem(TASK_PRIVATE_DNS, "AdGuard Private DNS Shield",
            "Routes system DNS to dns.adguard-dns.com to block ads & trackers", true, true),
        new TaskItem(TASK_AA_CABLE_ONLY, "Android Auto Cable-Only Mode",
            "Locks Android Auto to wired USB to prevent wireless dropouts", true, true),
        new TaskItem(TASK_CHARGE_LIMIT_80, "80% Custom Charge Limit (Smart Charge OFF)",
            "Caps charging at 80% and blocks Smart Charging override", true, true),
        new TaskItem(TASK_BYPASS_CHARGING, "Hardware Bypass Charging (At ≥80%)",
            "Routes AC power straight to motherboard at ≥80%", true, true),
        new TaskItem(TASK_FAST_DOZE, "Fast Doze Instant Standby (60s)",
            "Starts deep idle sleep 60s after screen-off instead of 30 min", true, true),
        new TaskItem(TASK_CELLULAR_SLEEP, "Cellular Modem Sleep on Wi-Fi",
            "Sleeps 5G/4G data modem whenever Wi-Fi is active", true, true),
        new TaskItem(TASK_AOD_SMART_MODE, "Smart AOD & Deep Standby",
            "Sets Always-On Display to pickup/touch trigger only", true, true),
        new TaskItem(TASK_WEATHER_TAMER, "Weather CPU Tamer",
            "Blocks stock Weather background GPS polling and wake locks", true, true),
        new TaskItem(TASK_CRITICAL_WHITELIST, "Verify Critical Apps Whitelist",
            "Whitelists Messaging, Calls, SMS, Alarms & Shizuku in Doze", true, true),
        new TaskItem(TASK_RESTRICT_DRAINERS, "Restrict Background Drainers",
            "Puts heavy social/OEM background apps into Restricted bucket", true, true),
        new TaskItem(TASK_CACHE_TRIM, "Trim Cache & Flush RAM",
            "Clears temporary system caches (1GB) and background processes", true, true),
        new TaskItem(TASK_STORAGE_TRIM, "Flash Storage Trim (sm fstrim)",
            "Trims flash storage blocks to keep read/write speed fast", true, true),
        new TaskItem(TASK_DEV_DEBUGGING_ON, "Wired & Wireless Debugging ON (+ Dev Mode)",
            "Turns on Developer Options, USB ADB & Wireless ADB", true, false),
        new TaskItem(TASK_BANKING_STEALTH_OFF, "Exit Banking Stealth & Uncloak Tools",
            "Restores standard app visibility & Accessibility/VPN tools", true, false),
        new TaskItem(TASK_SHIZUKU_CLOAK_OFF, "Ensure Shizuku Uncloaked",
            "Keeps Shizuku appops & server active for automation", true, false),
        new TaskItem(TASK_FHD_RESOLUTION, "Screen Resolution: Standard FHD+",
            "Switches display scaling to FHD+ for lower GPU power draw", true, false),
        new TaskItem(TASK_REFRESH_LTPO_AUTO, "Disable Extreme Refresh Lock (Adaptive Hz)",
            "Lets display use adaptive refresh rate scaling for battery life", true, false),
        new TaskItem(TASK_ANIM_05X, "0.5x Snappy System Animations",
            "Sets Window, Transition & Animator scales to 0.5x", true, false),
        new TaskItem(TASK_GESTURES_SHIELD, "Full-Screen Gestures Shield (Mode 2)",
            "Enforces AOSP gestural nav & side-back gesture insets", true, true),
        new TaskItem(TASK_ADB_NO_TIMEOUT, "Disable ADB Auth Timeout (Keep Keys Forever)",
            "Prevents 7-day automatic revocation of paired computer keys", false, false),
        new TaskItem(TASK_VERIFY_ADB_OFF, "Disable Verify Apps Over USB",
            "Skips slow Play Protect checks when sideloading via ADB", false, false),
        new TaskItem(TASK_COMPILE_CORE, "Native AOT Speed-Compile Core Apps",
            "Compiles WhatsApp, Chrome, SystemUI, Launcher (~15s)", false, true),
        new TaskItem(TASK_COMPILE_NEW, "Native AOT Speed-Compile All 3rd-Party Apps",
            "Runs dex2oat speed compile across installed user apps (~1-2m)", false, false),
        new TaskItem(TASK_COMPILE_ALL, "Deep AOT Re-Compile ALL Apps (-f force)",
            "Forces full native recompilation of every user app & UI (~3m)", false, false)
    };

    private TaskConfig() {}

    public static Set<String> getDefaultMasterTasks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!prefs.contains(KEY_MASTER_DEFAULT_CSV)) {
            return buildBuiltInSet(true);
        }
        return parseCsvSet(prefs.getString(KEY_MASTER_DEFAULT_CSV, ""));
    }

    public static void saveDefaultMasterTasks(Context context, Set<String> keys) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_MASTER_DEFAULT_CSV, toOrderedCsv(keys)).apply();
    }

    public static String getDefaultMasterTasksCsv(Context context) {
        return toOrderedCsv(getDefaultMasterTasks(context));
    }

    public static boolean isBloatwareEnabledInMaster(Context context) {
        return getDefaultMasterTasks(context).contains(TASK_BLOATWARE_SHIELD);
    }

    public static void setBloatwareEnabledInMaster(Context context, boolean enable) {
        Set<String> set = getDefaultMasterTasks(context);
        if (enable) {
            set.add(TASK_BLOATWARE_SHIELD);
        } else {
            set.remove(TASK_BLOATWARE_SHIELD);
        }
        saveDefaultMasterTasks(context, set);
    }

    public static Set<String> getDefaultDailyTasks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!prefs.contains(KEY_DAILY_DEFAULT_CSV)) {
            return buildBuiltInSet(false);
        }
        return parseCsvSet(prefs.getString(KEY_DAILY_DEFAULT_CSV, ""));
    }

    public static void saveDefaultDailyTasks(Context context, Set<String> keys) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DAILY_DEFAULT_CSV, toOrderedCsv(keys)).apply();
    }

    public static Set<String> getOneTimeDailyTasks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_DAILY_ONETIME_CSV, null);
        return raw == null ? null : parseCsvSet(raw);
    }

    public static void saveOneTimeDailyTasks(Context context, Set<String> keys) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (keys == null) {
            prefs.edit().remove(KEY_DAILY_ONETIME_CSV).apply();
        } else {
            prefs.edit().putString(KEY_DAILY_ONETIME_CSV, toOrderedCsv(keys)).apply();
        }
    }

    public static String consumeNextDailyTasksCsv(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String oneTime = prefs.getString(KEY_DAILY_ONETIME_CSV, null);
        if (oneTime != null && !oneTime.trim().isEmpty()) {
            prefs.edit().remove(KEY_DAILY_ONETIME_CSV).apply();
            return oneTime;
        }
        return toOrderedCsv(getDefaultDailyTasks(context));
    }

    public static boolean isBootAutoOptimizeEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_BOOT_AUTO_OPTIMIZE, false);
    }

    public static void setBootAutoOptimizeEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_BOOT_AUTO_OPTIMIZE, enabled).apply();
    }

    /**
     * Prevents duplicate launches if BOOT_COMPLETED arrives more than once within the same boot session.
     */
    public static boolean claimBootLaunchOnce(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long nowUptime = SystemClock.elapsedRealtime();
        long lastUptime = prefs.getLong(KEY_LAST_BOOT_UPTIME_HANDLED, -1L);
        if (lastUptime >= 0 && nowUptime >= lastUptime && (nowUptime - lastUptime) < 180000L) {
            return false;
        }
        prefs.edit().putLong(KEY_LAST_BOOT_UPTIME_HANDLED, nowUptime).apply();
        return true;
    }

    public static Set<String> buildBuiltInSet(boolean forMaster) {
        Set<String> set = new LinkedHashSet<>();
        for (TaskItem item : ALL_TASKS) {
            if (forMaster ? item.defaultMaster : item.defaultDaily) {
                set.add(item.key);
            }
        }
        return set;
    }

    public static Set<String> parseCsvSet(String csv) {
        Set<String> set = new LinkedHashSet<>();
        if (csv == null || csv.trim().isEmpty()) return set;
        for (String p : csv.split(",")) {
            String k = p.trim();
            if (!k.isEmpty()) set.add(k);
        }
        return set;
    }

    public static String toOrderedCsv(Set<String> keys) {
        if (keys == null || keys.isEmpty()) return "";
        List<String> ordered = new ArrayList<>();
        Set<String> added = new LinkedHashSet<>();
        for (TaskItem item : ALL_TASKS) {
            if (keys.contains(item.key)) {
                ordered.add(item.key);
                added.add(item.key);
            }
        }
        for (String extra : keys) {
            if (!added.contains(extra) && extra != null && !extra.trim().isEmpty()) {
                ordered.add(extra.trim());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(ordered.get(i));
        }
        return sb.toString();
    }
}
