package com.example.movieapp.response

import com.example.movieapp.model.Videos

data class VideosResponse (
    val results: ArrayList<Videos> = ArrayList()
)