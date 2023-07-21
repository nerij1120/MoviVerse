package com.example.movieapp.model

import com.example.movieapp.response.VideosResponse
import com.google.gson.annotations.SerializedName

class Movie: Item() {
    var id: Int = -1
    var title: String = ""
    @SerializedName("vote_average")
    val voteAverage: Double = 0.0
    @SerializedName("poster_path")
    var posterPath: String = ""
    @SerializedName("backdrop_path")
    val backdropPath: String = ""
    val runtime: Int = 0
    @SerializedName("release_date")
    var releaseDate: String = ""
    val genres: ArrayList<Genre>? = null
    val overview: String = ""
    val videos: VideosResponse? = null
    val credits: Credits? = null
}
