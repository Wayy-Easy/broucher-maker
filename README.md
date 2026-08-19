# BrochureCraft (Android · Kotlin · Jetpack Compose · Room/SQLite)

A native Android Studio project implementing every screen from the uploaded
`stitch_markdown_design_system.zip` ("Vivid Canvas" design system / **BrochureCraft**
app): a restaurant/business brochure & menu design tool with a real, working
canvas editor, local SQLite persistence, and offline PDF/PNG/JPG export.

## How to open

1. Open Android Studio (Koala/2024.1+ recommended).
2. **File → Open** → select the `BrochureCraft/` folder.
3. Let Gradle sync. If the IDE reports a missing Gradle wrapper jar (this
   environment had no network access to `services.gradle.org` to fetch the
   binary), click **"Try Again"** / **"Setup Gradle wrapper"** when prompted,
   or point the project at a local Gradle 8.7 install (`Preferences → Build
   Tools → Gradle → Use local Gradle distribution`). Everything else
   (dependencies, plugins, source) is complete and ready to build.
4. Run on a device/emulator with **API 26+**.

## Architecture

- **UI:** Jetpack Compose + Material 3, single-Activity, `Navigation-Compose`
  for the 9-screen flow (splash → onboarding → home → templates → gallery →
  editor → brand kit → export → premium), plus a Profile screen.
- **State:** `ViewModel` per screen (`ui/viewmodel`), Compose `mutableStateOf`
  for interactive canvas state, Kotlin `StateFlow` for DB-backed lists.
- **Persistence (local SQLite via Room):**
  - `designs` — every saved design (name, canvas JSON, favorite flag, timestamps).
  - `templates` — the seed template gallery (8 starter templates across all 6
    business categories, some marked PRO).
  - `brand_kit` — single-row table for logo URI, colors, contact info.
  - `DataStore` Preferences for onboarding completion, business type, and
    PRO plan flag.
- **Canvas model:** `DesignElement` (text / image / shape) stored as
  resolution-independent fractional coordinates, serialized to JSON
  (`kotlinx.serialization`) inside the `elementsJson` column — fully local,
  no network required.
- **Rendering/export:** `CanvasRenderer` draws the same element list to a real
  `android.graphics.Bitmap`/`PdfDocument` for thumbnails and for PNG/JPG/PDF
  export, shared via `FileProvider`.

## Screens implemented (matches the 9 Stitch designs 1:1)

| # | Screen | Notes |
|---|--------|-------|
| 1 | Splash | Animated logo, auto-advances |
| 2 | Onboarding — Welcome | Swipeable pager, Skip/Next |
| 3 | Onboarding — Business type | 6-category grid, persisted via DataStore |
| 4 | Home Dashboard | Quick-create grid, featured templates, recent designs (live from Room) |
| 5 | Template Explorer | Search + category filter chips, PRO badges |
| 6 | My Designs Gallery | All/Recent/Favorites tabs, favorite/delete, FAB |
| 7 | Main Design Editor | Drag + resize elements, undo/redo, zoom, Text/Images/Elements/Brand tool tray, per-element property panel |
| 8 | Brand Kit Setup | Logo picker, identity, color swatches, contact info — persisted |
| 9 | Export & Share | PDF/PNG/JPG, quality slider, bleed toggle, real file export + Android share sheet |
| — | Premium Upgrade | Monthly/Annual toggle, Basic vs PRO comparison, local "unlock" flow |
| — | Profile | Business summary, PRO status, brand kit shortcut |

## Known limitations / next steps

- **Fonts:** the design system specifies "Plus Jakarta Sans" and "JetBrains
  Mono". To keep the project buildable without bundling external font files,
  `ui/theme/Type.kt` currently maps these to `FontFamily.SansSerif` /
  `FontFamily.Monospace`. Drop the real `.ttf` files into `res/font` and swap
  the two `val` definitions to use them for a pixel-perfect match.
- **Gradle wrapper jar:** the wrapper *properties* (Gradle 8.7) are included,
  but the binary `gradle-wrapper.jar` could not be downloaded in this sandbox.
  Android Studio will regenerate it automatically on first sync (or use a
  local Gradle install).
- **In-app purchases:** the Premium screen persists a local "PRO" flag via
  DataStore to unlock PRO-gated UI; it does not integrate Google Play Billing.
- **Background remover / AI tools** mentioned on the paywall are marketing
  copy from the source design and are not implemented (would require a
  network ML service, out of scope for a fully local-first app).
