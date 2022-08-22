package com.example.moviverse.viewmodel.discovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.moviverse.response.TvShowsList
import kotlinx.coroutines.launch
import retrofit2.Response

class TvShowsViewModel: DiscoveryViewModel() {
    private var _myResponse: MutableLiveData<Response<TvShowsList>> = MutableLiveData()
    val myResponse: LiveData<Response<TvShowsList>> get() = _myResponse

    override fun getMovies(page: Int){
        viewModelScope.launch {
            val response = repository.getTvShows(page)
            _myResponse.value = response
        }
    }
}