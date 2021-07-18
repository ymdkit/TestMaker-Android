package jp.gr.java_conf.foobar.testmaker.service

import android.content.pm.PackageManager
import androidx.multidex.MultiDexApplication
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.gr.java_conf.foobar.testmaker.service.di.getTestMakerModules
import jp.gr.java_conf.foobar.testmaker.service.infra.db.Migration
import org.koin.android.ext.android.startKoin
import java.io.FileNotFoundException

/**
 * Created by keita on 2016/07/17.
 */
class TestMakerApplication : MultiDexApplication() {

    lateinit var config: RealmConfiguration

    override fun onCreate() {
        super.onCreate()

        instance = this

        Realm.init(this)

        config = RealmConfiguration.Builder()
            .schemaVersion(18)
            .build()

        try {
            Realm.migrateRealm(config, Migration())
        } catch (ignored: FileNotFoundException) {
            // If the Realm file doesn't exist, just ignore.
        }

        startKoin(
            this, listOf(
                getTestMakerModules(
                    realm = Realm.getInstance(config),
                    info = packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA
                    )
                )
            )
        )
    }

    companion object {
        lateinit var instance: TestMakerApplication private set  // <- これ
    }
}