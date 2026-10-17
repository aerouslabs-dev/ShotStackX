# ShotStackX — Organic Warm Clay UI update

This update restyles the active Jetpack Compose dashboard in the Organic Warm Clay visual system:

- Warm cream ambient background (#F7F3ED / #EFEAE1) with soft terracotta accents (#E87A5D / #E07A5F).
- Elevated cream surfaces with larger rounded corners, layered shadows and warm edge highlights.
- Rounded native system typography and spring-animated storage / pending counters.
- Spring-based press-scale feedback on the dashboard's main actions, filters, layout toggle, and analytics control.
- Screenshot cards receive soft entrance motion while retaining thumbnail hold-to-preview.
- Launcher icon keeps the distinctive stacked-screenshot trash-bin silhouette and adopts the warm brand color.
- The app root applies status-bar inset padding while keeping the background drawn behind the status bar.

The Room-backed deletion analytics, WorkManager cleanup scheduling, permission flows, and version metadata are retained from the supplied project. Build verification status is recorded separately after attempting the build in this environment.

## Build verification

`./gradlew assembleDebug --no-daemon` was attempted from the current environment. It stopped before project compilation because the Gradle wrapper could not resolve `services.gradle.org` (`UnknownHostException`). The source edits and Android XML resources were reviewed, but a successful APK build is **not verified** here.

