package com.moonface.wsee

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
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
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.moonface.wsee.models.Location
import com.moonface.wsee.models.Toilet
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

val fontFamily = FontFamily(
    Font(R.font.inter_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.inter_thin, weight = FontWeight.Thin),
    Font(R.font.inter_light, weight = FontWeight.Light),
    Font(R.font.inter_regular, weight = FontWeight.Normal),
    Font(R.font.inter_medium, weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold),
    Font(R.font.inter_bold, weight = FontWeight.Bold),
    Font(R.font.inter_extrabold, weight = FontWeight.ExtraBold),
    Font(R.font.inter_black, weight = FontWeight.Black)
)

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

@OptIn(ExperimentalPermissionsApi::class)
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
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
            )
            Text(text = "Add Toilet", fontFamily = fontFamily)
        }
    }

    if (viewModel.selectedToilet != null) {
        ToiletSheet(
            myLocation = myLocation,
            toilet = viewModel.selectedToilet!!,
            onClose = { viewModel.selectedToilet = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToiletSheet(myLocation: android.location.Location?, toilet: Toilet, onClose: () -> Unit) {
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onClose) {
        val distance =
            if (myLocation != null) myLocation.distanceTo(toilet.location.toAndroidLocation()) / 1000 else 0F
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = toilet.getTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = fontFamily,
                )
            }

            Spacer(modifier = Modifier)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (toilet.owner != null) {
                    Text(
                        text = toilet.owner.type.value.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = fontFamily,
                    )
                }
                Text(text = "•")
                if (toilet.rating != null) {
                    Text(
                        text = "${toilet.rating}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = fontFamily,
                    )
                    RatingBar(
                        value = toilet.rating,
                        size = 16.dp,
                        spaceBetween = 2.dp,
                        style = RatingBarStyle.Fill(),
                        isIndicator = true,
                        onValueChange = {},
                        onRatingChanged = {}
                    )
                    Text(
                        text = "(${toilet.reviewCount})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = fontFamily,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.directions_walk),
                    contentDescription = "walk",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${DecimalFormat("#,###.##").format(distance)}km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = fontFamily,
                )
            }

            Button(onClick = {
                val gmmIntentUri =
                    Uri.parse("google.navigation:q=${toilet.location.latitude},${toilet.location.longitude}&mode=w")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(context, mapIntent, null)
            }) {
                Icon(
                    painter = painterResource(R.drawable.directions),
                    contentDescription = "directions",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Directions", fontFamily = fontFamily)
            }
        }
    }
}