package com.example.infra.local.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("question", Context.MODE_PRIVATE)

    var auto: Boolean
        get() = sharedPreferences.getBoolean("auto", false)
        set(num) = sharedPreferences.edit().putBoolean("auto", num)
            .apply()

    var explanation: Boolean
        get() = sharedPreferences.getBoolean("explanation", false)
        set(f) = sharedPreferences.edit().putBoolean("explanation", f)
            .apply()

    var isRemovedAd: Boolean
        get() = sharedPreferences.getBoolean("isRemovedAd", false)
        set(i) = sharedPreferences.edit().putBoolean("isRemovedAd", i).apply()

    var isCaseInsensitive: Boolean
        get() = sharedPreferences.getBoolean("isCaseInsensitive", false)
        set(i) = sharedPreferences.edit().putBoolean("isCaseInsensitive", i).apply()

    //preferencesと連携するため

    private val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var isPlaySound: Boolean
        get() = defaultPreferences.getBoolean("audio", true)
        set(f) = defaultPreferences.edit().putBoolean("audio", f)
            .apply()

    var random: Boolean
        get() = defaultPreferences.getBoolean("random", true)
        set(f) = defaultPreferences.edit().putBoolean("random", f).apply()


    var isSwapProblemAndAnswer: Boolean
        get() = defaultPreferences.getBoolean("reverse", false)
        set(f) = defaultPreferences.edit().putBoolean("reverse", f)
            .apply()

    var refine: Boolean
        get() = defaultPreferences.getBoolean("refine", false)
        set(f) = defaultPreferences.edit().putBoolean("refine", f)
            .apply()

    var isSelfScoring: Boolean
        get() = defaultPreferences.getBoolean("manual", false)
        set(f) = defaultPreferences.edit().putBoolean("manual", f)
            .apply()

    var isAlwaysShowExplanation: Boolean
        get() = defaultPreferences.getBoolean("alwaysReview", false)
        set(f) = defaultPreferences.edit().putBoolean("alwaysReview", f)
            .apply()

    var isShowAnswerSettingDialog: Boolean
        get() = defaultPreferences.getBoolean("show_setting_dialog", true)
        set(f) = defaultPreferences.edit().putBoolean("show_setting_dialog", f)
            .apply()

    var questionCount: Int
        get() = sharedPreferences.getInt("question_count", 100)
        set(i) = sharedPreferences.edit().putInt("question_count", i).apply()

    var startPosition: Int
        get() = sharedPreferences.getInt("start_position", 0)
        set(i) = sharedPreferences.edit().putInt("start_position", i).apply()

    var uploadStudyPlus: String
        get() = defaultPreferences.getString(
            "study_plus",
            "auto"
        )
            ?: "auto"
        set(i) = defaultPreferences.edit().putString("study_plus", i).apply()

    var playCount: Int
        get() = defaultPreferences.getInt("play_count", 0)
        set(i) = defaultPreferences.edit().putInt("play_count", i).apply()

}
