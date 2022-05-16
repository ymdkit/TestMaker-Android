package com.example.ui.question

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.item.ClickableListItem
import com.example.ui.core.item.ConfirmActionListItem
import com.example.usecase.model.QuestionUseCaseModel

@Composable
fun OperateQuestion(
    question: QuestionUseCaseModel,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Text(
        modifier = Modifier.padding(16.dp),
        text = question.problem
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
    ClickableListItem(
        icon = {
            Icon(
                imageVector = Icons.Filled.FileCopy,
                contentDescription = "copy workbook"
            )
        },
        text = stringResource(id = R.string.copy_question),
        onClick = onCopy
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
            id = R.string.msg_delete_question
        ),
        confirmButtonText = stringResource(
            id = R.string.button_delete_confirm
        ),
        onConfirmed = onDelete
    )
}
