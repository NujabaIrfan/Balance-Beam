package com.example.balancebeam.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues // Import ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.example.balancebeam.R
import com.example.balancebeam.models.Transaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupHelper(private val context: Context) {

    private val fileName = "transactions_backup_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.json"
    private val channelId = "backup_channel"
    private val exportNotificationId = 100
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Backup Channel"
            val descriptionText = "Notification channel for backup operations"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, text: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("BackupHelper", "Notification permission not granted")
                return
            }
            notify(notificationId, builder.build())
        }
    }

    fun exportData(transactions: List<Transaction>): Boolean {
        return try {
            val jsonArray = JSONArray()
            transactions.forEach { transaction ->
                jsonArray.put(transaction.toJson())
            }

            // Save to app-specific storage first
            val internalFile = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(internalFile).use { it.write(jsonArray.toString().toByteArray()) }

            // Also save to Documents folder for user visibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply { // Use ContentValues here
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { stream ->
                        stream.write(jsonArray.toString().toByteArray())
                    }
                }
            }

            showNotification(
                "Export Successful",
                "Data exported to Documents/$fileName",
                exportNotificationId
            )
            true
        } catch (e: Exception) {
            Log.e("BackupHelper", "Error exporting data: ${e.message}", e)
            showNotification("Export Failed", "Failed to export data: ${e.localizedMessage}", exportNotificationId)
            false
        }
    }

    private fun Transaction.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("amount", amount)
            put("category", category)
            put("type", type)
            put("date", dateFormat.format(date))
            put("notes", notes ?: "")
        }
    }
}
