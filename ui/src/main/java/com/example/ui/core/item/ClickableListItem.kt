package com.example.ui.core.item

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClickableListItem(
    icon: @Composable (() -> Unit)? = null,
    text: String,
    secondaryText: String = "",
    onClick: () -> Unit
) {
    ListItem(
        icon = icon,
        modifier = Modifier.clickable {
            onClick()
        },
        text = {
            Text(text = text)
        },
        secondaryText = if (secondaryText.isNotEmpty()) { ->
            Text(text = secondaryText)
        } else null
    )
}