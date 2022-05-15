package com.example.ui.workbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.ColorMapper
import com.example.usecase.model.FolderUseCaseModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FolderListItem(
    modifier: Modifier = Modifier,
    folder: FolderUseCaseModel,
    onClick: (FolderUseCaseModel) -> Unit,
    onMenuClicked: (FolderUseCaseModel) -> Unit
) {
    ListItem(
        modifier = modifier.clickable {
            onClick(folder)
        },
        icon = {
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                imageVector = Icons.Filled.Folder,
                tint = ColorMapper(LocalContext.current).colorToGraphicColor(
                    folder.color
                ),
                contentDescription = "folder",
            )
        },
        text = { Text(folder.name) },
        secondaryText = {
            Text(
                stringResource(
                    id = R.string.number_exams,
                    folder.workbookCount,
                )
            )
        },
        trailing = {
            IconButton(onClick = { onMenuClicked(folder) }) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "menu"
                )
            }
        }
    )
}