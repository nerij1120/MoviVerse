package com.example.moviverse.view.discovery.viewPagerFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.moviverse.R
import com.example.moviverse.adapter.TvShowsAdapter
import com.example.moviverse.databinding.FragmentTvShowsBinding
import com.example.moviverse.viewmodel.DetailsViewModel
import com.example.moviverse.viewmodel.discovery.TvShowsViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class TvShowsFragment : Fragment() {
    private lateinit var viewModel: TvShowsViewModel
    private val detailsViewModel: DetailsViewModel by activityViewModels()
    private lateinit var adapter: TvShowsAdapter
    private var _binding: FragmentTvShowsBinding? = null
    private val binding get() = _binding!!

    //Var for pagination
    private var pageCount = 1
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var pastVisibleItems = 0
    private var loading = true
    private var end = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTvShowsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[TvShowsViewModel::class.java]

        viewModel.getMovies(pageCount++)

        adapter = TvShowsAdapter(ArrayList(), onClick = {
                movie, poster, title->
            detailsViewModel.getTvShowsDetails(movie.id)
            val extras = FragmentNavigatorExtras(
                poster to "tv_poster_${movie.id}_2",
                title to "tv_title_${movie.id}_2",
            )
            findNavController().navigate(
                BottomNavDirections.toTvDetails(2),
                extras
            )
        }, 2)

        binding.recView.adapter = adapter
        val lM = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recView.layoutManager = lM
        Log.e("tv_shows", "create_view")

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
                                binding.progressBar.visibility = View.VISIBLE
                                Log.e("Loading", "...")
                                // Fetch new data
                                viewModel.getMovies(pageCount++)

                            }
                        }
                        else{
                            loading = false
                            Toast.makeText(context, "No more movies to view", Toast.LENGTH_SHORT).show()
                            Timer("DelayRefresh", false).schedule(6000){
                                loading = true
                            }
                        }
                    }
                }
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("tv_shows", "viewCreated")
        viewModel.myResponse.observe(viewLifecycleOwner){
            if(it.isSuccessful){
                val data = it.body()?.results
                if (data != null) {
                    if(data.size == 0)
                    {
                        end = true
                    }
                    else if(data.size > 0){
                        adapter.moviesList.addAll(data)
                        adapter.notifyDataSetChanged()
                    }
                }
                loading = true
            }
            else{
                Toast.makeText(context, it.errorBody()?.string(), Toast.LENGTH_SHORT).show()
            }
            binding.progressBar.visibility = View.GONE
            stopShimmer()
        }
    }

    private fun stopShimmer(){
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}