package com.example.korset.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val collectionId: Int = 0,
    val name: String,
    val description: String? = null, // Описание коллекции (опционально)
    val createdAt: Long = System.currentTimeMillis() // Время создания
)