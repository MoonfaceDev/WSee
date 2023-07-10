package com.moonface.wsee.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.moonface.wsee.models.PlaceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceTypeDropdown(value: PlaceType, onChange: (PlaceType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = value.getLabel(),
            onValueChange = {},
            label = { Text("Place Type") },
            leadingIcon = {
                Icon(
                    painter = painterResource(value.getIcon()),
                    contentDescription = "place type icon",
                    modifier = Modifier.size(16.dp)
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            PlaceType.values().forEach { selectionOption ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(selectionOption.getIcon()),
                            contentDescription = "place type icon",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = { Text(selectionOption.getLabel()) },
                    onClick = {
                        onChange(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}