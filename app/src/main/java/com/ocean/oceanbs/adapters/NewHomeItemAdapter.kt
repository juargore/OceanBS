package com.ocean.oceanbs.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ocean.oceanbs.Constants.getTipoUsuario
import com.ocean.oceanbs.R
import com.ocean.oceanbs.activities.AftermarketActivity
import com.ocean.oceanbs.activities.MainActivity
import com.ocean.oceanbs.activities.NewMainActivity
import com.ocean.oceanbs.models.ItemNewHome
import com.ocean.oceanbs.models.OWNER

class NewHomeItemAdapter(
    private var elements: List<ItemNewHome>
): RecyclerView.Adapter<NewHomeItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtSubTitle: TextView = itemView.findViewById(R.id.txtSubTitle)
        val viewText: TextView = itemView.findViewById(R.id.viewText)
        val viewColorBottom: View = itemView.findViewById(R.id.viewColorBottom)
        val layParentStart: LinearLayout = itemView.findViewById(R.id.layParentStart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item_new_home, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount() = elements.size

    var onItemClicked: ((Intent?, String?) -> Unit)? = null

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = elements[position]

        with(holder.itemView) {
            holder.txtTitle.text = item.title
            holder.txtSubTitle.text = item.subtitle
            holder.viewColorBottom.setBackgroundColor(Color.parseColor(item.hexColor))
            holder.viewText.setBackgroundColor(Color.parseColor(item.hexColor))

            if (getTipoUsuario(context) == OWNER) {
                // Propietario
                if (!item.enabled) {
                    holder.layParentStart.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLighterGray))
                }
                if (item.enabled && item.openScreen && NewMainActivity.hasPendingNotifications) {
                    holder.viewText.text = "*"
                }
            } else {
                // Colaborador
                if (item.title.contains("SEGUIMIENTO", true)) {
                    // disable this cardview
                    holder.layParentStart.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLighterGray))
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
