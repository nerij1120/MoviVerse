package com.example.moviverse.viewmodel.discovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.moviverse.response.MoviesList
import kotlinx.coroutines.launch
import retrofit2.Response

class ActionViewModel: DiscoveryViewModel() {
    private var _myResponse: MutableLiveData<Response<MoviesList>> = MutableLiveData()
    val myResponse: LiveData<Response<MoviesList>> get() = _myResponse

    override fun getMovies(page: Int){
        viewModelScope.launch {
            val response = repository.getAction(page)
            _myResponse.value = response
        }
    }
}