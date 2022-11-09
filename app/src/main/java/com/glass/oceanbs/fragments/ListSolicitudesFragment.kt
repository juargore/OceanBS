@file:Suppress("DEPRECATION", "SpellCheckingInspection")
package com.glass.oceanbs.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.mustRefreshSolicitudes
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.EditSolicitudActivity
import com.glass.oceanbs.activities.ListIncidenciasActivity
import com.glass.oceanbs.adapters.ShortSolicitudAdapter
import com.glass.oceanbs.database.TableUser
import com.glass.oceanbs.extensions.alert
import com.glass.oceanbs.extensions.hide
import com.glass.oceanbs.models.ShortSolicitud
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ListSolicitudesFragment : Fragment() {

    private lateinit var progress : AlertDialog
    private lateinit var laySuccessFS: LinearLayout
    private lateinit var layFailFS: LinearLayout
    private lateinit var layParentS: LinearLayout
    private lateinit var rvSolicitudes: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var cardFecha: CardView
    private lateinit var cardTodas: CardView
    private lateinit var txtFecha: TextView

    private var listSolicitudes: ArrayList<ShortSolicitud> = ArrayList()
    private var userId = ""
    private var today = ""

    companion object{
        fun newInstance(): ListSolicitudesFragment {
            return ListSolicitudesFragment()
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_list_solicitudes, container, false)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initComponents(rootView)

        return rootView
    }

    @SuppressLint("InflateParams", "NewApi")
    private fun initComponents(view: View){
        layParentS = view.findViewById(R.id.layParentS)
        laySuccessFS = view.findViewById(R.id.laySuccessFS)
        layFailFS = view.findViewById(R.id.layFailFS)
        rvSolicitudes = view.findViewById(R.id.rvSolicitudes)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        cardFecha = view.findViewById(R.id.cardFecha)
        cardTodas = view.findViewById(R.id.cardTodas)
        txtFecha = view.findViewById(R.id.txtFecha)


        val cldr = Calendar.getInstance()
        val day = cldr.get(Calendar.DAY_OF_MONTH)
        val month = cldr.get(Calendar.MONTH)
        val year = cldr.get(Calendar.YEAR)

        val realMonth = month+1
        var strMonth = realMonth.toString()
        if(realMonth < 10)
            strMonth = "0$realMonth"

        val realDay = day
        var strDay = day.toString()
        if(realDay < 10)
            strDay = "0$realDay"

        today = "$year-$strMonth-$strDay"
        txtFecha.text = today

        cardFecha.setOnClickListener { showDatePicker(txtFecha) }
        cardTodas.setOnClickListener { getSolicitudesByDate("") }

        // set up progress dialg
        val builder = AlertDialog.Builder(requireContext(), R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        builder.setView(dialogView)
        progress = builder.create()
        progress.setCancelable(false)

        swipeRefresh.setOnRefreshListener {
            if(Constants.internetConnected(requireActivity())){
                getSolicitudesByDate(today)
            } else{
                Constants.showPopUpNoInternet(requireActivity())
            }
            swipeRefresh.isRefreshing = false
        }

        // after init -> get solicitudes if network available
        if(Constants.internetConnected(requireActivity())){
            getSolicitudesByDate(today)
        } else{
            Constants.showPopUpNoInternet(requireActivity())
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDatePicker(txtView : TextView){
        val cldr = Calendar.getInstance()
        val day1  = cldr.get(Calendar.DAY_OF_MONTH)
        val month1    = cldr.get(Calendar.MONTH)
        val year1 = cldr.get(Calendar.YEAR)

        val picker = DatePickerDialog(requireContext(), R.style.AppTheme_CustomDatePickerAccent, { _, year, month, day ->

                val realDay = day
                var strDay = realDay.toString()

                val realMonth = month+1
                var strMonth = realMonth.toString()
                if(realMonth < 10)
                    strMonth = "0$realMonth"

                if(realDay < 10)
                    strDay = "0$realDay"

                txtView.text = "$year-$strMonth-$strDay"
                getSolicitudesByDate("$year-$strMonth-$strDay")

            }, year1, month1, day1)
        picker.show()
    }

    private fun getSolicitudesByDate(fecha: String){
        Log.e("--", fecha)
        progress.show()
        userId = Constants.getUserId(requireContext())

        val user = TableUser(requireContext()).getCurrentUserById(Constants.getUserId(requireContext()), Constants.getTipoUsuario(requireContext()))

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaSolicitudesAGIdUsuario")
            .add("TipoUsuario", user.tipoUsuario.toString())
            .add("IdPropietario", user.idPropietario)
            .add("IdColaborador", user.idColaborador)
            .add("FechaAlta", fecha)
            .build()

        val request = Request.Builder().url(Constants.URL_SOLICITUDES).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body!!.string())
                        Log.e("RES", jsonRes.toString())

                        if(jsonRes.getInt("Error") > 0)
                            snackbar(requireContext(), layParentS, jsonRes.getString("Mensaje"), Constants.Types.ERROR)
                        else{

                            //If fecha is ALL -> reset date to today
                            if(fecha == "")
                                txtFecha.text = today

                            // create solicitud object and iterate json array
                            val arraySolicitud = jsonRes.getJSONArray("Datos")
                            listSolicitudes.clear()

                            for(i in 0 until arraySolicitud.length()){

                                val jsonObj : JSONObject = arraySolicitud.getJSONObject(i)
                                listSolicitudes.add( ShortSolicitud (
                                        jsonObj.getString("Id"),
                                        jsonObj.getString("FechaAlta"),
                                        jsonObj.getString("NombrePR"),
                                        jsonObj.getString("NombrePropietario"),
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

                            Constants.updateRefreshSolicitudes(requireContext(), false)
                        }; progress.dismiss()

                    } catch (e: Error){
                        progress.dismiss()
                        snackbar(requireContext(), layParentS, e.message.toString(), Constants.Types.ERROR)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    progress.dismiss()
                    snackbar(requireContext(), layParentS, e.message.toString(), Constants.Types.ERROR)
                }
            }
        })
    }

    private fun setUpRecyclerView(){
        rvSolicitudes.layoutManager = LinearLayoutManager(context)

        //val sortedList = listSolicitudes.sortedByDescending { it.FechaAlta }

        val adapter = ShortSolicitudAdapter(requireContext(), listSolicitudes, object : ShortSolicitudAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {

                // on click -> open Incidencias screen
                val intent = Intent(requireActivity(), ListIncidenciasActivity::class.java)
                intent.putExtra("solicitudId", listSolicitudes[pos].Id)
                intent.putExtra("desarrollo", listSolicitudes[pos].NombreDesarrollo)
                intent.putExtra("persona", listSolicitudes[pos].NombrePropietario)
                intent.putExtra("codigoUnidad", listSolicitudes[pos].CodigoUnidad)
                startActivity(intent)
            }
        }, object : ShortSolicitudAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {

                // on long click -> show pop up wih options
                showPopOptions(requireContext(), listSolicitudes[pos].Id, listSolicitudes[pos].NombreDesarrollo, listSolicitudes[pos].NombrePropietario, listSolicitudes[pos].CodigoUnidad)
            }
        })

        rvSolicitudes.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun showPopOptions(context: Context, solicitudId: String, desarrollo: String, persona: String, codigoUnidad: String){
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
            val intent = Intent(activity, EditSolicitudActivity::class.java)
            intent.putExtra("solicitudId", solicitudId)
            intent.putExtra("desarrollo", desarrollo)
            intent.putExtra("persona", persona)
            intent.putExtra("codigoUnidad", codigoUnidad)
            startActivity(intent)
        }

        dialog.show()
    }

    private fun showDeleteDialog(solicitudId: String){
        activity?.alert {
            title.hide()
            message.text = getString(R.string.msg_confirm_deletion)
            acceptClickListener {
                deleteSolicludByServer(solicitudId)
            }
            cancelClickListener { }
        }?.show()
        /*alert(resources.getString(R.string.msg_confirm_deletion),
            "")
        {
            positiveButton(resources.getString(R.string.accept)) {
                deleteSolicludByServer(solicitudId)
            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }*/
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
                activity?.runOnUiThread {
                    progress.dismiss()
                    snackbar(requireContext(), layParentS, "No es posible eliminar esta solicitud. Intente más tarde", Constants.Types.ERROR) }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    try{
                        val jsonRes = JSONObject(response.body!!.string())
                        if(jsonRes.getInt("Error") == 0){

                            // successfully deleted on Server -> refresh fragment
                            progress.dismiss()
                            snackbar(requireContext(), layParentS, jsonRes.getString("Mensaje"), Constants.Types.SUCCESS)
                            listSolicitudes.clear()
                            getSolicitudesByDate(today)
                        }
                    } catch (e: java.lang.Error){
                        progress.dismiss()
                        snackbar(requireContext(), layParentS, "No es posible eliminar esta solicitud. Intente más tarde", Constants.Types.ERROR)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if(mustRefreshSolicitudes(requireContext())){
            listSolicitudes.clear()
            getSolicitudesByDate(today)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(view != null){
            if(isVisibleToUser){
                if(mustRefreshSolicitudes(requireContext())){
                    listSolicitudes.clear()
                    getSolicitudesByDate(today)
                }
            }
        }
    }
}
