package com.example.movieapp.viewmodel.discovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movieapp.response.MoviesList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AnimationViewModel: DiscoveryViewModel() {
    private var _myResponse: MutableLiveData<Response<MoviesList>> = MutableLiveData()
    val myResponse: LiveData<Response<MoviesList>> get() = _myResponse
    private val errorSharedFlow = MutableSharedFlow<String>()
    val errorFlow = errorSharedFlow.asSharedFlow()

    override fun getMovies(page: Int){
        viewModelScope.launch {
            try {
                val response = repository.getAnimation(page)
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