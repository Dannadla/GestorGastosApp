package com.gestorgastos.app.data.model

data class Expense(
    val id: Int,
    val user_id: Int,
    val category: String,
    val amount: Double,
    val description: String?,
    val type: String, // "gasto" | "ingreso"
    val date: String
)

data class ExpenseRequest(
    val category: String,
    val amount: Double,
    val description: String,
    val type: String,
    val date: String
)

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class TypeTotal(
    val type: String,
    val total: Double
)

data class ExpenseSummary(
    val byCategory: List<CategoryTotal>,
    val totals: List<TypeTotal>
)
