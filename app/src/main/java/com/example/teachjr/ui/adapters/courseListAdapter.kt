package com.example.teachjr.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.model.RvCourseListInfo

class CourseListAdapter(): RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>() {

    private var courses: List<RvCourseListInfo> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_course_rv, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.apply {
            tvCourseCode.text = courses[position].subCode
            tvCourseName.text = courses[position].subName
        }
    }

    fun updateList(updatedCourses: List<RvCourseListInfo>) {
        courses = updatedCourses
    }

    override fun getItemCount(): Int {
        return courses.size
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCourseCode = itemView.findViewById<TextView>(R.id.tvCourseCode)
        var tvCourseName = itemView.findViewById<TextView>(R.id.tvCourseName)
    }
}