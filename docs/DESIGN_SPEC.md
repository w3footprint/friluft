# Design Specification
## FriLuft
**Version:** 1.0  
**Date:** 2026-07-07  
**Author:** Ali Abdullah

---

## 1. Design Philosophy

FriLuft should feel like stepping outside and looking up at the sky — immediate, calm, and clear. The primary purpose of every screen is to answer one question: *should I go outside?* Everything else is context.

**Keywords:** Fresh · Clear · Swedish · Outdoors

---

## 2. Design System

### Colors

| Token | Hex | Usage |
|---|---|---|
| SkyBlue | #4A90D9 | Primary — buttons, links, active states, temperature hero gradient |
| DeepBlue | #1B3A5C | Secondary — gradient end, dark surfaces |
| CloudGray | #ECF0F5 | Background — light, airy, like an overcast sky |
| SunYellow | #F5A623 | Outdoor Score: OKAY |
| GrassGreen | #4CAF50 | Outdoor Score: GOOD |
| StormRed | #E53935 | Outdoor Score: STAY_INSIDE |
| NightBlue | #0D1B2A | Dark mode background |

### Typography (Material 3)

| Style | Usage |
|---|---|
| Display (80sp bold) | Current temperature on Home |
| Title Large | Screen titles, card section labels |
| Title Medium | Forecast day labels, section headers |
| Body Large | City name in top bar, detail values |
| Body Medium | Score reason text, secondary body |
| Label Medium | Hour labels in chart, coordinate text in Search |

### Spacing

| Token | Value |
|---|---|
| xs | 4dp |
| sm | 8dp |
| md | 16dp |
| lg | 24dp |
| xl | 32dp |

### Corner Radius

| Component | Radius |
|---|---|
| Temperature hero | 20dp |
| Score card | 16dp |
| Details card | 16dp |
| Forecast day row | 12dp |
| Search input | 12dp |
| City rows | 0dp (list items) |

### Elevation

| Component | Elevation |
|---|---|
| Weather details card | 2dp |
| Forecast day rows | 1dp |
| Score card | 0dp (color carries the weight) |

---

## 3. Screen Inventory

| Screen | Route | Entry point |
|---|---|---|
| Home | `home` | App launch |
| Forecast | `forecast` | "Visa 7-dagarsprognos" button on Home |
| Search | `search` | Search icon in Home top bar |

---

## 4. Screen Designs

### Home

**Top bar**
- Left: location pin icon + city name (or "FriLuft" as fallback)
- Right: search icon → navigates to Search

**Content (scrollable, 16dp horizontal padding)**

1. **Temperature Hero** — full-width card, 220dp tall, vertical gradient (SkyBlue → DeepBlue), rounded 20dp
   - Large temperature: `${temp}°` at 80sp bold, white
   - Feels-like: `"Känns som ${feelsLike}°"` body large, white 85% opacity
   - Weather description: `weatherSymbolLabel()` title medium, white 90% opacity

2. **Outdoor Score Card** — full-width, 16dp padding inside
   - Background: GrassGreen / SunYellow / StormRed
   - Text color: White (GOOD, STAY_INSIDE) / Black (OKAY)
   - Title: score label (`"Perfekt utomhus"` / `"Går bra"` / `"Stanna inne"`) — title large bold
   - Subtitle: score reason — body medium, 85% opacity

3. **Weather Details Card** — surface color, 2dp elevation
   - Three `DetailItem` columns: Wind (m/s), Precipitation (mm/h), Humidity (%)
   - Each: icon (primary tint) + value (title medium semi-bold) + label (label medium, 60% opacity)

4. **Forecast button** — full-width, 12dp rounded, "Visa 7-dagarsprognos"

**Loading state:** `CircularProgressIndicator` centred  
**Permission denied state:** centred column with location icon, title, body, retry button  
**Error state (no cache):** error text centred, error color

---

### Forecast

**Top bar**
- Back arrow (AutoMirrored) + title "7-dagarsprognos"

**Content (LazyColumn, 16dp horizontal padding)**

1. **Section label:** "Timprognos idag" — title medium semi-bold

2. **Hourly Chart Card** — surface color, 2dp elevation, 16dp rounded
   - Top row: horizontally scrollable temperature + hour labels (12 entries, 52dp each)
   - Canvas line chart: temperature curve, 3dp stroke, circles at each point
   - Chart height: 80dp
   - Color: `MaterialTheme.colorScheme.primary`

3. **Section label:** "Veckoöversikt"

4. **DailyForecastRow** (×7) — surface card, 1dp elevation, 12dp rounded
   - Left: day label in Swedish (`"Mån 7 jul"`) — body large — `weight(1f)`
   - Centre: weather description — body medium, 60% opacity — `weight(1f)`
   - Right: `"${min}° / ${max}°"` — body large semi-bold

**Loading state:** `CircularProgressIndicator` centred  
**Error state:** error text centred

---

### Search

**Top bar**
- Back arrow + title "Sök stad"

**Content**

1. **OutlinedTextField** — full-width, 12dp rounded, search leading icon, placeholder "Sök bland svenska städer…"

2. **LazyColumn** of `CityRow` items
   - Each row: `LocationCity` icon (primary, 20dp) + city name (body large) + coordinates (label medium, 50% opacity)
   - `HorizontalDivider` between rows (8% opacity)
   - Tap: saves city to DataStore, pops back to Home

---

## 5. Weather Symbol Labels (Swedish)

| Symbol | Label |
|---|---|
| 1 | Klart |
| 2 | Nästan klart |
| 3–4 | Halvklart |
| 5–6 | Molnigt |
| 7 | Dimma |
| 8–10 | Regnskurar |
| 11 | Åskväder |
| 12–14 | Snöblandad regn |
| 15–17 | Snöbyar |
| 18–20 | Regn |
| 21 | Åska med regn |
| 22–24 | Snöblandat regn |
| 25–27 | Snöfall |

---

## 6. Motion & Transitions

Navigation uses default Compose NavHost transitions (no custom animation in v1.0). Scroll is standard `verticalScroll` / `LazyColumn`. Chart renders immediately on composition — no animated draw.

---

## 7. Accessibility

- All icons include `contentDescription` (or `null` for purely decorative ones)
- Color alone is not the only signal for Outdoor Score — the label text reinforces it
- Minimum touch target: 48dp (Material 3 default)
- Swedish locale used for date formatting, geocoding, and all UI strings
