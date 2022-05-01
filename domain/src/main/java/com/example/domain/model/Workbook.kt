package com.example.domain.model

data class Workbook(
    val id: Long,
    val name: String,
    val color: WorkbookColor,
    val questionList: List<Question>
)

enum class WorkbookColor {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    MINT,
    BLUE,
    NAVY,
    PURPLE
}
