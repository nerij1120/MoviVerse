package com.example.movieapp.view.details

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.movieapp.R
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentDescriptionBinding
import com.example.movieapp.model.Movie
import com.example.movieapp.model.TvShows


class DescriptionFragment<T>(private val item: T? = null): BaseFragment<FragmentDescriptionBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDescriptionBinding
        get() = {
            inflater, vp, b -> FragmentDescriptionBinding.inflate(inflater, vp, b)
        }

    override fun setView() {
        item?.apply {
            if (this is Movie){
                binding.overviewTxt.text = overview
                binding.releaseDateTxt.text = releaseDate
                binding.ratingTxt.text = "$voteAverage (TMdb)"
                binding.releaseDateHeader.text = getString(R.string.release_date)
                binding.runtimeTxt.text = "$runtime minutes"
            }else if (this is TvShows){
                binding.overviewTxt.text = overview
                binding.releaseDateTxt.text = firstAirDate
                binding.ratingTxt.text = "$voteAverage (TMdb)"
                binding.releaseDateHeader.text = getString(R.string.first_air_date)
                binding.runtimeTxt.text = "$noEp episode(s) - $noSeason season(s)"
            }
        }
    }
}