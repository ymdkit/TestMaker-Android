package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.LinearLayout;

import jp.gr.java_conf.foobar.testmaker.service.R;

public class WebViewActivity extends BaseActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        LinearLayout container = findViewById(R.id.container);
        container.addView(createAd());

        sendScreen("HTMLActivity");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        webView = findViewById(R.id.webView);
        webView.loadUrl(getIntent().getStringExtra("url"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(WebViewActivity.this, MainActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);

    }
}
