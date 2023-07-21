package com.example.movieapp.repository

import com.example.movieapp.api.RetrofitInstance

abstract class BaseRepository {
    protected val api = RetrofitInstance.api

}