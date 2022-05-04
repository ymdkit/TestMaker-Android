package com.example.core

enum class QuestionType(val value: Int) {
    WRITE(0),
    SELECT(1),
    COMPLETE(2),
    SELECT_COMPLETE(3);

    companion object {
        fun valueOf(value: Int) =
            when (value) {
                0 -> WRITE
                1 -> SELECT
                2 -> COMPLETE
                3 -> SELECT_COMPLETE
                else -> throw IllegalArgumentException("unexpected value: $value")
            }
    }
}