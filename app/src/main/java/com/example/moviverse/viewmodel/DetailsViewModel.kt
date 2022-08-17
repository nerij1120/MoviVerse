package com.example.moviverse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviverse.model.Movie
import com.example.moviverse.model.Person
import com.example.moviverse.model.TvShows
import com.example.moviverse.repository.DetailsRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class DetailsViewModel: ViewModel() {
    private val repository = DetailsRepository()
    private val movieDetails: MutableLiveData<Response<Movie>> = MutableLiveData()
    val details: LiveData<Response<Movie>> get() = movieDetails
    private val tvShowsDetails: MutableLiveData<Response<TvShows>> = MutableLiveData()
    val tvDetails: LiveData<Response<TvShows>> get() = tvShowsDetails
    private val creditsDetails: MutableLiveData<Response<Person>> = MutableLiveData()
    val personDetails: LiveData<Response<Person>> get() = creditsDetails

    fun getDetails(id: Int){
        viewModelScope.launch {
            val response = repository.getDetails(id)
            movieDetails.value = response
        }
    }

    fun getTvShowsDetails(id: Int){
        viewModelScope.launch {
            val response = repository.getTvShowsDetails(id)
            tvShowsDetails.value = response
        }
    }

    fun getPersonDetails(id: Int){
        viewModelScope.launch {
            val response = repository.getPersonDetails(id)
            creditsDetails.value = response
        }
    }
}