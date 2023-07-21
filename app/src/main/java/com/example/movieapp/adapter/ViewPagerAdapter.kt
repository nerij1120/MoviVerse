package com.example.movieapp.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movieapp.view.discovery.viewPagerFragment.ActionFragment
import com.example.movieapp.view.discovery.viewPagerFragment.AnimationFragment
import com.example.movieapp.view.discovery.viewPagerFragment.ComedyFragment
import com.example.movieapp.view.discovery.viewPagerFragment.HorrorFragment
import com.example.movieapp.view.discovery.viewPagerFragment.MoviesFragment
import com.example.movieapp.view.discovery.viewPagerFragment.RomanceFragment
import com.example.movieapp.view.discovery.viewPagerFragment.TvShowsFragment

class ViewPagerAdapter(
    parentFragment: Fragment
): FragmentStateAdapter(parentFragment) {

    override fun getItemCount(): Int {
        return 7
    }

    override fun createFragment(position: Int): Fragment {
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