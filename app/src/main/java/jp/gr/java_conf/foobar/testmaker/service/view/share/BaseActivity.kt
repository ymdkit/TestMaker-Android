package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherAdView
import com.google.firebase.analytics.FirebaseAnalytics

import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.studyplus.android.sdk.Studyplus

/**
 * Created by keita on 2016/08/19.
 */

open class BaseActivity : AppCompatActivity() {

    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private lateinit var firebaseAnalytic: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferenceManager = SharedPreferenceManager(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            firebaseAnalytic = FirebaseAnalytics.getInstance(this)
        }

        var info: ApplicationInfo? = null
        try {
            info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (info != null) {
            MobileAds.initialize(this, info.metaData.getString("testmaker_admob_key"))
            Studyplus.instance.setup(info.metaData.getString("studyplus_comsumer_key")!!, info.metaData.getString("secret_studyplus_comsumer_key")!!)

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

    protected fun createAd(container: LinearLayout) {

        if (sharedPreferenceManager.isRemovedAd) {
            container.visibility = View.GONE
            return
        }

        val adView = PublisherAdView(this)
        adView.adUnitId = "ca-app-pub-8942090726462263/8420884238"
        adView.setAdSizes(AdSize.BANNER)
        val adRequest = PublisherAdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("DA539D38B08126EBEF7E059DCA26831C")
                .addTestDevice("BDB57B5078A79B87345E711A52F0F995")
                .addTestDevice("4C3BA6538C8F304A33859DC20F66316E")
                .build()
        adView.loadAd(adRequest)

        container.addView(adView)
    }

}
