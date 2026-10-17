package com.aerouslabs.shotstackx.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A locally recorded, confirmed deletion. One event per MediaStore URI prevents double counting. */
@Entity(tableName = "deletion_events")
data class DeletionEventEntity(
    @PrimaryKey val uri: String,
    val deletedAt: Long,
    val bytesSaved: Long
)
