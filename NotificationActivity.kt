package com.mjw.smart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class NotificationActivity : AppCompatActivity() {

    // 전역 변수로 선언 (어디서든 접근 가능하게)
    private lateinit var rvNotification: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var adapter: NotificationAdapter

    // 데이터 리스트 (변경 가능하도록 MutableList 사용)
    private val notiList = mutableListOf(
        NotificationItem("Combined Utility Form", "05.05.2024 14:20"),
        NotificationItem("What documents do I need?", "05.05.2024 16:11"),
        NotificationItem("Can you help me calculate?", "15.04.2024 17:20"),
        NotificationItem("How do I declare income?", "25.03.2024 23:20"),
        NotificationItem("Financial aid programs?", "12.02.2024 18:21")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // 뷰 연결
        rvNotification = findViewById(R.id.rvNotification)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)

        // 어댑터 연결
        adapter = NotificationAdapter(notiList)
        rvNotification.adapter = adapter

        // ★ 처음에 데이터가 있는지 없는지 검사해서 화면 갱신
        updateView()

        // 1. 휴지통 버튼 클릭 (모두 삭제)
        findViewById<ImageView>(R.id.btnDeleteAll).setOnClickListener {
            if (notiList.isNotEmpty()) {
                notiList.clear() // 데이터 싹 비우기
                adapter.notifyDataSetChanged() // 리스트 새로고침
                updateView() // ★ 화면 상태 변경 (빈 화면 보여주기)
                Toast.makeText(this, "All history cleared", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "History is already empty", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. 빈 화면의 [New Schedule] 버튼 클릭
        findViewById<Button>(R.id.btnNewSchedule).setOnClickListener {
            // 일정 추가 화면으로 이동
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        // 3. 하단 메뉴 설정 (기존 코드 유지)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_alarm
        bottomNav.menu.findItem(R.id.menu_placeholder).isEnabled = false

        bottomNav.setOnItemSelectedListener { item ->
            val intent: Intent? = when (item.itemId) {
                R.id.menu_calendar -> Intent(this, MainActivity::class.java)
                R.id.menu_stats -> Intent(this, StatsActivity::class.java)
                R.id.menu_alarm -> null
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

        // 4. + 버튼
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }
    }

    // ★ 데이터 유무에 따라 화면을 바꿔주는 핵심 함수
    private fun updateView() {
        if (notiList.isEmpty()) {
            rvNotification.visibility = View.GONE      // 리스트 숨김
            layoutEmptyState.visibility = View.VISIBLE // 빈 화면 보임
        } else {
            rvNotification.visibility = View.VISIBLE   // 리스트 보임
            layoutEmptyState.visibility = View.GONE    // 빈 화면 숨김
        }
    }
}