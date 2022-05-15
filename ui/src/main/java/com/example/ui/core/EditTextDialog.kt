package com.example.ui.core

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.R

@Composable
fun EditTextDialog(
    title: String,
    value: String,
    onValueChanged: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    placeholder: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(
                modifier = Modifier
                    .padding(
                        start = 24.dp,
                        top = 32.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                ) {
                    Text(text = title)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChanged,
                        keyboardOptions = keyboardOptions,
                        singleLine = true,
                        placeholder = { Text(text = placeholder, maxLines = 1) }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onSubmit(value) }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}