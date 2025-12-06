package com.mjw.smart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


data class ProfileMenu(val title: String, val iconRes: Int)

// ★ 생성자에 (val onItemClick: (String) -> Unit) 추가됨!
class ProfileMenuAdapter(
    private val menuList: List<ProfileMenu>,
    private val onItemClick: (String) -> Unit // 클릭했을 때 할 일
) : RecyclerView.Adapter<ProfileMenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuList[position]
        holder.tvTitle.text = item.title
        holder.ivIcon.setImageResource(item.iconRes)

        // ★ 클릭 이벤트 추가
        holder.itemView.setOnClickListener {
            onItemClick(item.title) // 클릭된 메뉴의 제목을 전달
        }
    }

    override fun getItemCount() = menuList.size

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivMenuIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvMenuTitle)
    }
}