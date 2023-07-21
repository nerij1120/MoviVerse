package com.example.movieapp.api

import com.example.movieapp.utils.Constant.Companion.API_KEY
import com.example.movieapp.model.Movie
import com.example.movieapp.model.Person
import com.example.movieapp.model.TvShows
import com.example.movieapp.response.MoviesList
import com.example.movieapp.response.SearchList
import com.example.movieapp.response.TvShowsList
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

    @GET("movie/{movie_id}/recommendations?api_key=${API_KEY}&language=en-US&page=1")
    suspend fun getMoviesRecommendations(@Path("movie_id") id: Int): Response<MoviesList>

//    @GET("movie/{movie_id}/similar?api_key=${API_KEY}&language=en-US&page=1")
//    suspend fun getSimilarMovies(@Path("movie_id") id: Int): Response<MoviesList>

    @GET("tv/{movie_id}/recommendations?api_key=${API_KEY}&language=en-US&page=1")
    suspend fun getTVRecommendations(@Path("movie_id") id: Int): Response<TvShowsList>

//    @GET("tv/{movie_id}/similar?api_key=${API_KEY}&language=en-US&page=1")
//    suspend fun getSimilarTV(@Path("movie_id") id: Int): Response<TvShowsList>
}