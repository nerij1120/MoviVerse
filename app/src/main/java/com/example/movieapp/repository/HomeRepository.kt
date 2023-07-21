package com.example.movieapp.repository

import com.example.movieapp.response.MoviesList
import com.example.movieapp.response.TvShowsList
import retrofit2.Response

class HomeRepository: BaseRepository() {
    suspend fun getPopular(page: Int): Response<MoviesList>{
        return api.getPopular(page)
    }

    suspend fun getPopularTv(page: Int): Response<TvShowsList>{
        return api.getTvShows(page)
    }

    suspend fun getMoviesRecommendations(id: Int): Response<MoviesList>{
        return api.getMoviesRecommendations(id)
    }

    suspend fun getTVRecommendations(id: Int): Response<TvShowsList>{
        return api.getTVRecommendations(id)
    }
}