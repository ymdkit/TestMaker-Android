package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
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

    private var progress: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytic = FirebaseAnalytics.getInstance(this)

        var info: ApplicationInfo? = null
        try {
            info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

        if (info != null) {
            MobileAds.initialize(this, info.metaData.getString("com.google.android.gms.ads.jp.gr.java_conf.foobar.testmaker.service"))
            MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(
                    listOf(AdRequest.DEVICE_ID_EMULATOR,
                            "DA539D38B08126EBEF7E059DCA26831C",
                            "4C3BA6538C8F304A33859DC20F66316E",
                            "BDB57B5078A79B87345E711A52F0F995",
                            "BE05B66A799F19F3AF6808EAD82F69F6")
            ).build())
            Studyplus.instance.setup(info.metaData.getString("studyplus_comsumer_key")!!, info.metaData.getString("secret_studyplus_comsumer_key")!!)

            if (info.metaData.getBoolean("removeAd")) {
                sharedPreferenceManager.isRemovedAd = true
            }
        }
    }

    protected fun sendFirebaseEvent(event: String) {
        firebaseAnalytic.logEvent(event, Bundle())
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

        adView.loadAd(AdRequest.Builder().build())
    }

    protected fun showProgress(title: String = "") {
        progress = AlertDialog.Builder(this)
                .setTitle(title)
                .setCancelable(false)
                .setView(LayoutInflater.from(this).inflate(R.layout.dialog_progress, findViewById(R.id.layout_progress)))
                .create().also {
                    it.show()
                }
    }

    protected fun hideProgress() {
        progress?.dismiss()
    }
}
