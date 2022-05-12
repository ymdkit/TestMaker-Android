package com.example.ui.group

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.core.ClickableListItem
import com.example.usecase.model.GroupUseCaseModel

@Composable
fun GroupListItem(
    group: GroupUseCaseModel,
    onClick: (GroupUseCaseModel) -> Unit
) {
    ClickableListItem(
        icon = {
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                imageVector = Icons.Filled.Group,
                tint = MaterialTheme.colors.primary,
                contentDescription = "group"
            )
        },
        text = group.name,
        secondaryText = group.createdAt,
        onClick = {
            onClick(group)
        })
}