package com.gestorgastos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestorgastos.app.data.model.Expense
import com.gestorgastos.app.data.model.ExpenseRequest
import com.gestorgastos.app.data.model.ExpenseSummary
import com.gestorgastos.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExpenseUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val summary: ExpenseSummary? = null,
    val error: String? = null
)

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val expensesResult = repository.getExpenses()
            val summaryResult = repository.getSummary()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                expenses = expensesResult.getOrDefault(emptyList()),
                summary = summaryResult.getOrNull(),
                error = expensesResult.exceptionOrNull()?.message
                    ?: summaryResult.exceptionOrNull()?.message
            )
        }
    }

    fun addExpense(
        category: String,
        amount: Double,
        description: String,
        type: String,
        date: String
    ) {
        viewModelScope.launch {
            repository.createExpense(ExpenseRequest(category, amount, description, type, date))
                .onSuccess { loadDashboard() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            repository.deleteExpense(id)
                .onSuccess { loadDashboard() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}
