package com.example.ui.core


data class DialogMenuItem(
        val title: String,
        val iconRes: Int,
        val action: () -> Unit
)