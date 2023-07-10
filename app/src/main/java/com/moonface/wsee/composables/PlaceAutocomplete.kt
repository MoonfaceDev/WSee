package com.moonface.wsee.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.moonface.wsee.models.Place
import com.moonface.wsee.place_repository.PlaceRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceAutocomplete(
    bounds: LatLngBounds?,
    query: String,
    onQueryChange: (String) -> Unit,
    onSelect: (Place) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val repository by remember { mutableStateOf(PlaceRepository(context)) }
    var expanded by remember { mutableStateOf(false) }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var searchSession by remember { mutableStateOf(repository.getSession()) }

    suspend fun updatePredictions(query: String, bounds: LatLngBounds?) {
        predictions = if (query != "") searchSession.search(query, bounds) else emptyList()
        if (predictions.isNotEmpty()) {
            expanded = true
        }
    }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(query) {
        if (query == "") {
            searchSession = repository.getSession()
        }
    }

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = query,
            onValueChange = {
                onQueryChange(it)
                coroutineScope.launch { updatePredictions(it, bounds) }
            },
            label = { Text("Place Name") },
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            predictions.forEach {
                DropdownMenuItem(
                    text = { Text(it.getPrimaryText(null).toString()) },
                    onClick = {
                        expanded = false
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            val place = searchSession.fetchPlace(it.placeId)
                            if (place != null) {
                                onSelect(place)
                            }
                        }
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}