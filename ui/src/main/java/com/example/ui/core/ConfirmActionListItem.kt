package com.example.ui.core

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.ui.R

@Composable
fun ConfirmActionListItem(
    label: String,
    secondaryLabel: String = "",
    confirmMessage: String,
    confirmButtonText: String,
    onConfirmed: () -> Unit,
) {

    var isOpen: Boolean by remember { mutableStateOf(false) }

    ClickableListItem(
        text = label,
        secondaryText = secondaryLabel,
        onClick = {
            isOpen = true
        }
    )
    if (isOpen) {
        AlertDialog(
            onDismissRequest = { isOpen = false },
            title = {
                Text(text = stringResource(id = R.string.confirm))
            },
            text = { Text(text = confirmMessage) },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        isOpen = false
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = {
                        onConfirmed()
                        isOpen = false
                    }) {
                        Text(
                            text = confirmButtonText,
                            style = TextStyle(
                                color = MaterialTheme.colors.error
                            )
                        )
                    }
                }
            }
        )
    }
}