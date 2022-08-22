package com.example.moviverse.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moviverse.view.discovery.viewPagerFragment.*

class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 7
    }

    override fun createFragment(position: Int): Fragment {
        Log.e("position", "$position")
        return when(position){
            0->{
                MoviesFragment()
            }
            1->{
                TvShowsFragment()
            }
            2->{
                ActionFragment()
            }
            3->{
                ComedyFragment()
            }
            4->{
                HorrorFragment()
            }
            5->{
                RomanceFragment()
            }
            6->{
                AnimationFragment()
            }
            else -> {
                Fragment()
            }
        }
    }
}