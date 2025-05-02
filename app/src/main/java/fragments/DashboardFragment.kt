package com.example.balancebeam.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.balancebeam.AddTransactionActivity
import com.example.balancebeam.R
import com.example.balancebeam.activity_budget
import com.example.balancebeam.databinding.FragmentDashboardBinding
import com.example.balancebeam.utils.Constants
import com.example.balancebeam.viewmodels.FinanceViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FinanceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeChart(binding.expenseChart)
        setupObservers()
        setupClickListeners()
        viewModel.forceRefresh() // Trigger initial data load
    }

    override fun onResume() {
        super.onResume()
        viewModel.forceRefresh() // Refresh data when returning to fragment
    }

    private fun setupObservers() {
        viewModel.transactions.observe(viewLifecycleOwner) {
            Log.d("Dashboard", "Transactions updated: ${it?.size ?: 0} items")
            updateUI()
        }
        viewModel.monthlyBudget.observe(viewLifecycleOwner) {
            Log.d("Dashboard", "Budget updated: $it")
            updateUI()
        }
        viewModel.currency.observe(viewLifecycleOwner) {
            Log.d("Dashboard", "Currency updated: $it")
            updateUI()
        }
    }

    private fun setupClickListeners() {
        binding.budgetCard.setOnClickListener {
            startActivity(Intent(requireContext(), activity_budget::class.java))
        }

        binding.fabAddIncome.setOnClickListener {
            Intent(requireContext(), AddTransactionActivity::class.java).apply {
                putExtra(Constants.EXTRA_TRANSACTION_TYPE, Constants.TRANSACTION_TYPE_INCOME)
                startActivity(this)
            }
        }

        binding.fabAddExpense.setOnClickListener {
            Intent(requireContext(), AddTransactionActivity::class.java).apply {
                putExtra(Constants.EXTRA_TRANSACTION_TYPE, Constants.TRANSACTION_TYPE_EXPENSE)
                startActivity(this)
            }
        }
    }

    private fun initializeChart(chart: PieChart) {
        chart.apply {
            setUsePercentValues(false)
            description.isEnabled = false
            setDrawEntryLabels(false)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Spending Breakdown"
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400)
            legend.isEnabled = true

            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(ContextCompat.getColor(requireContext(), R.color.transparent))
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun updateUI() {
        if (!isAdded || view == null) return // Check if fragment is attached

        val currencyCode = viewModel.currency.value ?: "$"
        val formatter = NumberFormat.getCurrencyInstance().apply {
            currency = try {
                if (currencyCode.length == 3 && currencyCode.all { it.isLetter() }) {
                    Currency.getInstance(currencyCode)
                } else {
                    Currency.getInstance(Locale.getDefault())
                }
            } catch (e: IllegalArgumentException) {
                Currency.getInstance(Locale.getDefault())
            }
        }

        val income = viewModel.getCurrentMonthIncome()
        val expenses = viewModel.getCurrentMonthExpenses()
        val balance = income - expenses

        binding.incomeAmount.text = formatter.format(income)
        binding.expenseAmount.text = formatter.format(expenses)
        binding.balanceAmount.text = formatter.format(balance)
        binding.balanceAmount.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (balance >= 0) R.color.accent_success else R.color.warning
            )
        )

        updateBudgetUI(formatter)
        updatePieChart(binding.expenseChart)
    }

    private fun updateBudgetUI(formatter: NumberFormat) {
        val budget = viewModel.monthlyBudget.value ?: 0.0
        val expenses = viewModel.getCurrentMonthExpenses()

        if (budget > 0) {
            binding.budgetProgress.max = budget.toInt()
            binding.budgetProgress.progress = expenses.toInt()
            binding.budgetAmount.text = "${formatter.format(expenses)} / ${formatter.format(budget)}"

            val percentage = expenses / budget
            binding.budgetStatus.text = when {
                percentage >= 1 -> "Budget Exceeded!"
                percentage >= Constants.BUDGET_WARNING_THRESHOLD -> "Approaching Budget Limit"
                else -> "Within Budget"
            }
            binding.budgetStatus.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when {
                        percentage >= 1 -> R.color.warning
                        percentage >= Constants.BUDGET_WARNING_THRESHOLD -> R.color.warning
                        else -> R.color.accent_success
                    }
                )
            )
        } else {
            binding.budgetAmount.text = "No budget set"
            binding.budgetStatus.text = "Set a monthly budget"
            binding.budgetStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
        }
    }

    private fun updatePieChart(chart: PieChart) {
        val expensesByCategory = viewModel.getExpensesByCategory()
        if (expensesByCategory.isEmpty()) {
            chart.visibility = View.GONE
            binding.emptyChartText.visibility = View.VISIBLE
            return
        }

        chart.visibility = View.VISIBLE
        binding.emptyChartText.visibility = View.GONE

        val entries = expensesByCategory.map { PieEntry(it.value.toFloat(), it.key) }
        val currencyCode = viewModel.currency.value ?: "$"

        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return try {
                        val currency = if (currencyCode.length == 3 && currencyCode.all { it.isLetter() }) {
                            Currency.getInstance(currencyCode)
                        } else {
                            Currency.getInstance(Locale.getDefault())
                        }
                        NumberFormat.getCurrencyInstance().apply {
                            this.currency = currency
                        }.format(value.toDouble())
                    } catch (e: Exception) {
                        value.toInt().toString()
                    }
                }
            }
        }

        chart.data = PieData(dataSet)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}