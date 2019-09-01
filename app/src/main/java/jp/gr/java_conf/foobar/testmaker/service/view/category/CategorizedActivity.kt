package jp.gr.java_conf.foobar.testmaker.service.view.category

import android.os.Bundle
import android.view.MenuItem
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.share.ShowTestsActivity
import kotlinx.android.synthetic.main.activity_categorized.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class CategorizedActivity : ShowTestsActivity() {

    private val viewModel: CategorizedViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorized)

        createAd(container)

        initToolBar()

        initTestAndFolderAdapter(setValue = {

            testAndFolderAdapter.tests = viewModel.getCategorizedTests(intent.getStringExtra("category"))
            testAndFolderAdapter.categories = ArrayList()
            testAndFolderAdapter.allTests = viewModel.getTests()

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
