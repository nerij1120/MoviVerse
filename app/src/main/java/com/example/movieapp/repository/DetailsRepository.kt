package com.example.movieapp.repository

import com.example.movieapp.model.Movie
import com.example.movieapp.model.Person
import com.example.movieapp.model.TvShows
import retrofit2.Response

class DetailsRepository: BaseRepository() {
    suspend fun getDetails(id: Int): Response<Movie> {
        return api.getDetails(id)
    }

    suspend fun getTvShowsDetails(id: Int): Response<TvShows> {
        return api.getTvShowsDetails(id)
    }

    suspend fun getPersonDetails(id: Int): Response<Person>{
        return api.getPersonDetails(id)
    }
}