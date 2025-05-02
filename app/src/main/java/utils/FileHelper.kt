package com.example.balancebeam.utils

import android.content.Context

object FileHelper {
    private const val FILE_NAME = "user_credentials.txt"

    fun saveUser(context: Context, email: String, password: String) {
        val fileOutput = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
        fileOutput.write("$email:$password".toByteArray())
        fileOutput.close()
    }

    fun readUser(context: Context): Pair<String, String>? {
        return try {
            val fileInput = context.openFileInput(FILE_NAME)
            val content = fileInput.bufferedReader().readText()
            val parts = content.split(":")
            if (parts.size == 2) Pair(parts[0], parts[1]) else null
        } catch (e: Exception) {
            null
        }
    }
}
