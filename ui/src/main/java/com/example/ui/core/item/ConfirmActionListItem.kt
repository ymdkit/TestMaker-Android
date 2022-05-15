package com.example.ui.core.item

import androidx.compose.runtime.*
import com.example.ui.core.ConfirmDialog

@Composable
fun ConfirmActionListItem(
    icon: @Composable (() -> Unit)? = null,
    label: String,
    secondaryLabel: String = "",
    confirmMessage: String,
    confirmButtonText: String,
    onConfirmed: () -> Unit,
) {

    var isOpen: Boolean by remember { mutableStateOf(false) }

    ClickableListItem(
        icon = icon,
        text = label,
        secondaryText = secondaryLabel,
        onClick = {
            isOpen = true
        }
    )
    if (isOpen) {
        ConfirmDialog(
            onDismissRequest = { isOpen = false },
            confirmMessage = confirmMessage,
            confirmButtonText = confirmButtonText,
            onConfirmed = onConfirmed,
        )
    }
}