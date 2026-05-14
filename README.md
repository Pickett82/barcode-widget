# Barcode Widget

Offline-first Android app for storing loyalty card barcodes and exposing them in a resizable, scrollable home-screen widget.

## Features

- Add loyalty cards by typing a barcode or scanning one with the camera.
- Pick from a bundled known-store catalog with automatic badge styling.
- Add custom stores with an optional custom logo from local storage.
- Keep pinned cards at the top in manual order, with the rest sorted by usage and recency.
- Open barcodes instantly from the widget without a loading screen.

## Local development

Requirements:

- Java 17
- Android SDK with API 35 platform + build tools

Commands:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

## Notes

- Widget data is backed by Room and refreshed after create, delete, pin, and usage updates.
- Known-store metadata lives in `/home/runner/work/barcode-widget/barcode-widget/app/src/main/assets/store_catalog.json`.
