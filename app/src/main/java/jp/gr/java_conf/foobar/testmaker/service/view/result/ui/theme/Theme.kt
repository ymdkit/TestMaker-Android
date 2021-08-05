package jp.gr.java_conf.foobar.testmaker.service.view.result.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0XFF0099CC),
    secondary = Color(0XFFBA7827),
    surface = Color(0X00000000),
    onPrimary = Color.White,
    onSurface = Color(0X99FFFFFF),
    onBackground = Color(0X99FFFFFF),
    onSecondary = Color(0X99FFFFFF),
    onError = Color.White
)

private val LightColorPalette = lightColors(
    primary = Color(0XFF33B5E5),
    secondary = Color(0XFFFFA144),
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