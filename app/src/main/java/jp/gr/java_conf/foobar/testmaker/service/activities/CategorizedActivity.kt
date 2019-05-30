package jp.gr.java_conf.foobar.testmaker.service.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.MenuItem
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.android.synthetic.main.activity_categorized.*


class CategorizedActivity : ShowTestsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorized)

        createAd(container)

        initToolBar()

        initTestAndFolderAdapter(setValue = {

            testAndFolderAdapter.tests = realmController.getCategorizedList(intent.getStringExtra("category"))
            testAndFolderAdapter.categories = ArrayList()
            testAndFolderAdapter.allTests = realmController.list

        })

        recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
        recycler_view.setHasFixedSize(true) // アイテムは固定サイズ
        recycler_view.adapter = testAndFolderAdapter

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
