package com.example.balancebeam

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.balancebeam.viewmodels.FinanceViewModel
import com.google.android.material.textfield.TextInputEditText

class activity_budget : AppCompatActivity() {

    private val financeViewModel: FinanceViewModel by viewModels()
    private lateinit var etBudget: TextInputEditText
    private lateinit var actvCurrency: AutoCompleteTextView
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        etBudget     = findViewById(R.id.et_budget)
        actvCurrency = findViewById(R.id.actv_currency)
        btnSave      = findViewById(R.id.btn_save_budget)

        setupCurrencyDropdown()
        bindExistingValues()

        btnSave.setOnClickListener { saveBudget() }
    }

    private fun setupCurrencyDropdown() {
        // You can expand this list as needed
        val currencies = listOf("USD", "EUR", "GBP", "LKR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, currencies)
        actvCurrency.setAdapter(adapter)
    }

    private fun bindExistingValues() {
        // Show current budget
        financeViewModel.monthlyBudget.observe(this) { budget ->
            etBudget.setText(budget.toString())
        }
        // Show current currency
        financeViewModel.currency.observe(this) { curr ->
            actvCurrency.setText(curr, false)
        }
    }

    private fun saveBudget() {
        val budgetText = etBudget.text.toString().trim()
        val currText   = actvCurrency.text.toString().trim().uppercase()

        // Validate budget
        val budget = budgetText.toDoubleOrNull()
        if (budget == null || budget < 0) {
            etBudget.error = "Enter a valid amount"
            return
        }
        // Validate currency (simple 3‑letter check)
        if (currText.length != 3 || !currText.all { it.isLetter() }) {
            actvCurrency.error = "Use a 3‑letter code"
            return
        }

        financeViewModel.setMonthlyBudget(budget)
        financeViewModel.setCurrencyType(currText)

        Toast.makeText(this, "Budget and currency saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
