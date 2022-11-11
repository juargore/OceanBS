package com.glass.oceanbs.fragments.aftermarket

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.hide
import com.glass.oceanbs.extensions.show

class DocumentationFragment : Fragment() {

    private var root: View? = null

    companion object {
        fun newInstance() = DocumentationFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        root = infl.inflate(R.layout.fragment_documentation, cont, false)
        initValidation()
        return root
    }

    private fun initValidation() {
        val parent = root?.findViewById<LinearLayout>(R.id.layParentDocumentation)
        val desarrolloId = MainTracingFragment.desarrolloId

        println("AQUI: Id en Documentation: $desarrolloId")

        if (desarrolloId != null) {
            parent?.show()
            setupViews()
        } else {
            parent?.hide()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        root?.findViewById<Button>(R.id.btnYes)?.setOnClickListener {
            // todo yes
        }
        root?.findViewById<Button>(R.id.btnNo)?.setOnClickListener {
            // todo no
        }
        val progress = 30 // todo: set real percentage from Server
        val progressBar = root?.findViewById<View>(R.id.circularProgress)
        val str = progressBar?.findViewById<TextView>(R.id.progress_tv)
        val pr = progressBar?.findViewById<ProgressBar>(R.id.circular_determinative_pb)
        str?.text = "3 de 6"
        pr?.max = 60
        pr?.progress = progress
    }
}
