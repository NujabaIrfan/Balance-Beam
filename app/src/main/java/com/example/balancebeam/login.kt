package com.example.balancebeam

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.balancebeam.utils.FileHelper
import com.example.balancebeam.utils.SharedPrefManager.saveLoginState
import com.example.balancebeam.utils.SharedPrefManager.isLoggedIn
import com.example.balancebeam.Signup

class login : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (isLoggedIn(this)) {
            startActivity(Intent(this, dashboard::class.java))
            finish()
        }

        emailInput = findViewById(R.id.loinemail)
        passwordInput = findViewById(R.id.loginpass)
        loginButton = findViewById(R.id.loginButton)


        loginButton.setOnClickListener {
            val inputEmail = emailInput.text.toString().trim()
            val inputPassword = passwordInput.text.toString()

            val savedUser = FileHelper.readUser(this)
            if (savedUser != null && savedUser.first == inputEmail && savedUser.second == inputPassword) {
                saveLoginState(this, true)
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, dashboard::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }


        val nextButton = findViewById<TextView>(R.id.create_account)
        nextButton.setOnClickListener {
            val intent = Intent(this,Signup::class.java)
            startActivity(intent)
        }
    }
}