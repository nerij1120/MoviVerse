package com.example.moviverse.viewmodel.discovery

import androidx.lifecycle.ViewModel
import com.example.moviverse.repository.DiscoveryRepository

abstract class DiscoveryViewModel: ViewModel() {
    protected val repository = DiscoveryRepository()

    abstract fun getMovies(page: Int)
}