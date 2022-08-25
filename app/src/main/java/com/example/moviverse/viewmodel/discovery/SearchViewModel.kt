package com.example.moviverse.viewmodel.discovery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviverse.repository.SearchRepository
import com.example.moviverse.response.SearchList
import kotlinx.coroutines.launch
import retrofit2.Response

class SearchViewModel: ViewModel() {
    private val repository = SearchRepository()
    private val _searchList: MutableLiveData<Response<SearchList>> = MutableLiveData()
    val searchList : LiveData<Response<SearchList>> get() = _searchList

    fun getSearchResults(query: String, page: Int){
        viewModelScope.launch {
            val response = repository.getSearchResults(query, page)
            _searchList.value = response
        }
    }
}