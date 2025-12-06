package com.mjw.smart.data

data class Event(
    val date: String,
    val time: String,
    val title: String,
    val desc: String,
    val category: String // "Brainstorm", "Design", "Workout" 등 저장
)
{
    fun toMap() = mapOf(
        "date" to date,
        "time" to time,
        "title" to title,
        "desc" to desc,
        "category" to category
    )
}

object EventRepository {
    val eventList = mutableListOf<Event>()

    init {
        // 테스트 데이터에도 카테고리 정보 추가
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
