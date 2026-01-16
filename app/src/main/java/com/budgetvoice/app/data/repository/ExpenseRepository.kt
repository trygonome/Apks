package com.budgetvoice.app.data.repository

import com.budgetvoice.app.data.local.ExpenseDao
import com.budgetvoice.app.data.model.Expense
import com.budgetvoice.app.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Expense>> =
        expenseDao.getExpensesBetween(startDate, endDate)

    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> =
        expenseDao.getExpensesByCategory(category)

    fun getTotalBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Double?> =
        expenseDao.getTotalBetween(startDate, endDate)

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)

    // Helper methods for common date ranges
    fun getTodayExpenses(): Flow<List<Expense>> {
        val today = LocalDateTime.now().toLocalDate()
        return getExpensesBetween(
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        )
    }

    fun getThisWeekExpenses(): Flow<List<Expense>> {
        val now = LocalDateTime.now()
        val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1).toLocalDate().atStartOfDay()
        return getExpensesBetween(startOfWeek, now)
    }

    fun getThisMonthExpenses(): Flow<List<Expense>> {
        val now = LocalDateTime.now()
        val startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
        return getExpensesBetween(startOfMonth, now)
    }

    fun getTodayTotal(): Flow<Double?> {
        val today = LocalDateTime.now().toLocalDate()
        return getTotalBetween(
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        )
    }

    fun getThisWeekTotal(): Flow<Double?> {
        val now = LocalDateTime.now()
        val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1).toLocalDate().atStartOfDay()
        return getTotalBetween(startOfWeek, now)
    }

    fun getThisMonthTotal(): Flow<Double?> {
        val now = LocalDateTime.now()
        val startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
        return getTotalBetween(startOfMonth, now)
    }
}
