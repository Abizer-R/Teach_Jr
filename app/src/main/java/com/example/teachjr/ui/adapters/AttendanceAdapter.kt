package com.example.teachjr.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R

class AttendanceAdapter : RecyclerView.Adapter<AttendanceAdapter.CourseViewHolder>() {

    private var enrollments: List<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_attendance_rv, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.apply {
            val size = enrollments[position].length
            tvEnrollment.text = enrollments[position].substring(size - 3)
        }
    }

    override fun getItemCount(): Int {
        return enrollments.size
    }

    fun updateList(updatedList: List<String>) {
        enrollments = updatedList
        notifyDataSetChanged()
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvEnrollment = itemView.findViewById<TextView>(R.id.tvEnrollment)
    }
}