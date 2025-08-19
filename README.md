# SSPLite Android

Minimalistische Android-App (Kotlin/Compose + ExoPlayer) für SSP-inspirierte Emotionsregulation. Nutzer wählt einen Ordner, spielt eine Playlist und filtert Audio in Echtzeit mit dem FFP-Konzept (Frequenz/Formant/Prosodie).

## Funktionen
- Ordnerwahl über SAF, rekursives Laden von Audiodateien oder Playlists.
- Wiedergabe mit ExoPlayer, Fortschrittsanzeige und Session-Timer.
- FFP-Filter: Low-Shelf, zwei Peaking-Bänder, High-Shelf, optionale Mikro-Modulation.
- Preset-System: sechs Beispiel-Presets (JSON) plus Nutzer-Presets.
- Pre-Gain und Limiter zur Lautheitssicherung.

## Build
- `minSdk 24`, `targetSdk 34`.
- Jetpack Compose UI, Kotlin.
- GitHub Actions Workflow (`.github/workflows/android-release.yml`) baut signierte Release-APK.

## Keystore
Siehe [KEYS.md](KEYS.md) für Erstellung des Keystores und GitHub-Secrets.

## Presets
Beispiel-Presets liegen im Ordner `PRESETS/` und folgen dem JSON-Format aus `model/Preset.kt`.

## Ordnerstruktur
```
app/                # Android-App Modul
.github/workflows/  # CI
PRESETS/            # Beispiel-Presets
```
