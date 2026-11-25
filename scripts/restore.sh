#!/usr/bin/env bash
# ==============================================================================
# OxygenOS / ColorOS Restore & Re-Enable Script
# Reverses debloat actions and restores default system behavior
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
echo -e "${BLUE}      OnePlus / OxygenOS Restore & Re-Enable Suite   ${NC}"
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

restore_pkg() {
    local pkg="$1"
    echo -n "  • Restoring ${pkg}... "

    $ADB_CMD pm install-existing "$pkg" 2>/dev/null || true
    $ADB_CMD pm enable "$pkg" 2>/dev/null || true
    $ADB_CMD cmd package unsuspend --user 0 "$pkg" 2>/dev/null || true
    $ADB_CMD cmd appops set "$pkg" RUN_IN_BACKGROUND default 2>/dev/null || true

    echo -e "${GREEN}[Restored]${NC}"
}

restore_file() {
    local file="$1"
    local title="$2"

    if [ ! -f "$file" ]; then
        echo -e "${RED}File $file not found!${NC}"
        return
    fi

    echo -e "\n${BLUE}>>> ${title}${NC}"
    while IFS= read -r line || [ -n "$line" ]; do
        [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
        restore_pkg "$line"
    done < "$file"
}

echo "Select restore action:"
echo "  1) Restore Category A (Adware & Recommendations: App Market, Theme Store, Glance)"
echo "  2) Restore Category B (Telemetry & Loggers)"
echo "  3) Restore Category C (Meta / Facebook Services)"
echo "  4) Restore Category D (OnePlus Store, Community, Red Cable Club)"
echo "  5) Restore ALL Packages"
echo "  6) Reset Private DNS to Off"
echo "  7) Restore Specific Package by Name"
echo "  q) Quit"
echo ""

read -rp "Enter choice [1-7, q]: " choice

case "$choice" in
    1)
        restore_file "${PACKAGES_DIR}/adware.txt" "Restoring Adware & Recommendations"
        ;;
    2)
        restore_file "${PACKAGES_DIR}/telemetry.txt" "Restoring Telemetry & Loggers"
        ;;
    3)
        restore_file "${PACKAGES_DIR}/facebook.txt" "Restoring Meta / Facebook Services"
        ;;
    4)
        restore_file "${PACKAGES_DIR}/oem_apps.txt" "Restoring OEM Store & Community"
        ;;
    5)
        restore_file "${PACKAGES_DIR}/adware.txt" "Restoring Adware & Recommendations"
        restore_file "${PACKAGES_DIR}/telemetry.txt" "Restoring Telemetry & Loggers"
        restore_file "${PACKAGES_DIR}/facebook.txt" "Restoring Meta / Facebook Services"
        restore_file "${PACKAGES_DIR}/oem_apps.txt" "Restoring OEM Store & Community"
        ;;
    6)
        echo "Resetting Private DNS..."
        $ADB_CMD settings put global private_dns_mode off
        $ADB_CMD settings put global private_dns_specifier ""
        echo -e "${GREEN}[OK] Private DNS disabled.${NC}"
        ;;
    7)
        read -rp "Enter package name (e.g. com.heytap.market): " custom_pkg
        restore_pkg "$custom_pkg"
        ;;
    q|Q)
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice.${NC}"
        exit 1
        ;;
esac

echo -e "\n${GREEN}Restore completed successfully.${NC}"
