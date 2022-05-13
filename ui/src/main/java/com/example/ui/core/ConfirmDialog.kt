package com.example.ui.core

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.ui.R

@Composable
fun ConfirmDialog(
    onDismissRequest: () -> Unit,
    confirmMessage: String,
    confirmButtonText: String,
    onConfirmed: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
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
                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = MaterialTheme.colors.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = {
                    onConfirmed()
                    onDismissRequest()
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