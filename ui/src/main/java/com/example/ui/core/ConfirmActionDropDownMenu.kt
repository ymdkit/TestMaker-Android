package com.example.ui.core

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*

@Composable
fun ConfirmActionDropDownMenu(
    label: String,
    confirmMessage: String,
    confirmButtonText: String,
    onConfirmed: () -> Unit,
) {

    var isOpen: Boolean by remember { mutableStateOf(false) }

    DropdownMenuItem(
        onClick = { isOpen = true }
    ) {
        Text(label)
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