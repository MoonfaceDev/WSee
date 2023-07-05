package com.moonface.wsee

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.moonface.wsee.models.Location
import com.moonface.wsee.viewmodel.MainViewModel
import java.text.DecimalFormat

val MAP_STYLE = """
    [
      {
        "featureType": "poi",
        "elementType": "all",
        "stylers": [
          {
            "visibility": "off"
          }
        ]
      }
    ]
""".trimIndent()

fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int
): BitmapDescriptor? {

    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    // draw it onto the bitmap
    val canvas = android.graphics.Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(MainViewModel())
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
private fun MainScreen(viewModel: MainViewModel) {
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val context = LocalContext.current
    var myLocation by remember { mutableStateOf<android.location.Location?>(null) }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                viewModel.cameraLocation = Location.fromAndroidLocation(location!!)
            }
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000L
            ).build()
            client.requestLocationUpdates(
                locationRequest,
                { location -> myLocation = location },
                Looper.getMainLooper()
            )
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15F)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            viewModel.cameraLocation = Location.fromLatLng(cameraPositionState.position.target)
        }
    }

    LaunchedEffect(viewModel.cameraLocation) {
        if (viewModel.cameraLocation != null) {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(
                    viewModel.cameraLocation!!.toLatLng(),
                    cameraPositionState.position.zoom
                )
            viewModel.searchToilets()
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapStyleOptions = MapStyleOptions(MAP_STYLE)
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        )
    ) {
        viewModel.toilets.forEach { toilet ->
            val distance =
                if (myLocation != null) myLocation!!.distanceTo(toilet.location.toAndroidLocation()) / 1000 else 0F
            MarkerInfoWindow(
                state = MarkerState(position = toilet.location.toLatLng()),
                title = "${DecimalFormat("#,###.##").format(distance)}km",
                icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.toilet_marker),
                onClick = {
                    showBottomSheet = true
                    true
                },
            ) { marker ->
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9F),
                            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp),
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = marker.title ?: "",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExtendedFloatingActionButton(
            containerColor = FloatingActionButtonDefaults.containerColor.copy(alpha = 0.9F),
            onClick = {}
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
            )
            Text(text = "Add Toilet")
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {

        }
    }
}