package com.glass.oceanbs.fragments.aftermarket

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.glass.oceanbs.R

class DocumentationFragment : Fragment() {

    private var root: View? = null

    companion object {
        fun newInstance() = DocumentationFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        if (root == null) {
            root = infl.inflate(R.layout.fragment_documentation, cont, false)
            setupViews()
        }
        return root
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        root?.findViewById<Button>(R.id.btnYes)?.setOnClickListener {
            // todo yes
        }
        root?.findViewById<Button>(R.id.btnNo)?.setOnClickListener {
            // todo no
        }
        val progress = 32 // todo: set real percentage from Server
        val progressBar = root?.findViewById<View>(R.id.circularProgress)
        val str = progressBar?.findViewById<TextView>(R.id.progress_tv)
        val pr = progressBar?.findViewById<ProgressBar>(R.id.circular_determinative_pb)
        str?.text = "$progress%"
        pr?.progress = progress
    }
}
