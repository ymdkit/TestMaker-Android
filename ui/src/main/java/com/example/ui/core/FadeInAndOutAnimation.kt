package com.example.ui.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@ExperimentalAnimationApi
@Composable
fun FadeInAndOutAnimation(
    content: @Composable ColumnScope.() -> Unit
){
    var isShowEffect by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isShowEffect = false
    }

    Column {
        AnimatedVisibility(
            visible = isShowEffect,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 1000)
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 1000)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
    }


}