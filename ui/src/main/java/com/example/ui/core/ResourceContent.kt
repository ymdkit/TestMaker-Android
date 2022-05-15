package com.example.ui.core

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.core.utils.Resource
import com.example.ui.R

@Composable
fun <T> ResourceContent(
    resource: Resource<T>,
    onRetry: () -> Unit,
    onSuccess: @Composable (T) -> Unit,
) {
    when (resource) {
        is Resource.Success -> onSuccess(resource.value)
        is Resource.Failure -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = onRetry) {
                        Text(text = stringResource(id = R.string.reload))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(id = R.string.msg_failure_loading))
                }
            }
        }
        else -> {
            // do nothing
        }
    }
}
