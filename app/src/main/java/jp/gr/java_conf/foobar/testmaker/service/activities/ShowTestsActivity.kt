package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.TestAndFolderAdapter
import java.util.*

open class ShowTestsActivity : BaseActivity() {

    internal lateinit var testAndFolderAdapter: TestAndFolderAdapter

    protected fun initTestAndFolderAdapter(setValue: () -> Unit){

        testAndFolderAdapter = TestAndFolderAdapter(this,setValue)

        testAndFolderAdapter.setValue()

        testAndFolderAdapter.setOnClickListener(object :TestAndFolderAdapter.OnClickListener{

            override fun onClickPlayTest(id: Long) {

                sendFirebaseEvent("play")

                val test = realmController.getTest(id)

                if (test.getQuestions().size == 0) {

                    Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_questions), Toast.LENGTH_SHORT).show()

                } else {

                    initDialogPlayStart(test)

                }
            }

            override fun onClickEditTest(id: Long) {

                sendFirebaseEvent("edit")
                val i = Intent(this@ShowTestsActivity, EditActivity::class.java)
                i.putExtra("testId", id)
                startActivityForResult(i, REQUEST_EDIT)
            }

            override fun onClickDeleteTest(id: Long) {

                sendFirebaseEvent("delete")

                val test = realmController.getTest(id)

                val builder = AlertDialog.Builder(this@ShowTestsActivity, R.style.MyAlertDialogStyle)
                builder.setTitle(getString(R.string.delete_exam))
                builder.setMessage(getString(R.string.message_delete_exam, test.title))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->

                    realmController.deleteTest(test)
                    testAndFolderAdapter.setValue()

                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.create().show()

            }

            override fun onClickShareTest(id: Long) {
                val test = realmController.getTest(id)

                Toast.makeText(baseContext, getString(R.string.message_share_exam, test.title), Toast.LENGTH_LONG).show()

                try {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "text/plain"

                    intent.putExtra(Intent.EXTRA_TEXT, test.testToString(baseContext, false))
                    startActivity(intent)

                } catch (e: Exception) {

                    sendFirebaseEvent("export error: $e")

                }

            }

            override fun onClickOpen(category: String) {
                val i = Intent(this@ShowTestsActivity, CategorizedActivity::class.java)
                i.putExtra("category", category)

                startActivityForResult(i, REQUEST_EDIT)
            }
        })


    }

    private fun initDialogPlayStart(test: Test) {

        val dialogLayout = LayoutInflater.from(this@ShowTestsActivity).inflate(R.layout.dialog_start, findViewById(R.id.layout_dialog_start))

        val editLimit = dialogLayout.findViewById<EditText>(R.id.set_limit)
        editLimit.setText(test.limit.toString())

        val editStart = dialogLayout.findViewById<EditText>(R.id.set_start_position)
        editStart.setText((test.startPosition + 1 ).toString())

        val checkRandom = dialogLayout.findViewById<CheckBox>(R.id.check_random)
        checkRandom.isChecked = sharedPreferenceManager.random
        checkRandom.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.random = isChecked }

        val checkReverse = dialogLayout.findViewById<CheckBox>(R.id.check_reverse)
        checkReverse.isChecked = sharedPreferenceManager.reverse
        checkReverse.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.reverse = isChecked }

        val checkManual = dialogLayout.findViewById<CheckBox>(R.id.check_manual)
        checkManual.isChecked = sharedPreferenceManager.manual
        checkManual.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.manual = isChecked }

        val checkAudio = dialogLayout.findViewById<CheckBox>(R.id.check_audio)
        checkAudio.isChecked = sharedPreferenceManager.audio
        checkAudio.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.audio = isChecked }

        val checkRefine = dialogLayout.findViewById<CheckBox>(R.id.check_refine)
        checkRefine.isChecked = sharedPreferenceManager.refine
        checkRefine.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.refine = isChecked }

        val checkAlwaysReview = dialogLayout.findViewById<CheckBox>(R.id.check_always_review)
        checkAlwaysReview.isChecked = sharedPreferenceManager.alwaysReview
        checkAlwaysReview.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.alwaysReview = isChecked }

        val checkCaseInsensitive = dialogLayout.findViewById<CheckBox>(R.id.check_case_insensitive)
        checkCaseInsensitive.isChecked = sharedPreferenceManager.isCaseInsensitive
        checkCaseInsensitive.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.isCaseInsensitive = isChecked }
        if(Locale.getDefault().language != "en")checkCaseInsensitive.visibility = View.GONE

        val buttonStart = dialogLayout.findViewById<Button>(R.id.button_start)
        buttonStart.setOnClickListener {
            startAnswer(test, editStart.text.toString(),editLimit.text.toString())
        }

        val builder = AlertDialog.Builder(this@ShowTestsActivity, R.style.MyAlertDialogStyle)
        builder.setView(dialogLayout)
        builder.setTitle(getString(R.string.way))
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.show()

        hideDefaultButtonsFromDialog(dialog)

    }

    private fun hideDefaultButtonsFromDialog(dialog: AlertDialog) {

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        if (positiveButton != null) positiveButton.visibility = View.GONE

        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        if (negativeButton != null) negativeButton.visibility = View.GONE

    }

    private fun startAnswer(test: Test, start:String,limit: String) {

        var incorrect = false

        for (k in 0 until test.getQuestions().size) if (!(test.getQuestions()[k]!!.correct)) incorrect = true

        if (!incorrect && sharedPreferenceManager.refine) {

            Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show()

        } else if (limit == "") {

            Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_number), Toast.LENGTH_SHORT).show()

        }else if (start == "" || start.toInt() > test.getQuestions().size || start.toInt() < 1) {

            Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_start), Toast.LENGTH_SHORT).show()

        } else {

            val i = Intent(this@ShowTestsActivity, PlayActivity::class.java)
            i.putExtra("testId", test.id)

            realmController.updateLimit(test, Integer.parseInt(limit))
            realmController.updateStart(test,Integer.parseInt(start) - 1)
            realmController.updateHistory(test)

            startActivityForResult(i, REQUEST_EDIT)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        testAndFolderAdapter.setValue()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.itemId == R.id.action_compare) {

            AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setTitle(getString(R.string.sort))
                    .setItems(resources.getStringArray(R.array.sort_exam)) { _, which ->

                        sharedPreferenceManager.sort = which

                        testAndFolderAdapter.setValue()

                    }.show()

        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val REQUEST_EDIT = 11111
    }

}
