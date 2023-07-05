package com.moonface.wsee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moonface.wsee.models.Location
import com.moonface.wsee.models.Toilet
import com.moonface.wsee.models.ToiletQuery
import com.moonface.wsee.toilet_repository.ToiletRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = ToiletRepository()

    var cameraLocation: Location? by mutableStateOf(null)

    var toilets: List<Toilet> by mutableStateOf(emptyList())
        private set

    fun searchToilets() = viewModelScope.launch {
        toilets = repository.search(ToiletQuery(cameraLocation!!, 10))
    }
}