package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryArea: String,
    val deliveryFee: Int,
    val orderNotes: String,
    val itemsJson: String,
    val subtotal: Int,
    val grandTotal: Int,
    val paymentMethod: String = "Cash on Delivery",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)
