package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val titleBn: String,
    val category: String,
    val price: Int,
    val originalPrice: Int,
    val stock: Int,
    val imageUrl: String,
    val sizes: String,
    val colors: String,
    val description: String,
    val descriptionBn: String,
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
