package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerPhone = :phone ORDER BY createdAt DESC")
    fun getOrdersByCustomerPhone(phone: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getOrderByOrderNumber(orderNumber: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE orderNumber = :query OR customerPhone = :query ORDER BY createdAt DESC")
    suspend fun searchOrders(query: String): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: String)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int
}
