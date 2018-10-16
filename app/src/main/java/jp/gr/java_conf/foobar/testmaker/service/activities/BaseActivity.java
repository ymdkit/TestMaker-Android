package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager;
import jp.gr.java_conf.foobar.testmaker.service.TestMakerApplication;
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController;
import jp.studyplus.android.sdk.Studyplus;

/**
 * Created by keita on 2016/08/19.
 */

public class BaseActivity extends AppCompatActivity {

    RealmController realmController;

    public SharedPreferenceManager sharedPreferenceManager;

    final static int REQUEST_CODE_AUTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TestMakerApplication app = (TestMakerApplication) getApplication();

        realmController = new RealmController(getApplicationContext(), app.getConfig());

        sharedPreferenceManager = new SharedPreferenceManager(this);

        Studyplus.getInstance().setup("U6867w2Zt2tT2CRjJRteaMAUCvnEDfXZ", "d9cCv8aZCDaUL56bhZY5HBnzktpzpYefVAn3hV5hjjqmWhF97j985wyuMjLExLvQ");

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

    protected void createAd(LinearLayout container){

        if(sharedPreferenceManager.isRemovedAd()){
            container.setVisibility(View.GONE);
            return;
        }

        AdView adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-8942090726462263/8420884238");
        adView.setAdSize(AdSize.BANNER);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("DA539D38B08126EBEF7E059DCA26831C")
                .addTestDevice("BDB57B5078A79B87345E711A52F0F995")
                .build();
        adView.loadAd(adRequest);

        container.addView(adView);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realmController.close();
    }
}
