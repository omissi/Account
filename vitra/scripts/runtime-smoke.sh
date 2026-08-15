#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_DIR="${GITHUB_WORKSPACE:-$PROJECT_DIR}/runtime-evidence"
APK="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"

mkdir -p "$OUT_DIR"
adb install -r "$APK"
adb logcat -c
adb shell pm clear tech.alomessi.vitra >/dev/null
adb shell am start -W -n tech.alomessi.vitra/.MainActivity
sleep 3
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"
adb exec-out screencap -p > "$OUT_DIR/01-onboarding.png"
adb shell uiautomator dump /sdcard/onboarding.xml >/dev/null
adb pull /sdcard/onboarding.xml "$OUT_DIR/01-onboarding.xml" >/dev/null
grep -q "VITRA" "$OUT_DIR/01-onboarding.xml"

# Pixel 6 test profile: choose the Arabic first-launch button.
adb shell input tap 540 1450
sleep 3
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"
adb exec-out screencap -p > "$OUT_DIR/02-home.png"
adb shell uiautomator dump /sdcard/home.xml >/dev/null
adb pull /sdcard/home.xml "$OUT_DIR/02-home.xml" >/dev/null
grep -q "VITRA" "$OUT_DIR/02-home.xml"

# Open Settings from the bottom navigation and verify the process again.
adb shell input tap 945 2290
sleep 2
adb exec-out screencap -p > "$OUT_DIR/03-settings.png"
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"

# Return to Widgets and open the first catalog card.
adb shell input tap 135 2290
sleep 2
adb shell input tap 280 1160
sleep 2
adb exec-out screencap -p > "$OUT_DIR/04-detail.png"
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"

# Exercise all widget receivers without requiring a launcher placement.
for provider in ClockWidgetProvider DateWidgetProvider WeatherWidgetProvider PrayerWidgetProvider SearchWidgetProvider SystemWidgetProvider; do
  adb shell am broadcast -a android.appwidget.action.APPWIDGET_UPDATE -n "tech.alomessi.vitra/.widget.$provider" >/dev/null
done
sleep 2
adb logcat -d > "$OUT_DIR/runtime-logcat.txt"
! grep -q "FATAL EXCEPTION" "$OUT_DIR/runtime-logcat.txt"
! grep -q "Process: tech.alomessi.vitra.*has died" "$OUT_DIR/runtime-logcat.txt"
