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

tap_text() {
  local wanted="$1"
  adb shell uiautomator dump /sdcard/tap.xml >/dev/null
  adb pull /sdcard/tap.xml "$OUT_DIR/tap.xml" >/dev/null
  local coordinates
  coordinates="$(python3 - "$OUT_DIR/tap.xml" "$wanted" <<'PY'
import re
import sys
import xml.etree.ElementTree as ET

path, wanted = sys.argv[1], sys.argv[2]
for node in ET.parse(path).iter("node"):
    visible = node.attrib.get("text", "")
    described = node.attrib.get("content-desc", "")
    if visible == wanted or described == wanted or wanted in visible or wanted in described:
        points = [int(v) for v in re.findall(r"\d+", node.attrib.get("bounds", ""))]
        if len(points) == 4:
            print((points[0] + points[2]) // 2, (points[1] + points[3]) // 2)
            break
PY
)"
  test -n "$coordinates"
  adb shell input tap $coordinates
}

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
grep -q "V I T R A" "$OUT_DIR/01-onboarding.xml"

# Choose Arabic by semantic text rather than fragile screen coordinates.
tap_text "ابدأ بالعربية"
sleep 3
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"
adb exec-out screencap -p > "$OUT_DIR/02-home.png"
adb shell uiautomator dump /sdcard/home.xml >/dev/null
adb pull /sdcard/home.xml "$OUT_DIR/02-home.xml" >/dev/null
grep -q "V I T R A" "$OUT_DIR/02-home.xml"

# Open the Glassify-style drawer, then Settings.
tap_text "•••"
sleep 1
tap_text "الإعدادات"
sleep 2
adb exec-out screencap -p > "$OUT_DIR/03-settings.png"
adb shell uiautomator dump /sdcard/settings.xml >/dev/null
adb pull /sdcard/settings.xml "$OUT_DIR/03-settings.xml" >/dev/null
grep -q "الإعدادات" "$OUT_DIR/03-settings.xml"
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"

# Android Back returns from settings to the catalog.
adb shell input keyevent 4
sleep 2
tap_text "الساعة الكلاسيكية"
sleep 2
adb exec-out screencap -p > "$OUT_DIR/04-detail.png"
adb shell uiautomator dump /sdcard/detail.xml >/dev/null
adb pull /sdcard/detail.xml "$OUT_DIR/04-detail.xml" >/dev/null
grep -q "تخصيص التصميم" "$OUT_DIR/04-detail.xml"
test -n "$(adb shell pidof tech.alomessi.vitra | tr -d '\r')"

# Open the full customization studio and verify the missing screen now exists.
tap_text "تخصيص التصميم"
sleep 2
adb exec-out screencap -p > "$OUT_DIR/05-editor.png"
adb shell uiautomator dump /sdcard/editor.xml >/dev/null
adb pull /sdcard/editor.xml "$OUT_DIR/05-editor.xml" >/dev/null
grep -q "تخصيص الويدجت" "$OUT_DIR/05-editor.xml"
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
