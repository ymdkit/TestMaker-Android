package com.example.ui.core

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SectionHeaderListItem(
    text: String
) {
    ListItem(
        text = {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
        }
    )
}