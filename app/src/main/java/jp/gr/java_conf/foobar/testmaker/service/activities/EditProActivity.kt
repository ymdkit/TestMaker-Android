package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.toTest
import kotlinx.android.synthetic.main.activity_edit_pro.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pro)

        setSupportActionBar(pro_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        createAd(container)

        if (Build.VERSION.SDK_INT >= 21) button_save.stateListAnimator = null

        button_save.setOnClickListener {

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

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) { text.toTest(baseContext) }.let{
                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_update), Toast.LENGTH_LONG).show()

                it.id = intent.getLongExtra("testId", -1)
                realmController.copyToRealm(it)

                //realmController.convert(it,intent.getLongExtra("testId", -1))

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_pro, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val actionId = item.itemId

        if (actionId == android.R.id.home) {

            finish()

            return true

        } else if (actionId == R.id.nav_help) {

            startActivity(Intent(Intent.ACTION_VIEW, Uri
                    .parse(getString(R.string.help_url))))

        }

        return super.onOptionsItemSelected(item)
    }

}