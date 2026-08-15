# Vitra — Glass Widget Studio

Vitra is an original Android widget studio by **ALOMESSI TECH**. It combines a curated widget catalog, live customization, wallpaper-aware styling, favorites, privacy-first settings, and six focused widget families instead of flooding the Android picker with dozens of near-duplicate entries.

## Current prototype

- Arabic and English first-launch choice with RTL-ready copy.
- Premium dark glass visual system and original Vitra identity.
- Search surface, category filters, New/Pro labels, compatibility and size metadata.
- 12 catalog designs covering clock, digital time, date, weather, prayer, search, system, apps, sports and quotes.
- Detail screen with device preview, permissions, compatibility, favorite and direct pin actions.
- Live customization studio: accent palettes, glass opacity, corner radius, auto-style, layer tools and save/add flow.
- Wallpapers, favorites, privacy, permission and widget diagnostics screens.
- Six real Android widget providers: Clock, Date, Weather, Prayer, Search and System.
- No registration and no sensitive permission requested on first launch.

Weather and prayer cards intentionally show an unconfigured state until a trusted data provider and regional calculation flow are connected. They never display fabricated live data.

## Build

Requirements: JDK 17, Android SDK 35 and Gradle 8.10.2.

```bash
gradle :app:assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions builds and uploads the same APK as `Vitra-debug-apk`.

## Package

`tech.alomessi.vitra` · version `0.1.0` · minimum Android 8.0 (API 26)

## Product principles

1. Reliability before widget count.
2. Six picker families; variations live in the studio.
3. Just-in-time permissions and clear empty states.
4. Arabic and English are equal product languages.
5. No copied Glassify code, assets or branding.

Copyright © ALOMESSI TECH. All rights reserved.
