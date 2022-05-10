package com.example.ui.core

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.example.core.TestMakerColor

@Composable
fun ColorDropDownListItem(
    label: String,
    value: String,
    colorMapper: ColorMapper,
    onValueChange: (TestMakerColor) -> Unit,
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
        TestMakerColor.values().forEach {
            DropdownMenuItem(onClick = {
                onValueChange(it)
                showingDropDownMenu = false
            }) {
                Text(
                    text = colorMapper.colorToLabel(it),
                    color = colorMapper.colorToGraphicColor(it)
                )
            }
        }
    }
}