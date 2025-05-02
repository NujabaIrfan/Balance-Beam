package com.example.balancebeam

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.balancebeam.models.Transaction
import com.example.balancebeam.viewmodels.FinanceViewModel
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    // ① Obtain your AndroidViewModel
    private val financeViewModel: FinanceViewModel by viewModels()

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAmount: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var rgType: RadioGroup
    private lateinit var btnDate: Button
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnSave: Button

    private var selectedDate: Date? = null
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_transaction)

        // ② Wire up views
        etTitle       = findViewById(R.id.et_title)
        etAmount      = findViewById(R.id.et_amount)
        actvCategory  = findViewById(R.id.actv_category)
        rgType        = findViewById(R.id.rg_type)
        btnDate       = findViewById(R.id.btn_date)
        etNotes       = findViewById(R.id.et_notes)
        btnSave       = findViewById(R.id.btn_save)

        setupCategoryDropdown()
        setupDatePicker()

        btnSave.setOnClickListener { saveTransaction() }
    }

    private fun setupCategoryDropdown() {
        val categories = listOf("Food", "Transport", "Shopping", "Salary", "Other")
        actvCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        )
    }

    private fun setupDatePicker() {
        btnDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    selectedDate = cal.time
                    btnDate.text = dateFormatter.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun saveTransaction() {
        // ③ Gather & validate inputs
        val title    = etTitle.text.toString().trim().takeIf { it.isNotEmpty() }
            ?: run { etTitle.error = "Required"; return }
        val amount   = etAmount.text.toString().toDoubleOrNull()?.takeIf { it > 0 }
            ?: run { etAmount.error = "Enter a valid amount"; return }
        val category = actvCategory.text.toString().trim().takeIf { it.isNotEmpty() }
            ?: run { actvCategory.error = "Select a category"; return }
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        // ④ Use capitalized type so getExpensesByCategory()/getCurrentMonthFilter work
        val type = if (rgType.checkedRadioButtonId == R.id.rb_income) "Income" else "Expense"
        val notes = etNotes.text.toString().trim()

        // ⑤ Build & hand off to ViewModel
        val tx = Transaction(
            id       = UUID.randomUUID().toString(),
            title    = title,
            amount   = amount,
            category = category,
            type     = type,
            date     = selectedDate!!,
            notes    = notes
        )
        financeViewModel.addTransaction(tx)

        Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
