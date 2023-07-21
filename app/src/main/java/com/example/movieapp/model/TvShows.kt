package com.example.movieapp.model

import com.example.movieapp.response.VideosResponse
import com.google.gson.annotations.SerializedName

class TvShows: Item() {
    var id: Int = -1
    var name: String = ""
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0
    @SerializedName("poster_path")
    var posterPath: String = ""
    @SerializedName("backdrop_path")
    val backdropPath: String = ""
    @SerializedName("first_air_date")
    var firstAirDate: String = ""
    val genres: ArrayList<Genre>? = null
    val overview: String = ""
    val videos: VideosResponse? = null
    val credits: Credits? = null
    @SerializedName("number_of_episodes")
    val noEp: Int? = null
    @SerializedName("number_of_seasons")
    val noSeason: Int? = null
}