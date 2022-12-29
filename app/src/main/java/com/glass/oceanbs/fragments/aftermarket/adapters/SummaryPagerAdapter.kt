@file:Suppress("DEPRECATION")

package com.glass.oceanbs.fragments.aftermarket.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.glass.oceanbs.R
import com.glass.oceanbs.fragments.aftermarket.ConstructionFragment
import com.glass.oceanbs.fragments.aftermarket.DocumentationFragment
import com.glass.oceanbs.fragments.aftermarket.HistoryFragment
import com.glass.oceanbs.fragments.aftermarket.SummaryFragment

class SummaryPagerAdapter(
    val context: Context,
    manager: FragmentManager
) : FragmentPagerAdapter(manager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SummaryFragment.newInstance()
            1 -> ConstructionFragment.newInstance()
            2 -> DocumentationFragment.newInstance()
            else -> HistoryFragment.newInstance()
        }
    }

    override fun getCount() = 4

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> context.getString(R.string.new_aftermarket_summary)
            1 -> context.getString(R.string.new_aftermarket_construction)
            2 -> context.getString(R.string.new_aftermarket_documentation)
            else -> context.getString(R.string.new_aftermarket_history)
        }
    }
}
