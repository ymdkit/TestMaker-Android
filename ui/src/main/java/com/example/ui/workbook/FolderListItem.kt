package com.example.ui.workbook

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
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
    folder: FolderUseCaseModel,
) {
    ListItem(
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
        }
    )
}