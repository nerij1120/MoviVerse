package com.example.moviverse.repository

import com.example.moviverse.api.RetrofitInstance

abstract class BaseRepository {
    protected val api = RetrofitInstance.api

}