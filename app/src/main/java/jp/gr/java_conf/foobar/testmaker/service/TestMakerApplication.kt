package jp.gr.java_conf.foobar.testmaker.service

import androidx.multidex.MultiDexApplication
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

    lateinit var config: RealmConfiguration

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        config = RealmConfiguration.Builder()
                .schemaVersion(10)
                .build()

        try {
            Realm.migrateRealm(config, Migration())
        } catch (ignored: FileNotFoundException) {
            // If the Realm file doesn't exist, just ignore.
        }

        startKoin(this, listOf(
                getTestMakerModules(Realm.getInstance(config))
        ))


    }
}