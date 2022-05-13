package com.example.ui.core

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*

@Composable
fun ConfirmActionTextButton(
    label: String,
    confirmMessage: String,
    confirmButtonText: String,
    onConfirmed: () -> Unit,
) {

    var isOpen: Boolean by remember { mutableStateOf(false) }

    TextButton(onClick = {
        isOpen = true
    }) {
        Text(text = label)
    }
    if (isOpen) {
        ConfirmDialog(
            onDismissRequest = { isOpen = false },
            confirmMessage = confirmMessage,
            confirmButtonText = confirmButtonText,
            onConfirmed = onConfirmed,
        )
    }
}