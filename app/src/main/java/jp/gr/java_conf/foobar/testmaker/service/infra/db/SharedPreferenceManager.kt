package jp.gr.java_conf.foobar.testmaker.service.infra.db

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.R

class SharedPreferenceManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("question", Context.MODE_PRIVATE)

    var numOthers: Int
        get() = sharedPreferences.getInt("num_choose", 3)
        set(num) = sharedPreferences.edit().putInt("num_choose", num)
                .apply()

    var numAnswers: Int
        get() = sharedPreferences.getInt("num_write", 2)
        set(num) = sharedPreferences.edit().putInt("num_write", num)
                .apply()

    var numAnswersSelectComplete: Int
        get() = sharedPreferences.getInt("num_answers_select", 2)
        set(num) = sharedPreferences.edit().putInt("num_answers_select", num)
                .apply()

    var numOthersSelectComplete: Int
        get() = sharedPreferences.getInt("num_answers_select", 2)
        set(num) = sharedPreferences.edit().putInt("num_answers_select", num)
                .apply()

    var auto: Boolean
        get() = sharedPreferences.getBoolean("auto", false)
        set(num) = sharedPreferences.edit().putBoolean("auto", num)
                .apply()

    var explanation: Boolean
        get() = sharedPreferences.getBoolean("explanation", false)
        set(f) = sharedPreferences.edit().putBoolean("explanation", f)
                .apply()

    var isCheckOrder: Boolean
        get() = sharedPreferences.getBoolean("isCheckOrder", false)
        set(f) = sharedPreferences.edit().putBoolean("isCheckOrder", f)
                .apply()

    var isShowImageSetting: Boolean
        get() = sharedPreferences.getBoolean("isShowImageSetting", false)
        set(f) = sharedPreferences.edit().putBoolean("isShowImageSetting", f)
                .apply()

    var isResetForm: Boolean
        get() = sharedPreferences.getBoolean("isResetForm", true)
        set(f) = sharedPreferences.edit().putBoolean("isResetForm", f)
                .apply()

    var confirmSave: Boolean
        get() = sharedPreferences.getBoolean("confirmSave", false)
        set(f) = sharedPreferences.edit().putBoolean("confirmSave", f)
                .apply()

    var isRemovedAd: Boolean
        get() = sharedPreferences.getBoolean("isRemovedAd", false)
        set(i) = sharedPreferences.edit().putBoolean("isRemovedAd", i).apply()

    var isCaseInsensitive: Boolean
        get() = sharedPreferences.getBoolean("isCaseInsensitive", false)
        set(i) = sharedPreferences.edit().putBoolean("isCaseInsensitive", i).apply()

    var sort: Int
        get() = sharedPreferences.getInt("sort", -1)
        set(i) = sharedPreferences.edit().putInt("sort", i).apply()

    //preferencesと連携するため

    private val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var audio: Boolean
        get() = defaultPreferences.getBoolean("audio", !oldAudio)
        set(f) = defaultPreferences.edit().putBoolean("audio", f)
                .apply()

    var random: Boolean
        get() = defaultPreferences.getBoolean("random", oldRandom)
        set(f) = defaultPreferences.edit().putBoolean("random", f).apply()


    var reverse: Boolean
        get() = defaultPreferences.getBoolean("reverse", oldReverse)
        set(f) = defaultPreferences.edit().putBoolean("reverse", f)
                .apply()

    var refine: Boolean
        get() = defaultPreferences.getBoolean("refine", oldRefine)
        set(f) = defaultPreferences.edit().putBoolean("refine", f)
                .apply()

    var manual: Boolean
        get() = defaultPreferences.getBoolean("manual", oldManual)
        set(f) = defaultPreferences.edit().putBoolean("manual", f)
                .apply()

    var alwaysReview: Boolean
        get() = defaultPreferences.getBoolean("alwaysReview", oldAlwaysReview)
        set(f) = defaultPreferences.edit().putBoolean("alwaysReview", f)
                .apply()

    var isShowPlaySettingDialog: Boolean
        get() = defaultPreferences.getBoolean("show_setting_dialog", true)
        set(f) = defaultPreferences.edit().putBoolean("show_setting_dialog", f)
                .apply()

    var uploadStudyPlus: String
        get() = defaultPreferences.getString("study_plus", context.resources.getStringArray(R.array.upload_setting_study_plus_values)[oldUploadStudyPlus])
                ?: "auto"
        set(i) = defaultPreferences.edit().putString("study_plus", i).apply()

    var playCount: Int
        get() = defaultPreferences.getInt("play_count", 0)
        set(i) = defaultPreferences.edit().putInt("play_count", i).apply()


    //preferencesと連携させるため古いフラグを退避

    private var oldRandom: Boolean
        get() = sharedPreferences.getBoolean("random", false)
        set(f) = sharedPreferences.edit().putBoolean("random", f)
                .apply()

    private var oldReverse: Boolean
        get() = sharedPreferences.getBoolean("reverse", false)
        set(f) = sharedPreferences.edit().putBoolean("reverse", f)
                .apply()

    private var oldRefine: Boolean
        get() = sharedPreferences.getBoolean("refine", false)
        set(f) = sharedPreferences.edit().putBoolean("refine", f)
                .apply()

    private var oldAudio: Boolean
        get() = sharedPreferences.getBoolean("audio", false)
        set(f) = sharedPreferences.edit().putBoolean("audio", f)
                .apply()

    private var oldAlwaysReview: Boolean
        get() = sharedPreferences.getBoolean("alwaysReview", false)
        set(f) = sharedPreferences.edit().putBoolean("alwaysReview", f)
                .apply()

    private var oldManual: Boolean
        get() = sharedPreferences.getBoolean("manual", false)
        set(f) = sharedPreferences.edit().putBoolean("manual", f)
                .apply()

    private var oldUploadStudyPlus: Int
        get() = sharedPreferences.getInt("study_plus", 1)
        set(i) = sharedPreferences.edit().putInt("study_plus", i)
                .apply()

}
