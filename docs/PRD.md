# Product Requirements Document
## FriLuft
**Version:** 1.0  
**Date:** 2026-07-07  
**Author:** Ali Abdullah  
**Company:** W3Footprint

---

## 1. Problem Statement

Sweden has over 400,000 km² of forests, national parks, and coastline. Outdoor activity — hiking, cycling, running, camping — is central to Swedish culture. Yet most weather apps are generic: they show temperature and a cloud icon, leaving the user to guess whether conditions are actually suitable for going outside.

Planning an outdoor activity in Sweden requires reading multiple signals simultaneously — temperature and wind chill in winter, humidity and heat in summer, rain probability, wind speed for cycling. Current apps don't synthesize this into a clear recommendation. Users are left to interpret raw numbers themselves.

FriLuft solves this with a single daily Outdoor Score that tells the user, in plain Swedish, whether today is worth going outside and why.

---

## 2. Goal

Build a professional Android weather application targeting the Swedish outdoor enthusiast that:
- Shows current weather and a 7-day forecast from SMHI's authoritative data
- Translates weather conditions into a clear, actionable Outdoor Score
- Works fully offline after the first load
- Requires zero setup — no account, no API key, no subscription

---

## 3. Target Users

### Primary — Swedish Outdoor Enthusiast
- Aged 25–55, active lifestyle (hiking, cycling, running, camping)
- Checks weather before deciding to go outside
- Trusts SMHI data — familiar with it from Swedish TV forecasts
- Wants a quick, clear answer, not a weather dashboard
- Language: Swedish

### Secondary — Casual User
- Wants a clean, fast weather app with GPS auto-detection
- Uses city search to check weather for upcoming trips within Sweden
- Values simplicity over feature depth

---

## 4. Core Principles

- **One clear answer.** The Outdoor Score is the centrepiece — not a feature buried in a tab. Every screen element should support or enhance that answer.
- **SMHI as the source of truth.** Swedish users recognise SMHI. No third-party weather data.
- **No account required.** Open the app, get the weather. No sign-up, no paywall, no tracking.
- **Offline resilience.** A 30-minute cache means the app shows data even without connectivity.
- **Swedish throughout.** UI text, city names, day labels, and score labels are all in Swedish.

---

## 5. Outdoor Score Logic

The score is calculated from current weather parameters:

| Condition | Score |
|---|---|
| Precipitation ≥ 2.0 mm/h | Stanna inne |
| Wind ≥ 10.0 m/s | Stanna inne |
| Temperature < −10°C | Stanna inne |
| Precipitation 0.5–1.9 mm/h | Går bra |
| Wind 7.0–9.9 m/s | Går bra |
| Temperature < −5°C | Går bra |
| Temperature > 32°C | Går bra |
| All other conditions | Perfekt utomhus |

Each score includes a plain-Swedish reason: *"Kraftigt regn (3 mm/h)"*, *"Blåsigt (8 m/s)"*, *"Bra väder för friluftsliv"*.

---

## 6. Feature Scope

### v1.0

| Feature | Description |
|---|---|
| Home screen | Current temperature, feels-like, weather description, Outdoor Score card, wind/precipitation/humidity |
| Outdoor Score | GOOD / OKAY / STAY_INSIDE with Swedish label and reason |
| GPS detection | Auto-detects location on launch, reverse-geocodes to Swedish city name |
| City search | Search across 27 Swedish cities, persists last selection |
| 7-day forecast | Daily min/max temperatures, dominant weather symbol, day label in Swedish |
| Hourly chart | Canvas-drawn temperature line chart for the next 12 hours |
| Offline cache | 30-minute Room cache; serves stale data on network failure |

### Not in v1.0

- Push notifications / weather alerts
- Widget
- Precipitation radar / map
- Multiple saved locations
- Apple Watch / Wear OS companion

---

## 7. SMHI API

**Endpoint:** `https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/{lon}/lat/{lat}/data.json`

**Key parameters used:**

| SMHI parameter | Meaning |
|---|---|
| `t` | Air temperature (°C) |
| `ws` | Wind speed (m/s) |
| `wd` | Wind direction (degrees) |
| `pmean` | Mean precipitation (mm/h) |
| `r` | Relative humidity (%) |
| `vis` | Visibility (km) |
| `Wsymb2` | Weather symbol 1–27 |

No API key required. Rate limiting: one request per location per 30 minutes (enforced client-side via Room cache).

---

## 8. Cities

27 Swedish cities with hardcoded coordinates:

Stockholm, Göteborg, Malmö, Uppsala, Västerås, Örebro, Linköping, Helsingborg, Jönköping, Norrköping, Lund, Umeå, Gävle, Borås, Eskilstuna, Södertälje, Karlstad, Täby, Växjö, Halmstad, Sundsvall, Luleå, Trollhättan, Östersund, Borlänge, Falun, Kalmar.

---

## 9. Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | Fetch SMHI weather data |
| `ACCESS_FINE_LOCATION` | GPS coordinates for current location |
| `ACCESS_COARSE_LOCATION` | Fallback GPS when fine location unavailable |

Location permission is requested on first launch with a clear Swedish rationale. If denied, the user is prompted to search for a city manually.

---

## 10. Non-functional Requirements

| Requirement | Target |
|---|---|
| Cold start to weather displayed | < 3 seconds on 4G |
| Cached load time | < 500ms |
| Minimum Android version | 8.0 (API 26) |
| App size | < 15 MB |
| Offline support | Full UI with last-fetched data |
