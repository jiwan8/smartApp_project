package com.mjw.smart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


//데이터 설계도
data class NotificationItem(val title: String, val time: String)

//어댑터
class NotificationAdapter(private val notiList: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.NotiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiViewHolder {
        //item_notification가져오기
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotiViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotiViewHolder, position: Int) {
        val item = notiList[position]
        holder.tvTitle.text = item.title
        holder.tvTime.text = item.time

        //필요하다면 여기서 버튼 클릭 이벤트 등을 처리할 수 있습니다
    }

    override fun getItemCount() = notiList.size

    class NotiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNotiTitle)
        val tvTime: TextView = view.findViewById(R.id.tvNotiTime)
        // 점 3개 버튼이나 빨간 점 등은 지금 기능이 없으니 연결 안 해도 괜찮습니다.
    }
}