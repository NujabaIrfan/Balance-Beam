package com.example.balancebeam.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.balancebeam.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*



class PrefsHelper(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Budget methods
    fun setMonthlyBudget(budget: Double) {
        sharedPref.edit().putFloat(Constants.KEY_BUDGET, budget.toFloat()).apply()
    }

    fun getMonthlyBudget(): Double {
        return sharedPref.getFloat(Constants.KEY_BUDGET, 0f).toDouble()
    }

    // Currency methods
    fun setCurrencyType(currency: String) {
        sharedPref.edit().putString(Constants.KEY_CURRENCY, currency).apply()
    }

    fun getCurrencyType(): String {
        return sharedPref.getString(Constants.KEY_CURRENCY, "$") ?: "$"
    }

    // Transaction methods
    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPref.edit().putString("transactions", json).apply()
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPref.getString("transactions", null)
        return if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // First run check
    fun isFirstRun(): Boolean {
        val firstRun = sharedPref.getBoolean(Constants.KEY_FIRST_RUN, true)
        if (firstRun) {
            sharedPref.edit().putBoolean(Constants.KEY_FIRST_RUN, false).apply()
        }
        return firstRun
    }
}