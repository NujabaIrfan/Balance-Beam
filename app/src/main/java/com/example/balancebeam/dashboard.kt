package com.example.balancebeam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.balancebeam.databinding.ActivityDashboardBinding
import com.example.balancebeam.fragments.DashboardFragment
import com.example.balancebeam.fragments.ReportsFragment
import com.example.balancebeam.fragments.TransactionsFragment
import com.example.balancebeam.utils.SharedPrefManager
import com.google.android.material.button.MaterialButton

class dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    private val dashboardFragment = DashboardFragment()
    private val transactionsFragment = TransactionsFragment()
    private val reportsFragment = ReportsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Load default fragment
        loadFragment(dashboardFragment)

        // Bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    loadFragment(dashboardFragment)
                    true
                }
                R.id.navigation_transactions -> {
                    loadFragment(transactionsFragment)
                    true
                }
                R.id.navigation_reports -> {
                    loadFragment(reportsFragment)
                    true
                }
                else -> false
            }
        }

        // Logout
        binding.toolbar.findViewById<MaterialButton>(R.id.logoutButton).setOnClickListener {
            SharedPrefManager.saveLoginState(this, false)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, login::class.java))
            finish()
        }

        // ✅ Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
            }
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }
}
