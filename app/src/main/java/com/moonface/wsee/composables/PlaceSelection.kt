package com.moonface.wsee.composables

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.moonface.wsee.models.Location
import com.moonface.wsee.models.Place
import com.moonface.wsee.models.PlaceType
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.google.android.libraries.places.api.model.Place as GooglePlace

class SelectPlace : ActivityResultContract<Void?, Place?>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        val fields = listOf(
            GooglePlace.Field.ADDRESS,
            GooglePlace.Field.LAT_LNG,
            GooglePlace.Field.NAME,
            GooglePlace.Field.TYPES,
        )
        return Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Place? {
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                return Place(
                    address = place.address,
                    location = Location.fromLatLng(place.latLng!!),
                    name = place.name,
                    type = when {
                        place.types!!.contains(GooglePlace.Type.LODGING) -> PlaceType.HOTEL
                        place.types!!.contains(GooglePlace.Type.TOURIST_ATTRACTION) -> PlaceType.ATTRACTION
                        place.types!!.contains(GooglePlace.Type.RESTAURANT) -> PlaceType.RESTAURANT
                        else -> PlaceType.PUBLIC
                    }
                )
            }
        }
        return null
    }
}

@Composable
fun rememberPlaceSelection(): suspend () -> Place? {
    var continuation by remember { mutableStateOf<Continuation<Place?>?>(null) }

    val placePicker = rememberLauncherForActivityResult(
        contract = SelectPlace(),
        onResult = { continuation?.resume(it) },
    )

    suspend fun pickPlace(): Place? {
        placePicker.launch()
        return suspendCoroutine { continuation = it }
    }

    return ::pickPlace
}