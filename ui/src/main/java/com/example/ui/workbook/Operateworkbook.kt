package com.example.ui.workbook

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.ClickableListItem
import com.example.ui.core.item.ConfirmActionListItem
import com.example.usecase.model.WorkbookUseCaseModel

@Composable
fun OperateWorkbook(
    workbook: WorkbookUseCaseModel,
    onAnswer: () -> Unit,
    onEdit: () -> Unit,
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
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "answer workbook"
            )
        },
        text = stringResource(id = R.string.play),
        onClick = onAnswer
    )
    ClickableListItem(
        icon = {
            Icon(
                imageVector = Icons.Filled.Create,
                contentDescription = "edit workbook"
            )
        },
        text = stringResource(id = R.string.edit),
        onClick = onEdit
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
}
