package com.glass.oceanbs.fragments.aftermarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.ADDITIONAL_INFO
import com.glass.oceanbs.Constants.GET_DOCUMENTATION_ITEMS
import com.glass.oceanbs.Constants.NOTICE
import com.glass.oceanbs.Constants.OWNER_ID
import com.glass.oceanbs.Constants.PHASE
import com.glass.oceanbs.Constants.POST_ANSWER_SATISFACTION
import com.glass.oceanbs.Constants.RESPONSE
import com.glass.oceanbs.Constants.UNITY_ID
import com.glass.oceanbs.Constants.URL_ANSWER_SATISFACTION
import com.glass.oceanbs.Constants.URL_DOCUMENTATION_ITEMS
import com.glass.oceanbs.Constants.getUserId
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.*
import com.glass.oceanbs.fragments.aftermarket.MainTracingFragment.Companion.desarrolloId

class DocumentationFragment : Fragment() {

    private lateinit var root: View
    private lateinit var layParentDocumentation: LinearLayout

    companion object {
        fun newInstance() = DocumentationFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View {
        root = infl.inflate(R.layout.fragment_documentation, cont, false)
        layParentDocumentation = root.findViewById(R.id.layParentDocumentation)
        return root
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (desarrolloId != null) {
                layParentDocumentation.show()
                val item = MainTracingFragment.itemDocumentation
                if (item != null) {
                    setupViews(
                        title = item.title,
                        subtitle = item.subtitle,
                        phase = item.phase,
                        additionalInfo = item.additionalInfo
                    )
                } else {
                    getDataFromServer()
                }
            } else {
                layParentDocumentation.hide()
            }
        }
    }

    private fun getDataFromServer() {
        activity?.getDataFromServer(
            showLoader = false,
            webService = GET_DOCUMENTATION_ITEMS,
            url = URL_DOCUMENTATION_ITEMS,
            parent = layParentDocumentation,
            parameters = listOf(Parameter(
                key = UNITY_ID,
                value = desarrolloId
            ))
        ) { jsonRes ->
            var phase = 0
            var additionalInfo = ""
            val title = jsonRes.getString(Constants.TITLE)
            val subtitle = jsonRes.getString(Constants.CAPTION)
            val arr = jsonRes.getJSONArray(NOTICE)

            if (arr.length() > 0) {
                val obj = arr.getJSONObject(0)
                phase = obj.getInt(PHASE)
                additionalInfo = obj.getString(ADDITIONAL_INFO)
            }

            runOnUiThread {
                setupViews(
                    title = title,
                    subtitle = subtitle,
                    phase = phase,
                    additionalInfo = additionalInfo
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(
        title: String,
        subtitle: String,
        phase: Int,
        additionalInfo: String
    ) {
        root.findViewById<Button>(R.id.btnYes)?.setOnClickListener {
            sendReponseToServer(1) // yes
        }
        root.findViewById<Button>(R.id.btnNo)?.setOnClickListener {
            sendReponseToServer(0) // no
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
        pr?.progress = (phase * 20)
    }

    private fun sendReponseToServer(response: Int) {
        activity?.getDataFromServer(
            webService = POST_ANSWER_SATISFACTION,
            url = URL_ANSWER_SATISFACTION,
            parent = layParentDocumentation,
            parameters = listOf(
                Parameter(OWNER_ID, getUserId(requireContext())),
                Parameter(RESPONSE, response.toString())
            )
        ) {
            if (it.getInt("Error") == 0) {
                //val res = it.getString("Mensaje")
                runOnUiThread {
                    if (response == 1) {
                        Constants.snackbar(
                            view = layParentDocumentation,
                            context = requireContext(),
                            type = Constants.Types.SUCCESS,
                            message = "Muchas gracias por su respuesta.\nEstamos para servirle."
                        )
                    } else {
                        (parentFragment as MainTracingFragment).intermediateToUpdateConversationChat()
                    }
                    /*Constants.snackbar(
                        view = layParentDocumentation,
                        context = requireContext(),
                        type = Constants.Types.SUCCESS,
                        message = res
                    )*/
                }
            }
        }
    }
}
