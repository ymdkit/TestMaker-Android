package com.example.ui.core

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ContainedWideButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    color: Color = MaterialTheme.colors.primary
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = color
        ),
        onClick = onClick
    ) {
        Text(
            text = text,
        )
    }
}

@Composable
fun OutlinedWideButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
        )
    }
}