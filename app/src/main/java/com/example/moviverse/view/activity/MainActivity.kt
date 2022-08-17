package com.example.moviverse.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moviverse.R
import com.example.moviverse.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        binding.bottomNav.itemIconTintList = null

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.tvDetailsFragment->{
                    binding.bottomNav.visibility = View.GONE
                }
                R.id.detailsFragment2->{
                    binding.bottomNav.visibility = View.GONE
                }
                R.id.personDetailsFragment->{
                    binding.bottomNav.visibility = View.GONE
                }
                R.id.creditFragment->{
                    binding.bottomNav.visibility = View.GONE
                }
                R.id.tvCreditFragment->{
                    binding.bottomNav.visibility = View.GONE
                }
                else->{
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }

    }
}