package com.example.movieapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.model.*
import com.example.movieapp.repository.DetailsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class DetailsViewModel: ViewModel() {
    private val repository = DetailsRepository()
    private val movieDetails: MutableLiveData<Response<Movie>> = MutableLiveData()
    val details: LiveData<Response<Movie>> get() = movieDetails
    private val tvShowsDetails: MutableLiveData<Response<TvShows>> = MutableLiveData()
    val tvDetails: LiveData<Response<TvShows>> get() = tvShowsDetails
    private val creditsDetails: MutableLiveData<Response<Person>> = MutableLiveData()
    val personDetails: LiveData<Response<Person>> get() = creditsDetails
    private val errorSharedFlow = MutableSharedFlow<String>()
    val errorFlow = errorSharedFlow.asSharedFlow()

    fun getDetails(id: Int){
        viewModelScope.launch {
            try {
                val response = repository.getDetails(id)
                movieDetails.value = response
            }catch (e: Exception){
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

    fun getTvShowsDetails(id: Int){
        viewModelScope.launch {
            try {
                val response = repository.getTvShowsDetails(id)
                tvShowsDetails.value = response
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

    fun getPersonDetails(id: Int){
        viewModelScope.launch {
            try {
                val response = repository.getPersonDetails(id)
                creditsDetails.value = response
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