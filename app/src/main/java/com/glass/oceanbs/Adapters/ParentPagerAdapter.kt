package com.glass.oceanbs.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.glass.oceanbs.Fragments.NewSolicitudFragment
import com.glass.oceanbs.Fragments.SolicitudesFragment

class ParentPagerAdapter(var context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager) {

    override fun getItem(position: Int): Fragment {
        return when(position){
            0-> SolicitudesFragment.newInstance()
            else -> NewSolicitudFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0-> "Mis Solicitudes"
            else -> "Nueva Solicitud"
        }
    }

}