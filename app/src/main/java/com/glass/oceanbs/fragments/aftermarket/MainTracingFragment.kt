package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.glass.oceanbs.Constants.GET_ALL_DESARROLLOS
import com.glass.oceanbs.Constants.GET_DESARROLLOS_BY_OWNER_ID
import com.glass.oceanbs.Constants.OWNER_ID
import com.glass.oceanbs.Constants.URL_SUCURSALES
import com.glass.oceanbs.Constants.WEB_SERVICE
import com.glass.oceanbs.Constants.getTipoUsuario
import com.glass.oceanbs.Constants.getUserId
import com.glass.oceanbs.Constants.getUserName
import com.glass.oceanbs.R
import com.glass.oceanbs.extensions.getUserTypeStr
import com.glass.oceanbs.fragments.aftermarket.adapters.SummaryPagerAdapter
import com.glass.oceanbs.models.GenericObj
import com.glass.oceanbs.models.OWNER
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_main_tracing.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MainTracingFragment : Fragment() {

    private var rootView: View? = null
    private val desarrollosList = mutableListOf<GenericObj>()

    companion object {
        fun newInstance() = MainTracingFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        if (rootView == null) {
            rootView = infl.inflate(R.layout.fragment_main_tracing, cont, false)
            setupViews()
            setUpTabs()
            getListDesarrollos()
        }
        return rootView
    }

    private fun setupViews() {
        val txtUserName = rootView?.findViewById<TextView>(R.id.txtUserName)
        val txtOwner = rootView?.findViewById<TextView>(R.id.txtOwner)
        txtUserName?.text = getUserName(requireContext()).uppercase(Locale.getDefault())
        txtOwner?.text = getUserTypeStr(requireContext())
    }

    private fun setUpTabs() {
        val viewPager = rootView?.findViewById<ViewPager>(R.id.viewPager)
        val tabLayout = rootView?.findViewById<TabLayout>(R.id.tabLayout)
        viewPager?.adapter = SummaryPagerAdapter(requireContext(), childFragmentManager)
        viewPager?.offscreenPageLimit = 4
        tabLayout?.setupWithViewPager(viewPager)
    }

    private fun getListDesarrollos() {
        val builder = if (getTipoUsuario(requireContext()) == OWNER) {
            FormBody.Builder()
                .add(WEB_SERVICE, GET_DESARROLLOS_BY_OWNER_ID)
                .add(OWNER_ID, getUserId(requireContext())).build()
        } else {
            FormBody.Builder().add(WEB_SERVICE, GET_ALL_DESARROLLOS).build()
        }
        val request = Request.Builder().url(URL_SUCURSALES).post(builder).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonRes = JSONObject(response.body!!.string())
                if (jsonRes.getInt("Error") == 0) {
                    val arrayDesarrollos = jsonRes.getJSONArray("Datos")
                    desarrollosList.clear()

                    for (i in 0 until arrayDesarrollos.length()) {
                        val jsonObj = arrayDesarrollos.getJSONObject(i)

                        desarrollosList.add( GenericObj (
                            Id     = jsonObj.getString("Id"),
                            Codigo = jsonObj.getString("Codigo"),
                            Nombre = jsonObj.getString("Nombre"),
                            extra1 = "${jsonObj.getString("Calle")} ${jsonObj.getString("NumExt")}",
                            extra2 = jsonObj.getString("Fotografia"))
                        )
                    }
                    activity?.runOnUiThread { setUpDesarrolloSpinner() }
                }
            }
        })
    }

    private fun setUpDesarrolloSpinner() {
        val mList = mutableListOf<String>()
        desarrollosList.forEach { i -> mList.add(i.Nombre) }
        mList.add(0, requireContext().getString(R.string.new_aftermarket_select))

        with (spinnerDesarrollo) {
            adapter = ArrayAdapter(requireContext(), R.layout.spinner_text, mList)
            val userType = getTipoUsuario(requireContext())
            if (userType == OWNER && desarrollosList.size == 1) { setSelection(1) }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    if (pos != 0) {
                        val strId: String = desarrollosList[pos - 1].Id
                        //getListUnidad(strId)
                    } else {
                        //setUpSpinnerUnidad()
                    }
                }
            }
        }
    }
}
