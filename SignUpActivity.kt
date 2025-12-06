package com.mjw.smart

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()

        // 1. 토글 버튼 (Log In 누르면 로그인 화면으로 복귀)
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && checkedId == R.id.btnToggleLogin) {
                // 로그인 화면으로 돌아가기
                finish()
                overridePendingTransition(0, 0)
            }
        }

        // 2. 생년월일 날짜 선택 (DatePicker)
        val inputDateLayout = findViewById<TextInputLayout>(R.id.inputDate)
        inputDateLayout.editText?.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                inputDateLayout.editText?.setText(formattedDate)
            }, year, month, day).show()
        }

        // 3. Register 버튼 클릭
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            //이메일과 비번 받아오기
            val email = findViewById<TextInputLayout>(R.id.inputEmail).editText?.text.toString()
            val password = findViewById<TextInputLayout>(R.id.inputPassword).editText?.text.toString()
            //공백 칸 감지
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        // 회원가입 성공 시 LoginActivity로 돌아가기
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Registration Failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}