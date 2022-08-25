package com.example.moviverse.view.discovery

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.moviverse.BottomNavDirections
import com.example.moviverse.adapter.SearchAdapter
import com.example.moviverse.databinding.FragmentSearchBinding
import com.example.moviverse.model.SearchResult
import com.example.moviverse.viewmodel.DetailsViewModel
import com.example.moviverse.viewmodel.discovery.SearchViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchAdapter
    private val detailsViewModel: DetailsViewModel by activityViewModels()

    //Var for pagination
    private var pageCount = 1
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var pastVisibleItems = 0
    private var loading = true
    private var end = false
    private var query = ""

    private var searchList: ArrayList<SearchResult> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(context).inflateTransition(
            android.R.transition.move
        )

        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        adapter = SearchAdapter(ArrayList(), onClick = { res, poster, name->
            searchList.addAll(adapter.searchList)
            when(res.type){
                "movie"->{
                    detailsViewModel.getDetails(res.id)
                    val extras = FragmentNavigatorExtras(
                        poster to "movie_poster_${res.id}_8",
                        name to "movie_title_${res.id}_8",
                    )
                    findNavController().navigate(
                        BottomNavDirections.discoveryToDetails(8),
                        extras
                    )
                }
                "tv"->{
                    detailsViewModel.getTvShowsDetails(res.id)
                    val extras = FragmentNavigatorExtras(
                        poster to "tv_poster_${res.id}_8",
                        name to "tv_title_${res.id}_8",
                    )
                    findNavController().navigate(
                        BottomNavDirections.toTvDetails(8),
                        extras
                    )
                }
                "person"->{
                    detailsViewModel.getPersonDetails(res.id)
                    val extras = FragmentNavigatorExtras(
                        poster to "person_poster_${res.id}_8",
                        name to "person_name_${res.id}_8",
                    )
                    findNavController().navigate(
                        BottomNavDirections.toDetails(8),
                        extras
                    )
                }
            }
        })
        binding.moviesSearchRecView.adapter = adapter
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.moviesSearchRecView.apply{
            postponeEnterTransition()
            viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
        }
        val layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.moviesSearchRecView.layoutManager = layoutManager

        binding.searchEditTxt.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(p0.toString().trim() != "")
                    {
                        adapter.searchList.clear()
                        adapter.notifyDataSetChanged()
                        hideView()
                        showShimmer()
                    }
                }

                private val delay: Long = 1000
                private var timer = Timer()
                override fun afterTextChanged(p0: Editable?) {
                    timer.cancel()
                    timer = Timer()
                    timer.schedule(object: TimerTask(){
                        override fun run() {
                            Handler(Looper.getMainLooper()).post {
                                if(_binding != null){
                                    val q = p0?.toString()?.trim()
                                    if (q != null && q != ""){
                                        showShimmer()
                                        query = q
                                        if(searchList.size != 0){
                                            adapter.searchList.addAll(searchList)
                                            adapter.notifyDataSetChanged()
                                            hideView()
                                            searchList.clear()
                                            stopShimmer()
                                        }
                                        else{
                                            pageCount = 1
                                            loading = true
                                            end = false
                                            viewModel.getSearchResults(q, pageCount++)
                                            stopShimmer()
                                        }
                                    }
                                    else{
                                        query = ""
                                        binding.searchTxt.visibility = View.VISIBLE
                                        binding.searchImg.visibility = View.VISIBLE
                                        if(adapter.searchList.size != 0)
                                        {
                                            adapter.searchList.clear()
                                            adapter.notifyDataSetChanged()
                                        }
                                        stopShimmer()
                                    }
                                }
                            }
                        }
                    }, delay)
                }
            }
        )

        binding.moviesSearchRecView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(dy > 0){
                    //Scroll down
                    visibleItemCount = layoutManager.childCount
                    totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPositions(null)
                    if(firstVisibleItem != null && firstVisibleItem.isNotEmpty())
                        pastVisibleItems = firstVisibleItem[0]

                    if(loading)
                    {
                        if(!end)
                        {
                            if((visibleItemCount + pastVisibleItems) >= totalItemCount)
                            {
                                loading = false
                                Log.e("Loading", "...")
                                // Fetch new data
                                binding.progressBar.visibility = View.VISIBLE
                                viewModel.getSearchResults(query, pageCount++)
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

    private fun hideView() {
        binding.noDataImg.visibility = View.GONE
        binding.noDataTxt.visibility = View.GONE
        binding.searchTxt.visibility = View.GONE
        binding.searchImg.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(query == "")
            showKeyboard()

        viewModel.searchList.observe(viewLifecycleOwner){
            if(it.isSuccessful){
                val data = it.body()?.results
                if (data != null && query != "" && searchList.size == 0) {
                    if(data.size == 0)
                    {
                        end = true
                    }
                    else if(data.size > 0){
                        if(pageCount == 2)
                            adapter.searchList.clear()
                        adapter.searchList.addAll(data)
                        adapter.notifyDataSetChanged()
                    }

                    if(adapter.searchList.size == 0){
                        binding.noDataImg.visibility = View.VISIBLE
                        binding.noDataTxt.visibility = View.VISIBLE
                    }
                    else{
                        binding.noDataImg.visibility = View.GONE
                        binding.noDataTxt.visibility = View.GONE
                    }
                }
                loading = true
            }
            else{
                binding.progressBar.visibility = View.GONE
            }
            if(searchList.size != 0){
                binding.progressBar.visibility = View.GONE
            }
            else{
                //stopShimmer()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showKeyboard() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        binding.searchEditTxt.requestFocus()
        val imm =
            activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchEditTxt, 0)
    }

    private fun showShimmer(){
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
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