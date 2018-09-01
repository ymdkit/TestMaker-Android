package jp.gr.java_conf.foobar.testmaker.service.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.CategorizedAdapter
import kotlinx.android.synthetic.main.activity_categorized.*

class CategorizedActivity : ShowTestsActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorized)

        sendScreen("CategorizedActivity")

        container.addView(createAd())

        initToolBar()

        initTestAdapter()

        parentAdapter = CategorizedAdapter(this, realmController.mixedList,
                null, realmController, intent.getStringExtra("category"),
                testAdapter

        )

        recyclerview.layoutManager = LinearLayoutManager(applicationContext)
        recyclerview.setHasFixedSize(true) // アイテムは固定サイズ
        recyclerview.adapter = parentAdapter

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
