package com.example.moviverse.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviverse.repository.HomeRepository
import com.example.moviverse.response.MoviesList
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeViewModel: ViewModel() {
    private val repository = HomeRepository()
    private val popularList: MutableLiveData<Response<MoviesList>> = MutableLiveData()
    val popular: LiveData<Response<MoviesList>> get() = popularList

    fun getPopular(page: Int){
        viewModelScope.launch {
            val response = repository.getPopular(page)
            popularList.value = response
        }
    }
}