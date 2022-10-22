package com.example.teachjr.ui.adapters

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.model.RvCourseListItem

class CourseListAdapter(
    private val onItemClicked: (RvCourseListItem) -> Unit
): RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>() {

    private var courses: List<RvCourseListItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_course_rv, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.apply {
            tvCourseCode.text = courses[position].courseCode
            tvCourseName.text = courses[position].courseName
            val semSec = courses[position].sem_sec
            if(semSec != null) {
                val secIdx = semSec.indexOf("_", 0)
                tvSection.text = semSec.substring(secIdx + 1)
            } else {
                // TODO: CHECK IF THIS WORKS IN STUDENT HOMEPAGE
                tvSection.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                onItemClicked.invoke(courses[position])
            }
        }
    }

    fun updateList(updatedCourses: List<RvCourseListItem>) {
        courses = updatedCourses
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return courses.size
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCourseCode = itemView.findViewById<TextView>(R.id.tvCourseCode)
        var tvCourseName = itemView.findViewById<TextView>(R.id.tvCourseName)
        var tvSection = itemView.findViewById<TextView>(R.id.tvSection)
    }
}