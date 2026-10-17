# ShotStackX local deletion analytics

- Analytics is backed by a Room `deletion_events` table and a Flow observed by the UI.
- A migration from database version 3 to 4 creates the event table without destructive migration.
- A URI is the event primary key so the same screenshot is not counted twice.
- Only newly-deleted database rows produce events. Bytes come from the screenshot's recorded `fileSize` captured from MediaStore by the screenshot observer.
- Existing deleted rows are intentionally not backfilled: the previous schema did not store a trustworthy deletion timestamp, so fabricating historical daily/weekly values would be misleading.
- The weekly/monthly graph aggregates actual recorded events by local device-day; empty history displays an explicit empty state rather than sample data.

Build command: `./gradlew assembleDebug`. Build was attempted in the authoring environment but could not start because the Gradle distribution host was not resolvable (`UnknownHostException: services.gradle.org`). Compilation and runtime/device tests therefore remain unverified.
