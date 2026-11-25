#!/usr/bin/env bash
# ==============================================================================
# OxygenOS / ColorOS Network, Battery & Storage Optimizer
# ==============================================================================

set -euo pipefail

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGES_DIR="${SCRIPT_DIR}/packages"

echo -e "${BLUE}=====================================================${NC}"
echo -e "${BLUE}    OnePlus Network, Privacy & Battery Optimizer     ${NC}"
echo -e "${BLUE}=====================================================${NC}"

if ! command -v adb &>/dev/null; then
    echo -e "${RED}[ERROR] 'adb' command not found.${NC}"
    exit 1
fi

DEVICES=$(adb devices | grep -v "List" | grep "device$" | awk '{print $1}')
if [ -z "$DEVICES" ]; then
    echo -e "${RED}[ERROR] No connected device found.${NC}"
    exit 1
fi

ADB_CMD="adb shell"

# 1. Private DNS Configuration
echo -e "\n${BLUE}>>> 1. Private DNS (System-Wide Ad & Tracker Blocking)${NC}"
echo "Select DNS Provider:"
echo "  1) AdGuard DNS (dns.adguard-dns.com) [Default]"
echo "  2) Mullvad AdBlock DNS (adblock.doh.mullvad.net)"
echo "  3) Custom DNS Hostname (e.g. NextDNS profile)"
echo "  4) Skip DNS setup"

read -rp "Enter choice [1-4]: " dns_choice

case "$dns_choice" in
    1|"")
        DNS_HOST="dns.adguard-dns.com"
        ;;
    2)
        DNS_HOST="adblock.doh.mullvad.net"
        ;;
    3)
        read -rp "Enter custom DoT hostname: " DNS_HOST
        ;;
    4)
        DNS_HOST=""
        ;;
    *)
        DNS_HOST="dns.adguard-dns.com"
        ;;
esac

if [ -n "$DNS_HOST" ]; then
    echo -e "Setting Private DNS to: ${GREEN}${DNS_HOST}${NC}"
    $ADB_CMD settings put global private_dns_mode hostname
    $ADB_CMD settings put global private_dns_specifier "$DNS_HOST"
    echo -e "${GREEN}[OK] Private DNS configured.${NC}"
fi

# 2. Limit Advertising Tracking & System Recommendations
echo -e "\n${BLUE}>>> 2. Privacy & Recommendation Toggles${NC}"
echo "Enforcing Advertising ID limits and disabling OEM recommendations..."

$ADB_CMD settings put secure limit_ad_tracking 1
$ADB_CMD settings put global limit_ad_tracking 1
$ADB_CMD settings put secure settings_personalize_recommendations 0
$ADB_CMD settings put system oplus_intelligent_recommend_show 0
$ADB_CMD settings put secure qs_scene_recommendation_enable 0
$ADB_CMD settings put secure app_recommend_channel 0
$ADB_CMD settings put system kgd_pictorial_recommendation 0
$ADB_CMD settings put system kgd_pictorial_recommendation_show 0
$ADB_CMD settings put secure kgd_pictorial_recommendation 0
$ADB_CMD settings put secure kgd_pictorial_recommendation_show 0
$ADB_CMD settings put system data_statistics_switch 0
$ADB_CMD settings put secure data_statistics_switch 0
$ADB_CMD settings put global data_statistics_switch 0
$ADB_CMD settings put system oplus_customize_cta_user_experience 0
$ADB_CMD settings put secure oplus_customize_cta_user_experience 0
$ADB_CMD settings put global oplus_customize_cta_user_experience 0
$ADB_CMD settings put secure instant_usage_stats_user_consent 0
$ADB_CMD settings put global upload_apk_enable 0

# Disable Google diagnostic & error telemetry
$ADB_CMD settings put global usage_stats_consent_status 0 2>/dev/null || true
$ADB_CMD settings put secure send_action_app_error 0 2>/dev/null || true

echo -e "${GREEN}[OK] Ad tracking restricted and telemetry reporting disabled.${NC}"

# 3. Wi-Fi & Bluetooth Standby Optimizations
echo -e "\n${BLUE}>>> 3. Standby Battery & Network Power Management${NC}"

# Stop background BLE beacon scanning
echo -n "  • Disabling background Bluetooth beacon scanning... "
$ADB_CMD settings put global ble_scan_always_enabled 0
echo -e "${GREEN}[OK]${NC}"

# Enforce Wi-Fi scan throttling
echo -n "  • Enforcing background Wi-Fi scan throttling... "
$ADB_CMD settings put global wifi_scan_throttle_enabled 1
echo -e "${GREEN}[OK]${NC}"

# Enable Bad Wi-Fi automatic cellular fallback
echo -n "  • Enabling automatic failover on unresponsive Wi-Fi... "
$ADB_CMD settings put global network_avoid_bad_wifi 1
echo -e "${GREEN}[OK]${NC}"

# 4. Storage TRIM
echo -e "\n${BLUE}>>> 4. UFS Flash Storage TRIM${NC}"
echo -n "  • Running solid-state storage TRIM... "
$ADB_CMD sm fstrim >/dev/null 2>&1 || true
echo -e "${GREEN}[OK]${NC}"

# 5. Optional Silent Push Daemons
echo -e "\n${BLUE}>>> 5. OxygenOS Silent Background Push Daemons${NC}"
echo "Freeze silent pushers (RUS XML updates & system app auto-updater)?"
read -rp "Freeze silent pushers? [y/N]: " freeze_pushers
if [[ "$freeze_pushers" =~ ^[yY]$ ]]; then
    if [ -f "${PACKAGES_DIR}/silent_pushers.txt" ]; then
        while IFS= read -r pkg || [ -n "$pkg" ]; do
            [[ -z "$pkg" || "$pkg" =~ ^[[:space:]]*# ]] && continue
            echo -n "  • Freezing $pkg... "
            $ADB_CMD pm disable-user --user 0 "$pkg" 2>/dev/null || true
            $ADB_CMD cmd package suspend --user 0 "$pkg" 2>/dev/null || true
            $ADB_CMD am force-stop "$pkg" 2>/dev/null || true
            echo -e "${GREEN}[Done]${NC}"
        done < "${PACKAGES_DIR}/silent_pushers.txt"
    fi
fi

# 6. UI Responsiveness & Animation Speedups
echo -e "\n${BLUE}>>> 6. UI Snappiness & High Refresh Rate${NC}"
echo -n "  • Setting 0.5x animation scales for 2x faster UI transitions... "
$ADB_CMD settings put global window_animation_scale 0.5
$ADB_CMD settings put global transition_animation_scale 0.5
$ADB_CMD settings put global animator_duration_scale 0.5
echo -e "${GREEN}[OK]${NC}"

echo -n "  • Enabling extreme high refresh rate support across apps... "
$ADB_CMD settings put system app_extreme_high_refresh_switch 1
echo -e "${GREEN}[OK]${NC}"

echo -e "\n${GREEN}=====================================================${NC}"
echo -e "${GREEN}      Optimization applied successfully!             ${NC}"
echo -e "${GREEN}=====================================================${NC}"
