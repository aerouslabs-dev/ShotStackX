package com.aerouslabs.shotstackx.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aerouslabs.shotstackx.data.db.entity.ScreenshotEntity
import com.aerouslabs.shotstackx.data.db.entity.DeletionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots ORDER BY createdAt DESC")
    fun getAllScreenshotsFlow(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots")
    suspend fun getAllScreenshots(): List<ScreenshotEntity>

    @Query("SELECT * FROM screenshots WHERE uri = :uri LIMIT 1")
    suspend fun getScreenshotByUri(uri: String): ScreenshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: ScreenshotEntity)

    @Update
    suspend fun updateScreenshot(screenshot: ScreenshotEntity)

    @Delete
    suspend fun deleteScreenshot(screenshot: ScreenshotEntity)

    @Query("SELECT * FROM screenshots WHERE uri IN (:uris) AND deleted = 0")
    suspend fun getNotDeletedScreenshots(uris: List<String>): List<ScreenshotEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeletionEvents(events: List<DeletionEventEntity>)

    @Query("SELECT * FROM deletion_events ORDER BY deletedAt ASC")
    fun getDeletionEventsFlow(): Flow<List<DeletionEventEntity>>

    @Query("UPDATE screenshots SET deleted = 1 WHERE uri IN (:uris)")
    suspend fun updateDeletedFlag(uris: List<String>)

    /** Persist the event and state change atomically. Existing deleted rows are not counted twice. */
    @Transaction
    suspend fun markAsDeleted(uris: List<String>, deletedAt: Long = System.currentTimeMillis()) {
        if (uris.isEmpty()) return
        val newlyDeleted = getNotDeletedScreenshots(uris)
        if (newlyDeleted.isNotEmpty()) {
            insertDeletionEvents(newlyDeleted.map {
                DeletionEventEntity(uri = it.uri, deletedAt = deletedAt, bytesSaved = it.fileSize.coerceAtLeast(0L))
            })
        }
        updateDeletedFlag(uris)
    }

    // Used by the janitor: only archived screenshots that haven't been kept or already deleted
    @Query("SELECT * FROM screenshots WHERE archived = 1 AND kept = 0 AND deleted = 0")
    suspend fun getArchivedForCleanup(): List<ScreenshotEntity>

    // Legacy fallback — old unarchived screenshots beyond a time threshold (not kept, not deleted)
    @Query("SELECT * FROM screenshots WHERE archived = 0 AND kept = 0 AND deleted = 0 AND createdAt < :threshold")
    suspend fun getOldPendingScreenshots(threshold: Long): List<ScreenshotEntity>
}
