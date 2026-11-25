#!/usr/bin/env bash
set -euo pipefail

# Auto-detect Android SDK
if [ -z "${ANDROID_HOME:-}" ]; then
    if [ -n "${ANDROID_SDK_ROOT:-}" ]; then
        ANDROID_HOME="$ANDROID_SDK_ROOT"
    elif [ -d "$HOME/Library/Android/sdk" ]; then
        ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        ANDROID_HOME="$HOME/Android/Sdk"
    else
        echo "ERROR: Set ANDROID_HOME or ANDROID_SDK_ROOT to your Android SDK directory." >&2
        exit 1
    fi
fi

SDK_BUILD_TOOLS="${SDK_BUILD_TOOLS:-$(ls -d "$ANDROID_HOME"/build-tools/* 2>/dev/null | sort -V | tail -n1)}"
PLATFORM_DIR="${PLATFORM_DIR:-$(ls -d "$ANDROID_HOME"/platforms/android-* 2>/dev/null | sort -V | tail -n1)}"
PLATFORM_JAR="$PLATFORM_DIR/android.jar"
FRAMEWORK_AIDL="$PLATFORM_DIR/framework.aidl"

if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
    JAVA_BIN="$JAVA_HOME/bin"
elif [ -x "/opt/homebrew/opt/openjdk/bin/javac" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk"
    JAVA_BIN="$JAVA_HOME/bin"
elif command -v javac >/dev/null 2>&1; then
    JAVA_BIN="$(dirname "$(command -v javac)")"
else
    echo "ERROR: JDK (javac / keytool) not found. Set JAVA_HOME." >&2
    exit 1
fi
export PATH="$JAVA_BIN:$PATH"

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "Using build-tools: $SDK_BUILD_TOOLS"
echo "Using platform:    $PLATFORM_DIR"

SHIZUKU_API_JAR="$PROJECT_DIR/libs/shizuku-api.jar"
SHIZUKU_PROVIDER_JAR="$PROJECT_DIR/libs/shizuku-provider.jar"
SHIZUKU_AIDL_JAR="$PROJECT_DIR/libs/shizuku-aidl.jar"
SHIZUKU_SHARED_JAR="$PROJECT_DIR/libs/shizuku-shared.jar"

echo "=== 1. Cleaning and preparing directories ==="
rm -rf build gen bin obj
mkdir -p build gen bin obj

echo "=== 2. Compiling AIDL interface ==="
"$SDK_BUILD_TOOLS/aidl" -p"$FRAMEWORK_AIDL" -I"$PROJECT_DIR/src" -o"$PROJECT_DIR/gen" "$PROJECT_DIR/src/com/oneplus/optimizer/IUserService.aidl"

echo "=== 3. Compiling Android Resources (AAPT2 compile) ==="
"$SDK_BUILD_TOOLS/aapt2" compile --dir res -o build/compiled_res.zip

echo "=== 4. Linking Resources (AAPT2 link) ==="
"$SDK_BUILD_TOOLS/aapt2" link \
    -I "$PLATFORM_JAR" \
    --manifest AndroidManifest.xml \
    -o build/unaligned_res.apk \
    --java gen/ \
    --auto-add-overlay \
    build/compiled_res.zip

echo "=== 5. Compiling Java Source Files (javac) ==="
find src gen -name "*.java" > build/sources.txt
"$JAVA_BIN/javac" -source 17 -target 17 \
    -cp "$PLATFORM_JAR:$SHIZUKU_API_JAR:$SHIZUKU_PROVIDER_JAR:$SHIZUKU_AIDL_JAR:$SHIZUKU_SHARED_JAR" \
    -d obj \
    @build/sources.txt

echo "=== 6. Converting Bytecode to Dalvik Executable (D8) ==="
find obj -name "*.class" > build/classes.txt
"$SDK_BUILD_TOOLS/d8" \
    --lib "$PLATFORM_JAR" \
    --output bin/ \
    $(cat build/classes.txt) \
    "$SHIZUKU_API_JAR" \
    "$SHIZUKU_PROVIDER_JAR" \
    "$SHIZUKU_AIDL_JAR" \
    "$SHIZUKU_SHARED_JAR"

echo "=== 7. Adding classes.dex to APK ==="
cp build/unaligned_res.apk build/app_with_dex.apk
cd bin
"$JAVA_BIN/jar" -uf "$PROJECT_DIR/build/app_with_dex.apk" classes.dex
cd "$PROJECT_DIR"

echo "=== 8. Aligning APK (zipalign) ==="
"$SDK_BUILD_TOOLS/zipalign" -f -p 4 build/app_with_dex.apk build/aligned.apk

echo "=== 9. Signing APK ==="
if [ ! -f debug.keystore ]; then
    if [ -f "../AAWirelessToggle/debug.keystore" ]; then
        cp "../AAWirelessToggle/debug.keystore" debug.keystore
    else
        "$JAVA_BIN/keytool" -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
    fi
fi

"$SDK_BUILD_TOOLS/apksigner" sign \
    --ks debug.keystore \
    --ks-pass pass:android \
    --out OnePlusOptimizer.apk \
    build/aligned.apk

echo "=== Build Complete! Output: OnePlusOptimizer.apk ==="
ls -lh OnePlusOptimizer.apk
