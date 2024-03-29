package jp.gr.java_conf.foobar.testmaker.service

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager

/**
 * Created by keita on 2016/07/17.
 */
@HiltAndroidApp
class TestMakerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        var info: ApplicationInfo? = null
        try {
            info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (info != null) {
            MobileAds.initialize(this)
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder().setTestDeviceIds(
                    listOf(
                        AdRequest.DEVICE_ID_EMULATOR,
                        "DA539D38B08126EBEF7E059DCA26831C",
                        "4C3BA6538C8F304A33859DC20F66316E",
                        "BDB57B5078A79B87345E711A52F0F995",
                        "BE05B66A799F19F3AF6808EAD82F69F6",
                        "DC457DC275E092B11752A53455350569",
                        "E59693DE3AAEF610C4917627DF5CE9F7"
                    )
                ).build()
            )

            if (info.metaData.getBoolean("removeAd")) {
                SharedPreferenceManager(applicationContext).isRemovedAd = true
            }
        }
    }
}