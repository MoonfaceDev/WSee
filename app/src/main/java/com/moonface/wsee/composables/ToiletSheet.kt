package com.moonface.wsee.composables

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.moonface.wsee.R
import com.moonface.wsee.checkLocationPermissions
import com.moonface.wsee.models.Toilet
import java.text.DecimalFormat

@SuppressLint("MissingPermission")
@Composable
private fun LocationUpdates(listener: LocationListener) {
    val context = LocalContext.current
    DisposableEffect(true) {
        if (checkLocationPermissions(context)) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
                .build()
            client.requestLocationUpdates(locationRequest, listener, Looper.getMainLooper())
            onDispose {
                client.removeLocationUpdates(listener)
            }
        }
        onDispose {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToiletSheet(
    toilet: Toilet,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var myLocation by rememberSaveable { mutableStateOf<Location?>(null) }
    LocationUpdates { myLocation = it }

    ModalBottomSheet(onDismissRequest = onClose) {
        val distance =
            if (myLocation != null) myLocation!!.distanceTo(toilet.location.toAndroidLocation()) / 1000 else 0F
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
                )
            }

            Spacer(modifier = Modifier)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (toilet.owner != null) {
                    Icon(
                        painter = painterResource(toilet.owner.type.getIcon()),
                        contentDescription = "owner type icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = toilet.owner.type.value.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(text = "•")
                if (toilet.rating != null) {
                    Text(
                        text = "${toilet.rating}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                )
            }

            Button(onClick = { toilet.location.navigate(context) }) {
                Icon(
                    painter = painterResource(R.drawable.directions),
                    contentDescription = "directions",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Directions")
            }
        }
    }
}
