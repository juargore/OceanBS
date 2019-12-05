package com.glass.oceanbs.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glass.oceanbs.R

class SolicitudesFragment : Fragment() {

    companion object{
        fun newInstance(): SolicitudesFragment {
            return SolicitudesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_solicitudes, container, false)

        return rootView
    }

}
