package com.example.movieapp.model

import com.google.gson.annotations.SerializedName

class Person {
    val id : Int = -1
    val name: String = ""
    @SerializedName("profile_path")
    val profilePath: String? = null
    @SerializedName("place_of_birth")
    val placeOfBirth: String? = null
    @SerializedName("known_for_department")
    val knownFor: String = ""
    val birthday: String? = null
    val deathDay: String? = null
    val biography: String = ""
}