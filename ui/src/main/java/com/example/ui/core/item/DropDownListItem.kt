package com.example.ui.core.item

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*

@Composable
fun DropDownListItem(
    label: String,
    value: String,
    keyValueList: List<Pair<String, String>>,
    onValueChange: (String) -> Unit,
) {

    var showingDropDownMenu: Boolean by remember { mutableStateOf(false) }

    ClickableListItem(
        text = label,
        secondaryText = value,
        onClick = {
            showingDropDownMenu = true
        }
    )
    DropdownMenu(
        expanded = showingDropDownMenu,
        onDismissRequest = {
            showingDropDownMenu = false
        }) {
        keyValueList.forEach {
            DropdownMenuItem(onClick = {
                onValueChange(it.second)
                showingDropDownMenu = false
            }) {
                Text(it.first)
            }
        }
    }
}