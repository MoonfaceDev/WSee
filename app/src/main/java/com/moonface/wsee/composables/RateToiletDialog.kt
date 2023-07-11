package com.moonface.wsee.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.moonface.wsee.models.CreateReview
import com.moonface.wsee.viewmodel.MainViewModel

@Composable
fun RateToiletDialog(toiletId: String, onClose: () -> Unit) {
    val viewModel = viewModel<MainViewModel>()

    var rating: Int by remember { mutableStateOf(5) }

    Dialog(onDismissRequest = onClose) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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

            Divider()

            Button(onClick = {
                viewModel.createReview(toiletId, CreateReview(rating))
                onClose()
            }) {
                Text(text = "Submit Review")
            }
        }
    }
}