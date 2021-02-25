package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityGroupBinding
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity

class GroupActivity : BaseActivity() {

    private val binding by lazy { DataBindingUtil.setContentView<ActivityGroupBinding>(this, R.layout.activity_group) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        createAd(binding.adView)

        initToolBar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> {
                    true
                }
            }
}