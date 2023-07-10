package com.moonface.wsee.place_repository

import android.content.Context
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.model.Place.Type
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.moonface.wsee.models.Location
import com.moonface.wsee.models.Place
import com.moonface.wsee.models.PlaceType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SearchSession(private val client: PlacesClient) {
    private var token: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

    suspend fun search(query: String, bounds: LatLngBounds?): List<AutocompletePrediction> {
        val request = FindAutocompletePredictionsRequest.builder()
            .run { if (bounds != null) setLocationBias(RectangularBounds.newInstance(bounds)) else this }
            .setTypesFilter(listOf(PlaceTypes.ESTABLISHMENT))
            .setSessionToken(token)
            .setQuery(query)
            .build()
        return suspendCoroutine { cont ->
            client.findAutocompletePredictions(request)
                .addOnSuccessListener {
                    cont.resume(it.autocompletePredictions)
                }
                .addOnFailureListener {
                    cont.resume(emptyList())
                }
        }
    }

    suspend fun fetchPlace(placeId: String): Place? {
        val request = FetchPlaceRequest.builder(
            placeId,
            listOf(Field.ADDRESS, Field.LAT_LNG, Field.NAME, Field.TYPES)
        ).setSessionToken(token).build()

        return suspendCoroutine { cont ->
            client.fetchPlace(request)
                .addOnSuccessListener {
                    val types = it.place.types!!
                    cont.resume(
                        Place(
                            address = it.place.address,
                            location = Location.fromLatLng(it.place.latLng!!),
                            name = it.place.name,
                            type = when {
                                types.contains(Type.LODGING) -> PlaceType.HOTEL
                                types.contains(Type.TOURIST_ATTRACTION) -> PlaceType.ATTRACTION
                                types.contains(Type.RESTAURANT) -> PlaceType.RESTAURANT
                                else -> PlaceType.PUBLIC
                            },
                        )
                    )
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }
}

class PlaceRepository(context: Context) {
    private var client = Places.createClient(context)

    fun getSession(): SearchSession {
        return SearchSession(client)
    }
}