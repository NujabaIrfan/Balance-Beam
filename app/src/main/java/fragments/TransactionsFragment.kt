package com.example.balancebeam.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.balancebeam.AddTransactionActivity
import com.example.balancebeam.adapters.TransactionAdapter
import com.example.balancebeam.databinding.FragmentTransactionsBinding
import com.example.balancebeam.utils.Constants
import com.example.balancebeam.viewmodels.FinanceViewModel

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FinanceViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[FinanceViewModel::class.java]

        // Setup RecyclerView
        adapter = TransactionAdapter(
            emptyList(),
            onItemClick = { transaction -> /* Handle click */ },
            onItemDelete = { transaction -> viewModel.deleteTransaction(transaction.id) }
        )

        binding.transactionsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TransactionsFragment.adapter
        }

        // Observe transactions
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.transactionsRecycler.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.transactionsRecycler.visibility = View.VISIBLE
                adapter.updateData(transactions.sortedByDescending { it.date })
            }
        }

        // Set up filter buttons
        binding.filterAll.setOnClickListener {
            adapter.updateData(viewModel.transactions.value ?: emptyList())
        }
        binding.filterIncome.setOnClickListener {
            adapter.updateData(viewModel.transactions.value?.filter {
                it.type == Constants.TRANSACTION_TYPE_INCOME
            } ?: emptyList())
        }
        binding.filterExpense.setOnClickListener {
            adapter.updateData(viewModel.transactions.value?.filter {
                it.type == Constants.TRANSACTION_TYPE_EXPENSE
            } ?: emptyList())
        }

        // Set up FAB
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
                putExtra(Constants.EXTRA_TRANSACTION_TYPE, Constants.TRANSACTION_TYPE_EXPENSE)
            }
            startActivity(intent)
        }
    } // ← FIX: This closing brace was missing

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
