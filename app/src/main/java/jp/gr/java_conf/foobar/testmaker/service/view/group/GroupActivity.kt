package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityGroupBinding
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity

class GroupActivity : BaseActivity() {

    private val binding by lazy { DataBindingUtil.setContentView<ActivityGroupBinding>(this, R.layout.activity_group) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createAd(binding.adView)

        supportFragmentManager.findFragmentById(binding.fragmentNav.id)?.let {
            val appBarConfiguration = AppBarConfiguration
                    .Builder()
                    .setFallbackOnNavigateUpListener {
                        onBackPressed()
                        true
                    }.build()

            setSupportActionBar(binding.toolbar)
            binding.toolbar.setupWithNavController(it.findNavController(), appBarConfiguration)

            if (intent.hasExtra(EXTRA_GROUP_ID)) {
                intent.getStringExtra(EXTRA_GROUP_ID)?.let { groupId ->
                    it.findNavController().navigate(GroupDetailFragmentDirections.actionGlobalGroupDetail(groupId = groupId))
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }

    companion object {

        const val EXTRA_GROUP_ID = "groupId"

        fun startActivityWithGroupId(activity: Activity, groupId: String) {
            val intent = Intent(activity, GroupActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, groupId)
            }
            activity.startActivity(intent)
        }
    }
}