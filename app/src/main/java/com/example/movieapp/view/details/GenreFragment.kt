package com.example.movieapp.view.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.movieapp.adapter.GenreAdapter
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentGenreBinding
import com.example.movieapp.model.Genre
import com.example.movieapp.model.Movie
import com.example.movieapp.model.TvShows


class GenreFragment<T>(val item: T? = null): BaseFragment<FragmentGenreBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGenreBinding
        get() = {
            inflater, group, b -> FragmentGenreBinding.inflate(inflater, group, b)
        }
    private lateinit var adapter: GenreAdapter
    private val genreList: ArrayList<Genre> = arrayListOf()
    override fun setView() {
        if (item is Movie){
            item.genres?.let { genreList.addAll(it) }
        }else if (item is TvShows){
            item.genres?.let { genreList.addAll(it) }
        }
        adapter = GenreAdapter(genreList)
        binding.genreRecView.adapter = adapter
        binding.genreRecView.layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
    }
}