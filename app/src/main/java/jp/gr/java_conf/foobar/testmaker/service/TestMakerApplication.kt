package jp.gr.java_conf.foobar.testmaker.service

import android.app.Application

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker

import java.io.FileNotFoundException

import io.realm.Realm
import io.realm.RealmConfiguration
import jp.gr.java_conf.foobar.testmaker.service.models.Migration

/**
 * Created by keita on 2016/07/17.
 */
class TestMakerApplication : Application() {

    private var tracker: Tracker? = null

    lateinit var config: RealmConfiguration

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        config = RealmConfiguration.Builder()
                .schemaVersion(6)
                .build()

        try {
            Realm.migrateRealm(config, Migration())
        } catch (ignored: FileNotFoundException) {
            // If the Realm file doesn't exist, just ignore.
        }


    }

    @Synchronized
    fun getTracker(): Tracker {
        if (tracker == null) {
            val analytics = GoogleAnalytics.getInstance(this)
            // Trackerの初期化はR.xmlからも可能.
            // https://developers.google.com/analytics/devguides/collection/android/v4/?hl=ja#analytics-xml
            tracker = analytics.newTracker(PROPERTY_ID)
        }
        return tracker!!
    }

    companion object {
        private const val PROPERTY_ID = "UA-73836463-7"
    }
}