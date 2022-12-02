package com.glass.oceanbs.fragments.aftermarket

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.ADDITIONAL_INFO
import com.glass.oceanbs.Constants.DATA
import com.glass.oceanbs.Constants.GET_DOCUMENTATION_ITEMS
import com.glass.oceanbs.Constants.PHASE
import com.glass.oceanbs.Constants.PROGRESS
import com.glass.oceanbs.Constants.UNITY_ID
import com.glass.oceanbs.Constants.URL_DOCUMENTATION_ITEMS
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.*

class DocumentationFragment : Fragment() {

    private lateinit var layParentDocumentation: LinearLayout

    companion object {
        fun newInstance() = DocumentationFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        val root = infl.inflate(R.layout.fragment_documentation, cont, false)
        initValidation(root)
        return root
    }

    private fun initValidation(root: View) {
        layParentDocumentation = root.findViewById(R.id.layParentDocumentation)
        val desarrolloId = MainTracingFragment.desarrolloId

        if (desarrolloId != null) {
            layParentDocumentation.show()
            getDataFromServer(root)
        } else {
            layParentDocumentation.hide()
        }
    }

    private fun getDataFromServer(root: View) {
        activity?.getDataFromServer(
            webService = GET_DOCUMENTATION_ITEMS,
            url = URL_DOCUMENTATION_ITEMS,
            parent = layParentDocumentation,
            parameters = listOf(Parameter(
                key = UNITY_ID,
                value = MainTracingFragment.desarrolloId
            ))
        ) { jsonRes ->
            var progress = 0
            var phase = 0
            var additionalInfo = ""
            val title = jsonRes.getString(Constants.TITLE)
            val subtitle = jsonRes.getString(Constants.CAPTION)

            val arr = jsonRes.getJSONArray(DATA)
            if (arr.length() > 0) {
                val obj = arr.getJSONObject(0)
                progress = obj.getInt(PROGRESS)
                phase = obj.getInt(PHASE)
                additionalInfo = obj.getString(ADDITIONAL_INFO)
            }

            runOnUiThread {
                setupViews(root, title, subtitle, progress, phase, additionalInfo)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(
        root: View,
        title: String,
        subtitle: String,
        progress: Int,
        phase: Int,
        additionalInfo: String
    ) {
        root.findViewById<Button>(R.id.btnYes)?.setOnClickListener {
            // todo yes
        }
        root.findViewById<Button>(R.id.btnNo)?.setOnClickListener {
            // todo no
        }

        val mTitle = root.findViewById<TextView>(R.id.txtTitle)
        val mSubTitle = root.findViewById<TextView>(R.id.txtSubtitle)
        val etInfo = root.findViewById<EditText>(R.id.etInfo)
        val progressBar = root.findViewById<View>(R.id.circularProgress)
        val str = progressBar?.findViewById<TextView>(R.id.progress_tv)
        val pr = progressBar?.findViewById<ProgressBar>(R.id.circular_determinative_pb)

        mTitle.text = title
        mSubTitle.text = subtitle
        etInfo.setText(additionalInfo)
        str?.text = "Fase $phase"
        pr?.max = 100
        pr?.progress = progress
    }
}
