package com.example.balancebeam

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.balancebeam.utils.FileHelper
import java.util.regex.Pattern

class Signup : AppCompatActivity() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var signUpButton: MaterialButton

    // Password pattern requirements:
    // At least 8 characters, one uppercase, one lowercase, one digit, one special character
    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        nameInput = findViewById(R.id.name)
        emailInput = findViewById(R.id.emal)
        passwordInput = findViewById(R.id.pass)
        confirmPasswordInput = findViewById(R.id.conpass)
        signUpButton = findViewById(R.id.signup)

        signUpButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            when {
                name.isEmpty() -> {
                    nameInput.error = "Name cannot be empty"
                    nameInput.requestFocus()
                }
                email.isEmpty() -> {
                    emailInput.error = "Email cannot be empty"
                    emailInput.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Please enter a valid email"
                    emailInput.requestFocus()
                }
                password.isEmpty() -> {
                    passwordInput.error = "Password cannot be empty"
                    passwordInput.requestFocus()
                }
                !PASSWORD_PATTERN.matcher(password).matches() -> {
                    passwordInput.error = """
                        Password must contain:
                        - 8 characters minimum
                        - At least one uppercase letter
                        - At least one lowercase letter
                        - At least one digit
                        - At least one special character (@#$%^&+=!)
                        """.trimIndent()
                    passwordInput.requestFocus()
                }
                password != confirmPassword -> {
                    confirmPasswordInput.error = "Passwords don't match"
                    confirmPasswordInput.requestFocus()
                }
                else -> {
                    // All validations passed
                    FileHelper.saveUser(this, email, password)
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, login::class.java))
                    finish()
                }
            }
        }
    }
}