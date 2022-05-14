package com.example.ui.core

import androidx.compose.material.IconButton
import androidx.compose.runtime.*

@Composable
fun ConfirmActionIconButton(
    confirmMessage: String,
    confirmButtonText: String,
    onConfirmed: () -> Unit,
    icon: @Composable () -> Unit,
) {

    var isOpen: Boolean by remember { mutableStateOf(false) }

    IconButton(onClick = {
        isOpen = true
    }) {
        icon()
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