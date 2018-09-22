package jp.gr.java_conf.foobar.testmaker.service.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.SwitchCompat
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import com.isseiaoki.simplecropview.CropImageView
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadImage
import jp.gr.java_conf.foobar.testmaker.service.models.CategoryEditor
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import jp.gr.java_conf.foobar.testmaker.service.models.StructQuestion
import jp.gr.java_conf.foobar.testmaker.service.views.ColorChooser
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.EditAdapter
import kotlinx.android.synthetic.main.activity_edit.*
import java.io.IOException
import java.util.*

/**
 * Created by keita on 2017/02/12.
 */

open class EditActivity : BaseActivity() {

    internal lateinit var editAdapter: EditAdapter

    internal var typeQuestion: Int = 0

    internal var imagePath: String = ""
    internal var testId: Long = 0
    internal var questionId: Long = 0

    private lateinit var searchView: SearchView

    private val fileName: String
        get() {
            val c = Calendar.getInstance()
            return c.get(Calendar.YEAR).toString() + "_" + (c.get(Calendar.MONTH) + 1) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND) + "_" + c.get(Calendar.MILLISECOND) + ".png"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        sendScreen("EditActivity")

        container.addView(createAd())

        initToolBar()

        imagePath = ""
        testId = intent.getLongExtra("testId", -1)
        questionId = -1

        initAdapter()

        initViews()

        showLayoutWrite()

    }

    private fun initAdapter() {

        editAdapter = EditAdapter(this, realmController, testId)

        editAdapter.setOnClickListener(object : EditAdapter.OnClickListener {
            override fun onClickEditQuestion(position: Int) {

                showLayoutEdit()

                text_title.text = getString(R.string.edit_question)
                button_cancel.visibility = View.VISIBLE
                button_add.text = getString(R.string.save_question)

                val question: Quest = if (editAdapter.filter) realmController.getFilterQuestions(testId, editAdapter.searchWord)[position]
                else realmController.getQuestion(testId, position)

                set_problem.setText(question.problem)
                questionId = question.id

                if (question.imagePath != "") {

                    imagePath = question.imagePath
                    val task = AsyncLoadImage(applicationContext, button_image, imagePath, 1)
                    task.execute(null)

                } else {

                    button_image.setImageResource(R.drawable.ic_photo_white)

                }

                if (question.explanation != "") {

                    textInputLayout_explanation.visibility = View.VISIBLE
                    set_explanation.setText(question.explanation)

                } else {

                    textInputLayout_explanation.visibility = View.GONE

                }

                when (question.type) {

                    Constants.WRITE -> {
                        showLayoutWrite()

                        set_answer_write.setText(question.answer)

                        sharedPreferenceManager.numAnswers = 1

                        button_type.text = getString(R.string.action_choose)
                    }

                    Constants.SELECT -> {

                        showLayoutSelect()

                        sharedPreferenceManager.numOthers = question.selections.size

                        edit_select_view.reloadOthers(question.selections.size)

                        edit_select_view.setAnswer(question.answer)

                        edit_select_view.setOthers(question.selections)

                        button_type.text = getString(R.string.action_write)

                        sharedPreferenceManager.auto = question.auto

                        edit_select_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers)

                    }
                    Constants.COMPLETE -> {

                        showLayoutComplete()

                        sharedPreferenceManager.numAnswers = question.answers.size

                        edit_complete_view.reloadAnswers(question.answers.size)

                        edit_complete_view.setAnswers(question)

                        button_type.text = getString(R.string.action_write)
                    }

                    Constants.SELECT_COMPLETE -> {

                        showLayoutSelectComplete()

                        sharedPreferenceManager.numAnswersSelect = question.answers.size

                        sharedPreferenceManager.numOthers = question.selections.size + question.answers.size - 1

                        edit_select_complete_view.setAnswerNum(question.answers.size)

                        edit_select_complete_view.reloadSelects(question.answers.size + question.selections.size)

                        edit_select_complete_view.setSelections(question.answers,question.selections)

                        button_type.text = getString(R.string.action_choose)

                        sharedPreferenceManager.auto = question.auto

                        edit_select_complete_view.setAuto(sharedPreferenceManager.auto,sharedPreferenceManager.numOthers+1)

                    }

                }

            }

            override fun onClickDeleteQuestion(data: Quest) {

                val builder = AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
                builder.setTitle(getString(R.string.delete_question))
                builder.setMessage(getString(R.string.message_delete, data.problem))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->

                    if (data.imagePath != "") {
                        deleteFile(data.imagePath)
                    }

                    realmController.deleteQuestion(data)

                    editAdapter.notifyDataSetChanged()

                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.create().show()

            }
        })

    }

    private fun showLayoutEdit() {

        layout_body.visibility = View.VISIBLE
        ImageButton_expand.setImageResource(R.drawable.ic_expand_less_black)
        set_problem.isFocusable = true
        set_problem.requestFocus()
        button_add.visibility = View.VISIBLE

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        editAdapter.notifyDataSetChanged()

        if (resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            val uri: Uri?
            if (resultData != null) {

                try {

                    uri = resultData.data

                    val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_crop,
                            findViewById(R.id.layout_dialog_crop_image))

                    val cropView = dialogLayout.findViewById<CropImageView>(R.id.cropImageView)
                    cropView.imageBitmap = getBitmapFromUri(uri)

                    val builder = AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
                    builder.setView(dialogLayout)
                    builder.setTitle(getString(R.string.trim))
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setNegativeButton(android.R.string.cancel, null)

                    val dialog = builder.show()

                    val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setOnClickListener {

                        imagePath = fileName

                        val task = AsyncLoadImage(applicationContext, button_image, imagePath, 0)
                        task.execute(cropView.croppedBitmap)

                        dialog.dismiss()
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri?): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun cancelEditing() {

        hideLayoutEdit()

        button_cancel.visibility = View.GONE

        reset()

    }

    private fun hideLayoutEdit() {

        layout_body.visibility = View.GONE
        button_cancel.visibility = View.GONE

        ImageButton_expand.setImageResource(R.drawable.ic_expand_more_black)
        text_title.text = getString(R.string.add_question)

    }

    private fun addQuestion() {

        if (set_problem.text.toString() == "") {

            Toast.makeText(applicationContext, getString(R.string.message_shortage), Toast.LENGTH_LONG).show()

            return

        }

        when (typeQuestion) {

            Constants.WRITE ->

                if (set_answer_write.text.toString() == "") {

                    Toast.makeText(applicationContext, getString(R.string.message_shortage), Toast.LENGTH_LONG).show()

                    return

                } else {

                    val p = StructQuestion(set_problem.text.toString(), set_answer_write.text.toString())

                    p.setImagePath(imagePath)
                    p.setExplanation(set_explanation.text.toString())
                    realmController.addQuestion(testId, p, questionId)

                }

            Constants.SELECT ->

                if (edit_select_view.isFilled()) {

                    val p = StructQuestion(set_problem.text.toString(), edit_select_view.getAnswer(), edit_select_view.getOthers())
                    p.setAuto(sharedPreferenceManager.auto)
                    p.setImagePath(imagePath)
                    p.setExplanation(set_explanation.text.toString())
                    realmController.addQuestion(testId, p, questionId)

                } else {

                    Toast.makeText(applicationContext, getString(R.string.message_shortage), Toast.LENGTH_LONG).show()

                    return

                }


            Constants.COMPLETE ->

                if (edit_complete_view.isFilled()) {

                    val p = StructQuestion(set_problem.text.toString(), edit_complete_view.getAnswers())
                    p.setImagePath(imagePath)
                    p.setExplanation(set_explanation.text.toString())

                    realmController.addQuestion(testId, p, questionId)

                } else {

                    Toast.makeText(applicationContext, getString(R.string.message_shortage), Toast.LENGTH_LONG).show()

                    return
                }

            Constants.SELECT_COMPLETE ->

                if (edit_select_complete_view.isFilled()) {

                    val p = StructQuestion(set_problem.text.toString(), edit_select_complete_view.getAnswers(),edit_select_complete_view.getOthers())
                    p.setAuto(sharedPreferenceManager.auto)
                    p.setImagePath(imagePath)
                    p.setExplanation(set_explanation.text.toString())

                    realmController.addQuestion(testId, p, questionId)

                } else {

                    Toast.makeText(applicationContext, getString(R.string.message_shortage), Toast.LENGTH_LONG).show()

                    return
                }




        }

        reset()

        editAdapter.notifyDataSetChanged()

        button_cancel.visibility = View.GONE

        text_title.text = if (edit_select_view.visibility == View.GONE) getString(R.string.add_question_write) else getString(R.string.add_question_choose)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_edit, menu)

        searchView = menu.findItem(R.id.menu_search).actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {

                editAdapter.searchWord = s

                editAdapter.filter = true

                editAdapter.notifyDataSetChanged()

                return false
            }

            override fun onQueryTextChange(s: String): Boolean {

                editAdapter.searchWord = s

                editAdapter.filter = true

                editAdapter.notifyDataSetChanged()

                return false
            }
        })

        searchView.setOnCloseListener {

            editAdapter.searchWord = ""

            editAdapter.filter = false

            editAdapter.notifyDataSetChanged()

            false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val actionId = item.itemId

        when {
            actionId == R.id.action_setting -> {

                val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_edit_test,
                        findViewById(R.id.layout_dialog_edit_test))

                val name = dialogLayout.findViewById<EditText>(R.id.edit_title)

                val buttonCate = dialogLayout.findViewById<Button>(R.id.button_category)

                val colorChooser = dialogLayout.findViewById<ColorChooser>(R.id.color_chooser)

                if (Build.VERSION.SDK_INT >= 21) buttonCate.stateListAnimator = null

                buttonCate.tag = realmController.getTest(testId).category

                if (realmController.getTest(testId).category == "") {

                    buttonCate.text = getString(R.string.category)
                } else {
                    buttonCate.text = realmController.getTest(testId).category
                }

                buttonCate.setOnClickListener {
                    val categoryEditor = CategoryEditor(this@EditActivity, buttonCate, realmController, null)
                    categoryEditor.setCategory()
                }

                buttonCate.setOnLongClickListener {

                    // アラートダイアログ を生成
                    val builder = AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
                    builder.setMessage(getString(R.string.cancel_category))
                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        buttonCate.tag = ""
                        buttonCate.text = getString(R.string.category)
                        buttonCate.background = ResourcesCompat.getDrawable(resources, R.drawable.button_blue, null)
                    }
                    builder.setNegativeButton(android.R.string.cancel, null)
                    builder.create().show()

                    false
                }

                name.setText(realmController.getTest(testId).title)

                colorChooser.setColorId(realmController.getTest(testId).color)

                val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                builder.setView(dialogLayout)
                builder.setTitle(getString(R.string.edit_exam))
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setNegativeButton(android.R.string.cancel, null)

                val dialog = builder.show()

                val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                button.setOnClickListener {
                    // 場合によっては自分で明示的に閉じる必要がある
                    val sb = name.text as SpannableStringBuilder

                    if (sb.toString() == "") {

                        Toast.makeText(applicationContext, getString(R.string.message_wrong), Toast.LENGTH_SHORT).show()

                    } else {

                        realmController.updateTest(realmController.getTest(testId), sb.toString(), colorChooser.getColorId(), buttonCate.tag.toString())

                        dialog.dismiss()
                    }
                }

                dialog.show()

            }
            item.itemId == android.R.id.home -> {

                finish()

                return true
            }
            item.itemId == R.id.action_edit_pro -> {

                val i = Intent(this@EditActivity, EditProActivity::class.java)

                i.putExtra("testId", testId)
                startActivityForResult(i, 0)

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun showLayoutWrite() {
        typeQuestion = Constants.WRITE
        edit_write_view.visibility = View.VISIBLE
        edit_write_view.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.alpha_appear))
        edit_complete_view.visibility = View.GONE
        edit_select_view.visibility = View.GONE
        edit_select_complete_view.visibility = View.GONE
    }

    fun showLayoutComplete() {
        typeQuestion = Constants.COMPLETE
        edit_complete_view.visibility = View.VISIBLE
        edit_complete_view.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.alpha_appear))
        edit_select_view.visibility = View.GONE
        edit_write_view.visibility = View.GONE
        edit_select_complete_view.visibility = View.GONE

    }

    fun showLayoutSelect() {
        typeQuestion = Constants.SELECT
        edit_complete_view.visibility = View.GONE
        edit_write_view.visibility = View.GONE
        edit_select_view.visibility = View.VISIBLE
        edit_select_view.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.alpha_appear))
        edit_select_complete_view.visibility = View.GONE

    }

    fun showLayoutSelectComplete(){

        typeQuestion = Constants.SELECT_COMPLETE
        edit_complete_view.visibility = View.GONE
        edit_write_view.visibility = View.GONE
        edit_select_view.visibility = View.GONE
        edit_select_complete_view.visibility = View.VISIBLE
        edit_select_complete_view.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.alpha_appear))

    }

    private fun reset() {

        set_problem.setText("")
        set_problem.requestFocus()
        set_answer_write.setText("")
        set_explanation.setText("")
        questionId = -1
        imagePath = ""
        button_image.setImageResource(R.drawable.ic_photo_white)
        button_image.setBackgroundResource(R.drawable.button_blue)

        button_add.text = getString(R.string.action_add)

        edit_select_view.reset()
        edit_complete_view.reset()
        edit_select_complete_view.reset()

        edit_select_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers)
        edit_select_complete_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers +1)
    }

    private fun initViews() {

        if (sharedPreferenceManager.explanation) textInputLayout_explanation.visibility = View.VISIBLE

        ImageButton_expand.setOnClickListener {

            if (layout_body.visibility == View.VISIBLE) {

                hideLayoutEdit()

            } else {

                showLayoutEdit()

                text_title.text = if (edit_select_view.visibility == View.VISIBLE) getString(R.string.add_question_choose) else getString(R.string.add_question_write)

            }

            reset()
        }

        button_detail.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {

                val dialogLayout = LayoutInflater.from(this@EditActivity).inflate(R.layout.dialog_detail,
                        findViewById(R.id.layout_dialog_detail))

                val add = dialogLayout.findViewById<ImageButton>(R.id.add)
                val minus = dialogLayout.findViewById<ImageButton>(R.id.minus)

                val addSelect = dialogLayout.findViewById<ImageButton>(R.id.add_answer_select)
                val minusSelect = dialogLayout.findViewById<ImageButton>(R.id.minus_answer_select)

                val sizeAnswerSelect = dialogLayout.findViewById<TextView>(R.id.size_answer_select)

                val number = dialogLayout.findViewById<TextView>(R.id.size_choose)
                val changeAuto = dialogLayout.findViewById<SwitchCompat>(R.id.change_auto)

                val changeExplanation = dialogLayout.findViewById<SwitchCompat>(R.id.change_explanation)

                changeExplanation.isChecked = sharedPreferenceManager.explanation
                changeExplanation.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.explanation = isChecked }

                changeAuto.isChecked = sharedPreferenceManager.auto
                changeAuto.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.auto = isChecked }

                when (typeQuestion) {
                    Constants.WRITE, Constants.COMPLETE -> {

                        val e = dialogLayout.findViewById<LinearLayout>(R.id.layout_switch)
                        e.visibility = View.GONE

                        dialogLayout.findViewById<LinearLayout>(R.id.layout_number_answer_select).visibility = View.GONE

                        val t = dialogLayout.findViewById<TextView>(R.id.textView)
                        t.text = getString(R.string.number_answers)

                        number.text = sharedPreferenceManager.numAnswers.toString()

                        changeAuto.visibility = View.GONE


                    }
                    Constants.SELECT,Constants.SELECT_COMPLETE -> {

                        number.text = (sharedPreferenceManager.numOthers + 1).toString()

                        sizeAnswerSelect.text = "${sharedPreferenceManager.numAnswersSelect}"
                    }

                }

                addSelect.setOnClickListener {
                    if(Integer.parseInt(sizeAnswerSelect.text.toString())+1 < Integer.parseInt(number.text.toString())){
                        sizeAnswerSelect.text = "${Integer.parseInt(sizeAnswerSelect.text.toString())+1}"
                    }
                }

                minusSelect.setOnClickListener {
                    if(Integer.parseInt(sizeAnswerSelect.text.toString())-1 > 0){
                        sizeAnswerSelect.text = "${Integer.parseInt(sizeAnswerSelect.text.toString())-1}"
                    }
                }

                add.setOnClickListener { checkCount(number, 1) }

                minus.setOnClickListener { checkCount(number, -1) }

                val builder = AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
                builder.setView(dialogLayout)
                builder.setTitle(getString(R.string.action_detail))
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setNegativeButton(android.R.string.cancel, null)

                val dialog = builder.show()

                val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                button.setOnClickListener {
                    // 場合によっては自分で明示的に閉じる必要がある

                    when (typeQuestion) {

                        Constants.WRITE, Constants.COMPLETE -> {

                            edit_complete_view.reloadAnswers(Integer.parseInt(number.text.toString()))
                            sharedPreferenceManager.numAnswers = Integer.parseInt(number.text.toString())

                            if (sharedPreferenceManager.numAnswers > 1) {

                                showLayoutComplete()

                            } else {

                                showLayoutWrite()
                            }
                        }
                        Constants.SELECT,Constants.SELECT_COMPLETE -> {

                            if(Integer.parseInt(number.text.toString()) <= Integer.parseInt(sizeAnswerSelect.text.toString())){

                                Toast.makeText(applicationContext, getString(R.string.message_answers_num), Toast.LENGTH_SHORT).show()

                                return@setOnClickListener
                            }

                            if (Integer.parseInt(sizeAnswerSelect.text.toString()) > 1) {

                                showLayoutSelectComplete()

                            } else {

                                showLayoutSelect()
                            }

                            edit_select_complete_view.setAnswerNum(Integer.parseInt(sizeAnswerSelect.text.toString()))
                            edit_select_complete_view.reloadSelects(Integer.parseInt(number.text.toString()))
                            edit_select_view.reloadOthers(Integer.parseInt(number.text.toString()) - 1)
                            sharedPreferenceManager.numOthers = Integer.parseInt(number.text.toString()) - 1
                            sharedPreferenceManager.numAnswersSelect = Integer.parseInt(sizeAnswerSelect.text.toString())

                            edit_select_complete_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers+1)
                            edit_select_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers)

                        }
                    }

                    textInputLayout_explanation.visibility = if (sharedPreferenceManager.explanation) View.VISIBLE else View.GONE

                    dialog.dismiss()

                }

                dialog.show()
            }

            fun checkCount(number: TextView, i: Int) {
                val num = Integer.parseInt(number.text.toString())

                var mini = 0
                var max = 0

                when (typeQuestion) {
                    Constants.WRITE, Constants.COMPLETE -> {
                        mini = 1
                        max = 4
                    }
                    Constants.SELECT,Constants.SELECT_COMPLETE -> {
                        mini = 2
                        max = 6
                    }
                }

                if (num + i in mini..max) {
                    number.text = (num + i).toString()

                } else {
                    Toast.makeText(applicationContext, getString(R.string.limit, mini, max), Toast.LENGTH_SHORT).show()
                }
            }
        })

        button_type.setOnClickListener {

            if (button_type.text == getString(R.string.action_choose)) {

                if (sharedPreferenceManager.numAnswersSelect > 1) {
                    showLayoutSelectComplete()
                    edit_select_complete_view.reloadSelects(sharedPreferenceManager.numOthers + 1)
                    edit_select_complete_view.setAnswerNum(sharedPreferenceManager.numAnswersSelect)
                    edit_select_complete_view.setAuto(sharedPreferenceManager.auto,sharedPreferenceManager.numOthers +1)

                } else {
                    showLayoutSelect()
                    edit_select_view.reloadOthers(sharedPreferenceManager.numOthers)
                    edit_select_view.setAuto(sharedPreferenceManager.auto,sharedPreferenceManager.numOthers)

                }



                button_type.text = getString(R.string.action_write)
                text_title.text = getString(R.string.add_question_choose)

            } else {

                if (sharedPreferenceManager.numAnswers > 1) {
                    showLayoutComplete()
                } else {
                    showLayoutWrite()
                }

                button_type.text = getString(R.string.action_choose)
                text_title.text = getString(R.string.add_question_write)

            }

        }

        button_add.setOnClickListener { addQuestion() }

        button_cancel.setOnClickListener { cancelEditing() }

        button_image.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {

                if (imagePath != "") {

                    // リスト表示用のアラートダイアログ
                    val listDlg = AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
                    listDlg.setItems(
                            resources.getStringArray(R.array.action_image)
                    ) { _, which ->

                        when (which) {
                            0 -> openImage() //差し替え
                            1 -> { //取り消し
                                imagePath = ""
                                button_image.setImageResource(R.drawable.ic_photo_white)
                                button_image.setBackgroundResource(R.drawable.button_blue)
                            }
                        }
                    }

                    listDlg.show()

                } else {
                    openImage()
                }


            }

            fun openImage() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), REQUEST_PICK_IMAGE)
                } else {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    startActivityForResult(intent, REQUEST_SAF_PICK_IMAGE)
                }
            }
        })


        if (Build.VERSION.SDK_INT >= 21) {
            button_add.stateListAnimator = null
            button_cancel.stateListAnimator = null
            button_type.stateListAnimator = null
            button_detail.stateListAnimator = null
        }

        recycler_view.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view.setHasFixedSize(true) // アイテムは固定サイズ
        recycler_view.adapter = editAdapter
    }

    companion object {
        private const val REQUEST_PICK_IMAGE = 10011
        private const val REQUEST_SAF_PICK_IMAGE = 10012
    }
}
