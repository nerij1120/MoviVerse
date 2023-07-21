package com.example.movieapp.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.movieapp.R
import com.example.movieapp.base.BaseActivity
import com.example.movieapp.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = {
            inflater -> ActivityMainBinding.inflate(inflater)
        }
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupWithNavController(binding.bottomNav, navController)

//        binding.bottomNav.itemIconTintList = null

        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.home,
            R.id.discovery,
            R.id.profile
        ).build()


//        setupActionBarWithNavController(
//            this, navController, appBarConfiguration
//        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.home->{
                    binding.bottomNav.visibility = View.VISIBLE
                }
                R.id.discovery->{
                    binding.bottomNav.visibility = View.VISIBLE
                }
                R.id.profile->{
                    binding.bottomNav.visibility = View.VISIBLE
                }
                else->{
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    binding.bottomNav.visibility = View.GONE
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

}