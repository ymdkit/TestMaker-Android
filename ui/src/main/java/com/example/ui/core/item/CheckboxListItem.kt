package com.example.ui.core.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CheckboxListItem(
    label: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable {
                onCheckedChanged(!checked)
            },
        text = { Text(text = label) },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChanged,
            )
        }
    )
}