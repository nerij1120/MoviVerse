package com.example.movieapp.model

import com.google.gson.annotations.SerializedName

class SearchResult {
    @SerializedName("poster_path")
    val posterPath: String? = null
    @SerializedName("profile_path")
    val profilePath: String? = null
    @SerializedName("release_date")
    val releaseDate: String = ""
    @SerializedName("first_air_date")
    val firstAirDate: String = ""
    @SerializedName("media_type")
    val type: String = ""
    val id: Int = -1
    val title: String = ""
    val name: String = ""
}