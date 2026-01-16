package com.budgetvoice.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.budgetvoice.app.data.model.Expense
import com.budgetvoice.app.data.model.ExpenseCategory
import com.budgetvoice.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val isLoading: Boolean = false,
    val selectedPeriod: Period = Period.TODAY
)

enum class Period {
    TODAY, WEEK, MONTH
}

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            combine(
                repository.getTodayExpenses(),
                repository.getTodayTotal(),
                repository.getThisWeekTotal(),
                repository.getThisMonthTotal()
            ) { expenses, today, week, month ->
                ExpenseUiState(
                    expenses = expenses,
                    todayTotal = today ?: 0.0,
                    weekTotal = week ?: 0.0,
                    monthTotal = month ?: 0.0
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addExpense(amount: Double, category: ExpenseCategory, description: String = "", isManual: Boolean = false) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                description = description,
                timestamp = LocalDateTime.now(),
                isManual = isManual
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updatePeriod(period: Period) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        viewModelScope.launch {
            val expenses = when (period) {
                Period.TODAY -> repository.getTodayExpenses()
                Period.WEEK -> repository.getThisWeekExpenses()
                Period.MONTH -> repository.getThisMonthExpenses()
            }
            expenses.collect { list ->
                _uiState.value = _uiState.value.copy(expenses = list)
            }
        }
    }

    fun parseVoiceInput(voiceText: String): Pair<Double?, ExpenseCategory> {
        val normalized = voiceText.lowercase().trim()

        // Extract amount - look for numbers
        val amountRegex = """(\d+(?:[.,]\d+)?)""".toRegex()
        val amountMatch = amountRegex.find(normalized)
        val amount = amountMatch?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()

        // Extract category from remaining text
        val categoryText = normalized.replace(amountMatch?.value ?: "", "")
        val category = ExpenseCategory.fromString(categoryText)

        return Pair(amount, category)
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
