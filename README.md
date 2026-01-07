# Catdoky

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Jetpack%20Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Lottie](https://img.shields.io/badge/Lottie-00C4B3?logo=lottiefiles&logoColor=white)

Catdoky is a Sudoku game with a cat theme, built with Jetpack Compose. It includes classic 9x9 and 16x16 boards, animations, XP progression, and a guided tutorial.

## Features
- Sudoku 9x9 and 16x16
- Pause mode with cat animation
- Pencil mode and hints (3 per game)
- Error tracking (3 strikes)
- Long press to clear a cell
- Tutorial coach marks (one-time)
- XP and level progression
- Light and dark themes

## Tech Stack
- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- DataStore (preferences)
- Lottie Compose

## Project Structure
- `app/src/main/java/com/example/sudoky/ui/screens` - Compose screens
- `app/src/main/java/com/example/sudoky/sudoku` - Game logic and solver
- `app/src/main/java/com/example/sudoky/data` - Preferences, XP, and leveling rules
- `app/src/main/res/raw` - Lottie animations
- `app/src/main/res/mipmap-*` - App icons

## Build
```bash
./gradlew assembleDebug
```

## Notes
- App name: Catdoky
- Icons use `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`

