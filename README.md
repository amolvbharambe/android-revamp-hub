# Android Revamp Hub (`OnePlusOptimizer`)

A lightweight, **non-root** Android system tuning, privacy, debloating, and **Banking & UPI Stealth** dashboard powered by **Shizuku** (`Shell UID 2000`).

Designed primarily for **OnePlus / OPPO / Realme (OxygenOS / ColorOS / realme UI)** flagships, with universal Android modules that work across **any Android 8–17 device** running Shizuku.

---

## Dashboard Highlights

- **Clean Collapsible Blue-Glass Accordion**: Every section starts collapsed behind a frosted **Blue Glass** expandable header bar (`Hardware Specifications`, `Live System Metrics`, `Run Master Optimization`, `Terminal Log Output`, followed by Debugging, Dev Tuning, Banking Stealth, Shields, Battery, Display/Gestures, Auto-Care, Compilation & Manual Storage Cleanup).
- **Zero-Clutter Contextual Help (`?`)**: Tap any `?` button on a section header or option row to inspect **What it does** and **Why use it** without cluttering the dashboard.
- **Hidden Advanced Toggles**: High-risk toggles (`Deep Freeze Shizuku APK`, `Smart Charging Protection`, `Extreme Refresh Rate Lock`, `Stay Awake While Charging`) stay tucked behind expandable `⚙️ Advanced:` rows with live status badges.
- **Smallest Width Switcher (`Developer Options → Drawing`)**: Switch between **Mobile View** (`360`, `420`, `480`, `520 dp`), **Tablet View** (`560`, `580 dp`), or **Stock** DPI cleanly in one tap.
- **Customizable Checklists (Default vs One-Time)**: Select from 25 system tasks (with **OEM Bloatware & Telemetry Shield** enabled by default) for both **Run Master Optimization** and **Daily Auto-Care Service**, saving a persistent **Default** profile or triggering a **One-Time** run/override without altering defaults.
- **Reboot Auto-Optimize (`BOOT_COMPLETED`)**: Optional toggle to auto-open the app after phone reboot (using `SYSTEM_ALERT_WINDOW` / actionable notification fallback) and run your Default Master Optimization checklist as soon as Shizuku connects.

---

## Compatibility Matrix

| Feature / Card | Any Android Phone (Android 8–17 + Shizuku) | OnePlus / OPPO / Realme (OxygenOS / ColorOS / realme UI) | Notes |
| :--- | :---: | :---: | :--- |
| **Hardware Specs & Live Metrics** | ✅ Yes | ✅ Yes | Auto-detects SoC, RAM, storage, display DPI/Hz, battery & temperature dynamically. |
| **Debugging & Shizuku Control** | ✅ Yes | ✅ Yes | USB ADB, Wireless ADB, Dev Options toggle, direct `libshizuku.so` startup, offline recovery. |
| **Developer Mode Tuning** | ✅ Yes | ✅ Yes | Disable ADB auth timeout, toggle Verify Apps over USB, Wireless Display cert, hidden Stay Awake. |
| **Smallest Width (`Mobile` / `Tablet` dp)** | ✅ Yes | ✅ Yes | Calculates exact `wm density` from physical display width (`360–520 dp` Mobile, `560–580 dp` Tablet, or Stock reset). |
| **Banking & UPI Stealth Shield** | ✅ Yes | ✅ Yes | Conceals Dev Options/ADB, pauses Accessibility & Private DNS, cloaks modding/VPN apps (collapsible list), stops `shizuku_server`, cleans `/data/local/tmp`, supports **Shizuku-Next Spoofed Package IDs**. |
| **AdGuard Private DNS Shield** | ✅ Yes | ✅ Yes | Configures `dns.adguard-dns.com` via Private DNS (`Android 9+`). |
| **Android Auto Cable-Only Mode** | ⚠️ Partial (`Android 13+`) | ✅ Full | Revokes `NEARBY_WIFI_DEVICES`, `ACCESS_LOCAL_NETWORK` & ignores `CHANGE_WIFI_STATE` universally; `op_aa_cable_only_mode` is OxygenOS/ColorOS only. |
| **Fast Doze, Cell Sleep & Animations** | ✅ Yes | ✅ Yes | Tunes `device_idle_constants`, `mobile_data_always_on`, and `0x`/`0.5x`/`1x` animation scales. |
| **ARM64 AOT Compilation (`dex2oat`)** | ✅ Yes | ✅ Yes | Compiles core/all user packages + SystemUI/Launcher with `cmd package compile -m speed`. |
| **Flash Storage TRIM, Cache & RAM Flush** | ✅ Yes | ✅ Yes | Runs `sm fstrim`, `pm trim-caches`, `am kill-all` + manual `/sdcard/Download` cleanup. |
| **Custom Master Opt, Daily Auto-Care & Reboot Run** | ✅ Yes | ✅ Yes | 25 selectable tasks (Default vs One-Time), scheduled alarms & optional boot auto-launch. |
| **80% Charge Limit & Bypass Charging** | ❌ No | ✅ Yes | Uses OPlus charging keys (`regular_charge_protection_*`, `bypass_charge_*`). |
| **FHD+ Scaling & Peak Refresh Lock** | ❌ No | ✅ Yes | Uses `settings_screen_resolution_state` & `app_extreme_high_refresh_switch`. |
| **Smart AOD, OPlus Gestures & Bloat Shield** | ⚠️ AOSP Nav Mode 2 Only | ✅ Full | Full bloatware list targets `com.oplus.*`, `com.heytap.*`, `com.oneplus.*`, `com.coloros.*` & Meta services. |

---

## Prerequisites

1. **Android Phone** (Android 8+, target SDK 35; tested up to Android 17).
2. **Shizuku** or **Shizuku-Next** installed and started (via Wireless Debugging or PC ADB).
   - **Recommended for Banking / UPI users:** Use **Shizuku-Next** so you can use **Stealth mode** to clone Shizuku under a clean package ID.
3. **Computer with Android SDK / Build Tools** (to compile the APK from source) and `adb` (`Android Platform Tools`).

---

## Build & Install Instructions

### 1. Build the APK
Ensure `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) and a JDK (`JAVA_HOME` / `javac` / `keytool`) are available:

```bash
chmod +x OnePlusOptimizer/build.sh
./OnePlusOptimizer/build.sh
```

Output: `OnePlusOptimizer/OnePlusOptimizer.apk`

### 2. Install via ADB (Recommended `-g` Flag)
Installing with `-g` grants `WRITE_SECURE_SETTINGS` and `CHANGE_CONFIGURATION` upfront, which allows **Android Revamp Hub** to turn **Developer Options, USB Debugging, and Wireless Debugging** back ON directly from your phone without a PC:

```bash
adb install -r -g OnePlusOptimizer/OnePlusOptimizer.apk
```

*(Note for OxygenOS / ColorOS users: Android blocks runtime `pm grant com.oneplus.optimizer android.permission.WRITE_SECURE_SETTINGS` unless "Disable Permission Monitoring" is enabled in Developer Options, whereas `adb install -r -g` grants it immediately at install time.)*

### 3. Connect Shizuku
1. Open **Shizuku** (or your spoofed Shizuku-Next clone) and tap **Start**.
2. Open **Android Revamp Hub** → tap **Authorize** and allow access.

---

## Banking & UPI Stealth Setup (Important Guide)

Modern Indian & global banking/security SDKs (**Protectt.ai**, **Lookout**, **Bureau**) hold `QUERY_ALL_PACKAGES` and explicitly blacklist `moe.shizuku.privileged.api`. Simply running `pm disable-user` ("Deep Freeze") **does not** remove `moe.shizuku.privileged.api` from Android's `PackageManager`, so banking apps can still detect it unless Shizuku-Next's package ID is spoofed.

### Part 1: One-Time Setup — Spoof Shizuku-Next Package ID

1. Open **Android Revamp Hub** → scroll to **Banking & UPI Stealth Shield** → tap **`🎭 Setup Shizuku-Next Package Spoof`** *(this copies a safe replacement package ID such as `com.mcore.services` to your clipboard)*.
2. Tap **`Open Shizuku`** → open **Settings (gear icon) → `Stealth mode`**.
3. Fill in:
   - **Display name (optional):** e.g. **`System Helper`**
   - **Package name (optional):** paste **`com.mcore.services`** *(do not leave blank, otherwise `"shizuku"` remains in the package name)*
   - **CRITICAL Warning for OnePlus / OPPO / Realme users:** **Never** choose a package name starting with `com.oplus.`, `com.oneplus.`, `com.coloros.`, or `com.heytap.`. OxygenOS rejects third-party APKs with those prefixes at install time (`INSTALL_FAILED_SHARED_USER_INCOMPATIBLE: oplus named app is not match signature`).
4. Tap **`Hide`** → tap **`Uninstall`** / **`OK`** when Android prompts to remove the original `moe.shizuku.privileged.api`.
5. If no install prompt appears after the uninstall finishes, open your **Files** app → **Internal storage → `Shizuku`** folder → tap the exported `shizuku-next-*.apk` → **Install** *(you can delete that exported `.apk` afterwards so no `"shizuku"`-named APK remains on internal storage)*.
6. **Do NOT tap `Unhide with stub`** (a stub re-creates `moe.shizuku.privileged.api`). **Android Revamp Hub** automatically discovers your spoofed clone via `moe.shizuku.manager.permission.API_V23`.
7. Open **`System Helper`** → re-pair Wireless Debugging once → tap **Start** → open **Android Revamp Hub** and grant access. The spoof banner will turn green (**`SPOOFED`**).

### Part 2: Engaging Banking Stealth Mode

1. Open **Android Revamp Hub** → **Banking & UPI Stealth Shield**.
2. Tap **`🛡️ Engage Full Stealth`** *(do not toggle Stealth Cloak before Deep Freeze)* → **`Engage Stealth (Recommended)`**.
   This automatically executes in safe order while Shizuku is alive:
   - Self-grants `WRITE_SECURE_SETTINGS` & `CHANGE_CONFIGURATION` (if permitted)
   - Saves & disables Accessibility (`enabled_accessibility_services` & `accessibility_enabled=0`)
   - Saves & pauses Private DNS (`off`)
   - Cloaks & force-stops detected sideloaded/modding/VPN tools (`AdGuard`, `ReVanced`, `WireGuard`, `Tailscale`, etc.)
   - Cleans stale dev artifacts in `/data/local/tmp` (`*.apk`, `TestCert*`, `dump*.xml`, `screen*.png`)
   - Hides Developer Options, USB Debugging, Wireless Debugging
   - Disables Shizuku Watchdog & stops `shizuku_server` cleanly
3. Optional: Tap **`🔍 Audit`** to inspect every banking detection check (`Dev/USB/WiFi ADB`, `adbd`, `persist.sys.usb.config`, `service.adb.tcp.port`, VPN interfaces, certs, `/data/local/tmp`).
   - *Tip:* If `🔍 Audit` shows `port=5555` / `adbd=running` and a strict bank still flags ADB, reboot the phone once after engaging stealth to clear the active ADB TCP port.

### Part 3: Restoring Everything After Banking

1. Open **Android Revamp Hub** → tap **`🔓 Restore All`** *(always clickable, even when Shizuku is stopped)*.
2. **Developer Options, USB Debugging, and Wireless Debugging** are restored immediately offline (when `WRITE_SECURE_SETTINGS` was granted via `adb install -r -g`).
3. If Shizuku was stopped, **Android Revamp Hub** launches your spoofed Shizuku app: tap **Start**, then press **Back** to return — **Android Revamp Hub** automatically restores **Private DNS**, **Accessibility**, and uncloaks all tools.

---

## What "Run Master Optimization" Includes (Customizable Checklist)

Out of **25 selectable tasks**, **Run Master Optimization** enables **20 core daily-driver defaults** (customizable via **Select Default Tasks** or **One-Time Run** without overwriting defaults):

1. **OEM Bloatware & Telemetry Shield — ON** *(highlighted right inside Master Optimization)* — freezes Meta stubs, HeyTap Cloud/MCS, SAUHelper & OPlus analytics
2. **AdGuard Private DNS Shield — ON** (`dns.adguard-dns.com`)
3. **Android Auto Cable-Only Mode — ON** (blocks wireless AA handshakes while whitelisting wired AA)
4. **80% Custom Charge Limit — ON** (with Smart Charging OFF; *OxygenOS/ColorOS*)
5. **Hardware Bypass Charging at ≥80% — ON** (*OxygenOS/ColorOS*)
6. **Fast Doze Instant Standby — ON** (60s idle timeout via `device_idle_constants`)
7. **Cellular Modem Sleep on Wi-Fi — ON** (`mobile_data_always_on 0`)
8. **Smart AOD & Deep Standby — ON** (*OxygenOS/ColorOS*)
9. **Weather CPU Tamer — ON** (*OxygenOS/ColorOS*)
10. **Critical Apps Whitelist — ON** (Messaging, Phone, SMS, Alarms & Shizuku)
11. **Restrict Aggressive Background Drainers — ON** (puts background social/OEM drainers into Restricted bucket)
12. **Trim Cache & Flush RAM** (`pm trim-caches`, `am kill-all`)
13. **Flash Storage Trim** (`sm fstrim`)
14. **Wired & Wireless Debugging — ON** (Dev Options, USB & Wi-Fi ADB)
15. **Banking & UPI Stealth Mode — OFF** (restores standard environment & uncloaks tools)
16. **Shizuku Uncloaked — ON** (ensures Shizuku daemon & appops stay active)
17. **Screen Resolution FHD+ — ON** (*OxygenOS/ColorOS*)
18. **Extreme Refresh Lock — OFF** (adaptive refresh scaling; *OxygenOS/ColorOS*)
19. **0.5x Snappy Animation Scale — ON**
20. **Full-Screen Gestures Shield — ON** (enforces AOSP Mode 2 + edge gesture calibration)

Optional tasks in the checklist (disabled in default Master, selectable anytime for Default or One-Time / Daily Auto-Care): **Disable ADB Auth Timeout**, **Disable Verify Apps Over USB**, **Native AOT Speed-Compile Core Apps** *(enabled in Daily Default)*, **Compile All 3rd-Party Apps**, and **Deep AOT Re-Compile ALL Apps (`-f`)**.

---

## Standalone ADB Scripts (`scripts/`)

Prefer pure CLI over the Android app? Check [`scripts/`](scripts/):
- [`scripts/debloat.sh`](scripts/debloat.sh) — Interactive category-based OEM bloatware/telemetry/Meta freezer (`packages/*.txt`)
- [`scripts/optimize.sh`](scripts/optimize.sh) — Private DNS, BLE scan throttling, bad Wi-Fi failover, `sm fstrim`
- [`scripts/compile_speed.sh`](scripts/compile_speed.sh) — Full ART AOT `dex2oat` compilation
- [`scripts/restore.sh`](scripts/restore.sh) — Reverts debloat & system settings cleanly

---

## License

MIT — provided as-is; package disabling/settings tweaks are reversible via the in-app **Restore** / **Uncloak** controls or `adb shell pm enable <pkg>`.
