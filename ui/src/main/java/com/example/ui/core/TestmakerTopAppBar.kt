package com.example.ui.core

import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TestMakerTopAppBar(
    title: String
) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
    )
}