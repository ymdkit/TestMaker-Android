package jp.gr.java_conf.foobar.testmaker.service

import androidx.multidex.MultiDexApplication
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.gr.java_conf.foobar.testmaker.service.models.Migration
import jp.gr.java_conf.foobar.testmaker.service.modules.getTestMakerModules
import org.koin.android.ext.android.startKoin
import java.io.FileNotFoundException

/**
 * Created by keita on 2016/07/17.
 */
class TestMakerApplication : MultiDexApplication() {

    private var tracker: Tracker? = null

    lateinit var config: RealmConfiguration

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(
                getTestMakerModules()
        ))

        Realm.init(this)

        config = RealmConfiguration.Builder()
                .schemaVersion(10)
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