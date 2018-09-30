package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncTaskLoadTest
import jp.gr.java_conf.foobar.testmaker.service.models.StructTest
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

        button_save.setOnClickListener { _ ->

            val dialogLayout = LayoutInflater.from(this@EditProActivity).inflate(R.layout.dialog_alert_confirm, findViewById(R.id.layout_dialog_confirm))

            val checkBox = dialogLayout.findViewById<CheckBox>(R.id.check_alert)

            if (sharedPreferenceManager.confirmSave) {

                saveText()

                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this@EditProActivity, R.style.MyAlertDialogStyle)
            builder.setView(dialogLayout)
            builder.setTitle(getString(R.string.confirm))
            builder.setPositiveButton(android.R.string.ok) { _, _ ->

                if (checkBox.isChecked) sharedPreferenceManager.confirmSave = true

                saveText()

            }

            builder.setNegativeButton(android.R.string.cancel, null)
            builder.show()


        }

        edit_test.setText(realmController.getTest(intent.getLongExtra("testId", -1)).testToString(this,false))

    }

    private fun saveText(){

        val text = edit_test.text.toString()

        val loader = AsyncTaskLoadTest(text,baseContext)
        loader.setCallback(object :AsyncTaskLoadTest.AsyncTaskCallback{
            override fun postExecute(result: StructTest) {

                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_update), Toast.LENGTH_LONG).show()

                realmController.convert(result,intent.getLongExtra("testId", -1))

            }

            override fun progressUpdate(progress: Int) {
            }

            override fun cancel() {
            }

            override fun preExecute() {
            }

        })

        loader.execute()
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