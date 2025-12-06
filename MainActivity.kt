package com.mjw.smart

import android.accounts.Account
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

import com.google.api.services.calendar.Calendar as GoogleCalendar
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.*

interface OnDateClickListener {
    fun onDateClick(date: String)
}
class MainActivity : AppCompatActivity(), OnDateClickListener {

    private lateinit var accountPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // 달력 관련 변수
    private val currentCalendar: Calendar = Calendar.getInstance()
    private lateinit var calendarAdapter: CalendarAdapter
    private val dayList = mutableListOf<CalendarDay>()

    // 일정 리스트 관련 변수
    private lateinit var eventAdapter: EventAdapter
    private val displayEventList = mutableListOf<Event>() // 이제 EventData.kt의 Event를 씁니다.

    //구글 계정 정보
    private var googleAccount: GoogleSignInAccount? = null
    //구글 로그인 여부 확인
    private var useGoogleCalendar = false
    //로그인 방식 확인(일반 or 구글)
    private var loginType: String? = null

    //사용자에게 권한 허용 창 띄우기
    private lateinit var consentLauncher: ActivityResultLauncher<Intent>

    //AddEventActivity 결과 처리
    private lateinit var addEventLauncher: ActivityResultLauncher<Intent>

    //로컬에 일정 저장(임시, 추후 DB시스템에 저장되도록 수정)
    private fun addEventLocally(title: String, desc: String, start: Date, end: Date) {
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        EventRepository.addEvent(
            Event(
                date = dayFormat.format(start),
                time = "${timeFormat.format(start)}-${timeFormat.format(end)}",
                title = title,
                desc = desc,
                category = "Local"
            )
        )

        updateEventList(null)
        calendarAdapter.notifyDataSetChanged()

        Toast.makeText(this, "로컬 일정 추가됨", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //로그인 타입 저장
        loginType = intent.getStringExtra("login_type")
        useGoogleCalendar = false //로컬 모드가 기본
        //일반 로그인일 때
        if (loginType == "normal" || loginType == null) {
            useGoogleCalendar = false
            // 로컬 일정만 사용
        }//구글 로그인일 때
        else if (loginType == "google") {
            useGoogleCalendar = true

            googleAccount = GoogleSignIn.getLastSignedInAccount(this)
            if (googleAccount != null) {
                //기존 로그인 기록 있음
                fetchCalendarEventsWithCoroutine(googleAccount!!)
            } else {
                //로그인 기록 없으면 로그인 창 띄움
                accountPickerLauncher.launch(mGoogleSignInClient.signInIntent)
            }
        }

        //AddEventActivity 결과 처리
        addEventLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val title = data.getStringExtra("title") ?: return@registerForActivityResult
                val desc = data.getStringExtra("desc") ?: ""
                val startTimeMillis = data.getLongExtra("startTime", -1L)
                val endTimeMillis = data.getLongExtra("endTime", -1L)
                if (startTimeMillis == -1L || endTimeMillis == -1L) return@registerForActivityResult

                val startDate = Date(startTimeMillis)
                val endDate = Date(endTimeMillis)

                if (useGoogleCalendar && googleAccount != null) {
                    addEventToGoogleCalendar(title, desc, startDate, endDate)
                } else {
                    addEventLocally(title, desc, startDate, endDate)
                }
            }
        }

        consentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                // 사용자 동의 완료시, 다시 API 요청 실행
                googleAccount?.let { acc ->
                    fetchCalendarEventsWithCoroutine(acc)
                }
            }
        }

        //계정 선택
        accountPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    googleAccount = account
                    useGoogleCalendar = true //로그인 성공 시 구글 모드 활성화
                    fetchCalendarEventsWithCoroutine(account!!) // 계정 선택 후 캘린더 이벤트 가져오기
                } catch (e: ApiException) {
                    e.printStackTrace()
                    Toast.makeText(this, "계정 선택 실패", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "계정 선택 취소됨", Toast.LENGTH_SHORT).show()
            }
        }

        //Google Sign-In 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar.readonly"))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //달력 설정
        val rvCalendar = findViewById<RecyclerView>(R.id.rvCalendar)
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        calendarAdapter = CalendarAdapter(dayList, this)
        rvCalendar.adapter = calendarAdapter
        refreshCalendar() // 달력 날짜 계산

        //일정 리스트 설정
        val rvEvents = findViewById<RecyclerView>(R.id.rvEvents)
        eventAdapter = EventAdapter(displayEventList)
        rvEvents.adapter = eventAdapter

        //추가된 이벤트 나오도록 업데이트, 처음 켜면 전체 일정 보여주기
        updateEventList(null)

        //버튼 기능
        findViewById<ImageView>(R.id.btnPrevMonth).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            refreshCalendar()
        }

        findViewById<ImageView>(R.id.btnNextMonth).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            refreshCalendar()
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            addEventLauncher.launch(Intent(this, AddEventActivity::class.java))
        }
        //하단 메뉴 설정 (MainActivity)
        val bottomNav =
            findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.menu_calendar //현재: 달력
        bottomNav.menu.findItem(R.id.menu_placeholder).isEnabled = false

        bottomNav.setOnItemSelectedListener { item ->
            val intent: Intent? = when (item.itemId) {
                R.id.menu_calendar -> null // 자기 자신 클릭: 아무것도 안함
                R.id.menu_stats -> Intent(this, StatsActivity::class.java)
                R.id.menu_alarm -> Intent(this, NotificationActivity::class.java)
                R.id.menu_profile -> Intent(this, ProfileActivity::class.java)
                else -> null
            }

            if (intent != null) {
                startActivity(intent)
                overridePendingTransition(0, 0) //깜빡임 제거
                finish() //현재 화면 끄기 (뒤로가기 안 쌓이게)
            }
            true
        }
    }
    //구글 캘린더와 EventRepository에 일정 추가
    private fun addEventToGoogleCalendar(title: String, desc: String, startTime: Date, endTime: Date) {
        val account = googleAccount ?: return
        val credential = GoogleAccountCredential.usingOAuth2(
            this,
            listOf("https://www.googleapis.com/auth/calendar")
        )
        credential.selectedAccount = account.account

        val calendarService = GoogleCalendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("My Smart App").build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val gEvent = com.google.api.services.calendar.model.Event().apply {
                    summary = title
                    description = desc
                    start = com.google.api.services.calendar.model.EventDateTime()
                        .setDateTime(com.google.api.client.util.DateTime(startTime))
                        .setTimeZone(TimeZone.getDefault().id)
                    end = com.google.api.services.calendar.model.EventDateTime()
                        .setDateTime(com.google.api.client.util.DateTime(endTime))
                        .setTimeZone(TimeZone.getDefault().id)
                }

                val createdEvent = calendarService.events().insert("primary", gEvent).execute()

                //로컬 저장소에도 반영(임시)
                val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                EventRepository.addEvent(
                    Event(
                        date = dayFormat.format(startTime),
                        time = "${timeFormat.format(startTime)}-${timeFormat.format(endTime)}",
                        title = title,
                        desc = desc,
                        category = "Google"
                    )
                )

                withContext(Dispatchers.Main) {
                    calendarAdapter.notifyDataSetChanged() // 점 표시 갱신
                    updateEventList(null)
                    Toast.makeText(this@MainActivity, "일정 추가 완료", Toast.LENGTH_SHORT).show()
                }

            } catch (e: UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    consentLauncher.launch(e.intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "일정 추가 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 일정 추가 후 돌아왔을 때 갱신
    override fun onResume() {
        super.onResume()
        calendarAdapter.notifyDataSetChanged() // 점 표시 갱신
        // 돌아오면 전체 목록 혹은 마지막 선택 상태 갱신 (여기선 전체 초기화)
        updateEventList(null)
    }

    // 날짜 클릭 시 실행 (인터페이스)
    override fun onDateClick(date: String) {
        updateEventList(date)
    }

    // 리스트 갱신 함수
    private fun updateEventList(date: String?) {
        displayEventList.clear()

        if (date == null) {
            // 날짜 없으면 전체 일정 가져오기
            displayEventList.addAll(EventRepository.eventList)
        } else {
            // 선택한 날짜 일정만 가져오기
            displayEventList.addAll(EventRepository.getEventsByDate(date))
        }
        eventAdapter.notifyDataSetChanged()
    }

    // 달력 새로고침
    private fun refreshCalendar() {
        dayList.clear()

        val tvYear = findViewById<TextView>(R.id.tvYear)
        val tvMonth = findViewById<TextView>(R.id.tvMonth)
        val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)

        tvYear.text = yearFormat.format(currentCalendar.time)
        tvMonth.text = monthFormat.format(currentCalendar.time)

        val tempCalendar = currentCalendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)
        val emptyDayCount = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

        for (i in 0 until emptyDayCount) dayList.add(CalendarDay(0,0,0))

        val maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDay) dayList.add(
            CalendarDay(
                year = tempCalendar.get(Calendar.YEAR),
                month = tempCalendar.get(Calendar.MONTH) + 1,
                day = i
            )
        )
        calendarAdapter.notifyDataSetChanged()
    }

    private fun fetchCalendarEventsWithCoroutine(account: GoogleSignInAccount) {
        if (!useGoogleCalendar) return
        val credential = GoogleAccountCredential.usingOAuth2(
            this, listOf("https://www.googleapis.com/auth/calendar.readonly","https://www.googleapis.com/auth/calendar")
        )
        credential.selectedAccount = account.account
        val email = account.email
        if (email == null) {
            Log.e("GoogleLogin", "Google 계정 이메일을 가져올 수 없습니다.")
            Toast.makeText(this, "Google 계정 이메일이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedAccount = Account(email, "com.google")
        credential.selectedAccount = selectedAccount

        val calendarservice = GoogleCalendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("My Smart App").build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = com.google.api.client.util.DateTime(System.currentTimeMillis())
                Log.d("CalendarFetch", "Starting API call...")
                val calendarId = "primary"
                val events = calendarservice.events().list(calendarId)
                    .setTimeMin(now)
                    .setMaxResults(50)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                EventRepository.eventList.clear()
                events.items.forEach { item ->
                    //Google DateTime 가져오기
                    val startDateTime = item.start.dateTime
                    val endDateTime = item.end.dateTime

                    //날짜 처리 (yyyy-MM-dd 형식)
                    val dateStr = (startDateTime ?: item.start.date)
                        .toStringRfc3339()
                        .substring(0, 10)

                    //시간 처리
                    val startTime = if (startDateTime != null) {
                        startDateTime.toStringRfc3339().substring(11, 16)
                    } else {
                        "종일"
                    }
                    val endTime = if (endDateTime != null) {
                        endDateTime.toStringRfc3339().substring(11, 16)
                    } else {
                        ""
                    }

                    EventRepository.addEvent(
                        Event(
                            date = dateStr,
                            time = if (endTime.isNotEmpty()) "$startTime-$endTime" else startTime,
                            title = item.summary ?: "제목 없음",
                            desc = item.description ?: "",
                            category = "Google"
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    updateEventList(null)
                    calendarAdapter.notifyDataSetChanged()
                }

            } catch (e: GoogleJsonResponseException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "캘린더 이벤트 가져오기 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            catch (e: UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    consentLauncher.launch(e.intent)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "이벤트 가져오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

//데이터 클래스

    data class CalendarDay(val year: Int,
                           val month: Int,
                           val day: Int
    ) {
        val dateString: String
            get() = String.format("%04d-%02d-%02d", year, month, day)
    }

//가로 달력

    class CalendarAdapter(
        private val dayList: List<CalendarDay>,
        private val listener: OnDateClickListener
    ) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

        private var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_day, parent, false)
            return DayViewHolder(view)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            val item = dayList[position]
            holder.tvDate.text = item.day.toString()

            if (item.day == 0) {
                holder.tvDate.background = null
                holder.tvDate.text = ""
                holder.viewEventDot.visibility = View.INVISIBLE
                holder.itemView.isEnabled = false
            } else {
                holder.itemView.isEnabled = true

                // 점 표시
                val hasEvent = EventRepository.eventList.any { event ->
                    event.date == item.dateString
                }
                holder.viewEventDot.visibility = if (hasEvent) View.VISIBLE else View.INVISIBLE

                // 선택 효과
                //선택된 날짜 색상 처리
                if (selectedPosition == position) {
                    // 기존: R.drawable.ic_circle_blue -> 변경: R.drawable.bg_date_selected
                    holder.tvDate.setBackgroundResource(R.drawable.bg_date_selected)
                    holder.tvDate.setTextColor(Color.WHITE)
                } else {
                    holder.tvDate.setBackgroundColor(Color.TRANSPARENT)
                    holder.tvDate.setTextColor(Color.BLACK)
                }
            }

            holder.itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                if (item.day != 0) listener.onDateClick(item.dateString)
            }
        }

        override fun getItemCount() = dayList.size

        class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val viewEventDot: View = view.findViewById(R.id.viewEventDot)
        }
    }
//일정 리스트
    class EventAdapter(private val eventList: List<Event>) :
        RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
            return EventViewHolder(view)
        }
        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val item = eventList[position]
            holder.tvTime.text = item.time
            holder.tvTitle.text = item.title
            holder.tvDesc.text = item.desc

            //카테고리에 따라 점(viewDot) 색상 변경
            val colorCode = when (item.category) {
                "Design" -> "#4CAF50"   // 초록색
                "Workout" -> "#00BCD4"  // 하늘색
                "Brainstorm" -> "#2962FF" // 파란색
                "Google" -> "#000000"   //검정
                else -> "#9E9E9E"       // 그 외(회색)
            }

            // 점(Dot) 색상 적용 (backgroundTintList 사용)
            holder.viewDot.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor(colorCode))
        }

        override fun getItemCount() = eventList.size
        class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // item_event.xml에 있는 파란 점(viewDot) ID를 가져옴
            val viewDot: View = view.findViewById(R.id.viewDot)
            val tvTime: TextView = view.findViewById(R.id.tvTime)
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        }
    }
}