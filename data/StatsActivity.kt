package com.mjw.smart

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // 1. 하단 메뉴 설정
        // --- 하단 메뉴 설정 (StatsActivity) ---
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_stats // ★ 현재: 통계
        bottomNav.menu.findItem(R.id.menu_placeholder).isEnabled = false

        bottomNav.setOnItemSelectedListener { item ->
            val intent: Intent? = when (item.itemId) {
                R.id.menu_calendar -> Intent(this, MainActivity::class.java)
                R.id.menu_stats -> null // 자기 자신 클릭: 아무것도 안함
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

        // 2. + 버튼
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }
    }
}