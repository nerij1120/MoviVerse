package com.example.movieapp.view.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.adapter.DetailsVPAdapter
import com.example.movieapp.base.BaseActivity
import com.example.movieapp.databinding.ActivityDetailsBinding
import com.example.movieapp.model.Item
import com.example.movieapp.model.Movie
import com.example.movieapp.model.TvShows
import com.example.movieapp.model.Watching
import com.example.movieapp.utils.Constant
import com.example.movieapp.viewmodel.DetailsViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import kotlinx.coroutines.flow.collectLatest

class DetailsActivity : BaseActivity<ActivityDetailsBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityDetailsBinding
        get() = {
            ActivityDetailsBinding.inflate(it)
        }
    private lateinit var adapter: DetailsVPAdapter<*>
    private val viewModel: DetailsViewModel by viewModels()
    private lateinit var youTubePlayerTracker: YouTubePlayerTracker
    private lateinit var youtubePlayer: YouTubePlayer
    private var videoId = ""
    private var isPlaying = true
    private var isWatching = false
    private var hasVideo = false

    private lateinit var userPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var watchingReference: DatabaseReference
    private lateinit var recommendReference: DatabaseReference
    private lateinit var mListener: ValueEventListener
    private lateinit var watchingListener: ValueEventListener

    private var item: Item? = null
    private var menu: Menu? = null

    private var continuePlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setSupportActionBar(binding.toolbar)
//        binding.toolbarLayout.title = title

        auth = Firebase.auth
        userPreferences = getSharedPreferences("WATCHING_STATE", MODE_PRIVATE)

        binding.fab.setOnClickListener {
            if (youTubePlayerTracker.state == PlayerConstants.PlayerState.PLAYING){
                youtubePlayer.pause()
            }else{
                if (!isWatching){
                    binding.youtubePlayer.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
                        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                            this@DetailsActivity.youtubePlayer = youTubePlayer
                            if(hasVideo){
                                binding.backdropImage.visibility = View.GONE
                                binding.youtubePlayer.visibility = View.VISIBLE
                                youTubePlayer.loadOrCueVideo(
                                    lifecycle,
                                    videoId,
                                    0f
                                )
                                isWatching = true
                            }else{
                                binding.youtubePlayer.visibility = View.GONE
                                binding.backdropImage.visibility = View.VISIBLE
                                displayError("This movie/tv show has no trailer!")
                            }
                        }
                    })
                }else{
                    youtubePlayer.play()
                    binding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause))
                    val drawable = binding.fab.drawable
                    if (drawable is AnimatedVectorDrawable){
                        drawable.start()
                    }else if (drawable is AnimatedVectorDrawableCompat){
                        drawable.start()
                    }
                }

            }
        }

        youTubePlayerTracker = YouTubePlayerTracker()
        lifecycle.addObserver(binding.youtubePlayer)

        val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                // using pre-made custom ui
                val defaultPlayerUiController =
                    DefaultPlayerUiController(binding.youtubePlayer, youTubePlayer)
                binding.youtubePlayer.setCustomPlayerUi(defaultPlayerUiController.rootView)
                youTubePlayer.addListener(youTubePlayerTracker)
                this@DetailsActivity.youtubePlayer = youTubePlayer
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                this@DetailsActivity.youtubePlayer = youTubePlayer
                super.onStateChange(youTubePlayer, state)
                if(state == PlayerConstants.PlayerState.PLAYING)
                {
                    isWatching = true
                    val params = binding.toolbarLayout.layoutParams as AppBarLayout.LayoutParams
                    params.scrollFlags = SCROLL_FLAG_NO_SCROLL
                    binding.toolbarLayout.layoutParams = params
                    binding.fab.setImageDrawable(ContextCompat.getDrawable(this@DetailsActivity, R.drawable.avd_play_to_pause))
                    val drawable = binding.fab.drawable
                    if (drawable is AnimatedVectorDrawable){
                        drawable.start()
                    }else if (drawable is AnimatedVectorDrawableCompat){
                        drawable.start()
                    }
                }else {
                    val params = binding.toolbarLayout.layoutParams as AppBarLayout.LayoutParams
                    params.scrollFlags = SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or SCROLL_FLAG_SCROLL
                    binding.toolbarLayout.layoutParams = params
                    binding.fab.setImageDrawable(ContextCompat.getDrawable(this@DetailsActivity, R.drawable.avd_pause_to_play))
                    val drawable = binding.fab.drawable
                    if (drawable is AnimatedVectorDrawable){
                        drawable.start()
                    }else if (drawable is AnimatedVectorDrawableCompat){
                        drawable.start()
                    }
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

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        menu = binding.toolbar.menu
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_favorites->{
                    val user = auth.currentUser
                    if (user != null){
                        reference = FirebaseDatabase.getInstance().getReference("Favorites").child(user.uid)
                        val i = item
                        if (i is Movie){
                            reference
                                .child("movies")
                                .child(i.id.toString())
                                .get()
                                .addOnCompleteListener {
                                    if (it.isSuccessful){
                                        val data = it.result.value == null
                                        if (data){
                                            menuItem.icon = ContextCompat.getDrawable(this, R.drawable.avd_favorites)
                                            val drawable = menuItem.icon
                                            if (drawable is AnimatedVectorDrawable){
                                                drawable.start()
                                            }else if (drawable is AnimatedVectorDrawableCompat){
                                                drawable.start()
                                            }
                                            val fav = Movie()
                                            fav.id = i.id
                                            fav.title = i.title
                                            fav.posterPath = i.posterPath
                                            fav.releaseDate = i.releaseDate
                                            reference
                                                .child("movies")
                                                .child(i.id.toString())
                                                .setValue(fav)
                                                .addOnSuccessListener {
                                                    displayMessage("Added to your \"Favorites\" list")
                                                }
                                                .addOnFailureListener {e->
                                                    e.localizedMessage?.let { it1 ->
                                                        displayError(it1)
                                                    }
                                                }

                                        }else{
                                            menuItem.icon = ContextCompat.getDrawable(this, R.drawable.avd_unfavorites)
                                            val drawable = menuItem.icon
                                            if (drawable is AnimatedVectorDrawable){
                                                drawable.start()
                                            }else if (drawable is AnimatedVectorDrawableCompat){
                                                drawable.start()
                                            }
                                            reference
                                                .child("movies")
                                                .child(i.id.toString())
                                                .removeValue()
                                                .addOnSuccessListener {
                                                    displayMessage("Removed from your \"Favorites\" list")
                                                }
                                                .addOnFailureListener {e->
                                                    e.localizedMessage?.let { it1 ->
                                                        displayError(it1)
                                                    }
                                                }
                                        }
                                    }
                                }
                        }else if (i is TvShows){
                            reference
                                .child("tvShows")
                                .child(i.id.toString())
                                .get()
                                .addOnCompleteListener {
                                    if (it.isSuccessful){
                                        val data = it.result.value == null
                                        if (data){
                                            menuItem.icon = ContextCompat.getDrawable(this, R.drawable.avd_favorites)
                                            val drawable = menuItem.icon
                                            if (drawable is AnimatedVectorDrawable){
                                                drawable.start()
                                            }else if (drawable is AnimatedVectorDrawableCompat){
                                                drawable.start()
                                            }
                                            val fav = TvShows()
                                            fav.id = i.id
                                            fav.name = i.name
                                            fav.posterPath = i.posterPath
                                            fav.firstAirDate = i.firstAirDate
                                            reference
                                                .child("tvShows")
                                                .child(i.id.toString())
                                                .setValue(fav)
                                                .addOnSuccessListener {
                                                    displayMessage("Added to your \"Favorites\" list")
                                                }
                                                .addOnFailureListener {e->
                                                    e.localizedMessage?.let { it1 ->
                                                        displayError(it1)
                                                    }
                                                }

                                        }else{
                                            menuItem.icon = ContextCompat.getDrawable(this, R.drawable.avd_unfavorites)
                                            val drawable = menuItem.icon
                                            if (drawable is AnimatedVectorDrawable){
                                                drawable.start()
                                            }else if (drawable is AnimatedVectorDrawableCompat){
                                                drawable.start()
                                            }
                                            reference
                                                .child("tvShows")
                                                .child(i.id.toString())
                                                .removeValue()
                                                .addOnSuccessListener {
                                                    displayMessage("Removed from your \"Favorites\" list")
                                                }
                                                .addOnFailureListener {e->
                                                    e.localizedMessage?.let { it1 ->
                                                        displayError(it1)
                                                    }
                                                }
                                        }
                                    }
                                }
                        }
                    }else{
                        displayMessage("Please Login to add to your favorites!", "Login"){
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                    }
                    true
                }

                else -> {
                    false
                }
            }
        }

        intent.extras?.apply {
            val id = getInt("id")
            when(getString("type")){
                "movie"->{
                    viewModel.getDetails(id)
                }
                "tv" -> {
                    viewModel.getTvShowsDetails(id)
                }
            }
            continuePlaying = getBoolean("continue_playing")
        }

        viewModel.details.observe(this){
            if (it.isSuccessful){
                this.item = it.body()
                val item = it.body()
                val user = auth.currentUser
                if(user != null){
//                    reference.removeEventListener(mListener)
//                    watchingReference.removeEventListener(watchingListener)
//                    Log.e("remove", "listener")

                    addListener(item, user)
                    if(continuePlaying){
                        addWatchingListener(item, user)
                    }
                }
                else{
                    if(continuePlaying){
                        loadGuestWatchingState(item)
                    }
                }
                adapter = DetailsVPAdapter(this, item)
                binding.content.viewPager.adapter = adapter
                binding.toolbarLayout.title = item?.title

                Glide.with(this)
                    .asBitmap()
                    .placeholder(R.drawable.ic_movie_icon)
                    .error(R.drawable.ic_movie_error)
                    .load("${Constant.IMAGE_URL}/original${item?.backdropPath}")
                    .into(binding.backdropImage)

                val listTitle  = listOf("Details", "Genres", "Cast")
                binding.content.tblLayout.let {
                    binding.content.viewPager.let { it1 ->
                        TabLayoutMediator(it, it1) { tab, position ->
                            tab.text = listTitle[position]
                        }.attach()
                    }
                }

                hasVideo = false
                val videos = item?.videos?.results
                if (videos != null && videos.size != 0) {
                    for(vid in videos){
                        if(vid.type == "Trailer"){
                            hasVideo = true
                            videoId = vid.key
                            break
                        }
                    }
                }
            }
        }

        viewModel.tvDetails.observe(this){
            if (it.isSuccessful){
                this.item = it.body()
                val item = it.body()
                val user = auth.currentUser
                if(user != null){
//                    reference.removeEventListener(mListener)
//                    watchingReference.removeEventListener(watchingListener)
//                    Log.e("remove", "listener")

                    addListener(item, user)
                    if(continuePlaying)
                        addWatchingListener(item, user)
                }
                else{
                    if(continuePlaying)
                        loadGuestWatchingState(item)
                }
                adapter = DetailsVPAdapter(this, item)
                binding.content.viewPager.adapter = adapter
                binding.toolbarLayout.title = item?.name

                Glide.with(this)
                    .asBitmap()
                    .placeholder(R.drawable.ic_movie_icon)
                    .error(R.drawable.ic_movie_error)
                    .load("${Constant.IMAGE_URL}/original${item?.backdropPath}")
                    .into(binding.backdropImage)

                val listTitle  = listOf("Details", "Genres", "Cast")
                binding.content.tblLayout.let {
                    binding.content.viewPager.let { it1 ->
                        TabLayoutMediator(it, it1) { tab, position ->
                            tab.text = listTitle[position]
                        }.attach()
                    }
                }

                hasVideo = false
                val videos = item?.videos?.results
                if (videos != null && videos.size != 0) {
                    for(vid in videos){
                        if(vid.type == "Trailer"){
                            hasVideo = true
                            videoId = vid.key
                            break
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.errorFlow.collectLatest {
                displayError(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if(user != null){
            addWatchingListener(item, user)
            addListener(item, user)
        }
        else{
            loadGuestWatchingState(item)
        }
    }

    override fun onStop() {
        super.onStop()
        if(auth.currentUser != null){
            reference.removeEventListener(mListener)
            watchingReference.removeEventListener(watchingListener)
        }
        updateWatchingState()
    }

    private fun updateWatchingState() {
        if (isWatching) {
            val user = auth.currentUser
            val currentSecond = youTubePlayerTracker.currentSecond
            val vidId = youTubePlayerTracker.videoId
            if (vidId != null) {
                item?.let {
                    if (it is Movie){
                        val watching = Watching(it.id, "movie", currentSecond, vidId, it.backdropPath)
                        if (user != null) {
                            watchingReference =
                                FirebaseDatabase.getInstance()
                                    .getReference("Watching")
                                    .child(user.uid)
                            watchingReference.setValue(watching)
                                .addOnSuccessListener {
                                    recommendReference =
                                        FirebaseDatabase.getInstance()
                                            .getReference("Recommend")
                                            .child(user.uid)
                                            .child("movie")

                                    recommendReference.setValue(watching)
                                }
                        } else {
                            // Guest LOGIN
                            val editor = userPreferences.edit()
                            editor.putFloat("currentSecond", watching.time)
                            editor.putString("vidId", watching.vidId)
                            editor.putInt("id", watching.id)
                            editor.putInt("movie_id", watching.id)
                            editor.putString("type", "movie")
                            editor.putString("backdropPath", watching.backdropPath)
                            editor.apply()
                        }
                    }else if (it is TvShows){
                        val watching = Watching(it.id, "tv", currentSecond, vidId, it.backdropPath)
                        if (user != null) {
                            watchingReference =
                                FirebaseDatabase.getInstance()
                                    .getReference("Watching")
                                    .child(user.uid)
                            watchingReference.setValue(watching)
                                .addOnSuccessListener {
                                    recommendReference =
                                        FirebaseDatabase.getInstance()
                                            .getReference("Recommend")
                                            .child(user.uid)
                                            .child("tv")

                                    recommendReference.setValue(watching)
                                }
                        } else {
                            // Guest LOGIN
                            val editor = userPreferences.edit()
                            editor.putFloat("currentSecond", watching.time)
                            editor.putString("vidId", watching.vidId)
                            editor.putInt("id", watching.id)
                            editor.putInt("tv_id", watching.id)
                            editor.putString("type", "tv")
                            editor.putString("backdropPath", watching.backdropPath)
                            editor.apply()
                        }
                    } else {
                        displayMessage("Not TV or Movie Item")
                    }
                }
            }
        }
    }

    private fun loadGuestWatchingState(item: Item?) {
        val vidId = userPreferences.getString("vidId", null)
        val id = userPreferences.getInt("id", -1)
        val currentSecond = userPreferences.getFloat("currentSecond", 0f)
        val type = userPreferences.getString("type", "")
        item?.let {
            if (it is Movie){
                if(vidId != null && id == it.id && type == "movie"){
                    if(continuePlaying || isWatching){
                        binding.backdropImage.visibility = View.GONE
                        binding.youtubePlayer.getYouTubePlayerWhenReady(object : YouTubePlayerCallback{
                            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                binding.youtubePlayer.visibility = View.VISIBLE
                                if(isPlaying)
                                    youTubePlayer.loadOrCueVideo(
                                        lifecycle,
                                        vidId,
                                        currentSecond
                                    )
                                else{
                                    youTubePlayer.cueVideo(
                                        vidId,
                                        currentSecond
                                    )
                                }
                                isWatching = true
                            }
                        })
                    }
                }
            }else if (it is TvShows){
                if(vidId != null && id == it.id && type == "tv"){
                    if(continuePlaying || isWatching){
                        binding.backdropImage.visibility = View.GONE
                        binding.youtubePlayer.getYouTubePlayerWhenReady(object : YouTubePlayerCallback{
                            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                binding.youtubePlayer.visibility = View.VISIBLE
                                if(isPlaying)
                                    youTubePlayer.loadOrCueVideo(
                                        lifecycle,
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

    }

    private fun addListener(item: Item?, user: FirebaseUser) {
        reference = FirebaseDatabase.getInstance().getReference("Favorites").child(user.uid)
        mListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null){
                    menu?.getItem(0)?.icon = ContextCompat.getDrawable(this@DetailsActivity, R.drawable.avd_favorites)
                }else{
                    menu?.getItem(0)?.icon = ContextCompat.getDrawable(this@DetailsActivity, R.drawable.avd_unfavorites)
                }
                val drawable = menu?.getItem(0)?.icon
                if (drawable is AnimatedVectorDrawable){
                    drawable.start()
                }else if (drawable is AnimatedVectorDrawableCompat){
                    drawable.start()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                displayError(error.message)
            }
        }
        if (item is Movie){
            reference
                .child("movies")
                .child(item.id.toString())
                .addValueEventListener(mListener)
        }else if (item is TvShows){
            reference
                .child("tvShows")
                .child(item.id.toString())
                .addValueEventListener(mListener)
        }
    }

    private fun addWatchingListener(item: Item?, user: FirebaseUser) {
        watchingReference =
            FirebaseDatabase.getInstance()
                .getReference("Watching")
                .child(user.uid)
        watchingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val watchingState = snapshot.getValue(Watching::class.java)
                if(watchingState != null){
                    if(item is Movie && item.id == watchingState.id && watchingState.type == "movie"){
                        if(continuePlaying ||  isWatching){
                            binding.backdropImage.visibility = View.GONE
                            binding.youtubePlayer.getYouTubePlayerWhenReady(object : YouTubePlayerCallback{
                                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                    binding.youtubePlayer.visibility = View.VISIBLE
                                    if(isPlaying)
                                        youTubePlayer.loadOrCueVideo(
                                            lifecycle,
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
                    }else if(item is TvShows && item.id == watchingState.id && watchingState.type == "tv") {
                        if (continuePlaying || isWatching) {
                            binding.backdropImage.visibility = View.GONE
                            binding.youtubePlayer.getYouTubePlayerWhenReady(object :
                                YouTubePlayerCallback {
                                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                    binding.youtubePlayer.visibility = View.VISIBLE
                                    if (isPlaying)
                                        youTubePlayer.loadOrCueVideo(
                                            lifecycle,
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
            override fun onCancelled(error: DatabaseError) {
                displayError(error.message)
            }
        }
        watchingReference.addValueEventListener(watchingListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("videoId", youTubePlayerTracker.videoId)
        outState.putFloat("current_second", youTubePlayerTracker.currentSecond)
        outState.putBoolean("hasVideo", hasVideo)
        if(youTubePlayerTracker.state == PlayerConstants.PlayerState.PLAYING)
            outState.putBoolean("isPlaying", true)
        else
            outState.putBoolean("isPlaying", false)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val vidId = savedInstanceState.getString("videoId")
        val hasVideo = savedInstanceState.getBoolean("hasVideo")
        if(vidId != null){
            val currentSecond = savedInstanceState.getFloat("current_second")
            isPlaying = savedInstanceState.getBoolean("isPlaying")
            binding.youtubePlayer.getYouTubePlayerWhenReady(object : YouTubePlayerCallback{
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    if(hasVideo){
                        binding.backdropImage.visibility = View.GONE
                        binding.youtubePlayer.visibility = View.VISIBLE
                        youTubePlayer.loadOrCueVideo(
                            lifecycle,
                            vidId,
                            currentSecond
                        )
                    }
                    else{
                        binding.backdropImage.visibility = View.VISIBLE
                        binding.youtubePlayer.visibility = View.GONE
                    }
                }
            })
        }
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
}