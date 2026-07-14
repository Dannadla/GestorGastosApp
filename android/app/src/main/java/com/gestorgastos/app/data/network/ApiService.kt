package com.gestorgastos.app.data.network

import com.gestorgastos.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/expenses")
    suspend fun getExpenses(): Response<List<Expense>>

    @GET("api/expenses/summary")
    suspend fun getSummary(): Response<ExpenseSummary>

    @POST("api/expenses")
    suspend fun createExpense(@Body request: ExpenseRequest): Response<Expense>

    @PUT("api/expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: Int,
        @Body request: ExpenseRequest
    ): Response<Expense>

    @PATCH("api/expenses/{id}")
    suspend fun patchExpense(
        @Path("id") id: Int,
        @Body fields: Map<String, @JvmSuppressWildcards Any>
    ): Response<Expense>

    @DELETE("api/expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: Int): Response<Unit>
}
