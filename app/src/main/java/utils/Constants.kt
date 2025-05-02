package com.example.balancebeam.utils

object Constants{
    const val PREFS_NAME = "FinanceTrackerPrefs"
    const val KEY_BUDGET = "monthly_budget"
    const val KEY_CURRENCY = "currency_type"
    const val KEY_FIRST_RUN = "first_run"

    val CATEGORIES = listOf(
        "Food", "Transport", "Bills", "Entertainment",
        "Shopping", "Healthcare", "Education", "Other"
    )

    val INCOME_CATEGORIES = listOf(
        "Salary", "Bonus", "Gift", "Investment", "Other"
    )

    const val TRANSACTION_TYPE_INCOME = "Income"
    const val TRANSACTION_TYPE_EXPENSE = "Expense"
    const val EXTRA_TRANSACTION_TYPE = "transaction_type"

    const val BUDGET_WARNING_THRESHOLD = 0.8 // 80% of budget
    const val BACKUP_FILE_NAME = "finance_tracker_backup.json"
}