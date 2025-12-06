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


class NewGroupsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_groups)

        // 1. 버튼 클릭 이벤트
        // NewGroupsActivity.kt - onCreate 내부

        findViewById<Button>(R.id.btnJoin).setOnClickListener {
            // Join 화면으로 이동
            startActivity(Intent(this, JoinGroupActivity::class.java))
        }

        findViewById<Button>(R.id.btnMake).setOnClickListener {
            // Make 화면으로 이동
            startActivity(Intent(this, MakeGroupActivity::class.java))
        }

        // 2. 하단 메뉴 설정 (프로필에서 왔으므로 프로필 탭 활성화)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_profile
        bottomNav.menu.findItem(R.id.menu_placeholder).isEnabled = false

        bottomNav.setOnItemSelectedListener { item ->
            val intent: Intent? = when (item.itemId) {
                R.id.menu_calendar -> Intent(this, MainActivity::class.java)
                R.id.menu_stats -> Intent(this, StatsActivity::class.java)
                R.id.menu_alarm -> Intent(this, NotificationActivity::class.java)
                R.id.menu_profile -> Intent(this, ProfileActivity::class.java) // 프로필로 복귀
                else -> null
            }

            if (intent != null) {
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            true
        }

        // 3. + 버튼
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }
    }
}