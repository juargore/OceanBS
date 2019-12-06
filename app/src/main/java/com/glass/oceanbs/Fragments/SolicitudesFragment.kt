package com.glass.oceanbs.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Adapters.SolicitudAdapter
import com.glass.oceanbs.R

class SolicitudesFragment : Fragment() {

    private lateinit var rvSolicitudes: RecyclerView

    companion object{
        fun newInstance(): SolicitudesFragment {
            return SolicitudesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_solicitudes, container, false)

        rvSolicitudes = rootView.findViewById(R.id.rvSolicitudes)

        rvSolicitudes.layoutManager = LinearLayoutManager(context)

        val adapter = SolicitudAdapter(context!!, object : SolicitudAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {

            }
        }, object : SolicitudAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {

            }
        })

        rvSolicitudes.adapter = adapter

        return rootView
    }

}
