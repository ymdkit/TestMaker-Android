package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.studyplus.android.sdk.Studyplus
import org.koin.android.ext.android.inject

/**
 * Created by keita on 2016/08/19.
 */

open class BaseActivity : AppCompatActivity() {

    val sharedPreferenceManager: SharedPreferenceManager by inject()

    private lateinit var firebaseAnalytic: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            firebaseAnalytic = FirebaseAnalytics.getInstance(this)
        }

        var info: ApplicationInfo? = null
        try {
            info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

        if (info != null) {
            MobileAds.initialize(this, info.metaData.getString("com.google.android.gms.ads.jp.gr.java_conf.foobar.testmaker.service"))
            Studyplus.instance.setup(info.metaData.getString("studyplus_comsumer_key")!!, info.metaData.getString("secret_studyplus_comsumer_key")!!)

            if (info.metaData.getBoolean("removeAd")) {
                sharedPreferenceManager.isRemovedAd = true
            }
        }
    }

    protected fun sendFirebaseEvent(event: String) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            firebaseAnalytic.logEvent(event, Bundle())
        }
    }

    protected fun initToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun createAd(adView: AdView) {

        if (sharedPreferenceManager.isRemovedAd) {
            adView.visibility = View.GONE
            return
        }

        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("DA539D38B08126EBEF7E059DCA26831C")
                .addTestDevice("4C3BA6538C8F304A33859DC20F66316E")
                .addTestDevice("BDB57B5078A79B87345E711A52F0F995")
                .addTestDevice("BE05B66A799F19F3AF6808EAD82F69F6").build()
        adView.loadAd(adRequest)

    }

}
