package com.example.ui.core.item

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.core.ClickableListItem
import com.example.ui.core.EditTextDialog
import com.example.ui.preference.EditTextState

@Composable
fun EditTextListItem(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    dialogTitle: String,
    placeholder: String = "",
    onValueSubmitted: (String) -> Unit,
) {

    var editTextState: EditTextState by remember { mutableStateOf(EditTextState.Empty) }

    ClickableListItem(
        text = label,
        secondaryText = value,
        onClick = {
            editTextState = EditTextState.Editing(value)
        }
    )
    when (val state = editTextState) {
        is EditTextState.Empty -> {
            // do nothing
        }
        is EditTextState.Editing ->
            EditTextDialog(
                title = dialogTitle,
                value = state.value,
                onValueChanged = {
                    editTextState = EditTextState.Editing(it)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType
                ),
                placeholder = placeholder,
                onDismiss = {
                    editTextState = EditTextState.Empty
                },
                onSubmit = {
                    // todo 値のバリデーション
                    onValueSubmitted(it)
                    editTextState = EditTextState.Empty
                }
            )
    }
}