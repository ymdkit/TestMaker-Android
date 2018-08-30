package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager;
import jp.gr.java_conf.foobar.testmaker.service.TestMakerApplication;
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController;

/**
 * Created by keita on 2016/08/19.
 */

//edited by macBook air

public class BaseActivity extends AppCompatActivity {

    RealmController realmController;

    SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TestMakerApplication app = (TestMakerApplication) getApplication();

        realmController = new RealmController(getApplicationContext(), app.config);

        sharedPreferenceManager = new SharedPreferenceManager(this);
    }

    protected void sendScreen(String screenName) {

        TestMakerApplication app = (TestMakerApplication) getApplication();
        Tracker t = app.getTracker();
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());

    }

    protected void sendEvent(String action) {

        TestMakerApplication app = (TestMakerApplication) getApplication();
        Tracker t = app.getTracker();

        t.send(new HitBuilders.EventBuilder()
                .setCategory("event")
                .setAction(action)
                .build());


    }

    protected void initToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected AdView createAd() {
        AdView adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-8942090726462263/8420884238");
        adView.setAdSize(AdSize.BANNER);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("DA539D38B08126EBEF7E059DCA26831C")
                .addTestDevice("BDB57B5078A79B87345E711A52F0F995")
                .build();
        adView.loadAd(adRequest);

        return adView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realmController.close();
    }
}
