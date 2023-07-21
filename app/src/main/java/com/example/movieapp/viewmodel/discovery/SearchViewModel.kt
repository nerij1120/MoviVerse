package com.example.movieapp.viewmodel.discovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.repository.SearchRepository
import com.example.movieapp.response.SearchList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SearchViewModel: ViewModel() {
    private val repository = SearchRepository()
    private val _searchList: MutableLiveData<Response<SearchList>> = MutableLiveData()
    val searchList : LiveData<Response<SearchList>> get() = _searchList
    private val errorSharedFlow = MutableSharedFlow<String>()
    val errorFlow = errorSharedFlow.asSharedFlow()

    fun getSearchResults(query: String, page: Int){
        viewModelScope.launch {
            try {
                val response = repository.getSearchResults(query, page)
                _searchList.value = response
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