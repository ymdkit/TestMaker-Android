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
    content: @Composable () -> Unit
) {

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