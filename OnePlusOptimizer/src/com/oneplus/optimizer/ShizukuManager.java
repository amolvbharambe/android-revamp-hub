package com.oneplus.optimizer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import java.util.List;
import rikka.shizuku.Shizuku;

public class ShizukuManager {

    public static final String ORIGINAL_SHIZUKU_PKG = "moe.shizuku.privileged.api";
    public static final String SHIZUKU_PERMISSION = "moe.shizuku.manager.permission.API_V23";

    public interface Callback {
        void onReady(IUserService service);
        void onError(String message);
    }

    private static IUserService sUserService = null;

    private static final Shizuku.UserServiceArgs sArgs = new Shizuku.UserServiceArgs(
            new ComponentName("com.oneplus.optimizer", MyUserService.class.getName()))
            .daemon(false)
            .processNameSuffix("service")
            .tag("optimizer")
            .version(1);

    /** Resolves active Shizuku Manager package ID (supports Shizuku-Next Stealth clones). */
    public static String resolveShizukuPackage(Context context) {
        if (context == null) return ORIGINAL_SHIZUKU_PKG;
        PackageManager pm = context.getPackageManager();
        try {
            PermissionInfo pi = pm.getPermissionInfo(SHIZUKU_PERMISSION, 0);
            if (pi != null && pi.packageName != null && !pi.packageName.isEmpty()) {
                return pi.packageName;
            }
        } catch (Throwable ignored) {}

        try {
            Intent intent = new Intent("rikka.shizuku.intent.action.REQUEST_BINDER");
            List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DISABLED_COMPONENTS);
            if (list != null) {
                for (ResolveInfo ri : list) {
                    if (ri.activityInfo != null && ri.activityInfo.packageName != null
                            && !ORIGINAL_SHIZUKU_PKG.equals(ri.activityInfo.packageName)) {
                        return ri.activityInfo.packageName;
                    }
                }
                for (ResolveInfo ri : list) {
                    if (ri.activityInfo != null && ri.activityInfo.packageName != null) {
                        return ri.activityInfo.packageName;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return ORIGINAL_SHIZUKU_PKG;
    }

    public static boolean isPackageInstalled(Context context, String pkg) {
        if (context == null || pkg == null || pkg.isEmpty()) return false;
        try {
            context.getPackageManager().getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isOriginalShizukuInstalled(Context context) {
        return isPackageInstalled(context, ORIGINAL_SHIZUKU_PKG);
    }

    public static boolean isShizukuSpoofed(Context context) {
        String resolved = resolveShizukuPackage(context);
        return !isOriginalShizukuInstalled(context)
                && !ORIGINAL_SHIZUKU_PKG.equals(resolved)
                && isPackageInstalled(context, resolved);
    }

    public static Intent getShizukuLaunchIntent(Context context) {
        if (context == null) return null;
        PackageManager pm = context.getPackageManager();
        String pkg = resolveShizukuPackage(context);
        Intent intent = pm.getLaunchIntentForPackage(pkg);
        if (intent == null && !ORIGINAL_SHIZUKU_PKG.equals(pkg)) {
            intent = pm.getLaunchIntentForPackage(ORIGINAL_SHIZUKU_PKG);
        }
        return intent;
    }

    public static boolean isShizukuAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasShizukuPermission() {
        if (!isShizukuAvailable()) return false;
        try {
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void clearCachedService() {
        sUserService = null;
    }

    public static void getService(Context context, Callback callback) {
        if (sUserService != null && sUserService.asBinder().isBinderAlive()) {
            try {
                sUserService.setShizukuPackageHint(resolveShizukuPackage(context));
            } catch (Throwable ignored) {}
            callback.onReady(sUserService);
            return;
        }

        if (!isShizukuAvailable()) {
            callback.onError("Shizuku is not running. Please launch the Shizuku app first.");
            return;
        }

        if (!hasShizukuPermission()) {
            callback.onError("Shizuku permission not granted. Please tap Authorize.");
            return;
        }

        final Context appCtx = context != null ? context.getApplicationContext() : null;
        try {
            Shizuku.bindUserService(sArgs, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    sUserService = IUserService.Stub.asInterface(service);
                    try {
                        sUserService.setShizukuPackageHint(resolveShizukuPackage(appCtx != null ? appCtx : context));
                    } catch (Throwable ignored) {}
                    callback.onReady(sUserService);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    sUserService = null;
                }
            });
        } catch (Exception e) {
            callback.onError("Failed to connect Shizuku service: " + e.getMessage());
        }
    }
}
