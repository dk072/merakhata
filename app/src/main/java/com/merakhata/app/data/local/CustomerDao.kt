package com.merakhata.app.data.local

import androidx.room.*
import com.merakhata.app.data.model.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
