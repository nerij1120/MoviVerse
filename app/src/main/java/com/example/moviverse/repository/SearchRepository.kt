package com.example.moviverse.repository

import com.example.moviverse.response.SearchList
import retrofit2.Response

class SearchRepository: BaseRepository() {
    suspend fun getSearchResults(query: String, page: Int): Response<SearchList> {
        return api.getSearchResults(query, page)
    }
}