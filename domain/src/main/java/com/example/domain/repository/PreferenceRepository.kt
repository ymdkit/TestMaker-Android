package com.example.domain.repository

import com.example.core.TestMakerColor
import com.example.domain.model.AnswerSetting
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    val updateIsRemovedAdFlow: Flow<Boolean>
    val updateAnswerSettingFlow: Flow<AnswerSetting>
    val updateThemeColorFlow: Flow<TestMakerColor>
    val updateStudyPlusSettingFlow: Flow<String>

    fun isRemovedAd(): Boolean
    suspend fun putIsRemovedAd(isRemovedAd: Boolean)
    fun getAnswerSetting(): AnswerSetting
    suspend fun putAnswerSetting(answerSetting: AnswerSetting)
    suspend fun putThemeColor(color: TestMakerColor)
    fun getThemeColor(): TestMakerColor
    suspend fun putStudyPlusSetting(setting: String)
    fun getStudyPlusSetting(): String
}