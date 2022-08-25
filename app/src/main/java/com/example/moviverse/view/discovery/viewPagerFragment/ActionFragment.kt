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
import com.example.moviverse.BottomNavDirections
import com.example.moviverse.R
import com.example.moviverse.adapter.MovieSearchAdapter
import com.example.moviverse.databinding.FragmentActionBinding
import com.example.moviverse.viewmodel.DetailsViewModel
import com.example.moviverse.viewmodel.discovery.ActionViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class ActionFragment : Fragment() {
    private lateinit var viewModel: ActionViewModel
    private val detailsViewModel: DetailsViewModel by activityViewModels()
    private lateinit var adapter: MovieSearchAdapter
    private var _binding: FragmentActionBinding? = null
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
        _binding = FragmentActionBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[ActionViewModel::class.java]

        viewModel.getMovies(pageCount++)

        adapter = MovieSearchAdapter(ArrayList(), onClick = {
                movie, poster, title->
            detailsViewModel.getDetails(movie.id)
            val extras = FragmentNavigatorExtras(
                poster to "movie_poster_${movie.id}_3",
                title to "movie_title_${movie.id}_3",
            )
            findNavController().navigate(
                BottomNavDirections.discoveryToDetails(3),
                extras
            )
        }, 3)

        binding.recView.adapter = adapter
        binding.recView.apply{
            postponeEnterTransition()
            viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
        }

        val lM = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recView.layoutManager = lM
        Log.e("action", "create_view")

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
        Log.e("action", "view_created")
        viewModel.myResponse.observe(viewLifecycleOwner){
            if(it.isSuccessful){
                Log.e("observe", "hello")
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
