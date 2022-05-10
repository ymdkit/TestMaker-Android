package com.example.ui.core

import android.content.Context
import com.example.core.TestMakerColor
import com.example.ui.R
import com.example.ui.theme.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ColorMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun colorToLabel(color: TestMakerColor) =
        when (color) {
            TestMakerColor.BLUE -> context.getString(R.string.color_blue)
            TestMakerColor.RED -> context.getString(R.string.color_red)
            TestMakerColor.GREEN -> context.getString(R.string.color_green)
            TestMakerColor.ORANGE -> context.getString(R.string.color_orange)
            TestMakerColor.YELLOW -> context.getString(R.string.color_yellow)
            TestMakerColor.PINK -> context.getString(R.string.color_pink)
            TestMakerColor.PURPLE -> context.getString(R.string.color_purple)
            TestMakerColor.TEAL -> context.getString(R.string.color_teal)
            TestMakerColor.INDIGO -> context.getString(R.string.color_indigo)
            TestMakerColor.BROWN -> context.getString(R.string.color_brown)
        }

    fun colorToGraphicColor(color: TestMakerColor) =
        when (color) {
            TestMakerColor.BLUE -> Blue
            TestMakerColor.RED -> Red
            TestMakerColor.GREEN -> Green
            TestMakerColor.ORANGE -> Orange
            TestMakerColor.YELLOW -> Yellow
            TestMakerColor.PINK -> Pink
            TestMakerColor.PURPLE -> Purple
            TestMakerColor.TEAL -> Teal
            TestMakerColor.INDIGO -> Indigo
            TestMakerColor.BROWN -> Brown
        }

}