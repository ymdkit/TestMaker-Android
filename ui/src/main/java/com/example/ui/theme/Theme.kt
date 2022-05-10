package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0XFF0099CC),
    secondaryVariant = Color(0XFF0099CC)
//    primary = Color(0XFF0099CC),
//    secondary = Color(0XFFBA7827),
//    secondaryVariant = Color(0XFF0099CC),
//    surface = Color(0X00000000),
//    onPrimary = Color.White,
//    onSurface = Color(0X99FFFFFF),
//    onBackground = Color(0X99FFFFFF),
//    onSecondary = Color.White,
//    onError = Color.White
)

private val LightColorPalette = lightColors(
    primary = Color(0XFF0099CC),
    secondaryVariant = Color(0XFF0099CC)
//    primary = Color(0XFF33B5E5),
//    secondary = Color(0XFFFFA144),
//    secondaryVariant = Color(0XFF0099CC),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
)

@Composable
fun TestMakerAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}