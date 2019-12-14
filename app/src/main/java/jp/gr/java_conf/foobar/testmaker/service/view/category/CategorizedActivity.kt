package jp.gr.java_conf.foobar.testmaker.service.view.category

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityCategorizedBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.view.share.ShowTestsActivity
import kotlinx.android.synthetic.main.activity_categorized.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class CategorizedActivity : ShowTestsActivity() {

    private val viewModel: CategorizedViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorized)

        val binding = DataBindingUtil.setContentView<ActivityCategorizedBinding>(this, R.layout.activity_categorized)
        createAd(binding.adView)

        initToolBar()

        initTestAndFolderAdapter()

        viewModel.getCategorizedTests(intent.getStringExtra("category")).observeNonNull(this){
            mainController.tests = it
        }

        viewModel.getTests().observeNonNull(this){
            viewModel.fetchCategorizedTests(intent.getStringExtra("category"))
        }

        recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
        recycler_view.setHasFixedSize(true) // アイテムは固定サイズ
        recycler_view.adapter = mainController.adapter

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {

            finish()

            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
