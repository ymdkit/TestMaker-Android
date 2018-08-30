package jp.gr.java_conf.foobar.testmaker.service;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.FileNotFoundException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import jp.gr.java_conf.foobar.testmaker.service.models.Migration;

/**
 * Created by keita on 2016/07/17.
 */
public class TestMakerApplication extends Application {
    private final String PROPERTY_ID = "UA-73836463-7";

    private Tracker tracker;

    public RealmConfiguration config;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        config = new RealmConfiguration.Builder()
                .schemaVersion(5)
                .build();

        try {
            Realm.migrateRealm(config, new Migration());
        } catch (FileNotFoundException ignored) {
            // If the Realm file doesn't exist, just ignore.
        }


    }

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // Trackerの初期化はR.xmlからも可能.
            // https://developers.google.com/analytics/devguides/collection/android/v4/?hl=ja#analytics-xml
            tracker = analytics.newTracker(PROPERTY_ID);
        }
        return tracker;
    }
}