package com.example.ui.workbook

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.item.ConfirmActionListItem
import com.example.ui.core.item.EditTextListItem
import com.example.usecase.model.FolderUseCaseModel

@Composable
fun OperateFolder(
    folder: FolderUseCaseModel,
    onEdit: (FolderUseCaseModel) -> Unit,
    onDelete: () -> Unit
) {
    Text(
        modifier = Modifier.padding(16.dp),
        text = folder.name
    )
    EditTextListItem(
        icon = {
            Icon(
                imageVector = Icons.Filled.Create,
                contentDescription = "edit folder"
            )
        },
        label = stringResource(id = R.string.edit),
        value = folder.name,
        dialogTitle = stringResource(id = R.string.title_edit_folder),
        onValueSubmitted = {
            onEdit(folder.copy(name = it))
        },
        showingSecondaryText = false
    )
    ConfirmActionListItem(
        icon = {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "delete folder"
            )
        },
        label = stringResource(R.string.delete),
        confirmMessage = stringResource(
            id = R.string.msg_delete_folder,
            folder.name
        ),
        confirmButtonText = stringResource(
            id = R.string.button_delete_confirm
        ),
        onConfirmed = onDelete
    )
}
