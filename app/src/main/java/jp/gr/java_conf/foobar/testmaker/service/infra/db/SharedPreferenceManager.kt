package jp.gr.java_conf.foobar.testmaker.service.infra.db

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.gr.java_conf.foobar.testmaker.service.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("question", Context.MODE_PRIVATE)

    var isRemovedAd: Boolean
        get() = sharedPreferences.getBoolean("isRemovedAd", false)
        set(i) = sharedPreferences.edit().putBoolean("isRemovedAd", i).apply()

    //preferencesと連携するため

    private val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var audio: Boolean
        get() = defaultPreferences.getBoolean("audio", true)
        set(f) = defaultPreferences.edit().putBoolean("audio", f)
            .apply()

    var reverse: Boolean
        get() = defaultPreferences.getBoolean("reverse", false)
        set(f) = defaultPreferences.edit().putBoolean("reverse", f)
            .apply()

    var uploadStudyPlus: String
        get() = defaultPreferences.getString(
            "study_plus",
            context.resources.getStringArray(R.array.upload_setting_study_plus_values)[1]
        )
            ?: "auto"
        set(i) = defaultPreferences.edit().putString("study_plus", i).apply()

    var playCount: Int
        get() = defaultPreferences.getInt("play_count", 0)
        set(i) = defaultPreferences.edit().putInt("play_count", i).apply()

}
