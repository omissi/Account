# Vitra — Glass Widget Studio

Vitra is an original Android widget studio by **ALOMESSI TECH**. It combines a curated widget catalog, live customization, wallpaper-aware styling, favorites, privacy-first settings, and six focused widget families instead of flooding the Android picker with dozens of near-duplicate entries.

## Version 1.1

- Arabic and English first-launch choice with RTL-ready copy.
- Glassify-inspired pure-black catalog language, while keeping original Vitra code, branding and artwork.
- Search, category filters, compatibility and size metadata.
- 30 independently rendered catalog designs covering analog clocks, digital/world time, date, weather, prayer, search, system, apps, people, sports, counters and quotes.
- Detail screen with device preview, permissions, compatibility, favorite and direct pin actions.
- Full customization studio: clear/fill/gradient/image backgrounds, normal/blur/fractal effects, accent palettes, opacity, darkness, blur, radius and save/add flow.
- Drawer navigation plus search, wallpapers, favorites, tutorials, appearance, date/time, weather, prayer, synchronization, privacy and about screens.
- Six real Android widget providers: Clock, Date, Weather, Prayer, Search and System.
- Live, cached weather from Open-Meteo and prayer times from Aladhan for nine selectable cities.
- Per-widget accent, glass opacity and corner-radius configuration through the Android widget configuration flow; the chosen style is rendered into the real RemoteViews background.
- No registration and no sensitive permission requested on first launch.

## Build

Requirements: JDK 17, Android SDK 36 and Gradle 8.11.1.

```bash
gradle :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. The release workflow also builds an unsigned release APK and AAB, runs Android lint, and uploads all reports as GitHub Actions artifacts. A Play upload must be signed with the publisher's private upload key; keys are deliberately never stored in this repository.

## Package

`tech.alomessi.vitra` · version `1.1.0` (11) · minimum Android 8.0 (API 26) · target Android 16 (API 36)

## Product principles

1. Reliability before widget count.
2. Six picker families; variations live in the studio.
3. Just-in-time permissions and clear empty states.
4. Arabic and English are equal product languages.
5. No copied Glassify code, assets or branding.

## Data sources

Weather is powered by Open-Meteo and prayer times by Aladhan. Vitra sends the coordinates of the city selected from its built-in list; it does not request precise device location. See `docs/DATA_SOURCES.md` and `docs/PRIVACY_AR.md` before publishing. The free Open-Meteo API is intended for non-commercial use; a monetized release must use an appropriate paid/commercial plan or replace the provider.

Copyright © ALOMESSI TECH. All rights reserved.
