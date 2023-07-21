package com.example.movieapp.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.repository.HomeRepository
import com.example.movieapp.response.MoviesList
import com.example.movieapp.response.TvShowsList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class HomeViewModel: ViewModel() {
    private val repository = HomeRepository()
    private val popularList: MutableLiveData<Response<MoviesList>> = MutableLiveData()
    val popular: LiveData<Response<MoviesList>> get() = popularList
    private val popularTVList: MutableLiveData<Response<TvShowsList>> = MutableLiveData()
    val tvPopular: LiveData<Response<TvShowsList>> get() = popularTVList
    private val recommendList: MutableLiveData<Response<MoviesList>> = MutableLiveData()
    val recommend: LiveData<Response<MoviesList>> get() = recommendList
    private val recommendTVList: MutableLiveData<Response<TvShowsList>> = MutableLiveData()
    val tvRecommend: LiveData<Response<TvShowsList>> get() = recommendTVList
    private val errorSharedFlow = MutableSharedFlow<String>()
    val errorFlow = errorSharedFlow.asSharedFlow()

    fun getPopular(page: Int){
        viewModelScope.launch {
            try {
                val response = repository.getPopular(page)
                popularList.value = response
            } catch (e: Exception) {
                when (e){
                    is SocketTimeoutException->{
                        errorSharedFlow.emit("Please check your internet or the server is down")
                    }
                    is ConnectException, is UnknownHostException->{
                        errorSharedFlow.emit("No Internet Connection")
                    }
                    else->{
                        errorSharedFlow.emit(e.localizedMessage?:"Unknown Error")
                    }
                }
            }
        }
    }

    fun getPopularTV(page: Int){
        viewModelScope.launch {
            try {
                val response = repository.getPopularTv(page)
                popularTVList.value = response
            } catch (e: Exception) {
                when (e){
                    is SocketTimeoutException->{
                        errorSharedFlow.emit("Please check your internet or the server is down")
                    }
                    is ConnectException, is UnknownHostException->{
                        errorSharedFlow.emit("No Internet Connection")
                    }
                    else->{
                        errorSharedFlow.emit(e.localizedMessage?:"Unknown Error")
                    }
                }            }
        }
    }

    fun getRecommendations(id: Int){
        viewModelScope.launch {
            try {
                val response = repository.getMoviesRecommendations(id)
                recommendList.value = response
            } catch (e: Exception) {
                when (e){
                    is SocketTimeoutException->{
                        errorSharedFlow.emit("Please check your internet or the server is down")
                    }
                    is ConnectException, is UnknownHostException->{
                        errorSharedFlow.emit("No Internet Connection")
                    }
                    else->{
                        errorSharedFlow.emit(e.localizedMessage?:"Unknown Error")
                    }
                }
            }
        }
    }

    fun getTVRecommendations(id: Int){
        viewModelScope.launch {
            try {
                val response = repository.getTVRecommendations(id)
                recommendTVList.value = response
            } catch (e: Exception) {
                when (e){
                    is SocketTimeoutException->{
                        errorSharedFlow.emit("Please check your internet or the server is down")
                    }
                    is ConnectException, is UnknownHostException->{
                        errorSharedFlow.emit("No Internet Connection")
                    }
                    else->{
                        errorSharedFlow.emit(e.localizedMessage?:"Unknown Error")
                    }
                }            }
        }
    }

}