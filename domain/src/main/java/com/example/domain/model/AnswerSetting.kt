package com.example.domain.model

import com.example.core.QuestionCondition

data class AnswerSetting(
    val isRandomOrder: Boolean,
    val isSwapProblemAndAnswer: Boolean,
    val questionCondition: QuestionCondition,
    val isSelfScoring: Boolean,
    val isAlwaysShowExplanation: Boolean,
    val isPlaySound: Boolean,
    val isShowAnswerSettingDialog: Boolean,
    val questionCount: Int,
    val startPosition: Int,
)

