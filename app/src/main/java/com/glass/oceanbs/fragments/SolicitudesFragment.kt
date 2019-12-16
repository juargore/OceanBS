@file:Suppress("DEPRECATION", "SpellCheckingInspection")
package com.glass.oceanbs.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.mustRefreshSolicitudes
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.EditarSolicitudActivity
import com.glass.oceanbs.activities.IncidenciasActivity
import com.glass.oceanbs.adapters.ShortSolicitudAdapter
import com.glass.oceanbs.models.ShortSolicitud
import okhttp3.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.IOException

class SolicitudesFragment : Fragment() {

    private lateinit var progress : AlertDialog
    private lateinit var laySuccessFS: LinearLayout
    private lateinit var layFailFS: LinearLayout
    private lateinit var layParentS: LinearLayout
    private lateinit var rvSolicitudes: RecyclerView

    private var listSolicitudes: ArrayList<ShortSolicitud> = ArrayList()
    private var userId = ""

    companion object{
        fun newInstance(): SolicitudesFragment {
            return SolicitudesFragment()
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_solicitudes, container, false)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initComponents(rootView)

        return rootView
    }

    @SuppressLint("InflateParams")
    private fun initComponents(view: View){
        layParentS = view.findViewById(R.id.layParentS)
        laySuccessFS = view.findViewById(R.id.laySuccessFS)
        layFailFS = view.findViewById(R.id.layFailFS)
        rvSolicitudes = view.findViewById(R.id.rvSolicitudes)

        // set up progress dialg
        val builder = AlertDialog.Builder(context!!, R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        builder.setView(dialogView)
        progress = builder.create()
        progress.setCancelable(false)

        // after init -> get solicitudes
        getSolicitudes()
    }

    private fun getSolicitudes(){
        progress.show()
        userId = Constants.getUserId(context!!)

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaSolicitudesAGIdUsuario")
            .add("IdUsuario", userId)
            .add("EsColaborador", "1")
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body()!!.string())

                        if(jsonRes.getInt("Error") > 0)
                            snackbar(context!!, layParentS, jsonRes.getString("Mensaje"))
                        else{

                            // create solicitud object and iterate json array
                            val arraySolicitud = jsonRes.getJSONArray("Datos")

                            for(i in 0 until arraySolicitud.length()){

                                val jsonObj : JSONObject = arraySolicitud.getJSONObject(i)
                                listSolicitudes.add( ShortSolicitud (
                                        jsonObj.getString("Id"),
                                        jsonObj.getString("FechaAlta"),
                                        jsonObj.getString("NombrePR"),
                                        jsonObj.getString("NombreDesarrollo"),
                                        jsonObj.getString("CodigoUnidad"),
                                        jsonObj.getString("NombreUnidad")))
                            }

                            // show / hide layout according the number of rows
                            if(listSolicitudes.size > 0){
                                laySuccessFS.visibility = View.VISIBLE
                                layFailFS.visibility = View.GONE

                                setUpRecyclerView()
                            } else{
                                laySuccessFS.visibility = View.GONE
                                layFailFS.visibility = View.VISIBLE
                            }

                            Constants.updateRefreshSolicitudes(context!!, false)
                        }; progress.dismiss()

                    } catch (e: Error){
                        progress.dismiss()
                        snackbar(context!!, layParentS, e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progress.dismiss()
                    snackbar(context!!, layParentS, e.message.toString())
                }
            }
        })
    }

    private fun setUpRecyclerView(){
        rvSolicitudes.layoutManager = LinearLayoutManager(context)

        val adapter = ShortSolicitudAdapter(context!!, listSolicitudes, object : ShortSolicitudAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {

                // on click -> open Incidencias screen
                val intent = Intent(activity!!, IncidenciasActivity::class.java)
                intent.putExtra("solicitudId", listSolicitudes[pos].Id)
                intent.putExtra("desarrollo", listSolicitudes[pos].NombreDesarrollo)
                intent.putExtra("persona", listSolicitudes[pos].NombrePR)
                startActivity(intent)
            }
        }, object : ShortSolicitudAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {

                // on long click -> show pop up wih options
                showPopOptions(context!!, listSolicitudes[pos].Id, listSolicitudes[pos].NombreDesarrollo, listSolicitudes[pos].NombrePR)
            }
        })

        rvSolicitudes.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun showPopOptions(context: Context, solicitudId: String, desarrollo: String, persona: String){
        val dialog = Dialog(context, R.style.FullDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.pop_options)

        val title = dialog.findViewById<TextView>(R.id.txtOpTitle)
        val delete = dialog.findViewById<TextView>(R.id.txtOpDel)
        val edit = dialog.findViewById<TextView>(R.id.txtOpEdit)
        val cancel = dialog.findViewById<TextView>(R.id.txtOpCan)

        title.text = "Opciones de Solicitud"
        delete.text = "Eliminar Solicitud"
        edit.text = "Editar Solicitud"

        cancel.setOnClickListener { dialog.dismiss() }
        delete.setOnClickListener { showDeleteDialog(solicitudId); dialog.dismiss() }
        edit.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(activity, EditarSolicitudActivity::class.java)
            intent.putExtra("solicitudId", solicitudId)
            intent.putExtra("desarrollo", desarrollo)
            intent.putExtra("persona", persona)
            startActivity(intent)
        }

        dialog.show()
    }

    private fun showDeleteDialog(solicitudId: String){
        alert(resources.getString(R.string.msg_confirm_deletion),
            "Eliminar Solicitud")
        {
            positiveButton(resources.getString(R.string.accept)) {
                deleteSolicludByServer(solicitudId)
            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }
    }

    private fun deleteSolicludByServer(solicitudId: String){
        progress.show()

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","EliminaSolicitudAG")
            .add("Id", solicitudId)
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progress.dismiss()
                    snackbar(context!!, layParentS, "No es posible eliminar esta solicitud. Intente más tarde") }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try{
                        val jsonRes = JSONObject(response.body()!!.string())
                        if(jsonRes.getInt("Error") == 0){

                            // successfully deleted on Server -> refresh fragment
                            progress.dismiss()
                            snackbar(context!!, layParentS, jsonRes.getString("Mensaje"))
                            listSolicitudes.clear()
                            getSolicitudes()
                        }
                    } catch (e: java.lang.Error){
                        progress.dismiss()
                        snackbar(context!!, layParentS, "No es posible eliminar esta solicitud. Intente más tarde")
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if(mustRefreshSolicitudes(context!!)){
            listSolicitudes.clear()
            getSolicitudes()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(view != null){
            if(isVisibleToUser){
                if(mustRefreshSolicitudes(context!!)){
                    listSolicitudes.clear()
                    getSolicitudes()
                }
            }
        }

    }
}
