# Vitra data sources

## Weather — Open-Meteo

- Endpoint: `https://api.open-meteo.com/v1/forecast`
- Sent: latitude and longitude of the city explicitly selected from Vitra's built-in list, or the user-approved Android location when the user explicitly enables that option.
- Stored locally: temperature, WMO-derived condition text and last-update timestamp.
- Attribution appears in the app and widget.
- The free API is non-commercial and requires attribution under CC BY 4.0. Before enabling ads, subscriptions or any commercial distribution, the publisher must obtain an appropriate commercial plan or replace this integration.

## Prayer times — Aladhan

- Endpoint: `https://api.aladhan.com/v1/timings`
- Sent: latitude and longitude of the city explicitly selected from the built-in list, or user-approved device location, with the chosen calculation method.
- Stored locally: daily Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha values and last-update timestamp.
- Attribution appears in the app and widget.
- Prayer times depend on calculation standards and may differ from authoritative local calendars. The product must present them as estimates, not as a religious authority.

## Operational behavior

- Network calls use HTTPS, 9-second connect/read timeouts and a 10-minute in-process throttle.
- If either provider is unavailable, the last successful local value remains visible; Vitra does not invent a replacement value.
- Vitra requests device location only after the user presses “Use my location”; changing city remains an explicit user action.
