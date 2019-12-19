@file:Suppress("SpellCheckingInspection", "DEPRECATION")

package com.glass.oceanbs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.snackbar
import com.glass.oceanbs.R
import com.glass.oceanbs.adapters.IncidenciaAdapter
import com.glass.oceanbs.models.ShortIncidencia
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.io.IOException

class IncidenciasActivity : AppCompatActivity() {

    private lateinit var txtTitleDesarrolloI: TextView
    private lateinit var txtSubTitleDesarrolloI: TextView
    private lateinit var imgBackIncidencias: ImageView
    private lateinit var layParentI: RelativeLayout
    private lateinit var laySuccessI: RelativeLayout
    private lateinit var layFailI: RelativeLayout
    private lateinit var progress : AlertDialog

    private lateinit var rvIncidencias: RecyclerView
    private lateinit var fabNewIncidencia: FloatingActionButton
    private var listIncidencias: ArrayList<ShortIncidencia> = ArrayList()

    private var userId = ""
    private var solicitudId = ""
    private var desarrollo = ""
    private var persona = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incidencias)

        supportActionBar?.hide()

        val args = intent.extras
        solicitudId = args!!.getString("solicitudId").toString()
        desarrollo = args.getString("desarrollo").toString()
        persona = args.getString("persona").toString()

        initComponents()
    }

    @SuppressLint("InflateParams")
    private fun initComponents(){
        txtTitleDesarrolloI = findViewById(R.id.txtTitleDesarrolloI)
        txtSubTitleDesarrolloI = findViewById(R.id.txtSubTitleDesarrolloI)
        imgBackIncidencias = findViewById(R.id.imgBackIncidencias)
        layParentI = findViewById(R.id.layParentI)
        laySuccessI = findViewById(R.id.laySuccessI)
        layFailI = findViewById(R.id.layFailI)

        rvIncidencias = findViewById(R.id.rvIncidencias)
        fabNewIncidencia = findViewById(R.id.fabNewIncidencia)

        // set up progress dialg
        val builder = AlertDialog.Builder(this, R.style.HalfDialogTheme)
        val inflat = this.layoutInflater
        val dialogView = inflat.inflate(R.layout.progress, null)

        builder.setView(dialogView)
        progress = builder.create()
        progress.setCancelable(false)

        txtTitleDesarrolloI.text = desarrollo
        txtSubTitleDesarrolloI.text = persona

        fabNewIncidencia.setOnClickListener {
            val intent = Intent(applicationContext, NuevaIncidenciaActivity::class.java)
            intent.putExtra("solicitudId", solicitudId)
            startActivity(intent)
        }

        imgBackIncidencias.setOnClickListener { this.finish() }

        getIncidencias()
    }

    private fun getIncidencias(){
        progress.show()
        userId = Constants.getUserId(this)

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","ConsultaIncidenciasIdSolicitudAGApp")
            .add("IdSolicitudAG", solicitudId)
            .build()

        val request = Request.Builder().url(Constants.URL_INCIDENCIAS).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(response.body()!!.string())

                        if(jsonRes.getInt("Error") > 0)
                            snackbar(applicationContext, layParentI, jsonRes.getString("Mensaje"))
                        else{

                            // create incidencia object and iterate json array
                            val arrayIncidencias = jsonRes.getJSONArray("Datos")

                            for(i in 0 until arrayIncidencias.length()){
                                val j : JSONObject = arrayIncidencias.getJSONObject(i)

                                listIncidencias.add(ShortIncidencia(
                                        j.getString("Id"),
                                        j.getString("FechaAlta"),
                                        j.getString("Clasificacion"),
                                        j.getString("Vigencia"),
                                        j.getString("Status")))
                            }

                            // show / hide layout according the number of rows
                            if(listIncidencias.size > 0){
                                laySuccessI.visibility = View.VISIBLE
                                layFailI.visibility = View.GONE
                                setUpRecyclerView()
                            } else{
                                laySuccessI.visibility = View.GONE
                                layFailI.visibility = View.VISIBLE
                            }
                        }

                        Constants.updateRefreshIncidencias(applicationContext, false)
                        progress.dismiss()

                    } catch (e: Error){
                        progress.dismiss()
                        snackbar(applicationContext, layParentI, e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    snackbar(applicationContext, layParentI, e.message.toString())
                    progress.dismiss()
                }
            }
        })
    }

    private fun setUpRecyclerView(){
        rvIncidencias.layoutManager = LinearLayoutManager(this)
        val adapter = IncidenciaAdapter(this, listIncidencias, object : IncidenciaAdapter.InterfaceOnClick{
            override fun onItemClick(pos: Int) {
                val intent = Intent(applicationContext, EditarIncidenciaActivity::class.java)
                intent.putExtra("incidenciaId", listIncidencias[pos].Id)
                intent.putExtra("solicitudId", solicitudId)
                intent.putExtra("persona", persona)
                intent.putExtra("desarrollo", desarrollo)
                startActivity(intent)
            }
        }, object : IncidenciaAdapter.InterfaceOnLongClick{
            override fun onItemLongClick(pos: Int) {
                showDeleteDialog(listIncidencias[pos].Id)
            }
        })

        rvIncidencias.adapter = adapter
    }

    private fun showDeleteDialog(incidenciaId: String){
        alert(resources.getString(R.string.msg_confirm_deletion),
            "Eliminar Incidencia")
        {
            positiveButton(resources.getString(R.string.accept)) {
                deleteIncidenciaByServer(incidenciaId)
            }
            negativeButton(resources.getString(R.string.cancel)){}
        }.show().apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.let { it.textColor = resources.getColor(R.color.colorBlack) }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.let { it.textColor = resources.getColor(R.color.colorAccent) }
        }
    }

    private fun deleteIncidenciaByServer(incidenciaId: String){
        progress.show()

        val client = OkHttpClient()
        val builder = FormBody.Builder()
            .add("WebService","EliminaIncidencia")
            .add("Id", incidenciaId)
            .build()

        val request = Request.Builder().url(Constants.URL_INCIDENCIAS).post(builder).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progress.dismiss()
                    val msg = "No es posible eliminar esta solicitud. Intente más tarde"
                    snackbar(applicationContext, layParentI, msg)
                }
            }
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread{
                    try{
                        val jsonRes = JSONObject(response.body()!!.string())
                        if(jsonRes.getInt("Error") == 0){

                            // successfully deleted on Server -> refresh list
                            listIncidencias.clear()
                            progress.dismiss()

                            snackbar(applicationContext, layParentI, jsonRes.getString("Mensaje"))
                            getIncidencias()
                        } else{
                            snackbar(applicationContext, layParentI, jsonRes.getString("Mensaje"))
                            progress.dismiss()
                        }
                    } catch (e: java.lang.Error){
                        progress.dismiss()
                        snackbar(applicationContext, layParentI,
                            "No es posible eliminar esta incidencia. Intente más tarde")
                    }
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        if(Constants.mustRefreshIncidencias(this)){
            listIncidencias.clear()
            getIncidencias()
        }
    }

}
