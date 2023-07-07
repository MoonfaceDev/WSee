package com.moonface.wsee

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.moonface.wsee.composables.Text
import com.moonface.wsee.composables.ToiletSheet
import com.moonface.wsee.models.Location
import com.moonface.wsee.viewmodel.MainViewModel

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

private fun bitmapDescriptorFromVector(
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
            val context = LocalContext.current
            val colorScheme =
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                    context
                )
            MaterialTheme(colorScheme = colorScheme) {
                MainScreen(MainViewModel())
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MainScreen(viewModel: MainViewModel) {
    val locationPermissions = rememberMultiplePermissionsState(permissions = LOCATION_PERMISSIONS)
    val context = LocalContext.current
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                viewModel.cameraLocation = Location.fromAndroidLocation(location!!)
            }
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
                    viewModel.cameraLocation!!.toLatLng(), cameraPositionState.position.zoom
                )
            viewModel.searchToilets()
        }
    }

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
            Marker(
                state = MarkerState(position = toilet.location.toLatLng()),
                icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.toilet_marker),
                onClick = {
                    viewModel.selectedToilet = toilet
                    true
                },
            )
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
            onClick = {}
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            Text(text = "Add Toilet")
        }
    }

    if (viewModel.selectedToilet != null) {
        ToiletSheet(
            toilet = viewModel.selectedToilet!!,
            onClose = { viewModel.selectedToilet = null }
        )
    }
}