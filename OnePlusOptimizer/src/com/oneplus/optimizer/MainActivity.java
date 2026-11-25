package com.oneplus.optimizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    private static final int SHIZUKU_REQUEST_CODE = 1001;

    // 1. Hardware Specs Views
    private TextView mTvDeviceHeaderBadge;
    private TextView mTvSpecChip;
    private TextView mTvSpecSoc;
    private TextView mTvSpecRam;
    private TextView mTvSpecStorage;
    private TextView mTvSpecDisplay;
    private TextView mTvSpecBattery;
    private TextView mTvSpecOs;

    // 2. Metrics Views
    private TextView mTvMetricRam;
    private TextView mTvMetricBattery;
    private TextView mTvMetricStorage;
    private Button mBtnMetricTrimCache;
    private Button mBtnMetricFlushRam;

    // 3. Debugging & Shizuku Control Views
    private TextView mTvShizukuStatus;
    private Button mBtnShizukuAuth;
    private Switch mSwitchUsbDebugging;
    private Switch mSwitchWirelessDebugging;
    private Switch mSwitchDevOptions;
    private Button mBtnOpenDevSettings;
    private Button mBtnOpenShizukuApp;
    private Button mBtnStartShizukuDirect;
    private Button mBtnEnableDevMode;
    private Button mBtnRestoreShizuku;

    // 4. Developer Mode Tuning Views (New Section)
    private Switch mSwitchAdbTimeout;
    private Switch mSwitchVerifyAdbApps;
    private Button mBtnDirectWirelessAdb;
    private Switch mSwitchWfdCertification;
    private Switch mSwitchStayAwake;

    // 5. Banking & UPI Stealth Views
    private Button mBtnEngageFullStealth;
    private Button mBtnExitFullStealth;
    private Button mBtnAuditStealth;
    private TextView mTvShizukuSpoofBadge;
    private TextView mTvShizukuSpoofStatus;
    private Button mBtnSpoofShizuku;
    private Switch mSwitchBankingMode;
    private TextView mTvBankingStatus;
    private TextView mTvBankingBadge;
    private Switch mSwitchShizukuCloak;
    private Switch mSwitchShizukuDeepFreeze;
    private TextView mTvCertStatus;
    private Button mBtnManageCerts;
    private TextView mTvRiskStatus;
    private TextView mTvRiskBadge;
    private Button mBtnCloakAllRisk;
    private Button mBtnUncloakAllRisk;
    private LinearLayout mContainerRiskApps;

    // 5. System Shields & Connectivity Views
    private Switch mSwitchPrivateDns;
    private Switch mSwitchAaCableOnly;
    private Button mBtnOpenAaSettings;
    private Switch mSwitchBloatwareShield;
    private Button mBtnCleanBloatware;

    // 6. Master Optimize
    private Button mBtnMasterOptimize;

    // 7. Battery Health & Charging Views
    private TextView mTvBypassStatus;
    private Switch mSwitchChargeLimit;
    private Switch mSwitchSmartCharging;
    private Switch mSwitchBypassCharging;
    private Button mBtnEnforceCharging;

    // 8. Battery & Power Tuning Views
    private Button mBtnRestrictDrainers;
    private Switch mSwitchFastDoze;
    private Switch mSwitchCellularSleep;
    private Switch mSwitchFhdResolution;
    private TextView mTvResolutionDesc;
    private Switch mSwitchHighRefresh;
    private Switch mSwitchAodSmartMode;
    private Button mBtnShieldCriticalApps;

    // 9. System UI & Gestures Views
    private Switch mSwitchWeatherTamer;
    private Button mBtnAnim0, mBtnAnim05, mBtnAnim1;
    private Switch mSwitchGestureNav;
    private Button mBtnRestoreGestures;
    private Button mBtnGesturesInfo;

    // 10. Daily Automation Views
    private Switch mSwitchDailyAutocare;
    private Button mBtnPickTime;

    // 11. Performance & Native Compilation Views
    private Button mBtnCompileCore;
    private Button mBtnCompileAllApps;
    private Button mBtnTrimStorage;

    // 12. Manual Storage Cleanup Views (Strictly Last)
    private Button mBtnScanStorage;
    private TextView mTvStorageScanStatus;
    private LinearLayout mContainerStorageFiles;

    // 13. Console Views
    private TextView mTvConsole;
    private ScrollView mScrollConsole;
    private TextView mTvClearLog;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mUpdatingSwitches = false;

    private final Shizuku.OnRequestPermissionResultListener mPermissionListener =
            (requestCode, grantResult) -> {
                if (requestCode == SHIZUKU_REQUEST_CODE) {
                    mHandler.post(this::checkAndRefreshStatus);
                }
            };

    private final Shizuku.OnBinderReceivedListener mBinderReceivedListener = () -> {
        mHandler.post(this::checkAndRefreshStatus);
    };

    private final Shizuku.OnBinderDeadListener mBinderDeadListener = () -> {
        mHandler.post(this::checkAndRefreshStatus);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                () -> finish()
            );
        }

        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.root_scroll);
        View content = findViewById(R.id.content_container);
        if (root != null) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBar = 0;
                int navBar = 0;
                int leftInset = 0;
                int rightInset = 0;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    android.graphics.Insets sb = insets.getInsets(
                        android.view.WindowInsets.Type.statusBars() | 
                        android.view.WindowInsets.Type.displayCutout()
                    );
                    android.graphics.Insets nb = insets.getInsets(
                        android.view.WindowInsets.Type.navigationBars()
                    );
                    statusBar = sb.top;
                    navBar = nb.bottom;
                    leftInset = sb.left;
                    rightInset = sb.right;
                } else {
                    statusBar = insets.getSystemWindowInsetTop();
                    navBar = insets.getSystemWindowInsetBottom();
                    leftInset = insets.getSystemWindowInsetLeft();
                    rightInset = insets.getSystemWindowInsetRight();
                }

                float density = getResources().getDisplayMetrics().density;
                int sidePad = (int) (16 * density);
                int topPad = statusBar + (int) (14 * density);
                int bottomPad = navBar + (int) (24 * density);

                if (content != null) {
                    content.setPadding(sidePad + leftInset, topPad, sidePad + rightInset, bottomPad);
                }
                v.setPadding(0, 0, 0, 0);
                return insets;
            });
        }

        initViews();
        setupListeners();

        try {
            Shizuku.addBinderReceivedListenerSticky(mBinderReceivedListener);
            Shizuku.addBinderDeadListener(mBinderDeadListener);
            Shizuku.addRequestPermissionResultListener(mPermissionListener);
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndRefreshStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Shizuku.removeBinderReceivedListener(mBinderReceivedListener);
            Shizuku.removeBinderDeadListener(mBinderDeadListener);
            Shizuku.removeRequestPermissionResultListener(mPermissionListener);
        } catch (Throwable ignored) {}
    }

    private void initViews() {
        // 1. Hardware Specs
        mTvDeviceHeaderBadge = findViewById(R.id.tv_device_header_badge);
        mTvSpecChip = findViewById(R.id.tv_spec_chip);
        mTvSpecSoc = findViewById(R.id.tv_spec_soc);
        mTvSpecRam = findViewById(R.id.tv_spec_ram);
        mTvSpecStorage = findViewById(R.id.tv_spec_storage);
        mTvSpecDisplay = findViewById(R.id.tv_spec_display);
        mTvSpecBattery = findViewById(R.id.tv_spec_battery);
        mTvSpecOs = findViewById(R.id.tv_spec_os);
        populateDeviceHardwareOffline();

        // 2. Metrics
        mTvMetricRam = findViewById(R.id.tv_metric_ram);
        mTvMetricBattery = findViewById(R.id.tv_metric_battery);
        mTvMetricStorage = findViewById(R.id.tv_metric_storage);
        mBtnMetricTrimCache = findViewById(R.id.btn_metric_trim_cache);
        mBtnMetricFlushRam = findViewById(R.id.btn_metric_flush_ram);

        // 3. Debugging & Shizuku Control
        mTvShizukuStatus = findViewById(R.id.tv_shizuku_status);
        mBtnShizukuAuth = findViewById(R.id.btn_shizuku_auth);
        mSwitchUsbDebugging = findViewById(R.id.switch_usb_debugging);
        mSwitchWirelessDebugging = findViewById(R.id.switch_wireless_debugging);
        mSwitchDevOptions = findViewById(R.id.switch_dev_options);
        mBtnOpenDevSettings = findViewById(R.id.btn_open_dev_settings);
        mBtnOpenShizukuApp = findViewById(R.id.btn_open_shizuku_app);
        mBtnStartShizukuDirect = findViewById(R.id.btn_start_shizuku_direct);
        mBtnEnableDevMode = findViewById(R.id.btn_enable_dev_mode);
        mBtnRestoreShizuku = findViewById(R.id.btn_restore_shizuku);

        // 4. Developer Mode Tuning (New Section)
        mSwitchAdbTimeout = findViewById(R.id.switch_adb_timeout);
        mSwitchVerifyAdbApps = findViewById(R.id.switch_verify_adb_apps);
        mBtnDirectWirelessAdb = findViewById(R.id.btn_direct_wireless_adb);
        mSwitchWfdCertification = findViewById(R.id.switch_wfd_certification);
        mSwitchStayAwake = findViewById(R.id.switch_stay_awake);

        // 5. Banking & UPI Stealth
        mBtnEngageFullStealth = findViewById(R.id.btn_engage_full_stealth);
        mBtnExitFullStealth = findViewById(R.id.btn_exit_full_stealth);
        mBtnAuditStealth = findViewById(R.id.btn_audit_stealth);
        mTvShizukuSpoofBadge = findViewById(R.id.tv_shizuku_spoof_badge);
        mTvShizukuSpoofStatus = findViewById(R.id.tv_shizuku_spoof_status);
        mBtnSpoofShizuku = findViewById(R.id.btn_spoof_shizuku);
        mSwitchBankingMode = findViewById(R.id.switch_banking_mode);
        mTvBankingStatus = findViewById(R.id.tv_banking_status);
        mTvBankingBadge = findViewById(R.id.tv_banking_badge);
        mSwitchShizukuCloak = findViewById(R.id.switch_shizuku_cloak);
        mSwitchShizukuDeepFreeze = findViewById(R.id.switch_shizuku_deep_freeze);
        mTvCertStatus = findViewById(R.id.tv_cert_status);
        mBtnManageCerts = findViewById(R.id.btn_manage_certs);
        mTvRiskStatus = findViewById(R.id.tv_risk_apps_status);
        mTvRiskBadge = findViewById(R.id.tv_risk_badge);
        mBtnCloakAllRisk = findViewById(R.id.btn_cloak_all_risk);
        mBtnUncloakAllRisk = findViewById(R.id.btn_uncloak_all_risk);
        mContainerRiskApps = findViewById(R.id.container_risk_apps);

        // 5. System Shields & Connectivity
        mSwitchPrivateDns = findViewById(R.id.switch_private_dns);
        mSwitchAaCableOnly = findViewById(R.id.switch_aa_cable_only);
        mBtnOpenAaSettings = findViewById(R.id.btn_open_aa_settings);
        mSwitchBloatwareShield = findViewById(R.id.switch_bloatware_shield);
        mBtnCleanBloatware = findViewById(R.id.btn_clean_bloatware);

        // 6. Master Optimize
        mBtnMasterOptimize = findViewById(R.id.btn_master_optimize);

        // 7. Battery Health & Charging
        mTvBypassStatus = findViewById(R.id.tv_bypass_status);
        mSwitchChargeLimit = findViewById(R.id.switch_charge_limit);
        mSwitchSmartCharging = findViewById(R.id.switch_smart_charging);
        mSwitchBypassCharging = findViewById(R.id.switch_bypass_charging);
        mBtnEnforceCharging = findViewById(R.id.btn_enforce_charging);

        // 8. Battery & Power Tuning
        mBtnRestrictDrainers = findViewById(R.id.btn_restrict_drainers);
        mSwitchFastDoze = findViewById(R.id.switch_fast_doze);
        mSwitchCellularSleep = findViewById(R.id.switch_cellular_sleep);
        mSwitchFhdResolution = findViewById(R.id.switch_fhd_resolution);
        mTvResolutionDesc = findViewById(R.id.tv_resolution_desc);
        mSwitchHighRefresh = findViewById(R.id.switch_high_refresh);
        mSwitchAodSmartMode = findViewById(R.id.switch_aod_smart_mode);
        mBtnShieldCriticalApps = findViewById(R.id.btn_shield_critical_apps);

        // 9. System UI & Gestures
        mSwitchWeatherTamer = findViewById(R.id.switch_weather_tamer);
        mBtnAnim0 = findViewById(R.id.btn_anim_0);
        mBtnAnim05 = findViewById(R.id.btn_anim_05);
        mBtnAnim1 = findViewById(R.id.btn_anim_1);
        mSwitchGestureNav = findViewById(R.id.switch_gesture_navigation);
        mBtnRestoreGestures = findViewById(R.id.btn_restore_gestures);
        mBtnGesturesInfo = findViewById(R.id.btn_gestures_info);

        // 10. Daily Automation
        mSwitchDailyAutocare = findViewById(R.id.switch_daily_autocare);
        mBtnPickTime = findViewById(R.id.btn_pick_time);

        // 11. Performance & Native Compilation
        mBtnCompileCore = findViewById(R.id.btn_compile_core);
        mBtnCompileAllApps = findViewById(R.id.btn_compile_all_apps);
        mBtnTrimStorage = findViewById(R.id.btn_trim_storage);

        // 12. Manual Storage Cleanup (Strictly Last)
        mBtnScanStorage = findViewById(R.id.btn_scan_storage);
        mTvStorageScanStatus = findViewById(R.id.tv_storage_scan_status);
        mContainerStorageFiles = findViewById(R.id.container_storage_files);

        // 13. Console
        mTvConsole = findViewById(R.id.tv_console);
        mScrollConsole = findViewById(R.id.scroll_console);
        mTvClearLog = findViewById(R.id.tv_clear_log);

        SharedPreferences prefs = getSharedPreferences(DailyAlarmReceiver.PREF_NAME, Context.MODE_PRIVATE);
        boolean dailyActive = prefs.getBoolean(DailyAlarmReceiver.PREF_DAILY_ENABLED, true);
        int hour = prefs.getInt("pref_daily_hour", 3);
        int minute = prefs.getInt("pref_daily_minute", 0);
        mSwitchDailyAutocare.setChecked(dailyActive);
        updateScheduleTimeButton(hour, minute);
        if (dailyActive) {
            DailyAlarmReceiver.scheduleNextAlarm(this);
        }
    }

    private void setupListeners() {
        mBtnShizukuAuth.setOnClickListener(v -> requestShizuku());
        mTvClearLog.setOnClickListener(v -> mTvConsole.setText("Console cleared.\nReady."));

        // Touch isolation for console
        mScrollConsole.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        // 2. Metrics Quick Buttons
        mBtnMetricTrimCache.setOnClickListener(v -> runAsync("Trim Cache", service -> {
            logConsole("🧹 Trimming Dalvik and system cache...");
            String res = service.runTrimCache();
            logConsole(res);
            refreshMetrics(service);
        }));

        mBtnMetricFlushRam.setOnClickListener(v -> runAsync("Flush RAM", service -> {
            logConsole("⚡ Flushing background cached processes...");
            String res = service.runFlushRam();
            logConsole(res);
            refreshMetrics(service);
        }));

        // 3. Debugging & Shizuku Control Listeners
        mSwitchUsbDebugging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            try {
                android.provider.Settings.Global.putInt(getContentResolver(),
                    android.provider.Settings.Global.ADB_ENABLED, isChecked ? 1 : 0);
            } catch (Exception ignored) {}

            if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                runAsync("USB Debugging Toggle", service -> {
                    boolean active = service.setUsbDebuggingEnabled(isChecked);
                    logConsole(active ? "✔ Wired USB Debugging ENABLED (adb_enabled=1)" : "⚠ Wired USB Debugging DISABLED (Security mode)");
                });
            } else {
                logConsole(isChecked ? "✔ Wired USB Debugging requested ON" : "• Wired USB Debugging requested OFF");
            }
        });

        mSwitchWirelessDebugging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            try {
                android.provider.Settings.Global.putInt(getContentResolver(),
                    "adb_wifi_enabled", isChecked ? 1 : 0);
            } catch (Exception ignored) {}

            if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                runAsync("Wireless Debugging Toggle", service -> {
                    boolean active = service.setWirelessDebuggingEnabled(isChecked);
                    logConsole(active ? "✔ Wireless Debugging ENABLED (Port active for ADB)" : "⚠ Wireless Debugging DISABLED (Security mode)");
                });
            } else {
                logConsole(isChecked ? "✔ Wireless Debugging requested ON" : "• Wireless Debugging requested OFF");
            }
        });

        mSwitchDevOptions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            try {
                android.provider.Settings.Global.putInt(getContentResolver(),
                    android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, isChecked ? 1 : 0);
            } catch (Exception ignored) {}

            if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                runAsync("Developer Options Toggle", service -> {
                    boolean active = service.setDevOptionsEnabled(isChecked);
                    logConsole(active ? "✔ Developer Options Master ENABLED (development_settings_enabled=1)" : "⚠ Developer Options Master DISABLED (development_settings_enabled=0)");
                });
            } else {
                logConsole(isChecked ? "✔ Developer Options requested ON" : "• Developer Options requested OFF");
                if (isChecked) {
                    try {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                    } catch (Exception ignored) {}
                }
            }
        });

        mBtnOpenDevSettings.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                logConsole("⚠ Unable to open Developer Settings: " + e.getMessage());
            }
        });

        mBtnOpenShizukuApp.setOnClickListener(v -> openShizukuApp());

        mBtnStartShizukuDirect.setOnClickListener(v -> {
            if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                runAsync("Start Shizuku Server", service -> {
                    logConsole("⚡ Starting Shizuku server directly via native libshizuku.so...");
                    String res = service.startShizukuServerDirect();
                    logConsole("Result: " + res);
                    mHandler.post(this::checkAndRefreshStatus);
                });
            } else {
                restoreShizukuAppOrGuide();
            }
        });

        mBtnEnableDevMode.setOnClickListener(v -> {
            boolean success = enableDevAndAdbOffline();

            if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                runAsync("Enable Dev Mode", service -> {
                    service.setDevOptionsEnabled(true);
                    service.setUsbDebuggingEnabled(true);
                    service.setWirelessDebuggingEnabled(true);
                    logConsole("✔ Developer Options & Debugging turned ON via Shizuku!");
                    mHandler.post(this::checkAndRefreshStatus);
                });
            } else {
                if (!success) {
                    new AlertDialog.Builder(this)
                        .setTitle("Enable Developer Options")
                        .setMessage("To re-enable Developer Options on OxygenOS 15:\n\n" +
                                    "1. Tap 'Open About Device' below.\n" +
                                    "2. Tap 'Version', then tap 'Build number' 7 times rapidly.\n" +
                                    "3. Return to this app and tap 'Dev Settings' to turn on USB & Wireless Debugging.")
                        .setPositiveButton("Open About Device", (dialog, which) -> {
                            try {
                                startActivity(new Intent(android.provider.Settings.ACTION_DEVICE_INFO_SETTINGS));
                            } catch (Exception e) {
                                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                } else {
                    try {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception ignored) {}
                }
            }
        });

        mBtnRestoreShizuku.setOnClickListener(v -> performRestoreAll(false));

        // 4. Developer Mode Tuning Listeners
        mSwitchAdbTimeout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            try {
                android.provider.Settings.Global.putInt(getContentResolver(),
                    "adb_allowed_connection_time", isChecked ? 0 : 604800000);
            } catch (Exception ignored) {}
            runAsync("ADB Auth Timeout Toggle", service -> {
                boolean disabled = service.setAdbAuthTimeoutDisabled(isChecked);
                logConsole(disabled ? "✔ ADB Auth Timeout DISABLED (Authorizations will never expire)" 
                                    : "• ADB Auth Timeout Standard (7-day timeout active)");
            });
        });

        mSwitchVerifyAdbApps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            try {
                android.provider.Settings.Global.putInt(getContentResolver(),
                    "verifier_verify_adb_installs", isChecked ? 1 : 0);
            } catch (Exception ignored) {}
            runAsync("Verify ADB Apps Toggle", service -> {
                boolean enabled = service.setVerifyAdbAppsEnabled(isChecked);
                logConsole(enabled ? "✔ Verify Apps Over USB ENABLED (Play Protect scans ADB APKs)" 
                                   : "• Verify Apps Over USB DISABLED (Bypasses verification during ADB install)");
            });
        });

        mBtnDirectWirelessAdb.setOnClickListener(v -> {
            try {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings$DevelopmentSettingsDashboardActivity");
                intent.putExtra(":settings:fragment_args_key", "enable_adb_wireless");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                logConsole("✔ Opening Wireless Debugging Settings directly...");
            } catch (Exception e1) {
                try {
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                } catch (Exception e2) {
                    logConsole("⚠ Unable to open Wireless Debugging: " + e2.getMessage());
                }
            }
        });

        mSwitchWfdCertification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            try {
                android.provider.Settings.Global.putInt(getContentResolver(),
                    "wifi_display_certification_on", isChecked ? 1 : 0);
            } catch (Exception ignored) {}
            runAsync("WFD Certification Toggle", service -> {
                boolean enabled = service.setWifiDisplayCertEnabled(isChecked);
                logConsole(enabled ? "✔ Wireless Display Certification ENABLED (Miracast certification active)" 
                                   : "• Wireless Display Certification DISABLED");
            });
        });

        mSwitchStayAwake.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            try {
                android.provider.Settings.Global.putInt(getContentResolver(),
                    "stay_on_while_plugged_in", isChecked ? 7 : 0);
            } catch (Exception ignored) {}
            runAsync("Stay Awake Toggle", service -> {
                boolean stayAwake = service.setStayAwakeWhileCharging(isChecked);
                logConsole(stayAwake ? "✔ Stay Awake While Charging ENABLED (Screen never sleeps while plugged in)" 
                                     : "• Stay Awake While Charging DISABLED (Normal screen timeout)");
            });
        });

        // 5. Banking & UPI Stealth Mode Listeners
        mBtnEngageFullStealth.setOnClickListener(v -> showEngageStealthDialog());
        mBtnExitFullStealth.setOnClickListener(v -> performRestoreAll(true));
        mBtnAuditStealth.setOnClickListener(v -> runStealthAudit());
        mBtnSpoofShizuku.setOnClickListener(v -> showShizukuSpoofGuideDialog());

        mBtnCloakAllRisk.setOnClickListener(v -> runAsync("Cloak All Risk Apps", service -> {
            logConsole("🔒 Cloaking all detected sideloaded / modding apps...");
            service.cloakAllRiskApps(true);
            logConsole("✔ All detected risk apps cloaked & frozen.");
            String json = service.getRiskAppsDetailedJson();
            mHandler.post(() -> populateRiskAppsList(json, service));
        }));

        mBtnUncloakAllRisk.setOnClickListener(v -> runAsync("Uncloak All Risk Apps", service -> {
            logConsole("🔓 Uncloaking all detected sideloaded / modding apps...");
            service.cloakAllRiskApps(false);
            logConsole("✔ All detected risk apps uncloaked & restored.");
            String json = service.getRiskAppsDetailedJson();
            mHandler.post(() -> populateRiskAppsList(json, service));
        }));
        mSwitchBankingMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            if (!ShizukuManager.isShizukuAvailable() || !ShizukuManager.hasShizukuPermission()) {
                mUpdatingSwitches = true;
                mSwitchBankingMode.setChecked(!isChecked);
                mUpdatingSwitches = false;
                if (!isChecked) {
                    performRestoreAll(true);
                } else {
                    showEngageStealthDialog();
                }
                return;
            }
            runAsync("Banking Mode Toggle", service -> {
                boolean active = service.setBankingModeEnabled(isChecked);
                String riskReport = service.getDetectedRiskApps();
                String detailedRiskJson = service.getRiskAppsDetailedJson();
                mHandler.post(() -> {
                    if (mTvBankingStatus != null) {
                        mTvBankingStatus.setText(active ? "Active: Dev Mode & Sideloaded Apps Hidden (Safe for Banking)" : "Standby: Dev Mode & Apps Visible");
                    }
                    if (mTvBankingBadge != null) {
                        mTvBankingBadge.setText(active ? "STEALTH" : "STANDBY");
                        mTvBankingBadge.setTextColor(active ? Color.parseColor("#00E676") : Color.parseColor("#FF9100"));
                    }
                    updateRiskAppsUi(riskReport);
                    populateRiskAppsList(detailedRiskJson, service);
                    checkAndRefreshStatus();
                });
                logConsole(active ? "✔ Banking Stealth ACTIVE: Dev Options concealed, Accessibility & DNS paused, modding/VPN apps cloaked!"
                                  : "• Banking Stealth STANDBY: Dev Options, ADB, Accessibility, Private DNS & tools restored.");
            });
        });

        mSwitchShizukuCloak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            SharedPreferences cloakPrefs = getSharedPreferences("revamp_stealth_prefs", Context.MODE_PRIVATE);
            cloakPrefs.edit().putBoolean("pref_shizuku_cloaked", isChecked).apply();
            if (isChecked) {
                if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                    runAsync("Shizuku Cloaking Toggle", service -> {
                        boolean cloaked = service.setShizukuCloaked(true);
                        ShizukuManager.clearCachedService();
                        logConsole(cloaked ? "✔ Shizuku-Next Stealth Cloak ACTIVE: Watchdog OFF, app force-stopped, shizuku_server killed."
                                           : "⚠ Failed to schedule Shizuku Cloak.");
                        mHandler.postDelayed(this::checkAndRefreshStatus, 900);
                    });
                } else {
                    logConsole("✔ Shizuku-Next is already stopped/cloaked.");
                    mHandler.post(this::checkAndRefreshStatus);
                }
            } else {
                if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                    runAsync("Shizuku Uncloak", service -> {
                        service.setShizukuCloaked(false);
                        logConsole("• Shizuku-Next Stealth Cloak OFF: appops restored & server restarted.");
                        mHandler.postDelayed(this::checkAndRefreshStatus, 800);
                    });
                } else {
                    restoreShizukuAppOrGuide();
                }
            }
        });

        mSwitchShizukuDeepFreeze.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            final String resolvedPkg = ShizukuManager.resolveShizukuPackage(this);
            if (isChecked) {
                if (!ShizukuManager.isShizukuAvailable() || !ShizukuManager.hasShizukuPermission()) {
                    mUpdatingSwitches = true;
                    mSwitchShizukuDeepFreeze.setChecked(false);
                    mUpdatingSwitches = false;
                    showStartShizukuForDeepFreezeDialog();
                    return;
                }
                new AlertDialog.Builder(this)
                    .setTitle("Deep Freeze Shizuku APK?")
                    .setMessage("This runs 'pm disable-user --user 0 " + resolvedPkg + "', disables Watchdog, and kills shizuku_server in one step (no need to toggle Stealth Cloak first).\n\nNote: If any bank still detects Shizuku, use 'Setup Shizuku-Next Package Spoof' instead — pm disable-user does NOT hide moe.shizuku.privileged.api from PackageManager!\n\nTo unfreeze later: adb shell pm enable " + resolvedPkg)
                    .setPositiveButton("Deep Freeze", (d, w) -> {
                        SharedPreferences cloakPrefs = getSharedPreferences("revamp_stealth_prefs", Context.MODE_PRIVATE);
                        cloakPrefs.edit().putBoolean("pref_shizuku_cloaked", true).apply();
                        runAsync("Shizuku Deep Freeze", service -> {
                            service.setShizukuDeepFreeze(true);
                            ShizukuManager.clearCachedService();
                            logConsole("✔ Shizuku DEEP FROZEN (" + resolvedPkg + "): package disabled, Watchdog OFF, shizuku_server killed.");
                            mHandler.postDelayed(this::checkAndRefreshStatus, 900);
                        });
                    })
                    .setNegativeButton("Cancel", (d, w) -> {
                        mUpdatingSwitches = true;
                        mSwitchShizukuDeepFreeze.setChecked(false);
                        mUpdatingSwitches = false;
                    })
                    .setCancelable(false)
                    .show();
            } else {
                if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                    SharedPreferences cloakPrefs = getSharedPreferences("revamp_stealth_prefs", Context.MODE_PRIVATE);
                    cloakPrefs.edit().putBoolean("pref_shizuku_cloaked", false).apply();
                    runAsync("Shizuku Unfreeze", service -> {
                        service.setShizukuDeepFreeze(false);
                        logConsole("• Shizuku (" + resolvedPkg + ") unfrozen & restarted.");
                        mHandler.postDelayed(this::checkAndRefreshStatus, 800);
                    });
                } else {
                    mUpdatingSwitches = true;
                    mSwitchShizukuDeepFreeze.setChecked(isShizukuPackageDisabled());
                    mUpdatingSwitches = false;
                    showAdbUnfreezeDialog();
                }
            }
        });

        mBtnManageCerts.setOnClickListener(v -> {
            try {
                Intent intent = new Intent("com.android.settings.TRUSTED_CREDENTIALS_USER");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e1) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e2) {
                    logConsole("⚠ Unable to open credential settings: " + e2.getMessage());
                }
            }
        });

        // 5. System Shields & Connectivity Listeners
        mSwitchPrivateDns.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("DNS Toggle", service -> {
                boolean active = service.setPrivateDnsEnabled(isChecked);
                logConsole(active ? "✔ AdGuard Private DNS ENABLED (dns.adguard-dns.com)" : "⚠ Private DNS DISABLED (off)");
            });
        });

        mSwitchAaCableOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Android Auto Cable-Only Toggle", service -> {
                boolean cableOnly = service.setAACableOnly(isChecked);
                logConsole(cableOnly ? "✔ Android Auto CABLE-ONLY Mode Activated (Nearby Wi-Fi permissions revoked & wireless handshakes blocked)" 
                                     : "✔ Android Auto WIRELESS Mode Enabled");
            });
        });

        mBtnOpenAaSettings.setOnClickListener(v -> {
            try {
                Intent intent = new Intent();
                intent.setComponent(new android.content.ComponentName("com.google.android.projection.gearhead", "com.google.android.projection.gearhead.companion.settings.DefaultSettingsActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e1) {
                try {
                    Intent intent = new Intent("com.google.android.projection.gearhead.SETTINGS");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e2) {
                    logConsole("⚠ Unable to open Android Auto settings: " + e2.getMessage());
                }
            }
        });

        mSwitchBloatwareShield.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Bloatware Shield Toggle", service -> {
                boolean active = service.setBloatwareShieldEnabled(isChecked);
                logConsole(active ? "✔ Bloatware Shield ACTIVE (Meta telemetry & OEM bloat frozen)" : "• Bloatware Shield DISABLED");
            });
        });

        mBtnCleanBloatware.setOnClickListener(v -> runAsync("Freeze Bloatware", service -> {
            logConsole("🧹 Freezing OEM bloatware & telemetry packages...");
            boolean active = service.setBloatwareShieldEnabled(true);
            logConsole("✔ OEM Bloatware packages (HeyTap MCS, Cloud, SAUHelper, Meta) successfully frozen.");
            mHandler.post(() -> {
                mUpdatingSwitches = true;
                mSwitchBloatwareShield.setChecked(active);
                mUpdatingSwitches = false;
            });
        }));

        // 6. Master Optimization
        mBtnMasterOptimize.setOnClickListener(v -> runAsync("Master Optimization", service -> {
            logConsole("⚡ Executing Master System Optimization...");
            String res = service.runMasterOptimization();
            logConsole(res);
            refreshSwitches(service);
            refreshMetrics(service);
        }));

        // 7. Battery Health & Charging Listeners
        mSwitchChargeLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Charge Limit Toggle", service -> {
                boolean active = service.set80PercentLimitEnabled(isChecked);
                logConsole(active ? "✔ 80% Custom Charge Limit ACTIVE (Stops charging at 80%)"
                                  : "⚠ 80% Custom Charge Limit DISABLED (Charges to 100%)");
                updateBypassBanner(service);
            });
        });

        mSwitchSmartCharging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Smart Charging Toggle", service -> {
                boolean active = service.setSmartChargingEnabled(isChecked);
                logConsole(active ? "⚠ Smart Charging ENABLED (May charge to 100% on schedule)"
                                  : "✔ Smart Charging DISABLED (80% Custom Limit Preserved)");
            });
        });

        mSwitchBypassCharging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Bypass Charging Toggle", service -> {
                boolean active = service.setBypassChargingEnabled(isChecked);
                logConsole(active ? "✔ Hardware Bypass Charging ENABLED (Auto-activates when battery reaches 80%)"
                                  : "• Hardware Bypass Charging DISABLED");
                updateBypassBanner(service);
            });
        });

        mBtnEnforceCharging.setOnClickListener(v -> runAsync("Enforce Charging Profile", service -> {
            logConsole("🛡️ Enforcing hardcoded Battery Health & Charging Profile...");
            String res = service.enforceChargingProfile();
            logConsole(res);
            refreshSwitches(service);
        }));

        // 8. Battery & Power Tuning Listeners
        mBtnRestrictDrainers.setOnClickListener(v -> runAsync("Restrict Drainers", service -> {
            logConsole("⚡ Restricting aggressive background battery drainers...");
            String res = service.restrictBackgroundDrainers();
            logConsole(res);
        }));

        mSwitchFastDoze.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Fast Doze Toggle", service -> {
                boolean active = service.setFastDozeEnabled(isChecked);
                logConsole(active ? "✔ Fast Doze ACTIVE (Deep sleep activates 60s after screen-off)"
                                  : "• Fast Doze Standard (Stock 30-min idle timeout)");
            });
        });

        mSwitchCellularSleep.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Cellular Sleep Toggle", service -> {
                boolean active = service.setCellularSleepOnWifiEnabled(isChecked);
                logConsole(active ? "✔ Cellular Modem Sleep on Wi-Fi ACTIVE (Baseband RF sleeps)"
                                  : "• Mobile Data Always Active");
            });
        });

        mSwitchFhdResolution.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Resolution Toggle", service -> {
                boolean active = service.setFhdResolutionEnabled(isChecked);
                logConsole(active ? "✔ FHD+ Standard Resolution ENABLED (Dynamic Scaling)\n  GPU workload optimized, gestures unaffected." 
                                  : "✔ Native High Resolution RESTORED\n  Maximum panel sharpness active.");
                mHandler.post(() -> {
                    if (mTvResolutionDesc != null) {
                        mTvResolutionDesc.setText(active ? "Active: FHD+ Standard Mode • Power Saving"
                                                         : "Active: High Resolution Mode (Native Panel) • Max Quality");
                    }
                });
            });
        });

        mSwitchHighRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Refresh Rate Toggle", service -> {
                boolean active = service.setHighRefreshEnabled(isChecked);
                logConsole(active ? "✔ Extreme Refresh Rate LOCKED (Peak Hz)" : "• Extreme Refresh Rate Adaptive");
            });
        });

        mSwitchAodSmartMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Smart AOD Toggle", service -> {
                boolean active = service.setAodSmartModeEnabled(isChecked);
                logConsole(active ? "✔ Smart AOD & Deep Standby ENABLED (Pickup/Touch only, low OLED drain)" : "⚠ Continuous All-Day AOD Activated");
            });
        });

        mBtnShieldCriticalApps.setOnClickListener(v -> runAsync("Critical Apps Shield", service -> {
            logConsole("🛡️ Verifying Critical Apps Whitelist (Messaging, Calls, SMS, Alarms & Shizuku)...");
            String res = service.enforceCriticalAppsShield();
            logConsole(res);
        }));

        // 9. System UI & Gestures Listeners
        mSwitchWeatherTamer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Weather Tamer Toggle", service -> {
                boolean active = service.setWeatherTamerEnabled(isChecked);
                logConsole(active ? "✔ Weather CPU Tamer ACTIVE (Background GPS/wakeups restricted)" : "• Weather Tamer DISABLED");
            });
        });

        setupAnimationButtons();

        mSwitchGestureNav.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mUpdatingSwitches) return;
            runAsync("Gestures Navigation Toggle", service -> {
                boolean active = service.setGestureNavigationEnabled(isChecked);
                logConsole(active ? "✔ Full-Screen Gestures ENABLED (Mode 2, edge swipe & gestures active)"
                                  : "• 3-Button Navigation ENABLED (Mode 0, on-screen buttons)");
            });
        });

        mBtnRestoreGestures.setOnClickListener(v -> runAsync("Restore Gestures", service -> {
            logConsole("🛡️ Calibrating & restoring all system gestures...");
            String res = service.restoreGestures();
            logConsole(res);
            boolean active = service.isGestureNavigationEnabled();
            mHandler.post(() -> {
                mUpdatingSwitches = true;
                mSwitchGestureNav.setChecked(active);
                mUpdatingSwitches = false;
            });
        }));

        mBtnGesturesInfo.setOnClickListener(v -> showGesturesExplanationDialog());

        // 10. Daily Automation Listeners
        mSwitchDailyAutocare.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = getSharedPreferences(DailyAlarmReceiver.PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(DailyAlarmReceiver.PREF_DAILY_ENABLED, isChecked).apply();

            if (isChecked) {
                DailyAlarmReceiver.scheduleNextAlarm(this);
                logConsole("✔ Daily Auto-Care ENABLED");
                Toast.makeText(this, "Daily Auto-Care scheduled", Toast.LENGTH_SHORT).show();
            } else {
                DailyAlarmReceiver.cancelAlarm(this);
                logConsole("• Daily Auto-Care DISABLED");
            }
        });

        mBtnPickTime.setOnClickListener(v -> showTimePicker());

        // 11. Performance & Compilation Listeners
        mBtnCompileCore.setOnClickListener(v -> runAsync("Speed Compilation Core", service -> {
            logConsole("🚀 Starting native ARM64 speed compilation of core apps...");
            String res = service.compileDailyApps();
            logConsole(res);
        }));

        mBtnCompileAllApps.setOnClickListener(v -> runAsync("Speed Compilation All", service -> {
            logConsole("⚡ Starting native ARM64 speed compilation of ALL installed user apps + System UI & Launcher...");
            String res = service.compileAllInstalledApps();
            logConsole(res);
        }));

        mBtnTrimStorage.setOnClickListener(v -> runAsync("Flash Storage Trim", service -> {
            logConsole("🧹 Starting Flash storage trim & cache cleanup...");
            String res = service.runStorageTrim();
            logConsole(res);
            refreshMetrics(service);
        }));

        // 12. Manual Storage Cleanup Listeners (Strictly Last)
        mBtnScanStorage.setOnClickListener(v -> runAsync("Scan Downloaded Files", service -> {
            logConsole("🔍 Scanning /sdcard/Download for APKs and large files (>25MB)...");
            String json = service.scanLargeFilesJson();
            mHandler.post(() -> populateStorageFileList(json, service));
        }));
    }

    private void showGesturesExplanationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Full-Screen Gestures Shield")
            .setMessage("How Full-Screen Gestures Shield works:\n\n" +
                        "1. Navigation Mode 2 (AOSP Gestural Overlay):\n" +
                        "Locks the system into edge-to-edge gesture navigation mode and disables the legacy 3-button navigation bar.\n\n" +
                        "2. Touch Inset & Exclusion Calibration:\n" +
                        "Configures back gesture sensitivity on left and right screen edges so edge swipes don't conflict with in-app drawers, custom keyboards, or gaming controls.\n\n" +
                        "3. Motion & Screen-Off Gestures Guard (OxygenOS/ColorOS):\n" +
                        "Protects Double Tap to Wake / Sleep, lift to check phone, and off-screen gestures from being reset by battery optimization or system updates.\n\n" +
                        "4. Third-Party Launcher Protection:\n" +
                        "Prevents third-party launchers or background ADB commands from unintentionally reverting navigation to 3 buttons.")
            .setPositiveButton("Got It", null)
            .show();
    }

    private void populateStorageFileList(String jsonStr, IUserService service) {
        mContainerStorageFiles.removeAllViews();
        try {
            JSONArray arr = new JSONArray(jsonStr);
            if (arr.length() == 0) {
                mTvStorageScanStatus.setText("✔ Clean: No leftover installer APKs or large files (>25MB) found in /sdcard/Download.");
                mTvStorageScanStatus.setTextColor(Color.parseColor("#00E676"));
                return;
            }

            mTvStorageScanStatus.setText("Found " + arr.length() + " file(s) in /sdcard/Download. Manual action only:");
            mTvStorageScanStatus.setTextColor(Color.parseColor("#F8F8FA"));

            float density = getResources().getDisplayMetrics().density;
            int pad = (int) (10 * density);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                String name = item.optString("name", "Unknown File");
                String path = item.optString("path", "");
                String size = item.optString("size", "0MB");

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBackgroundResource(R.drawable.bg_chip);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(pad, pad, pad, pad);

                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowLp.topMargin = (int) (6 * density);
                row.setLayoutParams(rowLp);

                // Info Column
                LinearLayout infoCol = new LinearLayout(this);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                infoLp.rightMargin = (int) (8 * density);
                infoCol.setLayoutParams(infoLp);

                TextView tvName = new TextView(this);
                tvName.setText(name);
                tvName.setTextColor(Color.parseColor("#F8F8FA"));
                tvName.setTextSize(12);
                tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                tvName.setSingleLine(true);
                tvName.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
                infoCol.addView(tvName);

                TextView tvMeta = new TextView(this);
                tvMeta.setText(size + " • " + path);
                tvMeta.setTextColor(Color.parseColor("#95959E"));
                tvMeta.setTextSize(10);
                tvMeta.setSingleLine(true);
                tvMeta.setEllipsize(android.text.TextUtils.TruncateAt.START);
                infoCol.addView(tvMeta);

                row.addView(infoCol);

                // Delete Button
                Button btnDelete = new Button(this);
                btnDelete.setText("🗑️ Delete");
                btnDelete.setBackgroundResource(R.drawable.bg_button_secondary);
                btnDelete.setTextColor(Color.parseColor("#FF5252"));
                btnDelete.setTextSize(11);
                btnDelete.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                        (int) (90 * density), (int) (36 * density));
                btnDelete.setLayoutParams(btnLp);

                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                        .setTitle("Delete File?")
                        .setMessage("Permanently delete " + name + " (" + size + ") from /sdcard/Download?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            runAsync("Delete File", s -> {
                                String result = s.deleteFileManual(path);
                                logConsole(result);
                                mHandler.post(() -> mContainerStorageFiles.removeView(row));
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                });

                row.addView(btnDelete);
                mContainerStorageFiles.addView(row);
            }
        } catch (Exception e) {
            mTvStorageScanStatus.setText("⚠ Scan parsing error: " + e.getMessage());
            mTvStorageScanStatus.setTextColor(Color.parseColor("#FF5252"));
        }
    }

    private void updateBypassBanner(IUserService service) {
        new Thread(() -> {
            try {
                String status = service.getBypassDetailedStatus();
                mHandler.post(() -> {
                    if (mTvBypassStatus != null) {
                        mTvBypassStatus.setText(status);
                        if (status.startsWith("ENGAGED")) {
                            mTvBypassStatus.setTextColor(Color.parseColor("#00E676"));
                        } else if (status.startsWith("ARMED")) {
                            mTvBypassStatus.setTextColor(Color.parseColor("#FF9100"));
                        } else {
                            mTvBypassStatus.setTextColor(Color.parseColor("#95959E"));
                        }
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void setupAnimationButtons() {
        mBtnAnim0.setOnClickListener(v -> runAsync("Anim 0x", service -> {
            service.setAnimationSpeed(0.0f);
            logConsole("✔ System Animation Speed set to 0.0x (Instant)");
            mHandler.post(() -> updateAnimationSpeedUi(0.0f));
        }));

        mBtnAnim05.setOnClickListener(v -> runAsync("Anim 0.5x", service -> {
            service.setAnimationSpeed(0.5f);
            logConsole("✔ System Animation Speed set to 0.5x (Snappy)");
            mHandler.post(() -> updateAnimationSpeedUi(0.5f));
        }));

        mBtnAnim1.setOnClickListener(v -> runAsync("Anim 1.0x", service -> {
            service.setAnimationSpeed(1.0f);
            logConsole("✔ System Animation Speed set to 1.0x (Stock)");
            mHandler.post(() -> updateAnimationSpeedUi(1.0f));
        }));
    }

    private void updateAnimationSpeedUi(float speed) {
        boolean is0 = speed == 0.0f;
        boolean is05 = Math.abs(speed - 0.5f) < 0.01f;
        boolean is1 = Math.abs(speed - 1.0f) < 0.01f;

        mBtnAnim0.setBackgroundResource(is0 ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        mBtnAnim0.setTextColor(Color.parseColor(is0 ? "#EB0029" : "#F8F8FA"));

        mBtnAnim05.setBackgroundResource(is05 ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        mBtnAnim05.setTextColor(Color.parseColor(is05 ? "#EB0029" : "#F8F8FA"));

        mBtnAnim1.setBackgroundResource(is1 ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        mBtnAnim1.setTextColor(Color.parseColor(is1 ? "#EB0029" : "#F8F8FA"));
    }

    private void showTimePicker() {
        SharedPreferences prefs = getSharedPreferences(DailyAlarmReceiver.PREF_NAME, Context.MODE_PRIVATE);
        int currentHour = prefs.getInt("pref_daily_hour", 3);
        int currentMinute = prefs.getInt("pref_daily_minute", 0);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            prefs.edit()
                .putInt("pref_daily_hour", hourOfDay)
                .putInt("pref_daily_minute", minute)
                .apply();

            updateScheduleTimeButton(hourOfDay, minute);
            DailyAlarmReceiver.scheduleNextAlarm(this);
            String amPm = hourOfDay >= 12 ? "PM" : "AM";
            int displayHour = hourOfDay == 0 || hourOfDay == 12 ? 12 : hourOfDay % 12;
            logConsole(String.format(Locale.US, "✔ Daily Auto-Care re-scheduled for %02d:%02d %s", displayHour, minute, amPm));
        }, currentHour, currentMinute, false);
        dialog.show();
    }

    private void updateScheduleTimeButton(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour == 0 || hour == 12 ? 12 : hour % 12;
        mBtnPickTime.setText(String.format(Locale.US, "⏱️ Runs Daily at %02d:%02d %s (Tap to Change Time)", displayHour, minute, amPm));
    }

    private boolean isShizukuPackageDisabled() {
        String shizPkg = ShizukuManager.resolveShizukuPackage(this);
        try {
            android.content.pm.ApplicationInfo ai = getPackageManager().getApplicationInfo(shizPkg, android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS);
            return !ai.enabled;
        } catch (Exception e) {
            return getPackageManager().getLaunchIntentForPackage(shizPkg) == null;
        }
    }

    private boolean isShizukuPackageStopped() {
        String shizPkg = ShizukuManager.resolveShizukuPackage(this);
        try {
            android.content.pm.ApplicationInfo ai = getPackageManager().getApplicationInfo(shizPkg, android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS);
            return (ai.flags & android.content.pm.ApplicationInfo.FLAG_STOPPED) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void openShizukuApp() {
        String shizPkg = ShizukuManager.resolveShizukuPackage(this);
        try {
            Intent intent = ShizukuManager.getShizukuLaunchIntent(this);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (isShizukuPackageDisabled()) {
                showAdbUnfreezeDialog();
            } else {
                logConsole("⚠ Shizuku app (" + shizPkg + ") not found or disabled.");
            }
        } catch (Exception e) {
            logConsole("⚠ Failed to launch Shizuku (" + shizPkg + "): " + e.getMessage());
        }
    }

    private boolean enableDevAndAdbOffline() {
        try {
            android.provider.Settings.Global.putInt(getContentResolver(),
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
            android.provider.Settings.Global.putInt(getContentResolver(),
                android.provider.Settings.Global.ADB_ENABLED, 1);
            android.provider.Settings.Global.putInt(getContentResolver(),
                "adb_wifi_enabled", 1);
            logConsole("✔ Developer Options, USB & Wireless Debugging enabled via Settings.Global!");
            mUpdatingSwitches = true;
            mSwitchDevOptions.setChecked(true);
            mSwitchUsbDebugging.setChecked(true);
            mSwitchWirelessDebugging.setChecked(true);
            mUpdatingSwitches = false;
            return true;
        } catch (Exception e) {
            logConsole("• Standalone setting write note: " + e.getMessage());
            return false;
        }
    }

    private void performRestoreAll(boolean restoreFullBanking) {
        logConsole(restoreFullBanking ? "🔓 Restoring Dev Options, Debugging, Accessibility, DNS & Tools..." : "🔄 Restoring Shizuku-Next...");
        SharedPreferences cloakPrefs = getSharedPreferences("revamp_stealth_prefs", Context.MODE_PRIVATE);
        cloakPrefs.edit().putBoolean("pref_shizuku_cloaked", false).apply();

        if (restoreFullBanking) {
            enableDevAndAdbOffline();
        }

        if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
            cloakPrefs.edit().putBoolean("pref_pending_banking_restore", false).apply();
            runAsync(restoreFullBanking ? "Exit Banking Stealth" : "Restore Shizuku", service -> {
                String res = restoreFullBanking ? service.exitFullBankingStealth() : (service.setShizukuCloaked(false) ? "✔ Shizuku restored" : "✔ Shizuku active");
                logConsole(res);
                mHandler.post(this::checkAndRefreshStatus);
            });
        } else {
            if (restoreFullBanking) {
                cloakPrefs.edit().putBoolean("pref_pending_banking_restore", true).apply();
                logConsole("• Dev Options & Debugging enabled. Launch Shizuku and tap Start — remaining cloaked apps & Private DNS will auto-restore on return!");
            }
            restoreShizukuAppOrGuide();
        }
    }

    private void showStartShizukuForDeepFreezeDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Start Shizuku First for Deep Freeze")
            .setMessage("Because Shizuku Stealth Cloak is already active (shizuku_server is stopped), Android cannot execute 'pm disable-user' offline.\n\n" +
                        "Tip: You don't need to toggle Stealth Cloak before Deep Freeze — Deep Freeze already stops Watchdog & shizuku_server automatically!\n\n" +
                        "Tap 'Open Shizuku App' to start Shizuku, then return and tap 'Engage Full Stealth' or 'Deep Freeze'.")
            .setPositiveButton("Open Shizuku App", (d, w) -> {
                enableDevAndAdbOffline();
                openShizukuApp();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEngageStealthDialog() {
        if (!ShizukuManager.isShizukuAvailable() || !ShizukuManager.hasShizukuPermission()) {
            new AlertDialog.Builder(this)
                .setTitle("Start Shizuku-Next First")
                .setMessage("Engaging Full Banking Stealth needs active Shizuku permissions to pause Accessibility, Private DNS, modding/VPN apps, and cleanly shut down ADB & Watchdog.\n\nTap 'Open Shizuku' to start it, or tap 'Audit' to see current status.")
                .setPositiveButton("Open Shizuku", (d, w) -> restoreShizukuAppOrGuide())
                .setNeutralButton("Audit", (d, w) -> runStealthAudit())
                .setNegativeButton("Cancel", null)
                .show();
            return;
        }
        final boolean isSpoofed = ShizukuManager.isShizukuSpoofed(this);
        final String resolvedPkg = ShizukuManager.resolveShizukuPackage(this);
        String warnOrig = isSpoofed
            ? "✔ Spoofed Shizuku verified (" + resolvedPkg + "). Standard Stealth Cloak stops the daemon completely and lets you resume from the phone!"
            : "⚠ WARNING: Original package 'moe.shizuku.privileged.api' is STILL installed!\n\nICICI, HDFC, RBL, Kotak & NPCI (Protectt.ai / Lookout / Bureau) scan PackageManager for 'moe.shizuku.privileged.api' even when Deep Frozen.\n\nRecommended: Tap 'Spoof Shizuku First' below!";
        AlertDialog.Builder b = new AlertDialog.Builder(this)
            .setTitle("Engage Full Banking & UPI Stealth")
            .setMessage("Runs in exact safe sequence:\n" +
                        "1. Self-grants WRITE_SECURE_SETTINGS (for phone-only restore)\n" +
                        "2. Pauses Accessibility & Private DNS + cleans /data/local/tmp APKs\n" +
                        "3. Cloaks AdGuard VPN, ReVanced, Morphe & all modding tools\n" +
                        "4. Conceals Dev Options, USB ADB & Wireless ADB\n" +
                        "5. Turns Watchdog OFF & stops shizuku_server cleanly\n\n" + warnOrig)
            .setPositiveButton(isSpoofed ? "Engage Stealth (Recommended)" : "Stealth Cloak Only", (d, w) -> {
                SharedPreferences cloakPrefs = getSharedPreferences("revamp_stealth_prefs", Context.MODE_PRIVATE);
                cloakPrefs.edit().putBoolean("pref_shizuku_cloaked", true).putBoolean("pref_pending_banking_restore", false).apply();
                runAsync("Engage Full Stealth", service -> {
                    String res = service.engageFullBankingStealth(false);
                    ShizukuManager.clearCachedService();
                    logConsole(res);
                    mHandler.postDelayed(this::checkAndRefreshStatus, 950);
                });
            })
            .setNeutralButton("+ Deep Freeze APK", (d, w) -> {
                SharedPreferences cloakPrefs = getSharedPreferences("revamp_stealth_prefs", Context.MODE_PRIVATE);
                cloakPrefs.edit().putBoolean("pref_shizuku_cloaked", true).putBoolean("pref_pending_banking_restore", false).apply();
                runAsync("Engage Full Stealth + Deep Freeze", service -> {
                    String res = service.engageFullBankingStealth(true);
                    ShizukuManager.clearCachedService();
                    logConsole(res);
                    mHandler.postDelayed(this::checkAndRefreshStatus, 950);
                });
            });
        if (!isSpoofed) {
            b.setNegativeButton("🎭 Spoof First", (d, w) -> showShizukuSpoofGuideDialog());
        } else {
            b.setNegativeButton("Cancel", null);
        }
        b.show();
    }

    private void showShizukuSpoofGuideDialog() {
        final String suggestedPkg = "com.mcore.services";
        android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(android.content.ClipData.newPlainText("shizuku_spoof_pkg", suggestedPkg));
            Toast.makeText(this, "Copied safe Package ID: " + suggestedPkg, Toast.LENGTH_SHORT).show();
        }
        final String currentPkg = ShizukuManager.resolveShizukuPackage(this);
        final boolean isSpoofed = ShizukuManager.isShizukuSpoofed(this);
        String header = isSpoofed
            ? "✔ Already spoofed as: " + currentPkg + "\n\n"
            : "❌ Currently using exposed ID: " + currentPkg + "\n(Copied safe replacement '" + suggestedPkg + "' to clipboard!)\n\n";
        new AlertDialog.Builder(this)
            .setTitle("Shizuku-Next Package Spoof Setup")
            .setMessage(header +
                        "Why needed: Deep Freeze (pm disable-user) only disables the app — ICICI, HDFC, RBL, Kotak & NPCI still see 'moe.shizuku.privileged.api' via QUERY_ALL_PACKAGES.\n\n" +
                        "IMPORTANT: Never use 'com.oplus.*' / 'com.oneplus.*' / 'com.coloros.*' package IDs — OxygenOS rejects them with signature error!\n\n" +
                        "1-Time Setup inside Shizuku-Next (30 seconds):\n" +
                        "1. Tap 'Open Shizuku' below → Settings (gear icon) → 'Stealth mode'.\n" +
                        "2. Set Display name to 'System Helper'.\n" +
                        "3. Paste '" + suggestedPkg + "' into Package name (do NOT leave blank) & tap 'Hide'.\n" +
                        "4. Approve uninstalling original Shizuku when prompted.\n" +
                        "5. If no install popup appears after uninstall, open File Manager → Internal storage → 'Shizuku' folder and install the exported APK.\n" +
                        "6. CRITICAL: Do NOT tap 'Unhide with stub'. Start the new 'System Helper' app and grant OnePlus Revamp Hub access.")
            .setPositiveButton("Open Shizuku", (d, w) -> openShizukuApp())
            .setNegativeButton("Done", (d, w) -> checkAndRefreshStatus())
            .show();
    }

    private void runStealthAudit() {
        if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
            runAsync("Stealth Audit", service -> {
                String report = service.getStealthAuditSummary();
                logConsole(report);
                mHandler.post(() -> new AlertDialog.Builder(this)
                    .setTitle("Banking & UPI Readiness Audit")
                    .setMessage(report)
                    .setPositiveButton("OK", null)
                    .show());
            });
        } else {
            String shizPkg = ShizukuManager.resolveShizukuPackage(this);
            boolean origPresent = ShizukuManager.isOriginalShizukuInstalled(this);
            android.content.ContentResolver cr = getContentResolver();
            int dev = android.provider.Settings.Global.getInt(cr, android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
            int adb = android.provider.Settings.Global.getInt(cr, android.provider.Settings.Global.ADB_ENABLED, 0);
            int wadb = android.provider.Settings.Global.getInt(cr, "adb_wifi_enabled", 0);
            int acc = android.provider.Settings.Secure.getInt(cr, android.provider.Settings.Secure.ACCESSIBILITY_ENABLED, 0);
            String dns = android.provider.Settings.Global.getString(cr, "private_dns_mode");
            String offline = "=== Offline Banking Audit (Shizuku Stopped) ===\n"
                + "[1] Shizuku Pkg: " + (origPresent ? "❌ EXPOSED (moe.shizuku.privileged.api still installed!)" : "✔ Spoofed (" + shizPkg + ")") + "\n"
                + "[2] Shizuku Server: ✔ Stopped (Not running)\n"
                + "[3] Dev/USB/WiFi ADB: " + dev + "/" + adb + "/" + wadb + "\n"
                + "[4] Accessibility Master Flag: " + acc + "\n"
                + "[5] Private DNS Mode: " + (dns == null ? "default" : dns) + "\n\n"
                + "Tip: Ensure Dev/USB/WiFi=0/0/0, Accessibility=0, DNS=off & original moe.shizuku.privileged.api is spoofed!";
            logConsole(offline);
            new AlertDialog.Builder(this)
                .setTitle("Offline Stealth Audit")
                .setMessage(offline)
                .setPositiveButton("OK", null)
                .show();
        }
    }

    private void updateShizukuSpoofBanner() {
        if (mTvShizukuSpoofBadge == null || mTvShizukuSpoofStatus == null) return;
        String resolvedPkg = ShizukuManager.resolveShizukuPackage(this);
        boolean origPresent = ShizukuManager.isOriginalShizukuInstalled(this);
        if (!origPresent && !ShizukuManager.ORIGINAL_SHIZUKU_PKG.equals(resolvedPkg)
                && ShizukuManager.isPackageInstalled(this, resolvedPkg)) {
            mTvShizukuSpoofBadge.setText("SPOOFED");
            mTvShizukuSpoofBadge.setTextColor(Color.parseColor("#00E676"));
            mTvShizukuSpoofStatus.setText("✔ Spoofed as '" + resolvedPkg + "' (Original moe.shizuku.privileged.api completely removed!)");
            mTvShizukuSpoofStatus.setTextColor(Color.parseColor("#00E676"));
            if (mBtnSpoofShizuku != null) {
                mBtnSpoofShizuku.setText("🎭 Package Spoof Active (" + resolvedPkg + ")");
                mBtnSpoofShizuku.setTextColor(Color.parseColor("#00E676"));
            }
        } else {
            mTvShizukuSpoofBadge.setText("EXPOSED");
            mTvShizukuSpoofBadge.setTextColor(Color.parseColor("#FF5252"));
            mTvShizukuSpoofStatus.setText("⚠ Exposed as 'moe.shizuku.privileged.api' — Banking SDKs detect this ID even when Deep Frozen! Tap below to spoof.");
            mTvShizukuSpoofStatus.setTextColor(Color.parseColor("#FF9100"));
            if (mBtnSpoofShizuku != null) {
                mBtnSpoofShizuku.setText("🎭 Setup Shizuku-Next Package Spoof");
                mBtnSpoofShizuku.setTextColor(Color.parseColor("#FF9100"));
            }
        }
    }

    private void restoreShizukuAppOrGuide() {
        String shizPkg = ShizukuManager.resolveShizukuPackage(this);
        if (isShizukuPackageDisabled()) {
            showAdbUnfreezeDialog();
            return;
        }
        try {
            Intent intent = ShizukuManager.getShizukuLaunchIntent(this);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                logConsole("🔄 Shizuku (" + shizPkg + ") launched! Ensure Wireless Debugging is ON and tap 'Start'.");
                Toast.makeText(this, "Tap 'Start' in Shizuku (with Wireless Debugging ON)", Toast.LENGTH_LONG).show();
            } else {
                showAdbUnfreezeDialog();
            }
        } catch (Exception e) {
            logConsole("⚠ Unable to launch Shizuku (" + shizPkg + "): " + e.getMessage());
        }
    }

    private void showAdbUnfreezeDialog() {
        final String shizPkg = ShizukuManager.resolveShizukuPackage(this);
        final String cmd = "adb shell pm enable " + shizPkg;
        new AlertDialog.Builder(this)
            .setTitle("Shizuku APK Is Deep Frozen")
            .setMessage(shizPkg + " is disabled by Deep Freeze.\n\nBecause Android 15 prevents unprivileged apps from enabling disabled packages while Shizuku is stopped, run this ADB command from your computer to unfreeze:\n\n" + cmd)
            .setPositiveButton("Copy Command", (d, w) -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("adb_unfreeze", cmd));
                    Toast.makeText(this, "Copied ADB command to clipboard", Toast.LENGTH_SHORT).show();
                }
            })
            .setNeutralButton("Open Dev Settings", (d, w) -> {
                try {
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                } catch (Exception e) {
                    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                }
            })
            .setNegativeButton("OK", null)
            .show();
    }

    private void syncOfflineUiState() {
        mUpdatingSwitches = true;
        try {
            android.content.ContentResolver cr = getContentResolver();
            boolean devOn = android.provider.Settings.Global.getInt(cr, android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
            boolean usbAdb = android.provider.Settings.Global.getInt(cr, android.provider.Settings.Global.ADB_ENABLED, 0) == 1;
            boolean wirelessAdb = android.provider.Settings.Global.getInt(cr, "adb_wifi_enabled", 0) == 1;
            boolean adbTimeoutOff = android.provider.Settings.Global.getInt(cr, "adb_allowed_connection_time", 604800000) == 0;
            boolean verifyAdb = android.provider.Settings.Global.getInt(cr, "verifier_verify_adb_installs", 1) != 0;
            boolean wfdCert = android.provider.Settings.Global.getInt(cr, "wifi_display_certification_on", 0) == 1;
            boolean stayAwake = android.provider.Settings.Global.getInt(cr, "stay_on_while_plugged_in", 0) != 0;

            mSwitchDevOptions.setChecked(devOn);
            mSwitchUsbDebugging.setChecked(usbAdb);
            mSwitchWirelessDebugging.setChecked(wirelessAdb);
            if (mSwitchAdbTimeout != null) mSwitchAdbTimeout.setChecked(adbTimeoutOff);
            if (mSwitchVerifyAdbApps != null) mSwitchVerifyAdbApps.setChecked(verifyAdb);
            if (mSwitchWfdCertification != null) mSwitchWfdCertification.setChecked(wfdCert);
            if (mSwitchStayAwake != null) mSwitchStayAwake.setChecked(stayAwake);

            boolean frozen = isShizukuPackageDisabled();
            boolean stopped = isShizukuPackageStopped();
            SharedPreferences cloakPrefs = getSharedPreferences("revamp_stealth_prefs", Context.MODE_PRIVATE);
            boolean prefCloaked = cloakPrefs.getBoolean("pref_shizuku_cloaked", false);
            boolean cloaked = frozen || stopped || prefCloaked;

            if (mSwitchShizukuCloak != null) mSwitchShizukuCloak.setChecked(cloaked);
            if (mSwitchShizukuDeepFreeze != null) mSwitchShizukuDeepFreeze.setChecked(frozen);

            boolean bankingOfflineActive = !devOn && !usbAdb && !wirelessAdb;
            if (mSwitchBankingMode != null) mSwitchBankingMode.setChecked(bankingOfflineActive);
            if (mTvBankingBadge != null) {
                mTvBankingBadge.setText(bankingOfflineActive ? "STEALTH" : "STANDBY");
                mTvBankingBadge.setTextColor(bankingOfflineActive ? Color.parseColor("#00E676") : Color.parseColor("#FF9100"));
            }
            if (mTvBankingStatus != null) {
                mTvBankingStatus.setText(bankingOfflineActive
                    ? "Active: Dev Mode & ADB Hidden" + (cloaked ? " • Shizuku Stopped" : "")
                    : "Standby: Dev Mode & Apps Visible");
            }

            if (frozen) {
                mTvShizukuStatus.setText("● Shizuku-Next: Deep Frozen (Package Disabled)");
                mTvShizukuStatus.setTextColor(Color.parseColor("#FF9100"));
                mBtnShizukuAuth.setText("ADB Unfreeze Guide");
            } else if (cloaked) {
                mTvShizukuStatus.setText("● Shizuku-Next: Cloaked & Stopped (Stealth Active)");
                mTvShizukuStatus.setTextColor(Color.parseColor("#00E676"));
                mBtnShizukuAuth.setText("Open Shizuku-Next");
            } else {
                mTvShizukuStatus.setText("● Shizuku-Next: Not Running (Tap Start or Open App)");
                mTvShizukuStatus.setTextColor(Color.parseColor("#FF5252"));
                mBtnShizukuAuth.setText("Open Shizuku-Next");
            }
            updateShizukuSpoofBanner();
        } catch (Exception ignored) {
        } finally {
            mUpdatingSwitches = false;
        }
    }

    private void requestShizuku() {
        if (!ShizukuManager.isShizukuAvailable()) {
            restoreShizukuAppOrGuide();
            return;
        }
        try {
            Shizuku.requestPermission(SHIZUKU_REQUEST_CODE);
        } catch (Throwable t) {
            Toast.makeText(this, "Failed to request Shizuku: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndRefreshStatus() {
        boolean shizukuRunning = ShizukuManager.isShizukuAvailable();
        boolean shizukuPerm = ShizukuManager.hasShizukuPermission();

        if (!shizukuRunning) {
            ShizukuManager.clearCachedService();
            mBtnShizukuAuth.setVisibility(View.VISIBLE);
            syncOfflineUiState();
            setControlsEnabled(false);
            return;
        }

        if (!shizukuPerm) {
            mTvShizukuStatus.setText("● Shizuku-Next: Authorization Needed");
            mTvShizukuStatus.setTextColor(Color.parseColor("#FFA726"));
            mBtnShizukuAuth.setVisibility(View.VISIBLE);
            mBtnShizukuAuth.setText("Authorize Shizuku-Next Access");
            syncOfflineUiState();
            setControlsEnabled(false);
            return;
        }

        SharedPreferences cloakPrefs = getSharedPreferences("revamp_stealth_prefs", Context.MODE_PRIVATE);
        cloakPrefs.edit().putBoolean("pref_shizuku_cloaked", false).apply();
        final boolean pendingRestore = cloakPrefs.getBoolean("pref_pending_banking_restore", false);
        if (pendingRestore) {
            cloakPrefs.edit().putBoolean("pref_pending_banking_restore", false).apply();
        }

        String shizPkg = ShizukuManager.resolveShizukuPackage(this);
        mTvShizukuStatus.setText("● Shizuku: Active & Authorized (" + shizPkg + ")");
        mTvShizukuStatus.setTextColor(Color.parseColor("#00E676"));
        mBtnShizukuAuth.setVisibility(View.GONE);
        updateShizukuSpoofBanner();
        setControlsEnabled(true);

        ShizukuManager.getService(this, new ShizukuManager.Callback() {
            @Override
            public void onReady(IUserService service) {
                if (pendingRestore) {
                    new Thread(() -> {
                        try {
                            String msg = service.exitFullBankingStealth();
                            logConsole("✔ Auto-Restore: " + msg);
                        } catch (Exception ignored) {}
                        mHandler.post(() -> {
                            refreshSwitches(service);
                            refreshMetrics(service);
                            refreshHardwareSpecs(service);
                        });
                    }).start();
                } else {
                    mHandler.post(() -> {
                        refreshSwitches(service);
                        refreshMetrics(service);
                        refreshHardwareSpecs(service);
                    });
                }
            }

            @Override
            public void onError(String message) {
                logConsole("Shizuku Error: " + message);
            }
        });
    }

    private void populateDeviceHardwareOffline() {
        try {
            String mfg = android.os.Build.MANUFACTURER != null ? android.os.Build.MANUFACTURER : "Android";
            String model = android.os.Build.MODEL != null ? android.os.Build.MODEL : "Device";
            String titleModel = mfg.substring(0, 1).toUpperCase(Locale.US) + mfg.substring(1) + " " + model;
            if (mTvDeviceHeaderBadge != null) {
                mTvDeviceHeaderBadge.setText(titleModel + " • Android " + android.os.Build.VERSION.RELEASE + " (SDK " + android.os.Build.VERSION.SDK_INT + ")");
            }
            if (mTvSpecChip != null) {
                mTvSpecChip.setText(android.os.Build.DEVICE != null ? android.os.Build.DEVICE.toUpperCase(Locale.US) : "ANDROID");
            }
            if (mTvSpecSoc != null) {
                String hw = android.os.Build.HARDWARE != null ? android.os.Build.HARDWARE : "ARM64";
                String abi = android.os.Build.SUPPORTED_ABIS.length > 0 ? android.os.Build.SUPPORTED_ABIS[0] : "arm64-v8a";
                mTvSpecSoc.setText("⚡ Processor: " + hw + " (" + abi + ")");
            }
            if (mTvSpecRam != null) {
                android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
                    am.getMemoryInfo(mi);
                    double totalGb = mi.totalMem / (1024.0 * 1024.0 * 1024.0);
                    mTvSpecRam.setText(String.format(Locale.US, "🧠 RAM: %.1f GB Total System Memory", totalGb));
                }
            }
            if (mTvSpecStorage != null) {
                android.os.StatFs stat = new android.os.StatFs(android.os.Environment.getDataDirectory().getPath());
                double totalGb = stat.getTotalBytes() / (1024.0 * 1024.0 * 1024.0);
                mTvSpecStorage.setText(String.format(Locale.US, "💾 Storage: %.0f GB Usable Internal Data", totalGb));
            }
            if (mTvSpecDisplay != null) {
                android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
                float refreshHz = getWindowManager().getDefaultDisplay().getRefreshRate();
                mTvSpecDisplay.setText(String.format(Locale.US, "📱 Display: %dx%d @ %d DPI • %.0fHz", dm.widthPixels, dm.heightPixels, dm.densityDpi, refreshHz));
            }
            if (mTvSpecBattery != null) {
                android.os.BatteryManager bm = (android.os.BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                int pct = bm != null ? bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) : 0;
                mTvSpecBattery.setText("🔋 Battery: " + (pct > 0 ? pct + "% Charged" : "Standard Battery Subsystem"));
            }
            if (mTvSpecOs != null) {
                mTvSpecOs.setText("⚙️ OS: Android " + android.os.Build.VERSION.RELEASE + " • " + android.os.Build.DISPLAY);
            }
        } catch (Exception ignored) {}
    }

    private void refreshHardwareSpecs(IUserService service) {
        new Thread(() -> {
            try {
                String jsonStr = service.getHardwareSpecsJson();
                JSONObject obj = new JSONObject(jsonStr);
                String soc = obj.optString("soc", "");
                String ram = obj.optString("ram", "");
                String storage = obj.optString("storage", "");
                String display = obj.optString("display", "");
                String battery = obj.optString("battery", "");
                String os = obj.optString("os", "");
                mHandler.post(() -> {
                    if (!soc.isEmpty() && mTvSpecSoc != null) mTvSpecSoc.setText("⚡ Processor: " + soc);
                    if (!ram.isEmpty() && mTvSpecRam != null) mTvSpecRam.setText("🧠 RAM: " + ram);
                    if (!storage.isEmpty() && mTvSpecStorage != null) mTvSpecStorage.setText("💾 Storage: " + storage);
                    if (!display.isEmpty() && mTvSpecDisplay != null) mTvSpecDisplay.setText("📱 Display: " + display);
                    if (!battery.isEmpty() && mTvSpecBattery != null) mTvSpecBattery.setText("🔋 Battery: " + battery);
                    if (!os.isEmpty() && mTvSpecOs != null) mTvSpecOs.setText("⚙️ OS: " + os);
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void refreshMetrics(IUserService service) {
        new Thread(() -> {
            try {
                String jsonStr = service.getSystemMetricsJson();
                JSONObject obj = new JSONObject(jsonStr);
                String ramUsed = obj.optString("ram_used", "--");
                String ramTotal = obj.optString("ram_total", "--");
                int ramPct = obj.optInt("ram_pct", 0);

                int batLevel = obj.optInt("bat_level", 0);
                String batTemp = obj.optString("bat_temp", "--");

                String storageFree = obj.optString("storage_free", "--");

                mHandler.post(() -> {
                    mTvMetricRam.setText(String.format(Locale.US, "%s / %s GB (%d%%)", ramUsed, ramTotal, ramPct));
                    mTvMetricBattery.setText(String.format(Locale.US, "%d%% • %s", batLevel, batTemp));
                    mTvMetricStorage.setText(String.format(Locale.US, "%s Free", storageFree));
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void refreshSwitches(IUserService service) {
        new Thread(() -> {
            try {
                // Debugging
                boolean usbAdb = service.isUsbDebuggingEnabled();
                boolean wirelessAdb = service.isWirelessDebuggingEnabled();
                boolean devOptions = service.isDevOptionsEnabled();

                // Developer Mode Tuning
                boolean adbTimeout = service.isAdbAuthTimeoutDisabled();
                boolean verifyAdb = service.isVerifyAdbAppsEnabled();
                boolean wfdCert = service.isWifiDisplayCertEnabled();
                boolean stayAwake = service.isStayAwakeWhileCharging();

                // Battery & Charging
                boolean limit80 = service.is80PercentLimitEnabled();
                boolean smartChg = service.isSmartChargingEnabled();
                boolean bypassChg = service.isBypassChargingEnabled();
                String bypassStatus = service.getBypassDetailedStatus();

                // Battery & Power Tuning
                boolean fastDoze = service.isFastDozeEnabled();
                boolean cellSleep = service.isCellularSleepOnWifiEnabled();
                boolean fhd = service.isFhdResolutionEnabled();
                boolean highRefresh = service.isHighRefreshEnabled();
                boolean aodSmart = service.isAodSmartModeEnabled();

                // System Shields
                boolean dns = service.isPrivateDnsEnabled();
                boolean cableOnly = service.isAACableOnly();
                boolean bloatShield = service.isBloatwareShieldEnabled();
                boolean bankingMode = service.isBankingModeEnabled();
                boolean shizukuCloaked = service.isShizukuCloaked();
                boolean shizukuFrozen = service.isShizukuDeepFrozen();
                String certReport = service.checkUserCertificates();
                String riskReport = service.getDetectedRiskApps();
                String detailedRiskJson = service.getRiskAppsDetailedJson();

                // Daemons & UI
                boolean weatherTamer = service.isWeatherTamerEnabled();
                float animSpeed = service.getAnimationSpeed();
                boolean gestureNav = service.isGestureNavigationEnabled();

                mHandler.post(() -> {
                    mUpdatingSwitches = true;

                    // Debugging Switches
                    mSwitchUsbDebugging.setChecked(usbAdb);
                    mSwitchWirelessDebugging.setChecked(wirelessAdb);
                    mSwitchDevOptions.setChecked(devOptions);

                    // Developer Mode Tuning Switches
                    if (mSwitchAdbTimeout != null) mSwitchAdbTimeout.setChecked(adbTimeout);
                    if (mSwitchVerifyAdbApps != null) mSwitchVerifyAdbApps.setChecked(verifyAdb);
                    if (mSwitchWfdCertification != null) mSwitchWfdCertification.setChecked(wfdCert);
                    if (mSwitchStayAwake != null) mSwitchStayAwake.setChecked(stayAwake);

                    // Charging
                    mSwitchChargeLimit.setChecked(limit80);
                    mSwitchSmartCharging.setChecked(smartChg);
                    mSwitchBypassCharging.setChecked(bypassChg);
                    if (mTvBypassStatus != null) {
                        mTvBypassStatus.setText(bypassStatus);
                        if (bypassStatus.startsWith("ENGAGED")) {
                            mTvBypassStatus.setTextColor(Color.parseColor("#00E676"));
                        } else if (bypassStatus.startsWith("ARMED")) {
                            mTvBypassStatus.setTextColor(Color.parseColor("#FF9100"));
                        } else {
                            mTvBypassStatus.setTextColor(Color.parseColor("#95959E"));
                        }
                    }

                    // Power Tuning
                    mSwitchFastDoze.setChecked(fastDoze);
                    mSwitchCellularSleep.setChecked(cellSleep);
                    mSwitchFhdResolution.setChecked(fhd);
                    mSwitchHighRefresh.setChecked(highRefresh);
                    mSwitchAodSmartMode.setChecked(aodSmart);
                    if (mTvResolutionDesc != null) {
                        mTvResolutionDesc.setText(fhd ? "Active: FHD+ Standard Mode • Power Saving"
                                                      : "Active: High Resolution Mode (Native Panel) • Max Quality");
                    }

                    // Shields
                    mSwitchPrivateDns.setChecked(dns);
                    mSwitchAaCableOnly.setChecked(cableOnly);
                    mSwitchBloatwareShield.setChecked(bloatShield);

                    // Banking & UPI Stealth
                    if (mSwitchBankingMode != null) {
                        mSwitchBankingMode.setChecked(bankingMode);
                    }
                    if (mSwitchShizukuCloak != null) {
                        mSwitchShizukuCloak.setChecked(shizukuCloaked);
                    }
                    if (mSwitchShizukuDeepFreeze != null) {
                        mSwitchShizukuDeepFreeze.setChecked(shizukuFrozen);
                    }
                    if (mTvBankingStatus != null) {
                        mTvBankingStatus.setText(bankingMode ? "Active: Dev Mode & Sideloaded Apps Hidden (Safe for Banking)" : "Standby: Dev Mode & Apps Visible");
                    }
                    if (mTvBankingBadge != null) {
                        mTvBankingBadge.setText(bankingMode ? "STEALTH" : "STANDBY");
                        mTvBankingBadge.setTextColor(bankingMode ? Color.parseColor("#00E676") : Color.parseColor("#FF9100"));
                    }
                    if (mTvCertStatus != null && certReport != null) {
                        if (certReport.startsWith("CLEAN")) {
                            mTvCertStatus.setText("✔ No User CA Certificates Installed (Clean)");
                            mTvCertStatus.setTextColor(Color.parseColor("#00E676"));
                        } else {
                            int idx = certReport.indexOf(":");
                            int nextIdx = certReport.indexOf(":", idx + 1);
                            String msg = nextIdx != -1 ? certReport.substring(nextIdx + 1) : certReport;
                            mTvCertStatus.setText("⚠ " + msg);
                            mTvCertStatus.setTextColor(Color.parseColor("#FF5252"));
                        }
                    }
                    updateRiskAppsUi(riskReport);
                    populateRiskAppsList(detailedRiskJson, service);

                    // Daemons
                    mSwitchWeatherTamer.setChecked(weatherTamer);
                    updateAnimationSpeedUi(animSpeed);
                    mSwitchGestureNav.setChecked(gestureNav);

                    mUpdatingSwitches = false;
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void updateRiskAppsUi(String riskReport) {
        if (mTvRiskStatus == null || mTvRiskBadge == null || riskReport == null) return;
        if (riskReport.startsWith("CLEAN")) {
            mTvRiskBadge.setText("CLEAN");
            mTvRiskBadge.setTextColor(Color.parseColor("#00E676"));
            mTvRiskStatus.setText("✔ No sideloaded risk apps found on device.");
            mTvRiskStatus.setTextColor(Color.parseColor("#00E676"));
        } else if (riskReport.startsWith("DETECTED")) {
            String[] parts = riskReport.split(":", 4);
            int total = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int cloaked = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            String desc = parts.length > 3 ? parts[3] : "";

            if (cloaked >= total && total > 0) {
                mTvRiskBadge.setText("CLOAKED");
                mTvRiskBadge.setTextColor(Color.parseColor("#00E676"));
                mTvRiskStatus.setText("✔ " + total + " Tools Cloaked (" + desc + ") • Safe for Banking");
                mTvRiskStatus.setTextColor(Color.parseColor("#00E676"));
            } else {
                mTvRiskBadge.setText("EXPOSED");
                mTvRiskBadge.setTextColor(Color.parseColor("#FF5252"));
                mTvRiskStatus.setText("⚠ " + (total - cloaked) + " App(s) Exposed to Bank Scanners: " + desc + ". Toggle Stealth Mode ON to cloak.");
                mTvRiskStatus.setTextColor(Color.parseColor("#FF5252"));
            }
        }
    }

    private void populateRiskAppsList(String jsonStr, IUserService service) {
        if (mContainerRiskApps == null) return;
        mContainerRiskApps.removeAllViews();
        try {
            JSONArray arr = new JSONArray(jsonStr);
            if (arr.length() == 0) {
                if (mTvRiskStatus != null) {
                    mTvRiskStatus.setText("✔ No sideloaded root or installer modding tools found on device.");
                    mTvRiskStatus.setTextColor(Color.parseColor("#00E676"));
                }
                if (mTvRiskBadge != null) {
                    mTvRiskBadge.setText("CLEAN");
                    mTvRiskBadge.setTextColor(Color.parseColor("#00E676"));
                }
                return;
            }

            int cloakedCount = 0;
            float density = getResources().getDisplayMetrics().density;
            int pad = (int) (8 * density);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                int number = i + 1;
                String pkg = item.optString("pkg", "");
                String name = item.optString("name", pkg);
                boolean cloaked = item.optBoolean("cloaked", false);
                if (cloaked) cloakedCount++;

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBackgroundResource(R.drawable.bg_chip);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(pad, pad, pad, pad);

                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowLp.topMargin = (int) (6 * density);
                row.setLayoutParams(rowLp);

                // Info Column (Number + Name + Package)
                LinearLayout infoCol = new LinearLayout(this);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                infoLp.rightMargin = (int) (8 * density);
                infoCol.setLayoutParams(infoLp);

                TextView tvTitle = new TextView(this);
                tvTitle.setText(number + ". " + name);
                tvTitle.setTextColor(Color.parseColor(cloaked ? "#95959E" : "#F8F8FA"));
                tvTitle.setTextSize(12);
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTitle.setSingleLine(true);
                tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
                infoCol.addView(tvTitle);

                TextView tvPkg = new TextView(this);
                tvPkg.setText(pkg + (cloaked ? " • [CLOAKED]" : " • [VISIBLE]"));
                tvPkg.setTextColor(Color.parseColor(cloaked ? "#00E676" : "#FF9100"));
                tvPkg.setTextSize(10);
                tvPkg.setSingleLine(true);
                tvPkg.setEllipsize(android.text.TextUtils.TruncateAt.END);
                infoCol.addView(tvPkg);

                row.addView(infoCol);

                // Individual App Toggle Switch
                Switch sw = new Switch(this);
                sw.setChecked(cloaked);
                sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    runAsync("Toggle " + pkg, s -> {
                        boolean nowCloaked = s.setAppCloaked(pkg, isChecked);
                        logConsole((nowCloaked ? "✔ Cloaked: " : "• Uncloaked: ") + name);
                        String refreshedJson = s.getRiskAppsDetailedJson();
                        mHandler.post(() -> populateRiskAppsList(refreshedJson, s));
                    });
                });

                row.addView(sw);
                mContainerRiskApps.addView(row);
            }

            if (mTvRiskBadge != null) {
                if (cloakedCount >= arr.length()) {
                    mTvRiskBadge.setText("ALL CLOAKED");
                    mTvRiskBadge.setTextColor(Color.parseColor("#00E676"));
                } else {
                    mTvRiskBadge.setText((arr.length() - cloakedCount) + " EXPOSED");
                    mTvRiskBadge.setTextColor(Color.parseColor("#FF5252"));
                }
            }

            if (mTvRiskStatus != null) {
                mTvRiskStatus.setText("Found " + arr.length() + " sideloaded modding app(s) (" + cloakedCount + "/" + arr.length() + " cloaked). Toggle individually or tap Cloak All:");
                mTvRiskStatus.setTextColor(Color.parseColor("#F8F8FA"));
            }
        } catch (Exception e) {
            if (mTvRiskStatus != null) {
                mTvRiskStatus.setText("⚠ Error loading risk apps: " + e.getMessage());
            }
        }
    }

    private void setControlsEnabled(boolean enabled) {
        // ALWAYS KEEP DEBUGGING, RECOVERY & BANKING CONTROLS ENABLED SO USER CAN NEVER GET LOCKED OUT!
        mSwitchUsbDebugging.setEnabled(true);
        mSwitchWirelessDebugging.setEnabled(true);
        mSwitchDevOptions.setEnabled(true);
        mBtnOpenDevSettings.setEnabled(true);
        mBtnOpenShizukuApp.setEnabled(true);
        mBtnStartShizukuDirect.setEnabled(true);
        mBtnEnableDevMode.setEnabled(true);
        mBtnRestoreShizuku.setEnabled(true);

        // Developer Mode Tuning
        if (mSwitchAdbTimeout != null) mSwitchAdbTimeout.setEnabled(true);
        if (mSwitchVerifyAdbApps != null) mSwitchVerifyAdbApps.setEnabled(true);
        if (mBtnDirectWirelessAdb != null) mBtnDirectWirelessAdb.setEnabled(true);
        if (mSwitchWfdCertification != null) mSwitchWfdCertification.setEnabled(true);
        if (mSwitchStayAwake != null) mSwitchStayAwake.setEnabled(true);

        // Banking & Modding Apps
        if (mBtnEngageFullStealth != null) mBtnEngageFullStealth.setEnabled(true);
        if (mBtnExitFullStealth != null) mBtnExitFullStealth.setEnabled(true);
        if (mBtnAuditStealth != null) mBtnAuditStealth.setEnabled(true);
        if (mBtnSpoofShizuku != null) mBtnSpoofShizuku.setEnabled(true);
        if (mSwitchBankingMode != null) mSwitchBankingMode.setEnabled(true);
        if (mSwitchShizukuCloak != null) mSwitchShizukuCloak.setEnabled(true);
        if (mSwitchShizukuDeepFreeze != null) mSwitchShizukuDeepFreeze.setEnabled(true);
        if (mBtnCloakAllRisk != null) mBtnCloakAllRisk.setEnabled(true);
        if (mBtnUncloakAllRisk != null) mBtnUncloakAllRisk.setEnabled(true);

        // Master & Metrics
        mBtnMasterOptimize.setEnabled(enabled);
        mBtnMetricTrimCache.setEnabled(enabled);
        mBtnMetricFlushRam.setEnabled(enabled);

        // Compilation & Utilities
        mBtnCompileCore.setEnabled(enabled);
        mBtnCompileAllApps.setEnabled(enabled);
        mBtnTrimStorage.setEnabled(enabled);

        // Charging & Power
        mBtnShieldCriticalApps.setEnabled(enabled);
        mBtnEnforceCharging.setEnabled(enabled);
        mBtnRestrictDrainers.setEnabled(enabled);
        mSwitchChargeLimit.setEnabled(enabled);
        mSwitchSmartCharging.setEnabled(enabled);
        mSwitchBypassCharging.setEnabled(enabled);

        // Tuning
        mSwitchFastDoze.setEnabled(enabled);
        mSwitchCellularSleep.setEnabled(enabled);
        mSwitchFhdResolution.setEnabled(enabled);
        mSwitchHighRefresh.setEnabled(enabled);
        mSwitchAodSmartMode.setEnabled(enabled);

        // Shields
        mSwitchPrivateDns.setEnabled(enabled);
        mSwitchAaCableOnly.setEnabled(enabled);
        if (mBtnOpenAaSettings != null) mBtnOpenAaSettings.setEnabled(enabled);
        mSwitchBloatwareShield.setEnabled(enabled);
        mBtnCleanBloatware.setEnabled(enabled);

        // Banking
        if (mBtnManageCerts != null) mBtnManageCerts.setEnabled(enabled);

        // UI & Gestures
        mSwitchWeatherTamer.setEnabled(enabled);
        mBtnAnim0.setEnabled(enabled);
        mBtnAnim05.setEnabled(enabled);
        mBtnAnim1.setEnabled(enabled);
        mSwitchGestureNav.setEnabled(enabled);
        mBtnRestoreGestures.setEnabled(enabled);

        // Daily Auto-Care
        mSwitchDailyAutocare.setEnabled(enabled);
        mBtnPickTime.setEnabled(enabled);

        // Manual Storage
        mBtnScanStorage.setEnabled(enabled);
    }

    private interface ServiceAction {
        void run(IUserService service) throws Exception;
    }

    private void runAsync(String actionName, ServiceAction action) {
        setControlsEnabled(false);
        new Thread(() -> {
            ShizukuManager.getService(this, new ShizukuManager.Callback() {
                @Override
                public void onReady(IUserService service) {
                    try {
                        action.run(service);
                    } catch (Exception e) {
                        logConsole("❌ Error during " + actionName + ": " + e.getMessage());
                    } finally {
                        mHandler.post(() -> setControlsEnabled(
                            ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()));
                    }
                }

                @Override
                public void onError(String message) {
                    logConsole("❌ Shizuku Connection Error: " + message);
                    mHandler.post(() -> setControlsEnabled(
                        ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()));
                }
            });
        }).start();
    }

    private void logConsole(String message) {
        mHandler.post(() -> {
            mTvConsole.append("\n" + message);
            mScrollConsole.post(() -> mScrollConsole.fullScroll(View.FOCUS_DOWN));
        });
    }
}
