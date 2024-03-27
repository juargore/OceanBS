package com.ocean.oceanbs.fragments.aftermarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.ocean.oceanbs.Constants
import com.ocean.oceanbs.Constants.ADDITIONAL_INFO
import com.ocean.oceanbs.Constants.GET_DOCUMENTATION_ITEMS
import com.ocean.oceanbs.Constants.NOTICE
import com.ocean.oceanbs.Constants.OWNER_ID
import com.ocean.oceanbs.Constants.PHASE
import com.ocean.oceanbs.Constants.POST_ANSWER_SATISFACTION
import com.ocean.oceanbs.Constants.RESPONSE
import com.ocean.oceanbs.Constants.UNITY_ID
import com.ocean.oceanbs.Constants.URL_ANSWER_SATISFACTION
import com.ocean.oceanbs.Constants.URL_DOCUMENTATION_ITEMS
import com.ocean.oceanbs.Constants.getUserId
import com.ocean.oceanbs.R
import com.ocean.oceanbs.extensions.*
import com.ocean.oceanbs.fragments.aftermarket.MainTracingFragment.Companion.desarrolloId

class DocumentationFragment : Fragment() {

    private lateinit var root: View
    private lateinit var layParentDocumentation: LinearLayout
    private lateinit var select: TextView
    private lateinit var scroll: NestedScrollView

    companion object {
        fun newInstance() = DocumentationFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View {
        root = infl.inflate(R.layout.fragment_documentation, cont, false)
        layParentDocumentation = root.findViewById(R.id.layParentDocumentation)
        select = root.findViewById(R.id.txtSelectD)
        scroll = root.findViewById(R.id.scrollD)
        return root
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (desarrolloId != null) {
                scroll.show()
                select.hide()
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
                scroll.hide()
                select.show()
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
                runOnUiThread {
                    if (response == 1) {
                        Constants.snackbar(
                            view = layParentDocumentation,
                            context = requireContext(),
                            type = Constants.Types.SUCCESS,
                            message = "Muchas gracias por su respuesta.\nEstamos para servirle."
                        )
                    } else {
                        val toast = Toast.makeText(requireContext(), "Espere un momento...", Toast.LENGTH_SHORT)
                        toast.show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            toast.cancel()
                            (parentFragment as MainTracingFragment).intermediateToUpdateConversationChat()
                        }, 2000)
                    }
                }
            }
        }
    }
}
