package com.budgetvoice.app.data.local

import androidx.room.*
import com.budgetvoice.app.data.model.Expense
import com.budgetvoice.app.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getExpensesBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY timestamp DESC")
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startDate AND timestamp <= :endDate")
    fun getTotalBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE timestamp >= :startDate AND timestamp <= :endDate GROUP BY category")
    fun getCategoryTotalsBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Map<ExpenseCategory, Double>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?
}
