package com.mjw.smart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MakeGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_group)

        // 중복 확인 버튼
        findViewById<Button>(R.id.btnCheckDuplicate).setOnClickListener {
            Toast.makeText(this, "Checked!", Toast.LENGTH_SHORT).show()
        }

        // Make 버튼
        findViewById<Button>(R.id.btnMake).setOnClickListener {
            Toast.makeText(this, "Group Created!", Toast.LENGTH_SHORT).show()
        }

        // 하단 메뉴 설정
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_profile
        bottomNav.menu.findItem(R.id.menu_placeholder).isEnabled = false

        bottomNav.setOnItemSelectedListener { item ->
            val intent: Intent? = when (item.itemId) {
                R.id.menu_calendar -> Intent(this, MainActivity::class.java)
                R.id.menu_stats -> Intent(this, StatsActivity::class.java)
                R.id.menu_alarm -> Intent(this, NotificationActivity::class.java)
                R.id.menu_profile -> Intent(this, ProfileActivity::class.java)
                else -> null
            }
            if (intent != null) {
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            true
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }
    }
}