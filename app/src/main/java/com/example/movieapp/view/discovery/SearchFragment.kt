package com.example.movieapp.view.discovery


import android.app.ActivityOptions
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.movieapp.adapter.SearchAdapter
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentSearchBinding
import com.example.movieapp.model.SearchResult
import com.example.movieapp.view.activity.DetailsActivity
import com.example.movieapp.view.activity.MainActivity
import com.example.movieapp.view.activity.PersonActivity
import com.example.movieapp.viewmodel.discovery.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule


class SearchFragment : BaseFragment<FragmentSearchBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchBinding
        get() = {
            layoutInflater, viewGroup, b ->
            FragmentSearchBinding.inflate(layoutInflater, viewGroup, b)
        }
    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchAdapter

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

        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        viewModel.searchList.observe(this){
            if(it.isSuccessful){
                val data = it.body()?.results
                if (data != null && query != "") {
                    if(data.size == 0)
                    {
                        end = true
                    }
                    else{
                        searchList.addAll(data)
                        adapter.notifyDataSetChanged()
                    }

                    if(searchList.size == 0){
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
            stopShimmer()
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun setView() {
        // Inflate the layout for this fragment
        adapter = SearchAdapter(searchList, onClick = { res->
            when(res.type){
                "movie"->{
                    val intent = Intent(requireContext(), DetailsActivity::class.java)
                    intent.putExtra("id", res.id)
                    intent.putExtra("type", "movie")
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
                }
                "tv"->{
                    val intent = Intent(requireContext(), DetailsActivity::class.java)
                    intent.putExtra("id", res.id)
                    intent.putExtra("type", "tv")
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
                }
                "person"->{
                    val intent = Intent(requireContext(), PersonActivity::class.java)
                    intent.putExtra("id", res.id)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
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

    override fun setEvent() {
        binding.searchEditTxt.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(p0.toString().trim() != "")
                    {
                        hideView()
                        showShimmer()
                    }else{
                        query = ""
                        binding.searchTxt.visibility = View.VISIBLE
                        binding.searchImg.visibility = View.VISIBLE
                        stopShimmer()
                    }
                    searchList.clear()
                    adapter.notifyDataSetChanged()
                }

                private val delay: Long = 1000
                private var timer = Timer()
                override fun afterTextChanged(p0: Editable?) {
                    timer.cancel()
                    timer = Timer()
                    timer.schedule(object: TimerTask(){
                        override fun run() {
                            activity?.runOnUiThread {
                                val q = p0?.toString()?.trim()
                                if (q != null && q != ""){
                                    query = q
                                    pageCount = 1
                                    loading = true
                                    end = false
                                    viewModel.getSearchResults(q, pageCount++)
                                }
                            }
                        }
                    }, delay)
                }
            }
        )
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

    override fun setViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.errorFlow.collectLatest {
                (activity as MainActivity).displayError(it)
                stopShimmer()
                binding.progressBar.visibility = View.GONE
                binding.searchTxt.visibility = View.VISIBLE
                binding.searchImg.visibility = View.VISIBLE
            }
        }
    }
}