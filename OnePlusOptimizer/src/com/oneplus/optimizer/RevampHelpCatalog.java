package com.oneplus.optimizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;

/**
 * Centralized registry of contextual What & Why help dialogs bound to `?` icons
 * on every section header and option row.
 */
public final class RevampHelpCatalog {

    private static final class HelpEntry {
        final int viewId;
        final String title;
        final String what;
        final String why;

        HelpEntry(int viewId, String title, String what, String why) {
            this.viewId = viewId;
            this.title = title;
            this.what = what;
            this.why = why;
        }
    }

    private static final HelpEntry[] ENTRIES = new HelpEntry[] {
        new HelpEntry(R.id.help_section_hw_specs, "Hardware Specifications",
            "Reads live SoC, physical RAM pool, /data storage size, display panel resolution/DPI, battery telemetry, and Android/OEM build IDs directly from your device.",
            "Verifies exact hardware configuration and firmware build tags accurately without third-party benchmark apps."),
        new HelpEntry(R.id.help_section_live_metrics, "Live System Metrics",
            "Displays real-time RAM pressure (/proc/meminfo), battery percentage & thermal sensor temperature (°C), free internal storage space, and quick memory/cache reclamation buttons.",
            "Lets you spot thermal throttling (>38°C), high RAM usage, or storage pressure and reclaim resources safely in 1 tap."),
        new HelpEntry(R.id.help_metrics_actions, "Trim Cache & Flush RAM",
            "Trim Cache runs 'pm trim-caches 1000M' to clear stale app/ART cache; Flush RAM runs 'am kill-all' to stop idle background-cached processes.",
            "Frees dead cache storage and memory cleanly without clearing logins, app data, or active foreground apps."),
        new HelpEntry(R.id.help_section_master_opt, "Run Master Optimization",
            "Executes your customizable Default Optimization checklist in one tap (or runs a temporary One-Time selection) across shields, power, DNS, bloatware, battery limits, and debugging.",
            "Quickly restores your full daily-driver setup after a reboot, firmware OTA update, or exiting Banking Stealth."),
        new HelpEntry(R.id.help_master_custom, "Default vs One-Time Selection",
            "Select Default Tasks saves your persistent checklist (used by Run Master Optimization & Reboot Auto-Optimize). One-Time Run lets you pick checkboxes from all available tasks just for this single execution without changing your saved Default.",
            "Gives total control over both permanent routines and temporary custom runs."),
        new HelpEntry(R.id.help_master_bloatware, "OEM Bloatware & Telemetry Shield in Master Opt",
            "Includes or excludes OEM Bloatware & Telemetry Shield inside your Default Master Optimization checklist.",
            "When ON (recommended), every Master Optimization run freezes Meta daemons, HeyTap cloud/MCS, SAUHelper, OneTrace & OEM analytics automatically."),
        new HelpEntry(R.id.help_boot_auto_optimize, "Open & Optimize After Reboot",
            "Listens for Android BOOT_COMPLETED after phone restart, launches Android Revamp Hub automatically, and runs your Default Master Optimization checklist as soon as Shizuku connects.",
            "Keeps charging rules, bloatware freeze, Fast Doze, Private DNS, and gestures applied cleanly after every reboot."),
        new HelpEntry(R.id.help_section_terminal, "Terminal Log Output",
            "Shows live shell output, exit diagnostics, errors, and confirmation logs for every optimization, stealth switch, and compilation command.",
            "Lets you audit exactly which ADB/UID 2000 commands ran and confirm they succeeded."),
        new HelpEntry(R.id.help_section_debugging, "Debugging & Shizuku Control",
            "Controls Developer Options, Wired USB ADB, Wireless Debugging, Shizuku service status, and offline on-phone recovery tools.",
            "Lets you manage Shizuku and restore Developer/ADB access directly on the phone without needing a computer."),
        new HelpEntry(R.id.help_shizuku_status, "Shizuku Status & Authorize",
            "Shows live binder connection to your spoofed or standard Shizuku daemon (UID 2000 privilege bridge).",
            "Revamp Hub requires active Shizuku authorization for system-level commands, package freezes, Doze tuning, and compilation."),
        new HelpEntry(R.id.help_usb_debugging, "Wired USB Debugging",
            "Toggles global 'adb_enabled' for Type-C USB cable ADB access.",
            "Needed for PC ADB file transfers or emergency commands; disable (or use Banking Stealth) when running strict banking apps."),
        new HelpEntry(R.id.help_wireless_debugging, "Wireless Debugging",
            "Toggles global 'adb_wifi_enabled' for Wi-Fi ADB pairing and cable-free Shizuku operation.",
            "Enables starting/using Shizuku without a USB cable; hidden automatically when Banking Stealth is engaged."),
        new HelpEntry(R.id.help_dev_options, "Developer Options Master",
            "Toggles 'development_settings_enabled' globally in Settings.Global.",
            "Most banking & UPI apps check this flag first. Hide during banking or keep enabled for tuning & debugging."),
        new HelpEntry(R.id.help_debug_buttons, "Quick Actions & Offline Recovery",
            "Opens Dev Settings or Shizuku directly, runs libshizuku, or restores Dev Mode/ADB offline using granted WRITE_SECURE_SETTINGS permission.",
            "Prevents lockouts after Banking Stealth so you can always recover Debugging & Shizuku right on the phone."),
        new HelpEntry(R.id.help_section_dev_tuning, "Developer Mode Tuning",
            "Adjusts persistent Developer Options system flags (ADB timeout, USB sideload scan, wireless shortcut, Miracast, and hidden Stay Awake).",
            "Saves repeated re-pairing and manual scrolling inside Android's Developer Options menu."),
        new HelpEntry(R.id.help_adb_timeout, "Disable ADB Auth Timeout",
            "Sets 'adb_allowed_connection_time' to 0 (infinite).",
            "Stops Android from revoking trusted USB debugging RSA authorizations from your computer every 7 days."),
        new HelpEntry(R.id.help_verify_adb_apps, "Verify Apps Over USB",
            "Toggles 'verifier_verify_adb_installs' for ADB/USB APK installs.",
            "Turning OFF bypasses slow Play Protect cloud scans so local APK builds install faster."),
        new HelpEntry(R.id.help_direct_wireless_adb, "Open Wireless Debugging Directly",
            "Opens the system Wireless Debugging pairing screen straight from Revamp Hub.",
            "Lets you check port numbers or pair code without digging through Settings → Developer Options."),
        new HelpEntry(R.id.help_wfd_certification, "Wireless Display Certification",
            "Toggles 'wifi_display_certification_on' in Settings.Global.",
            "Exposes extra Miracast / Wi-Fi Direct debugging controls for smart TV or external sink casting."),
        new HelpEntry(R.id.help_stay_awake, "Stay Awake While Charging (Advanced)",
            "Sets 'stay_on_while_plugged_in' across AC/USB/Wireless charging so the display never turns off while plugged in.",
            "Hidden under Advanced to prevent accidental screen burn-in; enable temporarily during long USB debugging or log runs."),
        new HelpEntry(R.id.help_section_banking, "Banking & UPI Stealth Shield",
            "Sequentially grants on-phone recovery perms, pauses Accessibility & Private DNS, cloaks modding/VPN tools, hides Dev Mode/ADB, and stops Shizuku cleanly.",
            "Strict banking & security SDKs block access if Dev Mode, ADB, VPNs, or exposed modding tools are active."),
        new HelpEntry(R.id.help_banking_actions, "Engage Stealth / Restore All / Audit",
            "Engage Full Stealth conceals your modding environment in safe order. Restore All recovers Dev Options, ADB, Accessibility, DNS, and tools right on-phone. Audit inspects every detector signal.",
            "Lets you enter and exit banking-safe mode in 1 tap without losing phone-only recovery."),
        new HelpEntry(R.id.help_shizuku_spoof, "Shizuku-Next Package Spoof",
            "Checks whether 'moe.shizuku.privileged.api' has been renamed using Shizuku-Next's built-in Stealth mode (e.g. 'com.mcore.services').",
            "Security SDKs query PackageManager even for frozen packages. Renaming eliminates package-name detection permanently while keeping phone-only restart working."),
        new HelpEntry(R.id.help_banking_mode, "Banking & UPI Stealth Mode Switch",
            "Hides Dev Options, USB/Wireless ADB, Accessibility services, Private DNS, and cloaks sideloaded modding apps while keeping Shizuku running until cloaked.",
            "Use Engage Full Stealth for full teardown including Shizuku, or use this switch directly when testing incremental flags."),
        new HelpEntry(R.id.help_shizuku_cloak, "Shizuku-Next Stealth Cloak",
            "Turns Watchdog OFF, force-stops the Shizuku-Next/spoofed app, and kills shizuku_server cleanly while leaving the app enabled.",
            "Passes runtime daemon & process scans cleanly and lets you tap Open Shizuku to restart it right from your phone."),
        new HelpEntry(R.id.help_deep_freeze, "Deep Freeze Shizuku-Next APK (Advanced)",
            "Runs 'pm disable-user --user 0', turns Watchdog OFF, and stops shizuku_server. Unfreezing requires 'adb shell pm enable <pkg>' from a PC.",
            "Hidden intentionally: only needed if a strict app still flags Shizuku even after Package Spoof + Stealth Cloak."),
        new HelpEntry(R.id.help_certs, "User CA Certificates",
            "Scans user-installed root CA certificates and opens Android's Trusted Credentials UI.",
            "Proxy or HTTPS-filtering root certificates can trigger MITM warnings in sensitive banking/corporate apps."),
        new HelpEntry(R.id.help_risk_apps, "Sideloaded / Modding Apps Cloak",
            "Scans installed modding, root, injector, or ad-blocking apps (ReVanced, AdGuard, WireGuard, Tailscale, installers, etc.) and lets you freeze/unfreeze them all or individually.",
            "Collapsed until needed so you can cloak specific detected tools before opening sensitive banking apps and uncloak right after."),
        new HelpEntry(R.id.help_section_shields, "System Shields & Connectivity",
            "Configures system-wide AdGuard DNS ad-blocking, wired USB-only Android Auto stabilization, and OEM bloatware/telemetry freezing.",
            "Improves privacy, removes in-app ads, prevents car projection dropouts, and eliminates background telemetry without a VPN."),
        new HelpEntry(R.id.help_private_dns, "AdGuard Private DNS Shield",
            "Sets private DNS mode to 'hostname' with 'dns.adguard-dns.com' over DNS-over-TLS.",
            "Blocks ads and analytics domains across browsers and apps with zero local VPN battery overhead."),
        new HelpEntry(R.id.help_aa_cable_only, "Android Auto Cable-Only Mode",
            "Revokes Nearby Wi-Fi / hotspot permissions on com.google.android.projection.gearhead, blocks wireless handshakes, and whitelists AA in Doze.",
            "Stops wireless projection interference and keeps wired USB Android Auto rock-solid with minimal heat."),
        new HelpEntry(R.id.help_bloatware_shield, "OEM Bloatware & Telemetry Shield",
            "Freezes Meta services, HeyTap Cloud/MCS/Browser/Market, SAUHelper, OPlus logkit/onetrace/statistics, and disables error reporting telemetry.",
            "Cuts silent background analytics uploads, RAM waste, and OEM wakeups on OxygenOS/ColorOS."),
        new HelpEntry(R.id.help_section_battery_health, "Battery Health & Charging Protection",
            "Configures OPlus kernel 80% charging cutoffs, motherboard hardware bypass charging, and guards against overnight Smart Charging overrides.",
            "Protects long-term battery capacity from high-voltage degradation and charging heat while plugged in."),
        new HelpEntry(R.id.help_charge_limit, "80% Custom Charge Limit",
            "Enables OxygenOS/ColorOS 'regular_charge_protection_switch_state' at an 80% ceiling.",
            "Holding lithium-ion cells below ~80-85% drastically extends multi-year cycle health."),
        new HelpEntry(R.id.help_bypass_charging, "Hardware Bypass Charging",
            "Routes wall/car charger power directly to the motherboard once battery reaches ≥80% (0 mA cell charging current).",
            "Eliminates battery heat and charge-cycle wear while gaming or running navigation in your car."),
        new HelpEntry(R.id.help_smart_charging, "Smart Charging Protection (Advanced)",
            "Controls ColorOS AI overnight trickle-to-100% learning.",
            "Hidden & kept OFF by default because Smart Charging can override your strict 80% limit overnight."),
        new HelpEntry(R.id.help_section_power_tuning, "Battery & Power Tuning",
            "Tunes Android Doze timers, cellular modem standby on Wi-Fi, FHD+ vs Native scaling, Smart AOD, drainer standby buckets, and hidden Extreme Refresh lock.",
            "Maximizes screen-off & active battery endurance while keeping urgent notifications instant."),
        new HelpEntry(R.id.help_restrict_drainers, "Restrict Aggressive Background Drainers",
            "Moves heavy social/OEM background apps into RESTRICTED standby bucket & denies background execution.",
            "Prevents unnecessary background wakeups and telemetry polling without breaking apps when opened."),
        new HelpEntry(R.id.help_fast_doze, "Fast Doze & Instant Standby",
            "Tunes 'device_idle_constants' so deep Doze standby engages 60s after screen-off.",
            "Cuts pocket and desk overnight drain compared to stock ~30 minute wait windows."),
        new HelpEntry(R.id.help_cellular_sleep, "Cellular Modem Sleep on Wi-Fi",
            "Turns OFF 'mobile_data_always_on' when connected to Wi-Fi.",
            "Puts the 5G/4G data modem to sleep indoors on Wi-Fi, lowering heat and standby radio drain (calls/SMS stay live)."),
        new HelpEntry(R.id.help_fhd_resolution, "Screen Resolution: Standard (FHD+)",
            "Switches OxygenOS/ColorOS dynamic resolution between Standard FHD+ and Native High Panel Resolution.",
            "FHD+ reduces GPU pixel workload and extends battery while preserving smooth gestures."),
        new HelpEntry(R.id.help_aod_smart_mode, "Smart AOD & Deep Standby",
            "Configures Always-On Display for pickup/tap arousal only and enables deep standby sleep.",
            "Keeps the OLED display completely off on your desk instead of draining power displaying an all-day clock."),
        new HelpEntry(R.id.help_critical_apps, "Verify Critical Apps Whitelist",
            "Adds Messaging, Phone, Contacts, SMS, Alarms, Android Auto, and Shizuku to Doze whitelist & active standby.",
            "Guarantees calls, OTP SMS, and instant messaging never delay even with Fast Doze enabled."),
        new HelpEntry(R.id.help_high_refresh, "Extreme Refresh Rate Lock (Advanced)",
            "Locks peak/minimum refresh rate instead of adaptive LTPO scaling.",
            "Hidden by default to protect battery life & thermals; unlock temporarily only for peak-Hz supported gaming."),
        new HelpEntry(R.id.help_section_ui_gestures, "System UI & Gestures",
            "Controls Weather CPU Tamer, animation scales, Developer Options Smallest Width (Drawing DPI) presets, and Full-Screen Navigation Gestures.",
            "Combines responsiveness, layout density tuning (mobile vs tablet view), and reliable edge gestures in one place."),
        new HelpEntry(R.id.help_weather_tamer, "Weather CPU Tamer",
            "Sets OEM Weather background run & wake-lock ops to ignore and restricts standby bucket.",
            "Fixes an OEM bug where stock Weather wakes the CPU and polls GPS silently in the background."),
        new HelpEntry(R.id.help_animation_scale, "System Animation Scale",
            "Sets Window, Transition, and Animator duration scales cleanly via Settings.Global.",
            "0.5x doubles UI speed while staying fluid; 0.0x disables animations completely; 1.0x restores stock."),
        new HelpEntry(R.id.help_smallest_width, "Smallest Width (Drawing Scaling)",
            "Sets 'wm density' cleanly from your physical screen resolution to hit exact shortest screen width in dp across Mobile View (360, 420, 480, 520 dp) and Tablet View (560, 580 dp), or resets Stock.",
            "Matches Developer Options → Drawing → Smallest width without math. Lower dp enlarges UI/text; 560/580 dp triggers compact tablet two-pane layouts in supported apps."),
        new HelpEntry(R.id.help_gesture_nav, "Full-Screen Gestures Shield",
            "Enforces AOSP gestural overlay (mode 2 / ColorOS hide_navigationbar_enable=3) and side-back gesture insets.",
            "Repairs broken swipe-back or pill gestures after resolution/DPI changes or launcher switches."),
        new HelpEntry(R.id.help_section_daily_care, "Daily Health Check & Auto-Care",
            "Runs scheduled background maintenance daily at your chosen time via exact AlarmManager using Shizuku.",
            "Keeps newly installed apps compiled, bloatware frozen, drainers restricted, and flash trimmed automatically."),
        new HelpEntry(R.id.help_daily_autocare, "Daily Auto-Care Service",
            "Arms/cancels DailyAlarmReceiver (survives reboots via BootReceiver) and schedules your daily run time.",
            "Automates daily maintenance so performance stays fresh without manual taps."),
        new HelpEntry(R.id.help_daily_custom, "Daily Default vs One-Time Tasks",
            "Daily Default configures the tasks executed every scheduled day. One-Time Override lets you pick checkboxes from all available tasks either to Run Once Immediately OR queue just for the next scheduled daily alarm (automatically reverting to Daily Default afterwards).",
            "Lets you customize what Daily Auto-Care maintains persistently or run/schedule a one-off task set safely."),
        new HelpEntry(R.id.help_section_perf_compile, "Performance & Native Compilation",
            "Runs ART dex2oat AOT native speed compilation ('-m speed') and flash storage fstrim.",
            "Eliminates runtime JIT stutters, speeds up cold app launches, and maintains storage I/O speed after OS updates."),
        new HelpEntry(R.id.help_perf_actions, "Compile Core / Compile ALL / Flash Trim",
            "Compile Core targets key daily apps (~15s). Compile ALL compiles all 3rd-party user apps + SystemUI (~2-4m). Flash Trim runs 'sm fstrim' & clears 1 GB system caches.",
            "Run Compile ALL right after an OTA or batch Play Store updates for peak smoothness."),
        new HelpEntry(R.id.help_section_storage_cleanup, "Manual Storage Cleanup",
            "Scans /sdcard/Download for leftover .apk, .apkm, .xapk installers and large files (>25 MB) with individual Delete prompts.",
            "Safely recovers gigabytes of leftover downloads — zero automated deletion ever happens."),
        new HelpEntry(R.id.help_scan_storage, "Scan Downloaded APKs & Large Files",
            "Lists large files and APK installers sorted by size with per-item confirmation delete buttons.",
            "Lets you clean forgotten installers cleanly without risking personal media or documents.")
    };

    private RevampHelpCatalog() {}

    public static void bindAll(final Activity activity) {
        for (final HelpEntry entry : ENTRIES) {
            View v = activity.findViewById(entry.viewId);
            if (v != null) {
                v.setOnClickListener(view -> new AlertDialog.Builder(activity)
                    .setTitle(entry.title)
                    .setMessage("What it does:\n" + entry.what + "\n\nWhy use it:\n" + entry.why)
                    .setPositiveButton("Got It", null)
                    .show());
            }
        }
    }
}
