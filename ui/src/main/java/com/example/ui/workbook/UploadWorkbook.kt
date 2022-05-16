package com.example.ui.workbook

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.item.CheckboxListItem

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UploadWorkbook(
    workbookName: String,
    isUploading: Boolean,
    onUpload: (Boolean) -> Unit,
) {
    var isPrivateUpload by remember { mutableStateOf(false) }

    ListItem(
        text = {
            Text(
                text = workbookName,
            )
        }
    )
    CheckboxListItem(
        label = stringResource(id = R.string.check_private),
        checked = isPrivateUpload,
        onCheckedChanged = {
            isPrivateUpload = it
        }
    )
    Button(
        enabled = !isUploading,
        onClick = {
            onUpload(isPrivateUpload)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .defaultMinSize(minHeight = 48.dp),
    ) {
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colors.onPrimary
            )
        } else {
            Text(text = stringResource(id = R.string.button_upload_and_workbook))
        }
    }
}