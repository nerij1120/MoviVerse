package com.example.movieapp.repository

import com.example.movieapp.response.MoviesList
import com.example.movieapp.response.TvShowsList
import retrofit2.Response

class DiscoveryRepository: BaseRepository() {
    suspend fun getMovies(page: Int): Response<MoviesList>{
        return api.getPopular(page)
    }
    suspend fun getTvShows(page: Int): Response<TvShowsList>{
        return api.getTvShows(page)
    }
    suspend fun getAction(page: Int): Response<MoviesList>{
        return api.getAction(page)
    }
    suspend fun getComedy(page: Int): Response<MoviesList>{
        return api.getComedy(page)
    }
    suspend fun getHorror(page: Int): Response<MoviesList>{
        return api.getHorror(page)
    }
    suspend fun getRomance(page: Int): Response<MoviesList>{
        return api.getRomance(page)
    }
    suspend fun getAnimation(page: Int): Response<MoviesList>{
        return api.getAnimation(page)
    }
}