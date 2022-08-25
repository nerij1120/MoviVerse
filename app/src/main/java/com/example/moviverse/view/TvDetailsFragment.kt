package com.example.moviverse.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.moviverse.BottomNavDirections
import com.example.moviverse.R
import com.example.moviverse.adapter.CreditAdapter
import com.example.moviverse.adapter.GenreAdapter
import com.example.moviverse.databinding.FragmentDetailsBinding
import com.example.moviverse.model.TvShows
import com.example.moviverse.model.Watching
import com.example.moviverse.utils.Constant
import com.example.moviverse.view.activity.LoginActivity
import com.example.moviverse.viewmodel.DetailsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.DefaultPlayerUiController
import eightbitlab.com.blurview.RenderScriptBlur
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class TvDetailsFragment : Fragment() {
    private val viewModel: DetailsViewModel by activityViewModels()
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: DetailsFragmentArgs by navArgs()

    private lateinit var creditsAdapter: CreditAdapter
    private lateinit var genreAdapter: GenreAdapter

    private lateinit var scaleAnimation: ScaleAnimation
    private lateinit var bounceInterpolator: BounceInterpolator

    private lateinit var youTubePlayerTracker: YouTubePlayerTracker

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var watchingReference: DatabaseReference
    private lateinit var recommendReference: DatabaseReference
    private lateinit var watchingListener: ValueEventListener
    private lateinit var mListener: ValueEventListener

    private lateinit var userPreferences: SharedPreferences

    private var videoId = ""
    private var isPlaying = true
    private var hasVideo = false
    private var isShowing = false
    private var isWatching = false
    private var isNavigate = false

    private var tvShowsItem: TvShows? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAnimation()
    }

    private fun initAnimation() {
        val animation = TransitionInflater.from(context).inflateTransition(
            android.R.transition.move
        )

        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        scaleAnimation = ScaleAnimation(
            0.7f,
            1.0f,
            0.7f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.7f,
            Animation.RELATIVE_TO_SELF,
            0.7f
        )
        scaleAnimation.duration = 800
        bounceInterpolator = BounceInterpolator()
        scaleAnimation.interpolator = bounceInterpolator
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        userPreferences = activity?.getSharedPreferences("WATCHING_STATE", Context.MODE_PRIVATE)!!

        initCreditRecView()

        genreAdapter = GenreAdapter(ArrayList())
        binding.genreLayout.adapter = genreAdapter
        val lM = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.genreLayout.layoutManager = lM

        youTubePlayerTracker = YouTubePlayerTracker()

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.favoritesIc.setOnClickListener { v->
            val user = auth.currentUser
            if(user != null){
                v.startAnimation(scaleAnimation)
                reference = FirebaseDatabase.getInstance().getReference("Favorites").child(user.uid)
                if(binding.favoritesIc.isChecked){
                    addTvShowToFavorites()
                }
                else{
                    removeTvShowFromFavorites()
                }
            }else{
                binding.favoritesIc.isChecked = false
                Snackbar.make(v, "Please Login to add to your favorites", Snackbar.LENGTH_SHORT)
                    .setAction("Login"){
                        startActivity(Intent(activity, LoginActivity::class.java))
                    }
                    .show()
            }
        }

        binding.imageLayout.setOnClickListener {
            binding.youtubePlayer.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    if(hasVideo){
                        binding.imageLayout.visibility = View.GONE
                        binding.youtubePlayer.visibility = View.VISIBLE
                        youTubePlayer.loadOrCueVideo(
                            viewLifecycleOwner.lifecycle,
                            videoId,
                            0f
                        )
                        isWatching = true
                    }else{
                        binding.imageLayout.visibility = View.INVISIBLE
                        binding.noVideoImg.visibility = View.VISIBLE
                        binding.noVideoTxt.visibility = View.VISIBLE
                    }
                }
            })
        }

        binding.dropdownLayout.setOnClickListener {
            if(isShowing){
                isShowing = false
                binding.dropdownIcon.setImageDrawable(context?.let { it1 ->
                    AppCompatResources.getDrawable(
                        it1, R.drawable.ic_baseline_arrow_drop_down_24
                    )
                })
                binding.overviewTxt.maxLines = 3
            }
            else{
                isShowing = true
                binding.dropdownIcon.setImageDrawable(context?.let { it1 ->
                    AppCompatResources.getDrawable(
                        it1, R.drawable.ic_baseline_arrow_drop_up_24
                    )
                })
                binding.overviewTxt.maxLines = Int.MAX_VALUE
            }
        }

        binding.moreTxt.setOnClickListener {
            savePlayingState()
            findNavController().navigate(
                R.id.action_tvDetailsFragment_to_tvCreditFragment
            )
        }

        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayer)

        val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                // using pre-made custom ui
                val defaultPlayerUiController =
                    DefaultPlayerUiController(binding.youtubePlayer, youTubePlayer)
                binding.youtubePlayer.setCustomPlayerUi(defaultPlayerUiController.rootView)
                youTubePlayer.addListener(youTubePlayerTracker)
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)
                if(state == PlayerConstants.PlayerState.PLAYING)
                {
                    isWatching = true
                }
//                if(state == PlayerConstants.PlayerState.PLAYING && !isPlaying){
//                    youTubePlayer.pause()
//                    isPlaying = true
//                }
            }
        }

        // disable iframe ui
        val options: IFramePlayerOptions = IFramePlayerOptions.Builder().controls(0).build()
        binding.youtubePlayer.initialize(listener, true,  options)

        return binding.root
    }

    private fun initCreditRecView() {
        creditsAdapter = CreditAdapter(ArrayList(), onClick = { person, poster, title ->
            viewModel.getPersonDetails(person.id)
            val extras = FragmentNavigatorExtras(
                poster to "person_poster_${person.id}_1",
                title to "person_name_${person.id}_1"
            )
            savePlayingState()
            findNavController().navigate(
                BottomNavDirections.toDetails(1),
                extras
            )
        }, 1)
        binding.creditsRecView.adapter = creditsAdapter
        binding.creditsRecView.apply {
            postponeEnterTransition()
            viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
        }
        val layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        binding.creditsRecView.layoutManager = layoutManager
    }

    private fun savePlayingState() {
        if (youTubePlayerTracker.state == PlayerConstants.PlayerState.PLAYING)
            isPlaying = true
        else if (youTubePlayerTracker.state == PlayerConstants.PlayerState.PAUSED)
            isPlaying = false
    }

    private fun removeTvShowFromFavorites() {
        reference
            .child("tvShows")
            .child(tvShowsItem?.id.toString())
            .removeValue()
            .addOnSuccessListener {
                Snackbar.make(
                    binding.favoritesIc,
                    "Removed from your \"Favorites\" list",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                it.localizedMessage?.let { it1 ->
                    Snackbar.make(binding.favoritesIc, it1, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun addTvShowToFavorites() {
        val fav = TvShows()
        fav.id = tvShowsItem?.id!!
        fav.name = tvShowsItem?.name.toString()
        fav.posterPath = tvShowsItem?.posterPath.toString()
        fav.firstAirDate = tvShowsItem?.firstAirDate.toString()
        reference
            .child("tvShows")
            .child(tvShowsItem?.id.toString())
            .setValue(fav)
            .addOnSuccessListener {
                Snackbar.make(
                    binding.favoritesIc,
                    "Added to your \"Favorites\" list",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
            .addOnFailureListener {
                it.localizedMessage?.let { it1 ->
                    Snackbar.make(binding.favoritesIc, it1, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tvDetails.observe(viewLifecycleOwner){
            if (it.isSuccessful){
                val movie = it.body()
                tvShowsItem = movie

                Log.e("observer", "${movie?.name}")
                val fragmentNumber = args.fragmentNumber

                val user = auth.currentUser
                if(user != null){
//                    reference.removeEventListener(mListener)
//                    watchingReference.removeEventListener(watchingListener)
//                    Log.e("remove", "listener")

                    addListener(tvShowsItem, user)
                    if(fragmentNumber == 10)
                        addWatchingListener(tvShowsItem, user)
                }
                else{
                    if(fragmentNumber == 10)
                        loadGuestWatchingState(tvShowsItem)
                }

                binding.backdropImage.transitionName = "tv_poster_${movie?.id}_$fragmentNumber"
                binding.movieTitle.transitionName = "tv_title_${movie?.id}_$fragmentNumber"
                binding.ratingTxt.transitionName = "tv_rating_${movie?.id}_$fragmentNumber"

                hasVideo = false
                val videos = movie?.videos?.results
                if (videos != null && videos.size != 0) {
                    for(vid in videos){
                        if(vid.type == "Trailer"){
                            hasVideo = true
                            videoId = vid.key
                            break
                        }
                    }
                }

                context?.let { it1 ->
                    Glide.with(it1)
                        .asBitmap()
                        .placeholder(R.drawable.ic_movie_icon)
                        .error(R.drawable.ic_movie_error)
                        .load("${Constant.IMAGE_URL}/original${movie?.backdropPath}")
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(it1))))
                        .listener( object : RequestListener<Bitmap> {
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
                                        if(_binding != null){
                                            binding.backBtnBlurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                                            binding.backBtnBlurView.clipToOutline = true
                                            binding.backBtnBlurView.setupWith(binding.imageLayout, RenderScriptBlur(context))
                                                .setBlurRadius(1f)
                                                .setBlurAutoUpdate(true)
                                            binding.playBtnBlurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                                            binding.playBtnBlurView.clipToOutline = true
                                            binding.playBtnBlurView.setupWith(binding.imageLayout,
                                                RenderScriptBlur(context)
                                            )
                                                .setBlurRadius(1f)
                                                .setBlurAutoUpdate(true)
                                        }
                                    },500
                                )
                                return false
                            }
                        })
                        .into(binding.backdropImage)
                }
                binding.timeTxt.text = "${movie?.noEp} episode(s), ${movie?.noSeason} season(s)"
                binding.ratingTxt.text = "${movie?.voteAverage} (TMdb)"
                binding.movieTitle.text = movie?.name

                val date = movie?.firstAirDate
                binding.releaseDateHeader.text = getString(R.string.first_air_date)
                val format = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                val fm = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                try {
                    binding.releaseDateTxt.text = date?.let { it1 -> fm.parse(it1)?.let { t2 -> format.format(t2) } }
                }
                catch (e: ParseException){
                    e.printStackTrace()
                }

                binding.overviewTxt.maxLines = Int.MAX_VALUE
                binding.overviewTxt.text = movie?.overview
                Log.e("lines_before", "${binding.overviewTxt.lineCount}")
                if(isNavigate){
                    Handler(Looper.myLooper()!!).post {
                        handleOverviewLayout()
                    }
                }
                else{
                    handleOverviewLayout()
                }

                val credits = movie?.credits
                val cast = credits?.cast
                val crew = credits?.crew
                creditsAdapter.personList.clear()
                if(cast == null && crew == null){
                    binding.noCastFound.visibility = View.VISIBLE
                    binding.moreTxt.visibility = View.GONE
                }
                else if(cast?.size == 0 && crew?.size == 0){
                    binding.noCastFound.visibility = View.VISIBLE
                    binding.moreTxt.visibility = View.GONE
                }
                else if(cast?.size!! >= 3){
                    creditsAdapter.personList.add(cast[0])
                    creditsAdapter.personList.add(cast[1])
                    creditsAdapter.personList.add(cast[2])
                }
                else if(cast.size + crew?.size!! >= 3){
                    creditsAdapter.personList.addAll(cast)
                    var i = 0
                    while (creditsAdapter.personList.size < 3){
                        creditsAdapter.personList.add(crew[i++])
                    }
                }
                else{
                    binding.moreTxt.visibility = View.GONE
                    creditsAdapter.personList.addAll(cast)
                    creditsAdapter.personList.addAll(crew)
                }
                creditsAdapter.notifyDataSetChanged()

                val genres = movie?.genres
                genreAdapter.genreList.clear()
                if (genres != null) {
                    genreAdapter.genreList.addAll(genres)
                    genreAdapter.notifyDataSetChanged()
                }
            }
            else
            {
                Toast.makeText(context, it.errorBody()?.string(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleOverviewLayout() {
        Log.e("lines_after", "${binding.overviewTxt.lineCount}")
        if (binding.overviewTxt.lineCount > 3) {
            binding.dropdownLayout.visibility = View.VISIBLE
            binding.overviewTxt.maxLines = 3
        } else {
            binding.dropdownLayout.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        if(user != null){
            addListener(tvShowsItem, user)
            addWatchingListener(tvShowsItem, user)
        }
        else{
            loadGuestWatchingState(tvShowsItem)
        }
    }

    override fun onStop() {
        super.onStop()
        if(auth.currentUser != null)
        {
            reference.removeEventListener(mListener)
            watchingReference.removeEventListener(watchingListener)
            Log.e("remove", "listener")
        }
        isNavigate = true
        saveWatchingState()
    }

    private fun saveWatchingState() {
        if (isWatching) {
            val user = auth.currentUser
            val currentSecond = youTubePlayerTracker.currentSecond
            val vidId = youTubePlayerTracker.videoId
            if (vidId != null) {
                val watching = tvShowsItem?.let {
                    Watching(it.id, "tv", currentSecond, vidId, it.backdropPath)
                }
                if (user != null) {
                    watchingReference =
                        FirebaseDatabase.getInstance()
                            .getReference("Watching")
                            .child(user.uid)
                    watchingReference.setValue(watching)
                        .addOnSuccessListener {
                            Log.e("Log", "Save watching state successfully")
                            recommendReference =
                                FirebaseDatabase.getInstance()
                                    .getReference("Recommend")
                                    .child(user.uid)
                                    .child("tv")

                            recommendReference.setValue(watching)
                                .addOnSuccessListener {
                                    Log.e("Log", "Save Recommend TV")
                                }
                        }
                        .addOnFailureListener {
                            it.localizedMessage?.let { it1 -> Log.e("Log", it1) }
                        }
                } else {
                    // Guest LOGIN
                    val editor = userPreferences.edit()
                    watching?.time?.let { editor.putFloat("currentSecond", it) }
                    editor.putString("vidId", watching?.vidId)
                    watching?.id?.let {
                        editor.putInt("id", it)
                        editor.putInt("tv_id", it)
                    }
                    editor.putString("type", "tv")
                    editor.putString("backdropPath", watching?.backdropPath)
                    editor.apply()
                    Log.e("Log", "Save guest watching state successfully")
                }
            }
        }
    }

    private fun addWatchingListener(movieItem: TvShows?, user: FirebaseUser) {
        Log.e("watchingListener", "hello")
        watchingReference =
            FirebaseDatabase.getInstance()
                .getReference("Watching")
                .child(user.uid)
        watchingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val watchingState = snapshot.getValue(Watching::class.java)
                if(watchingState != null){
                    if(movieItem?.id == watchingState.id && watchingState.type == "tv"){
                        val fragmentNumber = args.fragmentNumber
                        if(fragmentNumber == 10 || (fragmentNumber != 10 && isWatching)){
                            if(_binding != null){
                                binding.imageLayout.visibility = View.GONE
                                binding.youtubePlayer.getYouTubePlayerWhenReady(object :
                                    YouTubePlayerCallback {
                                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                        binding.youtubePlayer.visibility = View.VISIBLE
                                        if(isPlaying)
                                            youTubePlayer.loadOrCueVideo(
                                                viewLifecycleOwner.lifecycle,
                                                watchingState.vidId,
                                                watchingState.time
                                            )
                                        else
                                            youTubePlayer.cueVideo(
                                                watchingState.vidId,
                                                watchingState.time
                                            )
                                        isWatching = true
                                    }
                                })
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        watchingReference.addValueEventListener(watchingListener)
    }

    private fun addListener(movie: TvShows?, user: FirebaseUser) {
        Log.e("favoritesListener", "hello")
        reference = FirebaseDatabase.getInstance().getReference("Favorites").child(user.uid)
        mListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding != null)
                    binding.favoritesIc.isChecked = snapshot.value != null
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        reference
            .child("tvShows")
            .child(movie?.id.toString())
            .addValueEventListener(mListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadGuestWatchingState(movieItem: TvShows?) {
        Log.e("GuestListener", "hello")
        val vidId = userPreferences.getString("vidId", null)
        val id = userPreferences.getInt("id", -1)
        val currentSecond = userPreferences.getFloat("currentSecond", 0f)
        val type = userPreferences.getString("type", "")
        if(vidId != null && id == movieItem?.id && type == "tv"){
            val fragmentNumber = args.fragmentNumber
            if(fragmentNumber == 10 || (fragmentNumber != 10 && isWatching)){
                if(_binding != null){
                    binding.imageLayout.visibility = View.GONE
                    binding.youtubePlayer.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                            binding.youtubePlayer.visibility = View.VISIBLE
                            if(isPlaying)
                                youTubePlayer.loadOrCueVideo(
                                    viewLifecycleOwner.lifecycle,
                                    vidId,
                                    currentSecond
                                )
                            else
                                youTubePlayer.cueVideo(
                                    vidId,
                                    currentSecond
                                )
                            isWatching = true
                        }
                    })
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("videoId", youTubePlayerTracker.videoId)
        outState.putFloat("current_second", youTubePlayerTracker.currentSecond)
        outState.putBoolean("hasVideo", hasVideo)
        if(youTubePlayerTracker.state == PlayerConstants.PlayerState.PLAYING){
            outState.putBoolean("isPlaying", true)
        }
        else
            outState.putBoolean("isPlaying", false)
        Log.e("hello", "save instance")
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.e("hello", "restore state")
        val vidId = savedInstanceState?.getString("videoId")
        val hasVideo = savedInstanceState?.getBoolean("hasVideo")
        if(vidId != null){
            val currentSecond = savedInstanceState.getFloat("current_second")
            isPlaying = savedInstanceState.getBoolean("isPlaying")
            binding.youtubePlayer.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    if(hasVideo == true){
                        binding.imageLayout.visibility = View.GONE
                        binding.youtubePlayer.visibility = View.VISIBLE
                        youTubePlayer.loadOrCueVideo(
                            viewLifecycleOwner.lifecycle,
                            vidId,
                            currentSecond
                        )
                    }
                    else{
                        binding.imageLayout.visibility = View.INVISIBLE
                        binding.noVideoImg.visibility = View.VISIBLE
                        binding.noVideoTxt.visibility = View.VISIBLE
                    }
                }
            })
        }
        Log.e("videoId","${savedInstanceState?.getString("videoId")}")
        Log.e("current_second","${savedInstanceState?.getFloat("current_second")}")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            binding.youtubePlayer.enterFullScreen()
        }
        else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            binding.youtubePlayer.exitFullScreen()
        }
    }

    private fun dipToPx(context: Context): Int {
        return (1f * context.resources.displayMetrics.density).toInt()
    }

}