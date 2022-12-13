package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.hide
import com.glass.oceanbs.extensions.show
import com.glass.oceanbs.fragments.aftermarket.MainTracingFragment.Companion.desarrolloId
import com.glass.oceanbs.fragments.aftermarket.adapters.ConversationItemAdapter
import com.glass.oceanbs.models.Chat

class MainConversationFragment : Fragment() {

    private var root: View? = null

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
        val parent = root?.findViewById<ConstraintLayout>(R.id.layParentConversation)
        if (desarrolloId != null) {
            parent?.show()
            setUpRecycler()
        } else {
            parent?.hide()
        }
    }

    private fun setUpRecycler() {
        with (ConversationItemAdapter(getList())) {
            root?.findViewById<RecyclerView>(R.id.rvConversation)?.let {
                it.adapter = this
            }
        }
    }

    // todo: get real data from Server
    private fun getList() = listOf(
        Chat(
            id = 0,
            message = "Hola, buenas tardes, soy el cliente XXX\n" +
                    "Es un test para ver cómo funciona esto. asdlkfjsa dfljsd flkjsd aslkdjflsa kdjflsahfk sahjdfk sahdfkjhsadkjfh sakdj",
            isCustomer = true
        ),
        Chat(
            id = 1,
            message = "Qué tal. En qué puedo ayudarle?",
            isCustomer = false
        ),
        Chat(
            id = 2,
            message = "Estoy a sus órdenes\n" +
                    "sldfj sa ldkfjls adkjflskdjfl ksjflk dsjfgkjsdlfkgj dlskjf gl sdjfglk jsdlfk gjdslkfjglskdjfglksjdlkfjalkdj flasjdflajsdl",
            isCustomer = false
        ),
        Chat(
            id = 3,
            message = "Sólo estaba probando el chat",
            isCustomer = true
        ),
        Chat(
            id = 4,
            message = "No tengo nada qué decir",
            isCustomer = true
        ),
        Chat(
            id = 5,
            message = "Gracias!",
            isCustomer = true
        ),
        Chat(
            id = 6,
            message = "Excelente! Buen día :)",
            isCustomer = false
        )
    )
}
