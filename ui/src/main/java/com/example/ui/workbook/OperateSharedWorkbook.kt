package com.example.ui.workbook

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.item.ClickableListItem
import com.example.ui.core.item.ConfirmActionListItem
import com.example.usecase.model.SharedWorkbookUseCaseModel

@Composable
fun OperateOwnSharedWorkbook(
    workbook: SharedWorkbookUseCaseModel,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Text(
        modifier = Modifier.padding(16.dp),
        text = workbook.name
    )
    ClickableListItem(
        icon = {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "share workbook"
            )
        },
        text = stringResource(id = R.string.share),
        onClick = onShare
    )
    ConfirmActionListItem(
        icon = {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "delete workbook"
            )
        },
        label = stringResource(R.string.delete),
        confirmMessage = stringResource(
            id = R.string.message_delete_exam,
            workbook.name
        ),
        confirmButtonText = stringResource(
            id = R.string.button_delete_confirm
        ),
        onConfirmed = onDelete
    )
    Button(
        enabled = !isDownloading,
        onClick = onDownload,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .defaultMinSize(minHeight = 48.dp),
    ) {
        if (isDownloading) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colors.onPrimary
            )
        } else {
            Text(text = stringResource(id = R.string.download))
        }
    }
}
