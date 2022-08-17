package com.example.moviverse.view.home

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.moviverse.BottomNavDirections
import com.example.moviverse.R
import com.example.moviverse.adapter.MovieAdapter
import com.example.moviverse.databinding.FragmentHomeBinding
import com.example.moviverse.model.Movie
import com.example.moviverse.model.User
import com.example.moviverse.model.Watching
import com.example.moviverse.utils.CenterZoomLayoutManager
import com.example.moviverse.utils.Constant.Companion.IMAGE_URL
import com.example.moviverse.viewmodel.DetailsViewModel
import com.example.moviverse.viewmodel.home.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import eightbitlab.com.blurview.RenderScriptBlur


class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private val detailsViewModel: DetailsViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    //Adapters
    private lateinit var adapter: MovieAdapter

    //Firebase
    private lateinit var watchingReference: DatabaseReference
    private lateinit var watchingListener: ValueEventListener
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var listener: ValueEventListener
    private lateinit var userPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        Log.e("createView", "hello")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        userPreferences = activity?.getSharedPreferences("WATCHING_STATE", Context.MODE_PRIVATE)!!

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.getPopular(1)

        //Init Rec Views and Adapters
        initPopularMoviesRecView()

        return binding.root
    }

    private fun initPopularMoviesRecView() {
        adapter = MovieAdapter(ArrayList(), onClick = { movie, poster, title  ->
            detailsViewModel.getDetails(movie.id)
            val extras = FragmentNavigatorExtras(
                poster to "movie_poster_${movie.id}_0",
                title to "movie_title_${movie.id}_0"
            )
            findNavController().navigate(
                R.id.discoveryToDetails,
                null,
                null,
                extras
            )
        }, 0)

        binding.moviesRecView.adapter = adapter
        binding.moviesRecView.apply {
            postponeEnterTransition()
            viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
        }

        val layoutManager = CenterZoomLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.moviesRecView.layoutManager = layoutManager

        binding.moviesRecView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(adapter.moviesList.size == 0)
                    return
                val offset: Int = binding.moviesRecView.computeHorizontalScrollOffset()
                var position: Float =
                    offset.toFloat() / (binding.moviesRecView.getChildAt(0).measuredWidth).toFloat()
                position += 0.5f
                val postInt: Int = position.toInt()
                val positionIndex = postInt + 1
                val listSize = adapter.moviesList.size
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
        Log.e("viewCreated", "hello")

        viewModel.popular.observe(viewLifecycleOwner){ response->
            Log.e("observe","popularMovies")
            if(response.isSuccessful){
                adapter.moviesList.clear()
                response.body()?.results?.let {
                    if(it.size > 0){
                        adapter.moviesList.addAll(it)
                        adapter.moviesList.add(0, Movie())
                        adapter.moviesList.add(Movie())
                        binding.popularNoMoviesFound.visibility = View.GONE
                    }
                }
                adapter.notifyDataSetChanged()
                Log.e("popularMovies","${adapter.moviesList.size}")
            }
            else{
                Snackbar.make(binding.root, "${response.errorBody()?.string()}", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStart() {
        Log.e("onStart", "hello")
        super.onStart()
        val user = auth.currentUser
        if(user != null)
        {
            addProfileListener(user)
            addWatchingListener(user)
        }
        else{
            updateGuestWatchingUI()
        }
    }

    private fun updateGuestWatchingUI() {
        Log.e("GuestListener", "hello")
        val vidId = userPreferences.getString("vidId", null)
        val id = userPreferences.getInt("id", -1)
        val movieId = userPreferences.getInt("movie_id", -1)
        val tvId = userPreferences.getInt("tv_id", -1)
        val type = userPreferences.getString("type", "")
        val backdropPath = userPreferences.getString("backdropPath", "")
        if(vidId != null){
            if(_binding != null){
                context?.let {
                    Glide.with(it)
                        .asBitmap()
                        .placeholder(R.drawable.ic_movie_icon)
                        .error(R.drawable.ic_movie_error)
                        .load("$IMAGE_URL/original${backdropPath}")
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(it))))
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
                                Handler(Looper.myLooper()!!).postDelayed(
                                    {
                                        if (_binding != null) {
                                            val radius = 1f
                                            binding.blurView.outlineProvider =
                                                ViewOutlineProvider.BACKGROUND
                                            binding.blurView.clipToOutline = true
                                            binding.blurView.setupWith(binding.mainImgLayout, RenderScriptBlur(context))
                                                .setBlurRadius(radius)
                                                .setBlurAutoUpdate(true)
                                        }
                                    }, 500
                                )
                                return false
                            }
                        })
                        .into(binding.mainImg)
                }
                binding.mainImgLayout.setOnClickListener {
                    if(type == "movie"){
                        binding.mainImg.transitionName = "movie_poster_${id}_10"
                        detailsViewModel.getDetails(id)
                        val extras = FragmentNavigatorExtras(
                            binding.mainImg to "movie_poster_${id}_10"
                        )
                        findNavController().navigate(
                            BottomNavDirections.discoveryToDetails(10),
                            extras
                        )
                    }
                    else if(type == "tv"){
                        binding.mainImg.transitionName = "tv_poster_${id}_10"
                        detailsViewModel.getTvShowsDetails(id)
                        val extras = FragmentNavigatorExtras(
                            binding.mainImg to "tv_poster_${id}_10"
                        )
                        findNavController().navigate(
                            BottomNavDirections.toTvDetails(10),
                            extras
                        )
                    }
                }
                binding.watchingTxt.text = getString(R.string.continue_watching)
            }
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
                if (userProfile != null && _binding != null) {
                    binding.greetingText.text = "Hello ${userProfile.name},"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Something is wrong", Toast.LENGTH_SHORT).show()
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
                    if(_binding != null){
                        updateWatchingUI(watchingState)
                    }
                }
                else{
                    binding.mainImgLayout.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        watchingReference.addValueEventListener(watchingListener)
    }

    private fun updateWatchingUI(watchingState: Watching) {
        context?.let {
            Glide.with(it)
                .asBitmap()
                .placeholder(R.drawable.ic_movie_icon)
                .error(R.drawable.ic_movie_error)
                .load("$IMAGE_URL/original${watchingState.backdropPath}")
                .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(it))))
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
                        Handler(Looper.myLooper()!!).postDelayed(
                            {
                                if (_binding != null) {
                                    val radius = 1f
                                    binding.blurView.outlineProvider =
                                        ViewOutlineProvider.BACKGROUND
                                    binding.blurView.clipToOutline = true
                                    binding.blurView.setupWith(binding.mainImgLayout, RenderScriptBlur(context))
                                        .setBlurRadius(radius)
                                        .setBlurAutoUpdate(true)
                                }
                            }, 500
                        )
                        return false
                    }
                })
                .into(binding.mainImg)
        }
        binding.mainImgLayout.setOnClickListener {
            if(watchingState.type == "movie"){
                binding.mainImg.transitionName = "movie_poster_${watchingState.id}_10"
                detailsViewModel.getDetails(watchingState.id)
                val extras = FragmentNavigatorExtras(
                    binding.mainImg to "movie_poster_${watchingState.id}_10"
                )
                findNavController().navigate(
                    BottomNavDirections.discoveryToDetails(10),
                    extras
                )
            }
            else if(watchingState.type == "tv"){
                binding.mainImg.transitionName = "tv_poster_${watchingState.id}_10"
                detailsViewModel.getTvShowsDetails(watchingState.id)
                val extras = FragmentNavigatorExtras(
                    binding.mainImg to "tv_poster_${watchingState.id}_10"
                )
                findNavController().navigate(
                    BottomNavDirections.toTvDetails(10),
                    extras
                )
            }
        }
        binding.watchingTxt.text = getString(R.string.continue_watching)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        auth.currentUser?.uid?.let {
            reference.child(it).removeEventListener(listener)
            watchingReference.removeEventListener(watchingListener)
        }
    }

    private fun dipToPx(context: Context): Int {
        return (36f * context.resources.displayMetrics.density).toInt()
    }
}