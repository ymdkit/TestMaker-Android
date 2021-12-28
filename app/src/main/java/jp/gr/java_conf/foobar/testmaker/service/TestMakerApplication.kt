package jp.gr.java_conf.foobar.testmaker.service

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.gr.java_conf.foobar.testmaker.service.di.getTestMakerModules
import jp.gr.java_conf.foobar.testmaker.service.infra.db.Migration
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
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

        var info: ApplicationInfo? = null
        try {
            info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (info != null) {
            MobileAds.initialize(this, info.metaData.getString("com.google.android.gms.ads.jp.gr.java_conf.foobar.testmaker.service"))
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder().setTestDeviceIds(
                listOf(
                    AdRequest.DEVICE_ID_EMULATOR,
                    "DA539D38B08126EBEF7E059DCA26831C",
                    "4C3BA6538C8F304A33859DC20F66316E",
                    "BDB57B5078A79B87345E711A52F0F995",
                    "BE05B66A799F19F3AF6808EAD82F69F6",
                    "DC457DC275E092B11752A53455350569")
            ).build())

            if (info.metaData.getBoolean("removeAd")) {
                SharedPreferenceManager(applicationContext).isRemovedAd = true
            }
        }
    }

    companion object {
        lateinit var instance: TestMakerApplication private set  // <- これ
    }
}