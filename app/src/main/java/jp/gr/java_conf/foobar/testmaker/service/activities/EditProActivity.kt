package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadTest
import kotlinx.android.synthetic.main.activity_edit_pro.*

class EditProActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pro)

        sendScreen("EditProActivity")

        setSupportActionBar(pro_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        container.addView(createAd())

        if (Build.VERSION.SDK_INT >= 21) button_save.stateListAnimator = null

        button_save.setOnClickListener {
            val text = edit_test.text.toString()

            val loader = AsyncLoadTest(text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(), null, realmController, this@EditProActivity, intent.getLongExtra("testId", -1))
            loader.execute()
        }

        edit_test.setText(realmController.getTest(intent.getLongExtra("testId", -1)).testToString(this))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_pro, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val actionId = item.itemId

        if (actionId == android.R.id.home) {

            val i = Intent(this@EditProActivity, EditActivity::class.java)

            i.putExtra("testId", intent.getLongExtra("testId", -1))

            startActivity(i)

            return true

        } else if (actionId == R.id.nav_help) {

            startActivity(Intent(Intent.ACTION_VIEW, Uri
                    .parse(getString(R.string.help_url))))

        }


        return super.onOptionsItemSelected(item)
    }

}