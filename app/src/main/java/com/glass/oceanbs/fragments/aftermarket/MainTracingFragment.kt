package com.glass.oceanbs.fragments.aftermarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.glass.oceanbs.Constants
import com.glass.oceanbs.Constants.GET_MAIN_ITEMS_HOME
import com.glass.oceanbs.Constants.OWNER_ID
import com.glass.oceanbs.Constants.URL_MAIN_ITEMS_HOME
import com.glass.oceanbs.Constants.getUserId
import com.glass.oceanbs.R
import com.glass.oceanbs.activities.AftermarketActivity
import com.glass.oceanbs.activities.NotificationActivity
import com.glass.oceanbs.extensions.Parameter
import com.glass.oceanbs.extensions.getDataFromServer
import com.glass.oceanbs.extensions.runOnUiThread
import com.glass.oceanbs.fragments.aftermarket.adapters.SummaryPagerAdapter
import com.glass.oceanbs.models.History
import com.glass.oceanbs.models.Unity
import com.google.android.material.tabs.TabLayout

class MainTracingFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var layParent: ConstraintLayout
    private lateinit var spinnerDesarrollo: Spinner
    private val unityList: ArrayList<Unity> = ArrayList()

    companion object {
        var desarrolloId : String? = null
        var desarrolloCode: String? = null
        var itemConstruction: History? = null
        var itemDocumentation: History? = null
        fun newInstance() = MainTracingFragment()
    }

    override fun onCreateView(infl: LayoutInflater, cont: ViewGroup?, state: Bundle?): View? {
        if (rootView == null) {
            rootView = infl.inflate(R.layout.fragment_main_tracing, cont, false)
            viewPager = rootView!!.findViewById(R.id.viewPager)
            tabLayout = rootView!!.findViewById(R.id.tabLayout)
            layParent = rootView!!.findViewById(R.id.layParent)
            spinnerDesarrollo = rootView!!.findViewById(R.id.spinnerDesarrollo)
            getListDesarrollos()
        }
        return rootView
    }

    private fun setUpTabs() {
        viewPager.adapter = SummaryPagerAdapter(requireContext(), childFragmentManager)
        viewPager.offscreenPageLimit = 4
        tabLayout.setupWithViewPager(viewPager)

        // actionScreen == 1.0 -> MainActivity::class.java
        // actionScreen == 2.0 -> AfterMarketActivity::class.java
        // actionScreen == 2.1 -> AfterMarketActivity::class.java + ConstructionFragment
        // actionScreen == 2.2 -> AfterMarketActivity::class.java + DocumentationFragment
        // actionScreen == 3.0 -> AfterMarketActivity::class.java + MainConversationFragment
        if (NotificationActivity.actionScreen == 2.1f) {
            changeToConstructionTab(null)
            NotificationActivity.actionScreen = 0.0f
        }
        if (NotificationActivity.actionScreen == 2.2f) {
            changeToDocumentationTab(null)
            NotificationActivity.actionScreen = 0.0f
        }
    }

    fun changeToConstructionTab(item: History?) {
        itemConstruction = item
        viewPager.currentItem = 1
    }

    fun changeToDocumentationTab(item: History?) {
        itemDocumentation = item
        viewPager.currentItem = 2
    }

    fun intermediateToUpdateConversationChat() {
        (activity as AftermarketActivity).changeToConversationTab()
    }

    private fun getListDesarrollos() {
        activity?.getDataFromServer(
            webService = GET_MAIN_ITEMS_HOME,
            url = URL_MAIN_ITEMS_HOME,
            parent = layParent,
            parameters = listOf(Parameter(
                key = OWNER_ID,
                value = getUserId(requireContext())
            ))
        ) { jsonRes ->
            with (jsonRes) {
                val units = getJSONArray(Constants.UNITS)
                for (i in 0 until units.length()) {
                    val j = units.getJSONObject(i)
                    unityList.add(
                        Unity(
                            id = j.getString(Constants.UNITY_ID),
                            code = j.getString(Constants.UNITY_CODE),
                            name = j.getString(Constants.UNITY_NAME)
                        )
                    )
                }
                runOnUiThread { setUpDesarrolloSpinner() }
            }
        }
    }

    private fun setUpDesarrolloSpinner() {
        val mList = mutableListOf<String>()
        unityList.forEach { mList.add("${it.code} - ${it.name}") }
        mList.add(0, requireContext().getString(R.string.new_aftermarket_select))

        with (spinnerDesarrollo) {
            adapter = ArrayAdapter(requireContext(), R.layout.spinner_text, mList)
            if (unityList.size == 1) { setSelection(1) }
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    desarrolloId = if (pos != 0) unityList[pos - 1].id else null
                    desarrolloCode = if (pos != 0) unityList[pos - 1].code else null
                    // reset history item to avoid re-charge from Construction or Documentation
                    if (desarrolloId == null) {
                        itemConstruction = null
                        itemDocumentation = null
                    }
                    setUpTabs()
                }
            }
        }
    }
}
