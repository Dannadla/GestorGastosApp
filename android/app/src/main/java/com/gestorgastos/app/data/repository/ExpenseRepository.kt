package com.gestorgastos.app.data.repository

import com.gestorgastos.app.data.model.Expense
import com.gestorgastos.app.data.model.ExpenseRequest
import com.gestorgastos.app.data.model.ExpenseSummary
import com.gestorgastos.app.data.network.ApiService

class ExpenseRepository(private val api: ApiService) {

    suspend fun getExpenses(): Result<List<Expense>> = try {
        val response = api.getExpenses()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("No se pudieron obtener los gastos"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSummary(): Result<ExpenseSummary> = try {
        val response = api.getSummary()
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("No se pudo obtener el resumen"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createExpense(request: ExpenseRequest): Result<Expense> = try {
        val response = api.createExpense(request)
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("No se pudo crear el registro"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateExpense(id: Int, request: ExpenseRequest): Result<Expense> = try {
        val response = api.updateExpense(id, request)
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("No se pudo actualizar el registro"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun patchExpense(id: Int, fields: Map<String, Any>): Result<Expense> = try {
        val response = api.patchExpense(id, fields)
        if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
        else Result.failure(Exception("No se pudo actualizar el registro"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteExpense(id: Int): Result<Unit> = try {
        val response = api.deleteExpense(id)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("No se pudo eliminar el registro"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
