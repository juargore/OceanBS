@file:Suppress("SpellCheckingInspection")

package com.glass.oceanbs.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.glass.oceanbs.R
import com.glass.oceanbs.fragments.NewSolicitudFragment

class EditarIncidenciaActivity : AppCompatActivity() {

    private lateinit var imgBackEdit: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_incidencia)

        supportActionBar?.hide()

        val extras = intent?.extras
        val solicitudId = extras?.getString("solicitudId")

        val bundle = Bundle()
        bundle.putBoolean("isEdit", true)
        bundle.putString("solicitudId", solicitudId)

        val fragment = NewSolicitudFragment()
        fragment.arguments = bundle

        supportFragmentManager.inTransaction {
            add(R.id.layFragment, fragment)
        }

        imgBackEdit = findViewById(R.id.imgBackEdit)
        imgBackEdit.setOnClickListener { this.finish() }
    }

    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }
}
