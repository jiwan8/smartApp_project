package com.mjw.smart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout


class AddFriendActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        // [AddFriendActivity.kt] onCreate 내부
        // ★ 뒤로가기 버튼 기능 추가
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish() // 현재 화면 닫기 (프로필로 돌아감)
        }

        // ... (나머지 검색 버튼, 하단 메뉴 코드는 그대로 유지) ...
        //  검색 버튼 클릭
        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val name = findViewById<TextInputLayout>(R.id.inputUserName).editText?.text.toString()

            if (name.isNotEmpty()) {
                Toast.makeText(this, "Searching for $name...", Toast.LENGTH_SHORT).show()
                // 나중에 실제 검색 로직 추가
            } else {
                Toast.makeText(this, "Please enter user name", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. 하단 메뉴 설정 (프로필 탭 활성화 상태로 둠)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_profile
        bottomNav.menu.findItem(R.id.menu_placeholder).isEnabled = false

        bottomNav.setOnItemSelectedListener { item ->
            val intent: Intent? = when (item.itemId) {
                R.id.menu_calendar -> Intent(this, MainActivity::class.java)
                R.id.menu_stats -> Intent(this, StatsActivity::class.java)
                R.id.menu_alarm -> Intent(this, NotificationActivity::class.java)
                R.id.menu_profile -> Intent(this, ProfileActivity::class.java) // 프로필로 돌아가기
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