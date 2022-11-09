package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glass.oceanbs.R

class MainConversationFragment : Fragment() {

    companion object {
        fun newInstance() = MainConversationFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_conversation, container, false)
    }
}
