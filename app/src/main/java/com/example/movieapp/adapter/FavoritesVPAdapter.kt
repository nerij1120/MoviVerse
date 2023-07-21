package com.example.movieapp.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movieapp.view.profile.FavoritesMoviesFragment
import com.example.movieapp.view.profile.FavoritesTvShowsFragment

class FavoritesVPAdapter(
    parentFragment: Fragment
): FragmentStateAdapter(parentFragment) {

    private val movies = FavoritesMoviesFragment()
    private val tvShows = FavoritesTvShowsFragment()

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if(position == 0)
        {
            movies
        }
        else{
            tvShows
        }
    }
}