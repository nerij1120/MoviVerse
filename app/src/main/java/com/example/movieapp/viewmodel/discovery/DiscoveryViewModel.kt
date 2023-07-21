package com.example.movieapp.viewmodel.discovery

import androidx.lifecycle.ViewModel
import com.example.movieapp.repository.DiscoveryRepository

abstract class DiscoveryViewModel: ViewModel() {
    protected val repository = DiscoveryRepository()

    abstract fun getMovies(page: Int)
}