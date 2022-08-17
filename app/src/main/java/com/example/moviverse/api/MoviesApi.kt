package com.example.moviverse.api

import com.example.moviverse.model.Movie
import com.example.moviverse.model.Person
import com.example.moviverse.model.TvShows
import com.example.moviverse.response.MoviesList
import com.example.moviverse.response.SearchList
import com.example.moviverse.response.TvShowsList
import com.example.moviverse.utils.Constant.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MoviesApi {
    @GET("movie/popular?api_key=${API_KEY}&language=en-US")
    suspend fun getPopular(@Query("page") page: Int): Response<MoviesList>

    @GET("movie/{id}?api_key=${API_KEY}&append_to_response=videos,credits")
    suspend fun getDetails(@Path("id") id: Int): Response<Movie>

    @GET("tv/{id}?api_key=${API_KEY}&append_to_response=videos,credits")
    suspend fun getTvShowsDetails(@Path("id") id: Int): Response<TvShows>

    @GET("person/{id}?api_key=${API_KEY}&append_to_response=credits&language=en-US")
    suspend fun getPersonDetails(@Path("id")id: Int) : Response<Person>

    @GET("tv/popular?api_key=${API_KEY}&language=en-US")
    suspend fun getTvShows(@Query("page") page: Int): Response<TvShowsList>

    @GET("discover/movie?api_key=${API_KEY}&language=en-US&with_genres=28")
    suspend fun getAction(@Query("page") page: Int): Response<MoviesList>

    @GET("discover/movie?api_key=${API_KEY}&language=en-US&with_genres=35")
    suspend fun getComedy(@Query("page") page: Int): Response<MoviesList>

    @GET("discover/movie?api_key=${API_KEY}&language=en-US&with_genres=27")
    suspend fun getHorror(@Query("page") page: Int): Response<MoviesList>

    @GET("discover/movie?api_key=${API_KEY}&language=en-US&with_genres=10749")
    suspend fun getRomance(@Query("page") page: Int): Response<MoviesList>

    @GET("discover/movie?api_key=${API_KEY}&language=en-US&with_genres=16")
    suspend fun getAnimation(@Query("page") page: Int): Response<MoviesList>

    @GET("search/multi?api_key=${API_KEY}&language=en-US")
    suspend fun getSearchResults(@Query("query") query: String, @Query("page") page:Int): Response<SearchList>
}