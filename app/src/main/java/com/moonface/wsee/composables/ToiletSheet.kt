package com.moonface.wsee.composables

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.moonface.wsee.R
import com.moonface.wsee.models.Toilet
import com.moonface.wsee.viewmodel.MainViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToiletSheet(
    toilet: Toilet,
    onClose: () -> Unit
) {
    val viewModel = viewModel<MainViewModel>()
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onClose) {
        val distance = viewModel.myLocation.distanceTo(toilet.place.location) / 1000
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
                Icon(
                    painter = painterResource(toilet.place.type.getIcon()),
                    contentDescription = "place type icon",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = toilet.place.type.getLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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

            Button(onClick = { toilet.place.location.navigate(context) }) {
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
