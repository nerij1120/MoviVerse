package com.example.moviverse.repository

import com.example.moviverse.response.MoviesList
import retrofit2.Response

class HomeRepository: BaseRepository() {
    suspend fun getPopular(page: Int): Response<MoviesList> {
        return api.getPopular(page)
    }
}