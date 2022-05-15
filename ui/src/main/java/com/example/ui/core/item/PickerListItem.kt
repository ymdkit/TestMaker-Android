package com.example.ui.core.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> PickerListItem(
    icon: @Composable (() -> Unit)? = null,
    text: String,
    secondaryText: String = "",
    itemList: List<Pair<String, T>>,
    onSelected: (T) -> Unit
) {

    var showingDialog by remember { mutableStateOf(false) }

    ClickableListItem(
        icon = icon,
        text = text,
        secondaryText = secondaryText
    ) {
        showingDialog = true
    }
    if (showingDialog) {
        Dialog(onDismissRequest = { showingDialog = false }) {
            Surface(shape = RoundedCornerShape(8.dp)) {
                Column(
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            top = 32.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        )
                ) {
                    LazyColumn {
                        items(itemList) {
                            ClickableListItem(text = it.first) {
                                onSelected(it.second)
                                showingDialog = false
                            }
                        }
                    }
                }
            }
        }
    }
}