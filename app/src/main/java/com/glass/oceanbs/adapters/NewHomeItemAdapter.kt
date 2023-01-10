package com.glass.oceanbs.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Constants.getTipoUsuario
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.AftermarketActivity
import com.glass.oceanbs.activities.MainActivity
import com.glass.oceanbs.activities.NewMainActivity
import com.glass.oceanbs.models.ItemNewHome
import com.glass.oceanbs.models.OWNER
import kotlinx.android.synthetic.main.card_item_new_home.view.*

class NewHomeItemAdapter(
    private var elements: List<ItemNewHome>
): RecyclerView.Adapter<NewHomeItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_new_home, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = elements.size

    var onItemClicked: ((Intent?, String?) -> Unit)? = null

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = elements[position]

        with(holder.itemView) {
            txtTitle.text = item.title
            txtSubTitle.text = item.subtitle
            viewColorBottom.setBackgroundColor(Color.parseColor(item.hexColor))
            viewText.setBackgroundColor(Color.parseColor(item.hexColor))

            if (getTipoUsuario(context) == OWNER) {
                // Propietario
                if (!item.enabled) {
                    layParentStart.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLighterGray))
                }
                if (item.enabled && item.openScreen && NewMainActivity.hasPendingNotifications) {
                    viewText.text = "*"
                }
            } else {
                // Colaborador
                if (item.title.contains("SEGUIMIENTO", true)) {
                    // disable this cardview
                    layParentStart.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLighterGray))
                    item.enabled = false
                }
                if (item.title.contains("POST", true)) {
                    // enable this cardview
                    item.enabled = true
                }
            }

            var intent: Intent? = null
            var url: String? = null

            when (position) {
                0 -> intent = Intent(context, AftermarketActivity::class.java)
                1 -> intent = Intent(context, MainActivity::class.java)
                else -> url = item.url
            }
            setOnClickListener {
                if (item.enabled) {
                    onItemClicked?.invoke(intent, url)
                } else {
                    if (getTipoUsuario(context) == OWNER) {
                        Toast.makeText(context, item.messageDisabled, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
