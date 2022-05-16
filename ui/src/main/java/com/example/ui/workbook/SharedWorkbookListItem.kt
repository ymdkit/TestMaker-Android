package com.example.ui.workbook

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.ColorMapper
import com.example.ui.core.item.ClickableListItem
import com.example.usecase.model.SharedWorkbookUseCaseModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SharedWorkbookListItem(
    workbook: SharedWorkbookUseCaseModel,
    onClick: (SharedWorkbookUseCaseModel) -> Unit
) {
    ClickableListItem(
        icon = {
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                imageVector = Icons.Filled.Description,
                contentDescription = "workbook",
                tint = ColorMapper(LocalContext.current).colorToGraphicColor(workbook.color)
            )
        },
        text = workbook.name,
        secondaryText = "${
            if (workbook.isPublic) stringResource(
                R.string.label_public
            )
            else stringResource(
                R.string.label_private
            )
        }  ${
            stringResource(
                id = R.string.num_questions,
                workbook.questionListCount
            )
        }",
        onClick = { onClick(workbook) }
    )
}