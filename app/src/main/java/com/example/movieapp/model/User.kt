package com.example.movieapp.model

data class User(
    val email: String = "",
    val name: String = "",
    val phone_number: String? = "",
    val photo_url: String? = ""
)