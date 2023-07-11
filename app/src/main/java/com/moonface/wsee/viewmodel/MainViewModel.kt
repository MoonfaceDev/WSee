package com.moonface.wsee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moonface.wsee.models.CreateReview
import com.moonface.wsee.models.CreateToilet
import com.moonface.wsee.models.Location
import com.moonface.wsee.models.Toilet
import com.moonface.wsee.models.ToiletQuery
import com.moonface.wsee.toilet_repository.ToiletRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = ToiletRepository()

    var myLocation: Location by mutableStateOf(Location(0F, 0F))

    var cameraLocation: Location by mutableStateOf(Location(0F, 0F))

    var toilets: List<Toilet> by mutableStateOf(emptyList())
        private set

    var selectedToilet: Toilet? by mutableStateOf(null)

    fun searchToilets() = viewModelScope.launch {
        toilets = repository.search(ToiletQuery(cameraLocation, 10))
    }

    fun createToilet(createToilet: CreateToilet) = viewModelScope.launch {
        repository.create(createToilet)
    }

    fun createReview(toiletId: String, createReview: CreateReview) = viewModelScope.launch {
        repository.createReview(toiletId, createReview)
    }
}