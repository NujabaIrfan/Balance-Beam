package com.example.balancebeam.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.balancebeam.models.Transaction
import com.example.balancebeam.utils.Constants
import com.example.balancebeam.utils.NotificationHelper
import com.example.balancebeam.utils.PrefsHelper
import java.util.*

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsHelper = PrefsHelper(application)
    private val notificationHelper = NotificationHelper(application)

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _monthlyBudget = MutableLiveData<Double>()
    val monthlyBudget: LiveData<Double> = _monthlyBudget

    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String> = _currency

    private var lastNotificationState: BudgetState? = null

    init {
        loadTransactions()
        loadBudget()
        loadCurrency()
    }

    fun loadTransactions() {
        _transactions.value = prefsHelper.getTransactions()
    }

    fun addTransaction(transaction: Transaction) {
        val current = _transactions.value?.toMutableList() ?: mutableListOf()
        current.add(transaction)
        _transactions.value = current
        saveTransactions()
        refreshBudgetStatus() // ✅ Check budget after adding
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        val current = _transactions.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            current[index] = updatedTransaction
            _transactions.value = current
            saveTransactions()
            refreshBudgetStatus()
        }
    }

    fun deleteTransaction(transactionId: String) {
        val current = _transactions.value?.toMutableList() ?: return
        current.removeAll { it.id == transactionId }
        _transactions.value = current
        saveTransactions()
        refreshBudgetStatus()
    }

    private fun saveTransactions() {
        _transactions.value?.let { prefsHelper.saveTransactions(it) }
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        prefsHelper.setMonthlyBudget(budget)
        refreshBudgetStatus()
    }

    private fun loadBudget() {
        _monthlyBudget.value = prefsHelper.getMonthlyBudget()
    }

    fun setCurrencyType(currency: String) {
        _currency.value = currency
        prefsHelper.setCurrencyType(currency)
    }

    private fun loadCurrency() {
        _currency.value = prefsHelper.getCurrencyType()
    }

    fun getExpensesByCategory(): Map<String, Double> {
        return _transactions.value?.filter { it.type == "Expense" }
            ?.groupBy { it.category }
            ?.mapValues { (_, list) -> list.sumOf { it.amount } }
            ?: emptyMap()
    }

    fun getCurrentMonthExpenses(): Double {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val expenses = _transactions.value?.filter {
            it.type == "Expense" &&
                    it.date.month == currentMonth &&
                    it.date.year + 1900 == currentYear
        }?.sumOf { it.amount } ?: 0.0

        monthlyBudget.value?.let { budget ->
            checkBudgetStatus(expenses, budget)
        }

        return expenses
    }

    fun getCurrentMonthIncome(): Double {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        return _transactions.value?.filter {
            it.type == "Income" &&
                    it.date.month == currentMonth &&
                    it.date.year + 1900 == currentYear
        }?.sumOf { it.amount } ?: 0.0
    }

    fun forceRefresh() {
        _transactions.value = prefsHelper.getTransactions()
        _monthlyBudget.value = prefsHelper.getMonthlyBudget()
        _currency.value = prefsHelper.getCurrencyType()
        refreshBudgetStatus()
    }

    private fun refreshBudgetStatus() {
        val expenses = getCurrentMonthExpenses()
        val budget = _monthlyBudget.value ?: return
        checkBudgetStatus(expenses, budget)
    }

    private fun checkBudgetStatus(expenses: Double, budget: Double) {
        if (budget <= 0) return

        val percentage = expenses / budget
        val currentState = when {
            percentage >= 1.0 -> BudgetState.EXCEEDED
            percentage >= Constants.BUDGET_WARNING_THRESHOLD -> BudgetState.WARNING
            else -> BudgetState.NORMAL
        }

        if (currentState != lastNotificationState) {
            when (currentState) {
                BudgetState.EXCEEDED -> notificationHelper.showBudgetExceededNotification()
                BudgetState.WARNING -> notificationHelper.showBudgetWarningNotification(percentage)
                BudgetState.NORMAL -> { /* No notification */ }
            }
            lastNotificationState = currentState
        }
    }

    private enum class BudgetState {
        NORMAL, WARNING, EXCEEDED
    }
}
