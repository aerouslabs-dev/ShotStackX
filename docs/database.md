# Database Schema

## Entity

```kotlin
@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey
    val uri: String,
    val fileName: String,
    val createdAt: Long,
    val archived: Boolean = false,
    val deleted: Boolean = false,
    val kept: Boolean = false
)
```

| Column | Type | Description |
|---|---|---|
| `uri` | `String` (PK) | Content URI of the screenshot |
| `fileName` | `String` | Display name of the file |
| `createdAt` | `Long` | Timestamp of detection (epoch millis) |
| `archived` | `Boolean` | Marked for cleanup |
| `deleted` | `Boolean` | Deleted from device storage |
| `kept` | `Boolean` | Preserved — excluded from cleanup |

## DAO

`ScreenshotDao` provides:
- Insert new screenshot
- Update status (archive, keep, delete)
- Query by URI
- Query archived screenshots pending cleanup
- Query all screenshots (for History screen)

## Repository

`ScreenshotRepository` orchestrates between Room and MediaStore:
- Reconciling local DB with device storage.
- Creating Scoped Storage-compatible deletion requests.
- Updating screenshot status.

`SettingsRepository` manages user preferences (e.g., auto-archive toggle) via `DataStore`.

## Files

| File | Path |
|---|---|
| Entity | `data/db/entity/ScreenshotEntity.kt` |
| DAO | `data/db/dao/ScreenshotDao.kt` |
| Database | `data/db/AppDatabase.kt` |
| Repository | `data/repository/ScreenshotRepository.kt` |
| Settings | `data/repository/SettingsRepository.kt` |
