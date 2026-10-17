# ShotStackX UI refinement notes

## Included
- Grid/List switch on the Tracked Screenshots section.
- Grid mode uses `LazyVerticalGrid` with scrolling delegated to the parent `LazyColumn`, so the existing page scroll and pull-to-reveal behavior remain in the parent scroll container.
- Grid cards retain hold-to-preview, archive, keep, and delete callbacks.
- Grid/List mode applies to Pending, Achieved, and Kept screenshots.
- Settings includes a functional Appearance section with System, Light, and Dark modes. The selected mode is persisted in `shotstackx_prefs` and applied to Material theme and glass surfaces.
- Updated adaptive launcher foreground/monochrome vector artwork for the Aerous Labs palette; manifest continues to reference `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`.

## Verification
- Android XML resources parsed successfully.
- No old `dev.sj010.ssjanitor` references remain in application source/build configuration. The old identifier may appear in historical changelog text only.
- `./gradlew assembleDebug --no-daemon` was attempted, but this environment could not resolve `services.gradle.org` to download the configured Gradle 9.4.1 distribution. Compilation and APK packaging are therefore not verified here.

## Build locally
Open this project in Android Studio, allow Gradle sync to finish, then run:

```bash
./gradlew assembleDebug
```

Expected debug APK path after a successful build: `app/build/outputs/apk/debug/app-debug.apk`.
