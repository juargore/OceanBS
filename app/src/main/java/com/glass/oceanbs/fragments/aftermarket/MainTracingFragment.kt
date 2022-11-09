package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glass.oceanbs.R
import com.glass.oceanbs.fragments.aftermarket.adapters.SummaryPagerAdapter
import com.glass.oceanbs.fragments.aftermarket.adapters.ViewPagerNoScroll
import com.google.android.material.tabs.TabLayout

class MainTracingFragment : Fragment() {

    companion object {
        fun newInstance() = MainTracingFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        val root = infl.inflate(R.layout.fragment_main_tracing, cont, false)
        setUpTabs(root)
        return root
    }

    private fun setUpTabs(root: View) {
        val viewPager = root.findViewById<ViewPagerNoScroll>(R.id.viewPager)
        val tabLayout = root.findViewById<TabLayout>(R.id.tabLayout)

        activity?.supportFragmentManager?.let {
            viewPager.adapter = SummaryPagerAdapter(requireContext(), it)
            viewPager.offscreenPageLimit = 4
            viewPager.setPagingEnabled(false)
            tabLayout.setupWithViewPager(viewPager)
        }
    }
}
