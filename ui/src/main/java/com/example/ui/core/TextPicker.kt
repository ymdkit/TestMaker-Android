package jp.gr.java_conf.foobar.testmaker.service.view.share.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextPicker(
    modifier: Modifier = Modifier,
    label: String,
    entries: List<String>,
    value: String,
    onValueChange: (String) -> Unit
) {

    var showingDropDownMenu by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = {
                showingDropDownMenu = true
            },
            border = BorderStroke(
                ButtonDefaults.OutlinedBorderSize,
                MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
            )
        ) {
            Text(
                text = label,
                color = MaterialTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.weight(weight = 1f, fill = true))
            Text(
                text = value,
                color = MaterialTheme.colors.onSurface
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
            )
        }

        DropdownMenu(
            expanded = showingDropDownMenu,
            onDismissRequest = {
                showingDropDownMenu = false
            }) {
            entries.forEach {
                DropdownMenuItem(onClick = {
                    onValueChange(it)
                    showingDropDownMenu = false
                }) {
                    Text(it)
                }
            }
        }
    }
}