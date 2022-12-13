@file:Suppress("DEPRECATION")

package com.glass.oceanbs.fragments.aftermarket.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.glass.oceanbs.R
import com.glass.oceanbs.fragments.aftermarket.MainConversationFragment
import com.glass.oceanbs.fragments.aftermarket.MainTracingFragment
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
