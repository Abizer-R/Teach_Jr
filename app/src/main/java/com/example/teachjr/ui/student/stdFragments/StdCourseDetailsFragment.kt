package com.example.teachjr.ui.student.stdFragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.data.model.StdAttendanceDetails
import com.example.teachjr.databinding.FragmentStdCourseDetailsBinding
import com.example.teachjr.ui.adapters.StdLecListAdapter
import com.example.teachjr.ui.viewmodels.studentViewModels.SharedStdViewModel
import com.example.teachjr.ui.viewmodels.studentViewModels.StdCourseViewModel
import com.example.teachjr.utils.AdapterUtils
import com.example.teachjr.utils.AnimationExtUtil.startAnimation
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class StdCourseDetailsFragment : Fragment() {

    private val TAG = StdCourseDetailsFragment::class.java.simpleName
    private lateinit var binding: FragmentStdCourseDetailsBinding

    private val courseViewModel by viewModels<StdCourseViewModel>()
    private val sharedStdViewModel by activityViewModels<SharedStdViewModel>()

    private val stdLecListAdapter = StdLecListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStdCourseDetailsBinding.inflate(layoutInflater)
        Log.i(TAG, "StudentTesting_CoursePage: Student CoursePage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            title = "Course Detail"

            // Adding up navigation
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        initialSetup()
        setupViews()
    }

    private fun initialSetup() {

        if(sharedStdViewModel.courseValuesNotNull()) {
            binding.tvCourseCode.text = sharedStdViewModel.courseCode
            binding.tvCourseName.text = sharedStdViewModel.courseName
            binding.tvProfName.text = sharedStdViewModel.profName

            courseViewModel.getAttendanceDetails(sharedStdViewModel.courseCode!!, sharedStdViewModel.userDetails!!.sem_sec!!)

        } else {
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        }

        courseViewModel.atdDetails.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
                    showLoading()
//                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    stopLoading()
                    // TODO: Convert Loading layout into an ErrorLayout ("Uh-Oh, an error occurred")
                    //  and show the error textview in white area
//                    binding.progressBar.visibility = View.GONE
                    Log.i(TAG, "StudentTesting_CoursePage: lecDetails_Error - ${it.errorMessage}")
                    Toast.makeText(context, "Error: ${it.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    stopLoading()
//                    binding.progressBar.visibility = View.GONE
                    Log.i(TAG, "StudentTesting_CoursePage: lecDetails - ${it.data}")

                    if(it.data != null) {
                        updateViews(it.data)
                    } else {
                        Toast.makeText(context, "Attendance details were null", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    private fun setupViews() {
        binding.rvLecList.apply {
            hasFixedSize()
            adapter = stdLecListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun updateViews(atdDetails: StdAttendanceDetails) {
        stopLoading()

        binding.tvUserName.text = sharedStdViewModel.userDetails!!.name
        binding.tvCurrDate.text = AdapterUtils.getFormattedDate2(Calendar.getInstance().timeInMillis.toString())
        binding.tvPresentCount.text = atdDetails.presentLecCount.toString()
        binding.tvAbsentCount.text = atdDetails.absentLecCount.toString()

        // TODO: Change the bar color acc to percentage
        // Setting up percentage Tv and bar
        val percentage = (atdDetails.presentLecCount * 100) / atdDetails.totalLecCount
        binding.tvPercentage.text = percentage.toString() + "%"

        // Dividing the progress into 1000 chunks for smooth animation
        binding.percentageBar.max = 1000
        ObjectAnimator.ofInt(binding.percentageBar, "progress", percentage * 10)
            .setDuration(2000)
            .start()

        val revLecList = atdDetails.lecList.reversed()

        // Show mark Attendance if attendance is onGoing
        if(revLecList[0].isContinuing) {
            setupFAB()
        }
        stdLecListAdapter.updateList(revLecList)
    }

    private fun setupFAB() {
        binding.fabMarkAtd.visibility = View.VISIBLE

        // Setting up the FAB explosion
        val animation = AnimationUtils.loadAnimation(context, R.anim.fab_explosion_anim)
        animation.apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
        }
        binding.fabMarkAtd.setOnClickListener {
            binding.fabMarkAtd.visibility = View.INVISIBLE
            binding.fabShape.visibility = View.VISIBLE
            // We will use our own extension func that we made (ViewExt.kt) and not the default one
            binding.fabShape.startAnimation(animation) {
                // This callback will be called when the animation ends
                findNavController().navigate(R.id.action_stdCourseDetailsFragment_to_stdMarkAtdFragment)
            }
        }
    }

    private fun showLoading() {
        binding.layoutLoaded.visibility = View.GONE
        binding.atdDetailLayout.visibility = View.GONE

        binding.layoutLoading.visibility = View.VISIBLE
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.loadingAnimation.playAnimation()
    }

    private fun stopLoading() {
        binding.layoutLoaded.visibility = View.VISIBLE
        binding.atdDetailLayout.visibility = View.VISIBLE

        binding.layoutLoading.visibility = View.GONE
        binding.loadingAnimation.visibility = View.GONE
        binding.loadingAnimation.cancelAnimation()
    }
}