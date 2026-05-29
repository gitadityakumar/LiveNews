# LiveNews Native Android

Native Android port of LiveNews built with Kotlin, Jetpack Compose, AndroidX Navigation 3, Media3, WebKit, and DataStore.

## Project Layout

- `app/src/main/java/com/perpetuitylab/livenews/MainActivity.kt`: Compose entry point and picture-in-picture handoff.
- `app/src/main/java/com/perpetuitylab/livenews/navigation/`: Navigation 3 back stack wiring for Home, Settings, and Network Inspector.
- `app/src/main/java/com/perpetuitylab/livenews/ui/home/`: Main live-news channel list, region selector, channel reload, and reorder controls.
- `app/src/main/java/com/perpetuitylab/livenews/ui/player/`: Draggable Media3 video player and custom controls.
- `app/src/main/java/com/perpetuitylab/livenews/ui/network/`: WebView-based `.m3u8` capture flow.
- `app/src/main/java/com/perpetuitylab/livenews/data/`: Channel metadata, stream URLs, and DataStore preferences.
- `app/src/test/`: JVM unit tests for `.m3u8` URL extraction and validation.
- `app/src/androidTest/`: Compose instrumented test coverage for the legacy sample `MainScreen`.

## Build And Test

Run commands from this `android-native/` directory.

```bash
./gradlew testDebugUnitTest
./gradlew :app:assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Optional instrumented-test compile check:

```bash
./gradlew :app:compileDebugAndroidTestKotlin
```

## Run

Install and launch the debug build on a connected device or emulator:

```bash
./gradlew :app:installDebug
adb shell monkey -p com.perpetuitylab.livenews 1
```

If using the Android CLI wrapper available in this environment, after `assembleDebug`:

```bash
android run --apks=app/build/outputs/apk/debug/app-debug.apk
```

## Feature Coverage

- India and USA live channel lists.
- Media3 HLS playback with play/pause, mute, jump-to-live, fit/fill, fullscreen, minimized draggable player, and Android picture-in-picture support.
- Bottom navigation between Home and Settings.
- Channel order persistence with DataStore.
- USA channel reload flow that opens the Network Inspector and stores captured `.m3u8` URLs.
- WebView network inspection using request interception plus injected JavaScript.

## Known Limitation

Channel reorder currently uses explicit move up/down controls. True long-press drag reorder is deferred until a Compose reorder dependency is added.
