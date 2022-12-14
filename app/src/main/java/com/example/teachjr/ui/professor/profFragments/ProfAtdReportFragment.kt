package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfAtdReportBinding
import com.example.teachjr.ui.adapters.AtdReportViewPagerAdapter
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfAtdReportViewModel
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfAtdReportFragment : Fragment() {

    private val TAG = ProfAtdReportFragment::class.java.simpleName
    private lateinit var binding: FragmentProfAtdReportBinding

    private val atdReportViewModel by activityViewModels<ProfAtdReportViewModel>()
    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    private lateinit var viewPagerAdapter: AtdReportViewPagerAdapter
    private val tabLayoutTitles = arrayListOf("Lectures", "Students")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfAtdReportBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {

            //Adding up navigation
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_white_32)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }


        viewPagerAdapter = AtdReportViewPagerAdapter(this)
//        binding.viewPager.offscreenPageLimit = 2 // This ensures that both are fragments stay alive all the time
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabLayoutTitles[position]
        }.attach()

        if(sharedProfViewModel.courseValuesNotNull()) {
            lifecycleScope.launch {
                atdReportViewModel.getAtdDetails(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!)
            }
        } else {
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        }
    }

}