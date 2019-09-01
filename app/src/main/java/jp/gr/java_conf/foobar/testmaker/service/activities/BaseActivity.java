package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

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

    protected FirebaseAnalytics firebaseAnalytic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TestMakerApplication app = (TestMakerApplication) getApplication();


        realmController = new RealmController(getApplicationContext(), app.getConfig());

        sharedPreferenceManager = new SharedPreferenceManager(this);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            firebaseAnalytic = FirebaseAnalytics.getInstance(this);
        }

        ApplicationInfo info
                = null;
        try {
            info = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        MobileAds.initialize(this, info.metaData.getString("testmaker_admob_key"));

        Studyplus.getInstance().setup(info.metaData.getString("studyplus_comsumer_key"), info.metaData.getString("secret_studyplus_comsumer_key"));

    }

    protected void sendFirebaseEvent(String event){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            firebaseAnalytic.logEvent(event, new Bundle());
        }
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

        PublisherAdView adView = new PublisherAdView(this);
        adView.setAdUnitId("ca-app-pub-8942090726462263/8420884238");
        adView.setAdSizes(AdSize.BANNER);
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("DA539D38B08126EBEF7E059DCA26831C")
                .addTestDevice("BDB57B5078A79B87345E711A52F0F995")
                .build();
        adView.loadAd(adRequest);

        container.addView(adView);
    }

}
