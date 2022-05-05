package com.example.domain.repository

import com.example.domain.model.AnswerSetting
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    val updateIsRemovedAdFlow: Flow<Boolean>
    val updateAnswerSettingFlow: Flow<AnswerSetting>

    fun isRemovedAd(): Boolean
    fun putIsRemovedAd(isRemovedAd: Boolean)
    fun getAnswerSetting(): AnswerSetting
    suspend fun putAnswerSetting(answerSetting: AnswerSetting)
}