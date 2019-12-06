package com.glass.oceanbs.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glass.oceanbs.R


class NewSolicitudFragment : Fragment() {

    companion object{
        fun newInstance(): NewSolicitudFragment {
            return NewSolicitudFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_new_solicitud, container, false)
    }

}
