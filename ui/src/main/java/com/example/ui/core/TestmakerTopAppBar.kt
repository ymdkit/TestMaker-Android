package com.example.ui.core

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TestMakerTopAppBar(
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    title: String
) {
    TopAppBar(
        title = { Text(text = title) },
        actions = actions,
        navigationIcon = navigationIcon,
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
    )
}