# Catdoky

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Jetpack%20Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Lottie](https://img.shields.io/badge/Lottie-00C4B3?logo=lottiefiles&logoColor=white)

Catdoky — это судоку с котиками на Jetpack Compose. В проекте есть классические 9x9 и 16x16, анимации, прогресс/уровни и встроенный туториал.

## Возможности
- Судоку 9x9 и 16x16
- Пауза с анимацией кота
- Режим карандаша и подсказки (3 на игру)
- Система ошибок (3 попытки)
- Долгое нажатие очищает ячейку
- Туториал (показывается один раз)
- XP и рост уровня
- Светлая и тёмная темы

## Технологии
- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- DataStore (preferences)
- Lottie Compose

## Структура проекта
- `app/src/main/java/com/example/sudoky/ui/screens` — экраны Compose
- `app/src/main/java/com/example/sudoky/sudoku` — логика игры и решатель
- `app/src/main/java/com/example/sudoky/data` — настройки, XP и уровни
- `app/src/main/res/raw` — Lottie-анимации
- `app/src/main/res/mipmap-*` — иконки приложения

## Сборка
```bash
./gradlew assembleDebug
```

## Примечания
- Название приложения: Catdoky
- Иконки используют `@mipmap/ic_launcher` и `@mipmap/ic_launcher_round`
