package jp.gr.java_conf.foobar.testmaker.service.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v7.app.AlertDialog
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
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.TestAdapter
import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter
import java.util.*

open class ShowTestsActivity : BaseActivity() {

    internal lateinit var testAdapter: TestAdapter

    internal var parentAdapter: ScrambleAdapter<*>? = null

    internal val REQUEST_EDIT = 11111

    protected fun initTestAdapter() {

        testAdapter = TestAdapter(this)

        testAdapter.setOnClickListener(object : TestAdapter.OnClickListener {

            override fun onClickPlayTest(id: Long) {

                val test = realmController.getTest(id)

                if (test.getQuestions().size == 0) {

                    Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_questions), Toast.LENGTH_SHORT).show()

                } else {

                    initDialogPlayStart(test)

                }
            }

            override fun onClickEditTest(id: Long) {

                val i = Intent(this@ShowTestsActivity, EditActivity::class.java)

                i.putExtra("testId", id)

                startActivityForResult(i, REQUEST_EDIT)
            }

            override fun onClickDeleteTest(id: Long) {

                val test = realmController.getTest(id)

                val builder = AlertDialog.Builder(this@ShowTestsActivity, R.style.MyAlertDialogStyle)
                builder.setTitle(getString(R.string.delete_exam))
                builder.setMessage(getString(R.string.message_delete_exam, test.title))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->

                    realmController.deleteTest(test)

                    if (parentAdapter != null) parentAdapter!!.notifyDataSetChanged()

                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.create().show()

            }
        })
    }

    private fun initDialogPlayStart(test: Test) {

        val dialogLayout = LayoutInflater.from(this@ShowTestsActivity).inflate(R.layout.dialog_start, findViewById(R.id.layout_dialog_start))

        val editLimit = dialogLayout.findViewById<EditText>(R.id.set_limit)
        editLimit.setText(test.limit.toString())

        val editStart = dialogLayout.findViewById<EditText>(R.id.set_start_position)
        editStart.setText((test.startPosition + 1 ).toString())

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
        if(Locale.getDefault().language == "ja")checkCaseInsensitive.visibility = View.GONE

        val actionNormal = dialogLayout.findViewById<Button>(R.id.action_normal)
        actionNormal.setOnClickListener { startAnswer(test, editStart.text.toString(),editLimit.text.toString(), false) }

        val actionRandom = dialogLayout.findViewById<Button>(R.id.action_random)
        actionRandom.setOnClickListener { startAnswer(test, editStart.text.toString(),editLimit.text.toString(), true) }

        if (Build.VERSION.SDK_INT >= 21) {
            actionNormal.stateListAnimator = null
            actionRandom.stateListAnimator = null
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

    private fun startAnswer(test: Test, start:String,limit: String, rand: Boolean) {

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

            if (rand) i.putExtra("random", 1)

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

        if (resultCode == Activity.RESULT_CANCELED) if (parentAdapter != null) parentAdapter!!.notifyDataSetChanged()

        if (requestCode == REQUEST_EDIT) if (parentAdapter != null) parentAdapter!!.notifyDataSetChanged()

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

                        if (parentAdapter != null) parentAdapter!!.notifyDataSetChanged()

                    }.show()

        }

        return super.onOptionsItemSelected(item)
    }

}
