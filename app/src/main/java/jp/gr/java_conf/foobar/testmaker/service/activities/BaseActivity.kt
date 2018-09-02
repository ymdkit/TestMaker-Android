package jp.gr.java_conf.foobar.testmaker.service.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.analytics.HitBuilders
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.TestMakerApplication
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController

/**
 * Created by keita on 2016/08/19.
 */

open class BaseActivity : AppCompatActivity() {

    internal lateinit var realmController: RealmController

    internal lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TestMakerApplication

        realmController = RealmController(applicationContext, app.config)

        sharedPreferenceManager = SharedPreferenceManager(this)
    }

    protected fun sendScreen(screenName: String) {

        val app = application as TestMakerApplication
        val t = app.tracker
        t.setScreenName(screenName)
        t.send(HitBuilders.ScreenViewBuilder().build())

    }

    protected fun sendEvent(action: String) {

        val app = application as TestMakerApplication
        val t = app.tracker

        t.send(HitBuilders.EventBuilder()
                .setCategory("event")
                .setAction(action)
                .build())


    }

    protected fun initToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun createAd(): AdView {
        val adView = AdView(this)
        adView.adUnitId = "ca-app-pub-8942090726462263/8420884238"
        adView.adSize = AdSize.BANNER
        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("DA539D38B08126EBEF7E059DCA26831C")
                .addTestDevice("BDB57B5078A79B87345E711A52F0F995")
                .build()
        adView.loadAd(adRequest)

        return adView
    }

    override fun onDestroy() {
        super.onDestroy()

        realmController.close()
    }
}
