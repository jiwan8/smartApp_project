package com.mjw.smart

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // 1. 뒤로가기 버튼
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // 현재 화면 닫기 (프로필 화면으로 돌아감)
        }

        // 2. 업데이트 버튼
        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            // 여기에 DB 저장 로직 추가 가능
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            finish() // 저장 후 화면 닫기
        }
    }
}