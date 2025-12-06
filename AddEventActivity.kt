package com.mjw.smart // ★ 패키지 이름 확인!

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import com.mjw.smart.data.Event
import com.mjw.smart.data.EventRepository

class AddEventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_event)

        //날짜 선택
        val inputDate = findViewById<TextInputLayout>(R.id.inputDate)
        inputDate.editText?.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateText = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                inputDate.editText?.setText(dateText)
            }, year, month, day).show()
        }

        //시작/종료 시간 선택
        val inputStartTime = findViewById<TextInputLayout>(R.id.inputStartTime)
        inputStartTime.editText?.setOnClickListener { showTimePicker(inputStartTime) }

        val inputEndTime = findViewById<TextInputLayout>(R.id.inputEndTime)
        inputEndTime.editText?.setOnClickListener { showTimePicker(inputEndTime) }

        // Priority 선택 확인
        val chipGroupPriority = findViewById<ChipGroup>(R.id.chipGroupPriority)
        chipGroupPriority.setOnCheckedChangeListener { group, checkedId ->
        }

        //생성 버튼 클릭
        val btnCreate = findViewById<Button>(R.id.btnCreate)
        btnCreate.setOnClickListener {
            //기존 입력값 가져오기 코드
            val name = findViewById<TextInputLayout>(R.id.inputName).editText?.text.toString()
            val dateText = inputDate.editText?.text.toString()
            val startTime = findViewById<TextInputLayout>(R.id.inputStartTime).editText?.text.toString()
            val endTime = findViewById<TextInputLayout>(R.id.inputEndTime).editText?.text.toString()

            val calendarStart = Calendar.getInstance()
            val calendarEnd = Calendar.getInstance()

            //추가됨
            val dateParts = dateText.split("/")
            if (dateParts.size == 3) {
                val day = dateParts[0].toInt()
                val month = dateParts[1].toInt() - 1 // Calendar는 0월부터 시작
                val year = dateParts[2].toInt()
                val startParts = startTime.split(":").map { it.toInt() }
                val endParts = endTime.split(":").map { it.toInt() }

                calendarStart.set(year, month, day, startParts[0], startParts[1])
                calendarEnd.set(year, month, day, endParts[0], endParts[1])
            }
            val desc = findViewById<TextInputLayout>(R.id.inputNote)
                .editText?.text.toString()
            val intent = Intent().apply {
                putExtra("title", name)
                putExtra("desc", desc)
                putExtra("startTime", calendarStart.timeInMillis)
                putExtra("endTime", calendarEnd.timeInMillis)
            }//여기까지

            val day = dateText.split("/").firstOrNull() ?: ""

            //카테고리 가져오기
            val chipGroupCategory = findViewById<ChipGroup>(R.id.chipGroupCategory)
            val selectedChipId = chipGroupCategory.checkedChipId
            var category = "Brainstorm" // 기본값

            if (selectedChipId != View.NO_ID) {
                val chip = findViewById<Chip>(selectedChipId)
                category = chip.text.toString() // "Design" or "Workout" ...
            }

            if (name.isEmpty() || day.isEmpty()) {
                Toast.makeText(this, "Please fill in name and date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val loginType = intent.getStringExtra("login_type") ?: "local"

            //Event 객체 생성 시 category도 같이 넣어서 저장
            val newEvent = Event(day, "$startTime-$endTime", name, "Note...", category)
            EventRepository.addEvent(newEvent)

            Toast.makeText(this, "Event Created!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    //시간 선택 팝업 함수
    private fun showTimePicker(inputLayout: TextInputLayout) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeText = String.format("%02d:%02d", selectedHour, selectedMinute)
            inputLayout.editText?.setText(timeText)
        }, hour, minute, true).show()
    }
}