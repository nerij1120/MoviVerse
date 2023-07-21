package com.example.movieapp.view.profile

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.movieapp.R
import com.example.movieapp.adapter.TvShowsAdapter
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentFavoritesTvShowsBinding
import com.example.movieapp.model.TvShows
import com.example.movieapp.view.activity.DetailsActivity
import com.example.movieapp.view.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class FavoritesTvShowsFragment : BaseFragment<FragmentFavoritesTvShowsBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFavoritesTvShowsBinding
        get() = {
            layoutInflater, viewGroup, b ->
            FragmentFavoritesTvShowsBinding.inflate(layoutInflater, viewGroup, b)
        }
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var adapter: TvShowsAdapter
    private lateinit var mListener: ValueEventListener
    private val moviesList = arrayListOf<TvShows>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = Firebase.auth
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setView() {
        adapter = TvShowsAdapter(moviesList){ movie->
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("id", movie.id)
            intent.putExtra("type", "tv")
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
        }

        binding.tvShowsRecView.adapter = adapter
        val layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.tvShowsRecView.layoutManager = layoutManager
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        if(user != null){
            reference = FirebaseDatabase.getInstance().getReference("Favorites").child(user.uid).child("tvShows")
            loadData()
        }else{
            binding.noMoviesLayout.visibility = View.VISIBLE
            binding.noMoviesTxt.text = getString(R.string.login_to_see_favorites)
        }
    }

    private fun loadData() {
        mListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                moviesList.clear()
                for (data in snapshot.children) {
                    val movie = data.getValue(TvShows::class.java)
                    if (movie != null) {
                        moviesList.add(movie)
                    }
                }
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                if (moviesList.isEmpty()) {
                    binding.noMoviesLayout.visibility = View.VISIBLE
                    binding.noMoviesTxt.text = getString(R.string.no_tvShows_favorites)
                } else {
                    binding.noMoviesLayout.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                (activity as MainActivity).displayError(error.message)
            }
        }
        reference.addValueEventListener(mListener)
    }

    override fun onStop() {
        super.onStop()
        if(auth.currentUser != null)
            reference.removeEventListener(mListener)
    }
}