package com.example.movieapp.viewmodel.discovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movieapp.response.TvShowsList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class TvShowsViewModel: DiscoveryViewModel() {
    private var _myResponse: MutableLiveData<Response<TvShowsList>> = MutableLiveData()
    val myResponse: LiveData<Response<TvShowsList>> get() = _myResponse
    private val errorSharedFlow = MutableSharedFlow<String>()
    val errorFlow = errorSharedFlow.asSharedFlow()

    override fun getMovies(page: Int){
        viewModelScope.launch {
            try {
                val response = repository.getTvShows(page)
                _myResponse.value = response
            } catch (e: Exception) {
                when (e){
                    is SocketTimeoutException ->{
                        errorSharedFlow.emit("Please check your internet or the server is down")
                    }
                    is ConnectException, is UnknownHostException ->{
                        errorSharedFlow.emit("No Internet Connection")
                    }
                    else->{
                        errorSharedFlow.emit(e.localizedMessage?:"Unknown Error")
                    }
                }
            }
        }
    }
}