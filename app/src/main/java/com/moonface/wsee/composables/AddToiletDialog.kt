package com.moonface.wsee.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.moonface.wsee.models.CreateToilet
import com.moonface.wsee.models.Location
import com.moonface.wsee.models.Place
import com.moonface.wsee.models.PlaceType
import com.moonface.wsee.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToiletDialog(onClose: () -> Unit) {
    val viewModel = viewModel<MainViewModel>()

    var placeAddress: String by remember { mutableStateOf("") }
    var placeLocation: Location by remember { mutableStateOf(viewModel.cameraLocation) }
    var placeName: String by remember { mutableStateOf("") }
    var placeType: PlaceType by remember { mutableStateOf(PlaceType.PUBLIC) }
    var rating: Int by remember { mutableStateOf(5) }

    fun updatePlace(place: Place) {
        placeAddress = place.address ?: ""
        placeLocation = place.location
        placeName = place.name ?: ""
        placeType = place.type
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(true) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(placeLocation.toLatLng(), 15F)
    }

    LaunchedEffect(placeLocation) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(placeLocation.toLatLng()))
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text("Add Toilet")
                        },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close"
                                )
                            }
                        },
                    )
                },
                content = { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                isMyLocationEnabled = true,
                            ),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                mapToolbarEnabled = false,
                            ),
                            onMapClick = { placeLocation = Location.fromLatLng(it) },
                            onPOIClick = { placeLocation = Location.fromLatLng(it.latLng) },
                            onMyLocationClick = { placeLocation = Location.fromAndroidLocation(it) }
                        ) {
                            Marker(
                                state = MarkerState(position = placeLocation.toLatLng())
                            )
                        }

                        val projection = cameraPositionState.projection
                        PlaceAutocomplete(
                            bounds = projection?.visibleRegion?.latLngBounds,
                            query = placeName,
                            onQueryChange = { placeName = it },
                            onSelect = { updatePlace(it) }
                        )

                        PlaceTypeDropdown(
                            value = placeType,
                            onChange = { placeType = it }
                        )

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = placeAddress,
                            onValueChange = { placeAddress = it },
                            label = { Text("Address") }
                        )

                        Divider()

                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Your Rating")
                            RatingBar(
                                value = rating.toFloat(),
                                size = 24.dp,
                                spaceBetween = 4.dp,
                                style = RatingBarStyle.Fill(),
                                onValueChange = { rating = it.toInt() },
                                onRatingChanged = {}
                            )
                        }

                        Divider()

                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {
                                viewModel.createToilet(
                                    CreateToilet(
                                        place = Place(
                                            address = placeAddress.ifEmpty { null },
                                            location = placeLocation,
                                            name = placeName.ifEmpty { null },
                                            type = placeType,
                                        ),
                                        rating = rating,
                                    )
                                )
                                onClose()
                            }) {
                                Text(text = "Submit Toilet")
                            }
                        }
                    }
                }
            )
        }
    }
}