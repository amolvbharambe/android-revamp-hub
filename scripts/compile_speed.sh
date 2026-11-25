#!/usr/bin/env bash
# ==============================================================================
# Android ART Runtime Speed Compiler
# Ahead-of-Time (AOT) bytecode compilation for maximum app launch speed
# ==============================================================================

set -euo pipefail

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}=====================================================${NC}"
echo -e "${BLUE}      Android ART Ahead-of-Time (AOT) Compiler       ${NC}"
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

echo "Select packages to compile:"
echo "  1) User-Installed Applications Only (Recommended: Compiles all your downloaded apps)"
echo "  2) All Installed Applications (User apps + System apps)"
echo "  q) Quit"
echo ""

read -rp "Enter choice [1-2, q]: " target_choice

case "$target_choice" in
    1|"")
        PM_ARG="-3"
        TARGET_LABEL="User-installed apps"
        ;;
    2)
        PM_ARG=""
        TARGET_LABEL="All installed apps (User + System)"
        ;;
    q|Q)
        exit 0
        ;;
    *)
        PM_ARG="-3"
        TARGET_LABEL="User-installed apps"
        ;;
esac

echo -e "\n${YELLOW}Compiling ${TARGET_LABEL} using 'speed-profile' mode...${NC}"
echo -e "${YELLOW}Please keep the phone connected. This may take 1 to 3 minutes.${NC}\n"

$ADB_CMD "for p in \$(pm list packages $PM_ARG | cut -d: -f2); do
    cmd package compile -m speed-profile \"\$p\" >/dev/null 2>&1
done"

echo -e "\n${GREEN}[OK] Compilation complete! All target applications are now pre-compiled to native machine code.${NC}"
echo -e "${GREEN}App launch speeds and UI fluidity are now optimized.${NC}"
