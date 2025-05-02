package com.example.balancebeam.models
import org.json.JSONObject
import java.util.*

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val date: Date,
    val notes: String?
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("amount", amount)
            put("category", category)
            put("type", type)
            put("date", date.time) // Store date as long (timestamp)
            put("notes", notes ?: "")
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Transaction {
            return Transaction(
                id = json.getString("id"),
                title = json.getString("title"),
                amount = json.getDouble("amount"),
                category = json.getString("category"),
                type = json.getString("type"),
                date = Date(json.getLong("date")),
                notes = json.optString("notes", "")
            )
        }
    }
}
