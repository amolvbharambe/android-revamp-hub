#!/usr/bin/env bash
# ==============================================================================
# OxygenOS / ColorOS Modular Debloater
# Tested on OnePlus 12 / 13 / 15 / Ace Series (Android 14, 15, 17)
# ==============================================================================

set -euo pipefail

# ANSI color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGES_DIR="${SCRIPT_DIR}/packages"

echo -e "${BLUE}=====================================================${NC}"
echo -e "${BLUE}      OnePlus / OxygenOS Modular Debloat Suite       ${NC}"
echo -e "${BLUE}=====================================================${NC}"

# 1. Verify ADB connection
if ! command -v adb &>/dev/null; then
    echo -e "${RED}[ERROR] 'adb' command not found. Please install Android Platform Tools.${NC}"
    exit 1
fi

DEVICES=$(adb devices | grep -v "List" | grep "device$" | awk '{print $1}')
if [ -z "$DEVICES" ]; then
    echo -e "${RED}[ERROR] No authorized ADB device found. Connect device via USB or Wireless ADB.${NC}"
    exit 1
fi

DEVICE_COUNT=$(echo "$DEVICES" | wc -l | tr -d ' ')
if [ "$DEVICE_COUNT" -gt 1 ]; then
    echo -e "${YELLOW}Multiple devices detected. Please select target serial:${NC}"
    select TARGET in $DEVICES; do
        ADB_CMD="adb -s $TARGET shell"
        break
    done
else
    ADB_CMD="adb shell"
fi

MODEL=$($ADB_CMD getprop ro.product.model || echo "Android Device")
RELEASE=$($ADB_CMD getprop ro.build.version.release || echo "Unknown")
echo -e "${GREEN}[OK] Connected to: ${MODEL} (Android ${RELEASE})${NC}\n"

# Helper to neutralize a package safely
neutralize_pkg() {
    local pkg="$1"
    echo -n "  • Processing ${pkg}... "

    # 1. Attempt user disable
    local disable_res
    disable_res=$($ADB_CMD pm disable-user --user 0 "$pkg" 2>&1 || true)

    # 2. Attempt user uninstall
    local uninst_res
    uninst_res=$($ADB_CMD pm uninstall -k --user 0 "$pkg" 2>&1 || true)

    # 3. If restricted system app, suspend it
    if [[ "$uninst_res" == *"Failure"* ]] || [[ "$disable_res" == *"default"* ]]; then
        $ADB_CMD cmd package suspend --user 0 "$pkg" 2>/dev/null || true
    fi

    # 4. Force stop & revoke background execution
    $ADB_CMD am force-stop "$pkg" 2>/dev/null || true
    $ADB_CMD cmd appops set "$pkg" RUN_IN_BACKGROUND ignore 2>/dev/null || true
    $ADB_CMD cmd appops set "$pkg" POST_NOTIFICATION ignore 2>/dev/null || true

    echo -e "${GREEN}[Done]${NC}"
}

process_file() {
    local file="$1"
    local title="$2"

    if [ ! -f "$file" ]; then
        echo -e "${RED}File $file not found!${NC}"
        return
    fi

    echo -e "\n${BLUE}>>> ${title}${NC}"
    while IFS= read -r line || [ -n "$line" ]; do
        # Skip empty lines and comments
        [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
        neutralize_pkg "$line"
    done < "$file"
}

# Menu
echo "Choose debloat categories to apply:"
echo "  1) Category A: Adware, Lockscreen Recommendations & Instant Apps (Recommended)"
echo "  2) Category B: Background Telemetry, Analytics & Loggers (Recommended)"
echo "  3) Category C: Preloaded Meta / Facebook Services (Recommended)"
echo "  4) Category D: OEM Store & Community Apps (OnePlus Store, Forums, Red Cable Club)"
echo "  5) All Recommended (Categories A + B + C)"
echo "  6) Full Debloat (Categories A + B + C + D)"
echo "  q) Quit"
echo ""

read -rp "Enter choice [1-6, q]: " choice

case "$choice" in
    1)
        process_file "${PACKAGES_DIR}/adware.txt" "Category A: Adware & Recommendations"
        ;;
    2)
        process_file "${PACKAGES_DIR}/telemetry.txt" "Category B: Telemetry & Analytics"
        ;;
    3)
        process_file "${PACKAGES_DIR}/facebook.txt" "Category C: Meta / Facebook Services"
        ;;
    4)
        process_file "${PACKAGES_DIR}/oem_apps.txt" "Category D: OEM Store & Community"
        ;;
    5)
        process_file "${PACKAGES_DIR}/adware.txt" "Category A: Adware & Recommendations"
        process_file "${PACKAGES_DIR}/telemetry.txt" "Category B: Telemetry & Analytics"
        process_file "${PACKAGES_DIR}/facebook.txt" "Category C: Meta / Facebook Services"
        ;;
    6)
        process_file "${PACKAGES_DIR}/adware.txt" "Category A: Adware & Recommendations"
        process_file "${PACKAGES_DIR}/telemetry.txt" "Category B: Telemetry & Analytics"
        process_file "${PACKAGES_DIR}/facebook.txt" "Category C: Meta / Facebook Services"
        process_file "${PACKAGES_DIR}/oem_apps.txt" "Category D: OEM Store & Community"
        ;;
    q|Q)
        echo "Exiting."
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice.${NC}"
        exit 1
        ;;
esac

echo -e "\n${GREEN}=====================================================${NC}"
echo -e "${GREEN}      Debloat completed successfully!                ${NC}"
echo -e "${GREEN}=====================================================${NC}"
