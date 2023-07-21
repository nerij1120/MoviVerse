package com.example.movieapp.view.discovery.viewPagerFragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.movieapp.adapter.MovieSearchAdapter
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentRomanceBinding
import com.example.movieapp.model.Movie
import com.example.movieapp.view.activity.DetailsActivity
import com.example.movieapp.view.activity.MainActivity
import com.example.movieapp.viewmodel.discovery.RomanceViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Timer
import kotlin.concurrent.schedule


class RomanceFragment : BaseFragment<FragmentRomanceBinding>() {
    private lateinit var viewModel: RomanceViewModel
    private lateinit var adapter: MovieSearchAdapter
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRomanceBinding
        get() = {
            layoutInflater, viewGroup, b ->
            FragmentRomanceBinding.inflate(layoutInflater, viewGroup, b)
        }

    //Var for pagination
    private var pageCount = 1
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var pastVisibleItems = 0
    private var loading = true
    private var end = false

    private val moviesList = arrayListOf<Movie>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[RomanceViewModel::class.java]
        viewModel.getMovies(pageCount++)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setView() {
        adapter = MovieSearchAdapter(moviesList){
                movie->

            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("id", movie.id)
            intent.putExtra("type", "movie")
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
        }

        binding.recView.adapter = adapter

        val lM = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recView.layoutManager = lM

        binding.recView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(dy > 0){
                    //Scroll down
                    visibleItemCount = lM.childCount
                    totalItemCount = lM.itemCount
                    val firstVisibleItem = lM.findFirstVisibleItemPositions(null)
                    if(firstVisibleItem != null && firstVisibleItem.isNotEmpty())
                        pastVisibleItems = firstVisibleItem[0]

                    if(loading)
                    {
                        if(!end)
                        {
                            if((visibleItemCount + pastVisibleItems) >= totalItemCount)
                            {
                                loading = false

                                // Fetch new data
                                viewModel.getMovies(pageCount++)

                            }
                        }
                        else{
                            loading = false
                            (activity as MainActivity).displayMessage("No more movies to view")
                            Timer("DelayRefresh", false).schedule(6000){
                                loading = true
                            }
                        }
                    }
                }
            }
        })
    }

    override fun setViewModel() {
        viewModel.myResponse.observe(viewLifecycleOwner){
            if(it.isSuccessful){
                val data = it.body()?.results
                if (data != null) {
                    if(data.size == 0)
                    {
                        end = true
                    }
                    else {
                        moviesList.addAll(data)
                        adapter.notifyDataSetChanged()
                    }
                }
                loading = true
            }
            else{
                (activity as MainActivity).displayError(it.errorBody()?.string()?:"Unknown Error")
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.errorFlow.collectLatest {
                (activity as MainActivity).displayError(it)
            }
        }
    }

}