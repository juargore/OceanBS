@file:Suppress("DEPRECATION")

package com.ocean.oceanbs.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ocean.oceanbs.fragments.CreateSolicitudFragment
import com.ocean.oceanbs.fragments.ListSolicitudesFragment

class ParentPagerAdapter(var context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> ListSolicitudesFragment.newInstance()
            else -> CreateSolicitudFragment.newInstance()
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence {
        return when(position) {
            0 -> "Mis Solicitudes"
            else -> "Nueva Solicitud"
        }
    }
}
