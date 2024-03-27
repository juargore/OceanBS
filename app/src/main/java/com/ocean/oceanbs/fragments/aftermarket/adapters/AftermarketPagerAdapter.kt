@file:Suppress("DEPRECATION")

package com.ocean.oceanbs.fragments.aftermarket.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ocean.oceanbs.R
import com.ocean.oceanbs.fragments.aftermarket.MainConversationFragment
import com.ocean.oceanbs.fragments.aftermarket.MainTracingFragment
import java.util.*

class AftermarketPagerAdapter(val context: Context, manager: FragmentManager): FragmentPagerAdapter(manager) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> MainTracingFragment.newInstance()
            else -> MainConversationFragment.newInstance()
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> context.getString(R.string.new_aftermarket_tracing).toUpperCase(Locale.ROOT)
            else -> context.getString(R.string.new_aftermarket_conversation).toUpperCase(Locale.ROOT)
        }
    }
}
