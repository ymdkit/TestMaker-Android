package com.example.core

enum class StudyPlusSetting(val label: String, val value: String) {
    NOT("投稿しない", "not"),
    AUTO("自動投稿", "auto"),
    MANUAL("手動投稿", "manual");

    companion object {
        fun fromValue(value: String) =
            when (value) {
                "not" -> NOT
                "auto" -> AUTO
                "manual" -> MANUAL
                else -> NOT
            }
    }
}