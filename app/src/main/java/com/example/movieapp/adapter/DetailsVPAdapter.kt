package com.example.movieapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movieapp.model.Movie
import com.example.movieapp.model.TvShows
import com.example.movieapp.view.details.CastFragment
import com.example.movieapp.view.details.DescriptionFragment
import com.example.movieapp.view.details.GenreFragment

class DetailsVPAdapter<T>(fragmentActivity: FragmentActivity, private val item: T? = null): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        when (item) {
            is Movie -> {
                return when (position){
                    0 -> {
                        DescriptionFragment(item)
                    }
                    1 -> {
                        GenreFragment(item)
                    }
                    2 -> {
                        CastFragment(item)

                    }
                    else -> {
                        Fragment()
                    }
                }
            }

            is TvShows -> {
                return when (position){
                    0 -> {
                        DescriptionFragment(item)
                    }
                    1 -> {
                        GenreFragment(item)
                    }
                    2 -> {
                        CastFragment(item)
                    }
                    else -> {
                        Fragment()
                    }
                }
            }
            else -> {
                return Fragment()
            }
        }
    }


}