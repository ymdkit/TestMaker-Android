package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.android.synthetic.main.activity_help.*

class WebViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        container.addView(createAd())

        sendScreen("WebViewActivity")

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webView.loadUrl(intent.getStringExtra("url"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.itemId == android.R.id.home) {

            finish()

            return true
        }

        return super.onOptionsItemSelected(item)

    }
}
