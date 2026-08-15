#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_DIR="${GITHUB_WORKSPACE:-$PROJECT_DIR}/runtime-evidence"
APK="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"

mkdir -p "$OUT_DIR"
collect_failure_evidence() {
  adb logcat -d > "$OUT_DIR/runtime-logcat.txt" 2>/dev/null || true
  adb exec-out screencap -p > "$OUT_DIR/runtime-last-screen.png" 2>/dev/null || true
  adb shell dumpsys activity activities > "$OUT_DIR/runtime-activities.txt" 2>/dev/null || true
}
trap collect_failure_evidence EXIT

adb install -r "$APK"
adb logcat -c
adb shell pm clear tech.alomessi.vitra >/dev/null
adb shell am start -W -n tech.alomessi.vitra/.MainActivity
sleep 3
adb exec-out screencap -p > "$OUT_DIR/01-onboarding.png"
adb logcat -d > "$OUT_DIR/01-launch-logcat.txt"
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"
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

# Android Back reliably returns from every secondary tab to the catalog.
adb shell input keyevent 4
sleep 2
adb shell input tap 280 1160
sleep 2
adb exec-out screencap -p > "$OUT_DIR/04-detail.png"
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"

# Verify all widget providers are registered. Android correctly blocks the shell
# from forging the protected APPWIDGET_UPDATE broadcast; real updates are sent
# by the launcher after placement.
adb shell dumpsys package tech.alomessi.vitra > "$OUT_DIR/package-registration.txt"
for provider in ClockWidgetProvider DateWidgetProvider WeatherWidgetProvider PrayerWidgetProvider SearchWidgetProvider SystemWidgetProvider; do
  grep -q "$provider" "$OUT_DIR/package-registration.txt"
done
sleep 2
adb logcat -d > "$OUT_DIR/runtime-logcat.txt"
! grep -q "Process: tech.alomessi.vitra" "$OUT_DIR/runtime-logcat.txt"
trap - EXIT
