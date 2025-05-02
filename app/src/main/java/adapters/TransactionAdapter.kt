package com.example.balancebeam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.balancebeam.R
import com.example.balancebeam.databinding.ItemTransactionBinding
import com.example.balancebeam.models.Transaction
import com.example.balancebeam.utils.Constants
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val onItemDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val currency = Currency.getInstance("USD") // Adjust as needed
            val formatter = NumberFormat.getCurrencyInstance().apply {
                this.currency = currency
            }

            binding.apply {
                tvTitle.text = transaction.title
                tvAmount.text = formatter.format(transaction.amount)
                tvCategory.text = transaction.category
                tvDate.text = transaction.date.toString() // Format date properly

                // Set icon based on transaction type
                val iconRes = if (transaction.type == Constants.TRANSACTION_TYPE_INCOME) {
                    R.drawable.plus
                } else {
                    R.drawable.minus
                }
                ivType.setImageResource(iconRes)

                root.setOnClickListener { onItemClick(transaction) }
                btnDelete.setOnClickListener { onItemDelete(transaction) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}