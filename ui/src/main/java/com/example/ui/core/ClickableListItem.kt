package com.example.ui.core

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClickableListItem(
    text: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable {
            onClick()
        },
        text = {
            Text(text = text)
        }
    )
}