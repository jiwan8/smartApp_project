package com.mjw.smart

import android.content.Intent
import android.os.Bundle
import android.widget.TextView // ★ TextView import 확인!
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mjw.smart.ui.login.LoginActivity


class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // ★★★ 1. 로그아웃 버튼 (TextView로 수정됨) ★★★
        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // ... (앞부분 생략) ...

        // ... (앞부분 생략) ...

        val rvProfileMenu = findViewById<RecyclerView>(R.id.rvProfileMenu)
        rvProfileMenu.layoutManager = LinearLayoutManager(this)

        // 1. 메뉴 리스트에 "Groups" 추가!
        val menuList = listOf(
            ProfileMenu("Groups", R.drawable.ic_nav_profile), // ★ Groups 추가
            ProfileMenu("Notification", R.drawable.ic_nav_alarm),
            ProfileMenu("Settings", R.drawable.ic_nav_stats),
            ProfileMenu("Help & Support", R.drawable.ic_nav_profile)
        )

        // 2. 어댑터 연결 (클릭 시 이동 로직 추가)
        rvProfileMenu.adapter = ProfileMenuAdapter(menuList) { title ->
            // 클릭된 메뉴 제목(title)에 따라 이동
            if (title == "Groups") {
                val intent = Intent(this, NewGroupsActivity::class.java)
                startActivity(intent)
            }
            else {
                // 다른 메뉴는 토스트 메시지
                // Toast.makeText(this, "$title Clicked", Toast.LENGTH_SHORT).show()
            }
        }

        // 친구 추가 버튼 연결
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddFriends).setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            startActivity(intent)
        }
        // [ProfileActivity.kt] onCreate 내부

        // Edit Profile 버튼 클릭 시 이동
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // ... (뒷부분 생략) ...

        // 3. 하단 메뉴 설정
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_profile
        bottomNav.menu.findItem(R.id.menu_placeholder).isEnabled = false

        bottomNav.setOnItemSelectedListener { item ->
            val intent: Intent? = when (item.itemId) {
                R.id.menu_calendar -> Intent(this, MainActivity::class.java)
                R.id.menu_stats -> Intent(this, StatsActivity::class.java)
                R.id.menu_alarm -> Intent(this, NotificationActivity::class.java)
                R.id.menu_profile -> null
                else -> null
            }

            if (intent != null) {
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            true
        }

        // 4. + 버튼
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }
    }
}