package com.aerouslabs.shotstackx.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aerouslabs.shotstackx.data.db.dao.ScreenshotDao
import com.aerouslabs.shotstackx.data.db.entity.DeletionEventEntity
import com.aerouslabs.shotstackx.data.db.entity.ScreenshotEntity

@Database(entities = [ScreenshotEntity::class, DeletionEventEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun screenshotDao(): ScreenshotDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "screenshot_janitor_database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                .also { INSTANCE = it }
        }

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE screenshots ADD COLUMN kept INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE screenshots ADD COLUMN fileSize INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS deletion_events (uri TEXT NOT NULL PRIMARY KEY, deletedAt INTEGER NOT NULL, bytesSaved INTEGER NOT NULL)")
                // Historical rows have no reliable deletion timestamp. Do not fabricate past analytics.
            }
        }
    }
}
