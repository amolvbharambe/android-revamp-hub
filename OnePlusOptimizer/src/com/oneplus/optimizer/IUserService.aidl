package com.oneplus.optimizer;

interface IUserService {
    void destroy();
    String executeShell(String command);
    String runMasterOptimization();
    String compileDailyApps();
    String compileNewApps();
    String runStorageTrim();
    String runTrimCache();
    String runFlushRam();
    String getSystemMetricsJson();

    boolean isPrivateDnsEnabled();
    boolean setPrivateDnsEnabled(boolean enable);

    boolean isHighRefreshEnabled();
    boolean setHighRefreshEnabled(boolean enable);

    boolean isAACableOnly();
    boolean setAACableOnly(boolean cableOnly);

    boolean isWirelessDebuggingEnabled();
    boolean setWirelessDebuggingEnabled(boolean enable);

    boolean isAodSmartModeEnabled();
    boolean setAodSmartModeEnabled(boolean enable);

    boolean isFhdResolutionEnabled();
    boolean setFhdResolutionEnabled(boolean enable);
    String getCurrentResolutionInfo();

    boolean is80PercentLimitEnabled();
    boolean set80PercentLimitEnabled(boolean enable);

    boolean isSmartChargingEnabled();
    boolean setSmartChargingEnabled(boolean enable);

    boolean isBypassChargingEnabled();
    boolean setBypassChargingEnabled(boolean enable);
    int getBypassThreshold();
    boolean setBypassThreshold(int threshold);
    String enforceChargingProfile();

    boolean isFastDozeEnabled();
    boolean setFastDozeEnabled(boolean enable);

    boolean isCellularSleepOnWifiEnabled();
    boolean setCellularSleepOnWifiEnabled(boolean enable);

    String enforceCriticalAppsShield();

    boolean isBloatwareShieldEnabled();
    boolean setBloatwareShieldEnabled(boolean enable);

    boolean isWeatherTamerEnabled();
    boolean setWeatherTamerEnabled(boolean enable);

    float getAnimationSpeed();
    boolean setAnimationSpeed(float speed);

    boolean isGestureNavigationEnabled();
    boolean setGestureNavigationEnabled(boolean enable);
    String restoreGestures();

    boolean isBankingModeEnabled();
    boolean setBankingModeEnabled(boolean enable);
    String checkUserCertificates();
    String getDetectedRiskApps();

    boolean isShizukuCloaked();
    boolean setShizukuCloaked(boolean cloak);
    boolean isShizukuDeepFrozen();
    boolean setShizukuDeepFreeze(boolean freeze);

    boolean isDevOptionsEnabled();
    boolean setDevOptionsEnabled(boolean enable);
    boolean isUsbDebuggingEnabled();
    boolean setUsbDebuggingEnabled(boolean enable);
    String startShizukuServerDirect();

    String getHardwareSpecsJson();
    String getBypassDetailedStatus();

    String scanLargeFilesJson();
    String deleteFileManual(String filePath);
    String compileAllInstalledApps();
    String restrictBackgroundDrainers();

    String getRiskAppsDetailedJson();
    boolean setAppCloaked(String packageName, boolean cloak);
    boolean cloakAllRiskApps(boolean cloak);

    boolean isAdbAuthTimeoutDisabled();
    boolean setAdbAuthTimeoutDisabled(boolean disabled);
    boolean isVerifyAdbAppsEnabled();
    boolean setVerifyAdbAppsEnabled(boolean enable);
    boolean isWifiDisplayCertEnabled();
    boolean setWifiDisplayCertEnabled(boolean enable);
    boolean isStayAwakeWhileCharging();
    boolean setStayAwakeWhileCharging(boolean stayAwake);

    void setShizukuPackageHint(String packageName);
    String getShizukuPackageName();
    String engageFullBankingStealth(boolean deepFreezeShizuku);
    String exitFullBankingStealth();
    String getStealthAuditSummary();

    String runSelectedOptimizations(String taskKeysCsv);
    int getSmallestWidthDp();
    int getSmallestWidthDpi();
    String setSmallestWidthDp(int targetDp);
}
