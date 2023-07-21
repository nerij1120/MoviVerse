package com.example.movieapp.repository

import com.example.movieapp.response.SearchList
import retrofit2.Response

class SearchRepository: BaseRepository() {
    suspend fun getSearchResults(query: String, page: Int): Response<SearchList> {
        return api.getSearchResults(query, page)
    }
}