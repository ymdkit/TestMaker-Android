package com.example.ui.core

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.usecase.utils.Resource

@Composable
fun <T> ResourceContent(
    resource: Resource<T>,
    onSuccess: @Composable (T) -> Unit,
    onRetry: () -> Unit
) {
    when (resource) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> onSuccess(resource.value)
        is Resource.Failure -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = onRetry) {
                        Text(text = "リトライ")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("エラーが発生しました。リトライしてください")
                }
            }
        }
        else -> {
            // do nothing
        }
    }
}
