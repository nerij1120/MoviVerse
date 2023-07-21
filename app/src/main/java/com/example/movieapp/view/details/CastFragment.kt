package com.example.movieapp.view.details

import android.app.ActivityOptions
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.movieapp.adapter.CreditAdapter
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentCastBinding
import com.example.movieapp.model.Movie
import com.example.movieapp.model.Person
import com.example.movieapp.model.TvShows
import com.example.movieapp.view.activity.PersonActivity


class CastFragment<T>(private val item: T? = null) : BaseFragment<FragmentCastBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCastBinding
        get() = {
            inflater, view, b -> FragmentCastBinding.inflate(inflater, view, b)
        }
    private lateinit var adapter: CreditAdapter
    private val castList = arrayListOf<Person>()
    override fun setView() {
        when(item){
            is Movie ->{
                item.credits?.cast?.let { castList.addAll(it) }
            }
            is TvShows ->{
                item.credits?.cast?.let { castList.addAll(it) }
            }
        }
        adapter = CreditAdapter(castList){
            person ->
            val intent = Intent(requireContext(), PersonActivity::class.java)
            intent.putExtra("id", person.id)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
        }
        binding.castRecView.adapter = adapter
        binding.castRecView.layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
    }
}