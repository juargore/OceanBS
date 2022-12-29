package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.GET_CHAT_ITEMS
import com.glass.oceanbs.Constants.MESSAGE
import com.glass.oceanbs.Constants.OWNER_ID
import com.glass.oceanbs.Constants.POST_CHAT_MESSAGE
import com.glass.oceanbs.Constants.URL_CHAT_ITEMS
import com.glass.oceanbs.Constants.getUserId
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.*
import com.glass.oceanbs.fragments.aftermarket.MainTracingFragment.Companion.desarrolloId
import com.glass.oceanbs.fragments.aftermarket.adapters.ConversationItemAdapter
import com.glass.oceanbs.models.Chat
import com.glass.oceanbs.models.MessageType
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout

class MainConversationFragment : Fragment() {

    private var root: View? = null
    private lateinit var parent: ConstraintLayout
    private lateinit var etMessage: EditText
    private lateinit var btnSend: AppCompatImageButton
    private lateinit var swipeRefresh: SwipyRefreshLayout

    companion object {
        fun newInstance() = MainConversationFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        if (root == null) {
            root = infl.inflate(R.layout.fragment_main_conversation, cont, false)
        }
        return root
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) initValidation()
    }

    private fun initValidation() {
        parent = root?.findViewById(R.id.layParentConversation)!!
        etMessage = root?.findViewById(R.id.etMessage)!!
        btnSend = root?.findViewById(R.id.btnSend)!!
        swipeRefresh = root?.findViewById(R.id.swipeRefresh)!!

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            getItemsFromServer()
        }

        if (desarrolloId != null) {
            parent.show()
            setupMessageViews()
            getItemsFromServer()
        } else {
            parent.hide()
        }
    }

    private fun setupMessageViews() {
        btnSend.setOnClickListener {
            val msg = etMessage.text.toString()
            if (msg.isEmpty()) {
                Constants.snackbar(
                    view = parent,
                    context = requireContext(),
                    type = Constants.Types.ERROR,
                    message = getString(R.string.new_chat_empty_message)
                )
                return@setOnClickListener
            }
            sendMessageToServer(msg)
        }
    }

    private fun sendMessageToServer(message: String) {
        activity?.getDataFromServer(
            webService = POST_CHAT_MESSAGE,
            url = URL_CHAT_ITEMS,
            parent = parent,
            parameters = listOf(
                Parameter(OWNER_ID, getUserId(requireContext())),
                Parameter(MESSAGE, message)
            )
        ) {
            // if everything goes well -> show snackbar success
            if (it.getInt("Error") > 0) {
                val response = it.getString("Mensaje")
                runOnUiThread {
                    Constants.snackbar(
                        view = parent,
                        context = requireContext(),
                        type = Constants.Types.ERROR,
                        message = response
                    )
                }
            } else {
                runOnUiThread {
                    // clear input before refreshing list
                    etMessage.setText("")
                    // refresh list to show new message
                    getItemsFromServer()
                }
            }
        }
    }

    private fun getItemsFromServer() {
        activity?.getDataFromServer(
            webService = GET_CHAT_ITEMS,
            url = URL_CHAT_ITEMS,
            parent = parent,
            parameters = listOf(
                Parameter(
                    key = OWNER_ID,
                    value = getUserId(requireContext())
                )
            )
        ) { jsonRes ->
            val arr = jsonRes.getJSONArray(Constants.CONVERSATION)
            val mList = mutableListOf<Chat>()

            for (i in 0 until arr.length()) {
                val j = arr.getJSONObject(i)
                val type = when (j.getInt("TipoMensaje")) {
                    1 -> MessageType.CUSTOMER
                    2 -> MessageType.WORKER
                    else -> MessageType.AUTOMATED
                }
                mList.add(Chat(
                    id = 0,
                    workerName = j.getString("NombreColaborador"),
                    message = j.getString("Mensaje"),
                    date = j.getString("FechaAlta"),
                    hour = j.getString("HoraAlta"),
                    type = type
                ))
            }

            runOnUiThread {
                if (mList.isNotEmpty()) {
                    root?.findViewById<TextView>(R.id.txtNoMessages)?.visibility = View.GONE
                }
                setUpRecycler(mList)
            }
        }
    }

    private fun setUpRecycler(list: List<Chat>) {
        with (ConversationItemAdapter(list)) {
            root?.findViewById<RecyclerView>(R.id.rvConversation)?.let {
                it.adapter = this
                it.smoothScrollToPosition(list.size-1)
            }
        }
    }

    /*private fun getTestList() = listOf(
        Chat(
            id = 0,
            message = "Hola, buenas tardes, soy el cliente XXX\n" +
                    "Es un test para ver cómo funciona esto.",
            type = MessageType.CUSTOMER,
            workerName = null,
            date = "18:07:01",
            hour = "18:07:01"
        ),
        Chat(
            id = 1,
            message = "Qué tal. En qué puedo ayudarle?",
            type = MessageType.WORKER,
            workerName = "Jorge Pérez",
            date = "18:07:01",
            hour = "18:07:01"
        ),
        Chat(
            id = 3,
            message = "Sólo estaba probando el chat",
            type = MessageType.CUSTOMER,
            workerName = null,
            date = "18:07:01",
            hour = "18:07:01"
        ),
        Chat(
            id = 4,
            message = "Mensaje de respuesta automático",
            type = MessageType.AUTOMATED,
            workerName = "Automated response",
            date = "18:07:01",
            hour = "18:07:01"
        ),
        Chat(
            id = 6,
            message = "Excelente! Buen día :)",
            type = MessageType.WORKER,
            workerName = "Olivia García Hernández",
            date = "18:07:01",
            hour = "18:07:01"
        )
    )*/
}
