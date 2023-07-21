package com.example.movieapp.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.movieapp.R
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<VB: ViewBinding>: AppCompatActivity() {
    abstract val bindingInflater: (LayoutInflater)-> VB
    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)

        setView()
        setViewModel()
        setEvent()
    }

    open fun setView(){}

    open fun setViewModel(){}

    open fun setEvent(){}

    fun displayMessage(message: String){
        val view: View = if(findViewById<View>(R.id.main_view) != null) findViewById(R.id.main_view) else findViewById(android.R.id.content)
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .show()
    }

    fun displayMessage(message: String, actionTxt: String, onClick: ()->Unit){
        val view: View = if(findViewById<View>(R.id.main_view) != null) findViewById(R.id.main_view) else findViewById(android.R.id.content)
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(actionTxt) {
                onClick.invoke()
            }
            .show()
    }

    fun displayError(message: String){
        val view: View = if(findViewById<View>(R.id.main_view) != null) findViewById(R.id.main_view) else findViewById(android.R.id.content)
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .show()
    }
}