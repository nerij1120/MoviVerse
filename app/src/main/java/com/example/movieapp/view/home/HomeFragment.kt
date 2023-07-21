package com.example.movieapp.view.home

import android.app.ActivityOptions
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.movieapp.R
import com.example.movieapp.adapter.HomeTVAdapter
import com.example.movieapp.adapter.MovieAdapter
import com.example.movieapp.adapter.RecommendAdapter
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentHomeBinding
import com.example.movieapp.model.Item
import com.example.movieapp.model.Movie
import com.example.movieapp.model.TvShows
import com.example.movieapp.model.User
import com.example.movieapp.model.Watching
import com.example.movieapp.utils.CenterZoomLayoutManager
import com.example.movieapp.utils.Constant.Companion.IMAGE_URL
import com.example.movieapp.view.activity.DetailsActivity
import com.example.movieapp.view.activity.MainActivity
import com.example.movieapp.viewmodel.home.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.flow.collectLatest

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
        get() = {
            inflater, container, attach ->
            FragmentHomeBinding.inflate(inflater, container, attach)
        }
    private lateinit var viewModel: HomeViewModel
    private var hasRecommend = false
    private var hasTvRecommend = false

    //Adapters
    private lateinit var adapter: MovieAdapter
    private lateinit var recommendAdapter: RecommendAdapter
    private lateinit var tvAdapter: HomeTVAdapter
    private lateinit var recommendTvAdapter: RecommendAdapter

    //List
    private val movieList = arrayListOf<Movie>()
    private val tvList = arrayListOf<TvShows>()
    private val recommendMovies = arrayListOf<Item>()
    private val recommendTV = arrayListOf<Item>()

    //Firebase
    private lateinit var recommendReference: DatabaseReference
    private lateinit var tvRecommendListener: ValueEventListener
    private lateinit var movieRecommendListener: ValueEventListener
    private lateinit var watchingReference: DatabaseReference
    private lateinit var watchingListener: ValueEventListener
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var listener: ValueEventListener
    private lateinit var userPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        userPreferences = activity?.getSharedPreferences("WATCHING_STATE", MODE_PRIVATE)!!

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.getPopular(1)
        viewModel.getPopularTV(1)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setView() {
        //Init Rec Views and Adapters
        initPopularMoviesRecView()
        initPopularTvRecView()
        initRecommendMoviesRecView()
        initRecommendTVRecView()
    }

    override fun setViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.errorFlow.collectLatest {
                (activity as MainActivity).displayError(it)
            }
        }
    }

    private fun initPopularTvRecView() {
        tvAdapter = HomeTVAdapter(tvList){ movie ->
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("id", movie.id)
            intent.putExtra("type", "tv")
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
        }

        binding.tvRecView.adapter = tvAdapter

        val layoutManager = CenterZoomLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.tvRecView.layoutManager = layoutManager

        binding.tvRecView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(tvList.size == 0)
                    return
                val offset: Int = binding.tvRecView.computeHorizontalScrollOffset()
                var position: Float =
                    offset.toFloat() / (binding.tvRecView.getChildAt(0).measuredWidth).toFloat()
                position += 0.5f
                val postInt: Int = position.toInt()
                val positionIndex = postInt + 1
                val listSize = tvList.size
                if (positionIndex == listSize) {
                    binding.tvRecView.smoothScrollToPosition(positionIndex - 2)
                } else if (positionIndex == 1) {
                    binding.tvRecView.smoothScrollToPosition(1)
                }
            }
        })

        //Add snap helper to recyclerview to make it focus at 1 item
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.tvRecView)
    }

    private fun initRecommendTVRecView() {
        recommendTvAdapter = RecommendAdapter(recommendTV){ movie ->
            if (movie !is TvShows)
                return@RecommendAdapter
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("id", movie.id)
            intent.putExtra("type", "tv")
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
        }

        binding.recommendationTVRecView.adapter = recommendTvAdapter

        val lM = CenterZoomLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.recommendationTVRecView.layoutManager = lM

        binding.recommendationTVRecView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(recommendTV.isEmpty())
                    return
                val offset: Int = binding.recommendationTVRecView.computeHorizontalScrollOffset()
                var position: Float =
                    offset.toFloat() / (binding.recommendationTVRecView.getChildAt(0).measuredWidth).toFloat()
                position += 0.5f
                val postInt: Int = position.toInt()
                val positionIndex = postInt + 1
                val listSize = recommendTV.size
                if (positionIndex == listSize) {
                    binding.recommendationTVRecView.smoothScrollToPosition(positionIndex - 2)
                } else if (positionIndex == 1) {
                    binding.recommendationTVRecView.smoothScrollToPosition(1)
                }
            }
        })

        //Add snap helper to recyclerview to make it focus at 1 item
        val snap = LinearSnapHelper()
        snap.attachToRecyclerView(binding.recommendationTVRecView)
    }

    private fun initRecommendMoviesRecView() {
        recommendAdapter = RecommendAdapter(recommendMovies){ movie ->
            if (movie !is Movie)
                return@RecommendAdapter
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("id", movie.id)
            intent.putExtra("type", "movie")
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
        }

        binding.recommendationRecView.adapter = recommendAdapter

        val lM = CenterZoomLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.recommendationRecView.layoutManager = lM

        binding.recommendationRecView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(recommendMovies.size == 0)
                    return
                val offset: Int = binding.recommendationRecView.computeHorizontalScrollOffset()
                var position: Float =
                    offset.toFloat() / (binding.recommendationRecView.getChildAt(0).measuredWidth).toFloat()
                position += 0.5f
                val postInt: Int = position.toInt()
                val positionIndex = postInt + 1
                val listSize = recommendMovies.size
                if (positionIndex == listSize) {
                    binding.recommendationRecView.smoothScrollToPosition(positionIndex - 2)
                } else if (positionIndex == 1) {
                    binding.recommendationRecView.smoothScrollToPosition(1)
                }
            }
        })

        //Add snap helper to recyclerview to make it focus at 1 item
        val snap = LinearSnapHelper()
        snap.attachToRecyclerView(binding.recommendationRecView)
    }

    private fun initPopularMoviesRecView() {
        adapter = MovieAdapter(movieList){ movie  ->
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("id", movie.id)
            intent.putExtra("type", "movie")
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
        }

        binding.moviesRecView.adapter = adapter

        val layoutManager = CenterZoomLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.moviesRecView.layoutManager = layoutManager

        binding.moviesRecView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(movieList.size == 0)
                    return
                val offset: Int = binding.moviesRecView.computeHorizontalScrollOffset()
                var position: Float =
                    offset.toFloat() / (binding.moviesRecView.getChildAt(0).measuredWidth).toFloat()
                position += 0.5f
                val postInt: Int = position.toInt()
                val positionIndex = postInt + 1
                val listSize = movieList.size
                if (positionIndex == listSize) {
                    binding.moviesRecView.smoothScrollToPosition(positionIndex - 2)
                } else if (positionIndex == 1) {
                    binding.moviesRecView.smoothScrollToPosition(1)
                }
            }
        })

        //Add snap helper to recyclerview to make it focus at 1 item
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.moviesRecView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.popularRadio.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.popularMovies -> {
                    binding.moviesRecView.visibility = View.VISIBLE
                    binding.popularNoTvFound.visibility = View.GONE
                    binding.tvRecView.visibility = View.GONE
                    if (movieList.size == 0)
                    {
                        binding.popularNoMoviesFound.visibility = View.VISIBLE
                    }
                    else{
                        binding.popularNoMoviesFound.visibility = View.GONE
                    }
                }
                R.id.popularTV -> {
                    binding.tvRecView.visibility = View.VISIBLE
                    binding.moviesRecView.visibility = View.GONE
                    binding.popularNoMoviesFound.visibility = View.GONE
                    if (tvList.size == 0)
                    {
                        binding.popularNoTvFound.visibility = View.VISIBLE
                    }
                    else{
                        binding.popularNoTvFound.visibility = View.GONE
                    }
                }
            }
        }

        binding.recommendRadio.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.recommendMovies -> {
                    binding.recommendationRecView.visibility = View.VISIBLE
                    binding.recommendationTVRecView.visibility = View.GONE
                    binding.recommendNoTvFound.visibility = View.GONE
                    if (recommendMovies.size == 0)
                    {
                        binding.recommendNoMoviesFound.visibility = View.VISIBLE
                    }
                    else{
                        binding.recommendNoMoviesFound.visibility = View.GONE
                    }
                }
                R.id.recommendTV -> {
                    binding.recommendationTVRecView.visibility = View.VISIBLE
                    binding.recommendationRecView.visibility = View.GONE
                    binding.recommendNoMoviesFound.visibility = View.GONE
                    if (recommendTV.isEmpty())
                    {
                        binding.recommendNoTvFound.visibility = View.VISIBLE
                    }
                    else{
                        binding.recommendNoTvFound.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.popular.observe(viewLifecycleOwner){ response->
            if(response.isSuccessful){
                movieList.clear()
                response.body()?.results?.let {
                    if(it.size > 0){
                        if(!hasRecommend)
                            viewModel.getRecommendations(it[0].id)
                        movieList.addAll(it)
                        movieList.add(0, Movie())
                        movieList.add(Movie())
                        binding.popularNoMoviesFound.visibility = View.GONE
                    }
                }
                adapter.notifyDataSetChanged()
            }
            else{
                (activity as MainActivity).displayError(response.errorBody()?.string()?:"Unknown Error")
            }
        }

        viewModel.tvPopular.observe(viewLifecycleOwner){ response->
            if(response.isSuccessful){
                tvList.clear()
                response.body()?.results?.let {
                    if(it.size > 0){
                        if(!hasTvRecommend)
                            viewModel.getTVRecommendations(it[0].id)
                        tvList.addAll(it)
                        tvList.add(0, TvShows())
                        tvList.add(TvShows())
                        binding.popularNoTvFound.visibility = View.GONE
                    }
                }
                tvAdapter.notifyDataSetChanged()
            }
            else{
                (activity as MainActivity).displayError(response.errorBody()?.string()?:"Unknown Error")
            }
        }

        viewModel.recommend.observe(viewLifecycleOwner){
            if(it.isSuccessful){
                recommendMovies.clear()
                val data = it.body()?.results
                if(data != null){
                    if(data.size > 0){
                        recommendMovies.addAll(data)
                        recommendMovies.add(0, Movie())
                        recommendMovies.add(Movie())
                        binding.recommendNoMoviesFound.visibility = View.GONE
                    }
                    else if(hasRecommend){
                        if(movieList.size != 0){
                            viewModel.getRecommendations(movieList[1].id)
                        }
                    }
                }
                recommendAdapter.notifyDataSetChanged()
            }
            else{
                (activity as MainActivity).displayError(it.errorBody()?.string()?:"Unknown Error")
            }
        }

        viewModel.tvRecommend.observe(viewLifecycleOwner){
            if(it.isSuccessful){
                recommendTV.clear()
                val data = it.body()?.results
                if(data != null){
                    if(data.size > 0){
                        recommendTV.addAll(data)
                        recommendTV.add(0, TvShows())
                        recommendTV.add(TvShows())
                        binding.recommendNoTvFound.visibility = View.GONE
                    }else if(hasTvRecommend){
                        if(tvList.size != 0){
                            viewModel.getTVRecommendations(tvList[1].id)
                        }
                    }
                }
                recommendTvAdapter.notifyDataSetChanged()
            }else{
                (activity as MainActivity).displayError(it.errorBody()?.string()?:"Unknown Error")

            }
        }

    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if(user != null)
        {
            addProfileListener(user)
            addWatchingListener(user)
            addRecommendListener(user)
        }
        else{
            updateGuestWatchingUI()
        }
    }

    private fun addRecommendListener(user: FirebaseUser) {
        recommendReference = FirebaseDatabase.getInstance().getReference("Recommend")
            .child(user.uid)

        movieRecommendListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val movie = snapshot.getValue(Watching::class.java)
                if(movie != null){
                    hasRecommend = true
                    viewModel.getRecommendations(movie.id)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                (activity as MainActivity).displayError(error.message)
            }
        }

        tvRecommendListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val movie = snapshot.getValue(Watching::class.java)
                if(movie != null){
                    hasTvRecommend = true
                    viewModel.getTVRecommendations(movie.id)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                (activity as MainActivity).displayError(error.message)
            }
        }

        recommendReference.child("movie").addValueEventListener(movieRecommendListener)
        recommendReference.child("tv").addValueEventListener(tvRecommendListener)
    }

    private fun updateGuestWatchingUI() {
        val vidId = userPreferences.getString("vidId", null)
        val id = userPreferences.getInt("id", -1)
        val movieId = userPreferences.getInt("movie_id", -1)
        val tvId = userPreferences.getInt("tv_id", -1)
        val type = userPreferences.getString("type", "")
        val backdropPath = userPreferences.getString("backdropPath", "")
        if(tvId != -1)
        {
            hasTvRecommend = true
            viewModel.getTVRecommendations(tvId)
        }
        if(movieId != -1){
            hasRecommend = true
            viewModel.getRecommendations(movieId)
        }
        if(vidId != null){
            context?.let {
                Glide.with(it)
                    .asBitmap()
//                        .placeholder(R.drawable.ic_movie_icon)
                    .error(R.drawable.ic_movie_error)
                    .load("$IMAGE_URL/original${backdropPath}")
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            val radius = 4f
                            binding.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                            binding.blurView.clipToOutline = true
                            binding.blurView.setupWith(binding.mainImgLayout, RenderScriptBlur(it))
                                .setBlurRadius(radius)
                            return false
                        }
                    })
                    .into(binding.mainImg)
            }
            binding.mainImgLayout.setOnClickListener {
                if(type == "movie"){
                    val intent = Intent(requireContext(), DetailsActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("type", "movie")
                    intent.putExtra("continue_playing", true)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
                }
                else if(type == "tv"){
                    val intent = Intent(requireContext(), DetailsActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("type", "tv")
                    intent.putExtra("continue_playing", true)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
                }
            }
            binding.watchingTxt.text = getString(R.string.continue_watching)
        }
        else{
            binding.mainImgLayout.visibility = View.GONE
        }
    }

    private fun addProfileListener(user: FirebaseUser) {
        reference = FirebaseDatabase.getInstance().getReference("Users")
        val userID = user.uid
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(User::class.java)
                if (userProfile != null) {
                    binding.greetingText.text = "Hello ${userProfile.name},"
                    (activity as MainActivity).supportActionBar?.title = "Hello ${userProfile.name},"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                (activity as MainActivity).displayError(error.message)
            }
        }
        reference.child(userID).addValueEventListener(listener)
    }

    private fun addWatchingListener(user: FirebaseUser) {
        watchingReference =
            FirebaseDatabase.getInstance()
                .getReference("Watching")
                .child(user.uid)
        watchingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val watchingState = snapshot.getValue(Watching::class.java)
                if(watchingState != null){
                    updateWatchingUI(watchingState)
                }
                else{
                    binding.mainImgLayout.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                (activity as MainActivity).displayError(error.message)
            }
        }
        watchingReference.addValueEventListener(watchingListener)
    }

    private fun updateWatchingUI(watchingState: Watching) {
        context?.let {
            Glide.with(it)
                .asBitmap()
//                .placeholder(R.drawable.ic_movie_icon)
                .error(R.drawable.ic_movie_error)
                .load("$IMAGE_URL/original${watchingState.backdropPath}")
//                .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(it))))
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        val radius = 4f
                        binding.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                        binding.blurView.clipToOutline = true
                        binding.blurView.setupWith(binding.mainImgLayout, RenderScriptBlur(it))
                            .setBlurRadius(radius)
                        return false
                    }
                })
                .into(binding.mainImg)
        }
        binding.mainImgLayout.setOnClickListener {
            if(watchingState.type == "movie"){
                val intent = Intent(requireContext(), DetailsActivity::class.java)
                intent.putExtra("id", watchingState.id)
                intent.putExtra("type", "movie")
                intent.putExtra("continue_playing", true)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
            }
            else if(watchingState.type == "tv"){
                val intent = Intent(requireContext(), DetailsActivity::class.java)
                intent.putExtra("id", watchingState.id)
                intent.putExtra("type", "tv")
                intent.putExtra("continue_playing", true)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
            }
        }
        binding.watchingTxt.text = getString(R.string.continue_watching)
    }


    override fun onStop() {
        super.onStop()
        auth.currentUser?.uid?.let {
            reference.child(it).removeEventListener(listener)
            watchingReference.removeEventListener(watchingListener)
            recommendReference.child("movie").removeEventListener(movieRecommendListener)
            recommendReference.child("tv").removeEventListener(tvRecommendListener)
        }
    }
}