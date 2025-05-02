package com.example.balancebeam.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.balancebeam.R
import com.example.balancebeam.databinding.FragmentReportsBinding
import com.example.balancebeam.utils.Constants
import com.example.balancebeam.utils.BackupHelper
import com.example.balancebeam.viewmodels.FinanceViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FinanceViewModel
    private lateinit var backupHelper: BackupHelper

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            performExport()
        } else {
            showToast("Notification permission denied - you won't receive export confirmations")
            performExport()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[FinanceViewModel::class.java]
        backupHelper = BackupHelper(requireContext())

        viewModel.transactions.observe(viewLifecycleOwner) { updateCharts() }
        viewModel.currency.observe(viewLifecycleOwner) { updateCharts() }

        binding.timePeriodSelector.setOnCheckedChangeListener { _, _ -> updateCharts() }

        // Export Button
        binding.btnExport.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    performExport()
                }
            } else {
                performExport()
            }
        }
    }

    private fun performExport() {
        viewModel.transactions.value?.let { transactions ->
            val success = backupHelper.exportData(transactions)
            showToast(if (success) "Data exported successfully" else "Export failed")
        } ?: showToast("No transactions to export")
    }

    private fun updateCharts() {
        updateMonthlyTrendChart(binding.monthlyTrendChart)
        updateCategoryChart(binding.categoryChart)
    }

    private fun updateMonthlyTrendChart(chart: BarChart) {
        val transactions = viewModel.transactions.value ?: return
        val currency = viewModel.currency.value ?: "$"

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val monthlyData = mutableMapOf<Int, Pair<Float, Float>>()

        transactions.forEach { transaction ->
            calendar.time = transaction.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            if (year == currentYear) {
                val current = monthlyData.getOrDefault(month, Pair(0f, 0f))
                val amount = transaction.amount.toFloat()
                if (transaction.type == Constants.TRANSACTION_TYPE_INCOME) {
                    monthlyData[month] = current.copy(first = current.first + amount)
                } else {
                    monthlyData[month] = current.copy(second = current.second + amount)
                }
            }
        }

        val entriesIncome = mutableListOf<BarEntry>()
        val entriesExpense = mutableListOf<BarEntry>()
        val monthLabels = mutableListOf<String>()

        for (i in 0..11) {
            val data = monthlyData.getOrDefault(i, Pair(0f, 0f))
            entriesIncome.add(BarEntry(i.toFloat(), data.first))
            entriesExpense.add(BarEntry(i.toFloat(), data.second))
            monthLabels.add(
                Calendar.getInstance().apply { set(Calendar.MONTH, i) }
                    .getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
            )
        }

        val dataSetIncome = BarDataSet(entriesIncome, "Income").apply {
            color = ContextCompat.getColor(requireContext(), R.color.accent_success)
            valueTextSize = 10f
            setDrawValues(false)
        }

        val dataSetExpense = BarDataSet(entriesExpense, "Expense").apply {
            color = ContextCompat.getColor(requireContext(), R.color.warning)
            valueTextSize = 10f
            setDrawValues(false)
        }

        val data = BarData(dataSetIncome, dataSetExpense).apply {
            barWidth = 0.3f
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "$currency${"%.2f".format(value)}"
                }
            })
        }

        chart.apply {
            this.data = data
            description.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(monthLabels)
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -45f
            }
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            legend.isEnabled = true
            setFitBars(true)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateCategoryChart(chart: BarChart) {
        val expensesByCategory = viewModel.getExpensesByCategory()
        val currency = viewModel.currency.value ?: "$"

        if (expensesByCategory.isEmpty()) {
            chart.visibility = View.GONE
            binding.emptyChartText.visibility = View.VISIBLE
            return
        }

        chart.visibility = View.VISIBLE
        binding.emptyChartText.visibility = View.GONE

        val entries = expensesByCategory.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val categories = expensesByCategory.keys.toList()

        val dataSet = BarDataSet(entries, "Expenses by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 10f
            setDrawValues(false)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "$currency${"%.2f".format(value)}"
                }
            }
        }

        val data = BarData(dataSet)

        chart.apply {
            this.data = data
            description.isEnabled = false
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(categories)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -45f
            }
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            animateY(1000)
            invalidate()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}