package com.mjw.smart

//일정 설계도
data class Event(
    val date: String,  // 날짜
    val time: String,  // 시간
    val title: String, // 제목
    val desc: String,   // 설명
    val category: String
)

//일정을 저장장소 (앱 전체에서 공유됨)
object EventRepository {
    //일정들이 저장되는 리스트
    val eventList = mutableListOf<Event>()

    //테스트 데이터
    init {
        //여기 데이터들도 5개씩 꽉 차 있어야 함
        eventList.add(Event("2", "10:00-13:00", "Design UX", "Start screen 16", "Brainstorm"))
        eventList.add(Event("2", "19:00-20:00", "Workout", "Leg day", "Workout"))
        eventList.add(Event("15", "14:00-15:00", "Meeting", "Brainstorming", "Design"))
    }
    fun getEventsByDate(day: String): List<Event> {
        return eventList.filter { it.date == day }
    }

    fun addEvent(event: Event) {
        eventList.add(event)
    }
}