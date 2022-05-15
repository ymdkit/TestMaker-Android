package com.example.ui.theme

import android.preference.PreferenceManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.core.TestMakerColor
import com.example.ui.core.ColorMapper
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun TestMakerAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val systemUiController = rememberSystemUiController()
    if (darkTheme) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent
        )
    } else {
        systemUiController.setSystemBarsColor(
            color = Color.White
        )
    }

    // todo NavigationCompose 導入の際に、 ViewModel を Inject する形に修正
    // todo リアルタイムで反映
    val color = TestMakerColor.values().firstOrNull {
        PreferenceManager
            .getDefaultSharedPreferences(LocalContext.current)
            .getString(
                "theme_color",
                TestMakerColor.BLUE.name
            ) == it.name
    } ?: TestMakerColor.BLUE

    val colorMapper = ColorMapper(context = LocalContext.current)
    val themeColor = colorMapper.colorToGraphicColor(color)

    val colors = if (darkTheme) {
        darkColors(
            primary = themeColor,
            primaryVariant = themeColor,
            secondary = themeColor,
            secondaryVariant = themeColor
        )
    } else {
        lightColors(
            primary = themeColor,
            primaryVariant = themeColor,
            secondary = themeColor,
            secondaryVariant = themeColor
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}