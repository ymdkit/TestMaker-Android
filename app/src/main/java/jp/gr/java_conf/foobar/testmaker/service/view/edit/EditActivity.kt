package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.isseiaoki.simplecropview.CropImageView
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.setImageWithGlide
import jp.gr.java_conf.foobar.testmaker.service.extensions.swap
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryEditor
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ColorChooser
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.util.*
import kotlin.math.min

/**
 * Created by keita on 2017/02/12.
 */

open class EditActivity : BaseActivity() {

    internal lateinit var editAdapter: EditAdapter

    private val viewModel: EditViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()

    private val fileName: String
        get() {
            val c = Calendar.getInstance()
            return c.get(Calendar.YEAR).toString() + "_" + (c.get(Calendar.MONTH) + 1) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND) + "_" + c.get(Calendar.MILLISECOND) + ".png"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val binding = DataBindingUtil.setContentView<ActivityEditBinding>(this, R.layout.activity_edit)
        binding.lifecycleOwner = this
        binding.model = viewModel

        createAd(binding.adView)

        initToolBar()

        viewModel.testId = intent.getLongExtra("testId", -1)
        supportActionBar?.title = "${getString(R.string.title_activity_edit)}: ${viewModel.getTest().title}"

        viewModel.migrateOrder()

        initAdapter()

        initViews()

        showLayoutWrite()

        viewModel.spinnerAnswersPosition.observeNonNull(this) {

            when (viewModel.formatQuestion.value) {
                Constants.COMPLETE -> {
                    binding.editCompleteView.reloadAnswers(baseContext.resources.getStringArray(R.array.spinner_answers_complete)[it].toInt())
                }
                Constants.SELECT_COMPLETE -> {
                    binding.editSelectCompleteView.setAnswerNum(baseContext.resources.getStringArray(R.array.spinner_answers_select_complete)[it].toInt())
                }
            }

        }

        viewModel.spinnerSelectsPosition.observeNonNull(this) {
            when (viewModel.formatQuestion.value) {
                Constants.SELECT -> {
                    binding.editSelectView.reloadOthers(baseContext.resources.getStringArray(R.array.spinner_selects)[it].toInt() - 1)
                    binding.editSelectView.setAuto(sharedPreferenceManager.auto
                            , baseContext.resources.getStringArray(R.array.spinner_selects)[viewModel.spinnerSelectsPosition.value
                            ?: 0].toInt() - 1)
                }
                Constants.SELECT_COMPLETE -> {
                    binding.editSelectCompleteView.reloadSelects(baseContext.resources.getStringArray(R.array.spinner_selects_complete)[it].toInt())
                    binding.editSelectCompleteView.setAuto(sharedPreferenceManager.auto
                            , baseContext.resources.getStringArray(R.array.spinner_selects_complete)[viewModel.spinnerSelectsPosition.value
                            ?: 0].toInt())
                }
            }

        }

        viewModel.isAuto.observe(this, Observer {
            sharedPreferenceManager.auto = it ?: false
            when (viewModel.formatQuestion.value) {
                Constants.SELECT -> {
                    binding.editSelectView.setAuto(it
                            ?: false, baseContext.resources.getStringArray(R.array.spinner_selects)[viewModel.spinnerSelectsPosition.value
                            ?: 0].toInt() - 1)
                }
                Constants.SELECT_COMPLETE -> {
                    binding.editSelectCompleteView.setAuto(it
                            ?: false, baseContext.resources.getStringArray(R.array.spinner_selects_complete)[viewModel.spinnerSelectsPosition.value
                            ?: 0].toInt())
                }
            }
        })

        viewModel.isCheckOrder.observe(this, Observer {
            sharedPreferenceManager.isCheckOrder = it ?: false
        })

        viewModel.formatQuestion.observeNonNull(this) {
            when (it) {
                Constants.WRITE -> {
                    viewModel.editingView = null
                }
                Constants.SELECT -> {
                    viewModel.editingView = edit_select_view
                }
                Constants.COMPLETE -> {
                    viewModel.editingView = edit_complete_view
                }

                Constants.SELECT_COMPLETE -> {
                    viewModel.editingView = edit_select_complete_view
                }
            }
        }


        viewModel.clearQuestions()
        viewModel.getQuestions().observeNonNull(this) {
            editAdapter.questions = it
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchQuestions()
    }

    private fun initAdapter() {

        editAdapter = EditAdapter(this)

        editAdapter.setOnClickListener(object : EditAdapter.OnClickListener {
            override fun onClickEditQuestion(question: Quest) {

                showLayoutEdit()
                text_title.text = getString(R.string.edit_question)
                button_cancel.visibility = View.VISIBLE
                button_add.text = getString(R.string.save_question)
                set_problem.setText(question.problem)
                set_explanation.setText(question.explanation)

                viewModel.questionId = question.id
                viewModel.order = question.order

                if (question.imagePath != "") {
                    viewModel.imagePath = question.imagePath

                    if (question.imagePath.contains("/")) {

                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference.child(question.imagePath)

                        button_image.setImageWithGlide(baseContext, storageRef)

                    } else {
                        lifecycleScope.launch {
                            button_image.setImageWithGlide(baseContext,viewModel.loadImage())
                        }
                    }

                } else {
                    button_image.setImageResource(R.drawable.ic_insert_photo_white_24dp)
                }

                viewModel.isEditingExplanation.value = question.explanation.isNotEmpty()

                when (question.type) {

                    Constants.WRITE -> {
                        showLayoutWrite()

                        set_answer_write.setText(question.answer)
                        sharedPreferenceManager.numAnswers = 1
                    }

                    Constants.SELECT -> {

                        showLayoutSelect()

                        sharedPreferenceManager.numOthers = question.selections.size
                        edit_select_view.reloadOthers(question.selections.size)
                        edit_select_view.setAnswer(question.answer)
                        edit_select_view.setOthers(question.selections)
                        viewModel.isAuto.value = question.auto

                        viewModel.spinnerSelectsPosition.value = min(Constants.OTHER_SELECT_MAX - 1, question.selections.size - 1)

                        edit_select_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers)

                    }
                    Constants.COMPLETE -> {

                        showLayoutComplete()
                        sharedPreferenceManager.numAnswers = question.answers.size
                        sharedPreferenceManager.isCheckOrder = question.isCheckOrder
                        edit_complete_view.reloadAnswers(question.answers.size)
                        edit_complete_view.setAnswers(question)
                        viewModel.isCheckOrder.value = question.isCheckOrder

                        viewModel.spinnerAnswersPosition.value = min(Constants.ANSWER_MAX - 2, question.answers.size - 2)

                    }

                    Constants.SELECT_COMPLETE -> {

                        showLayoutSelectComplete()
                        sharedPreferenceManager.numAnswersSelect = question.answers.size
                        sharedPreferenceManager.numOthers = question.selections.size + question.answers.size - 1
                        edit_select_complete_view.setAnswerNum(question.answers.size)
                        edit_select_complete_view.reloadSelects(question.answers.size + question.selections.size)
                        edit_select_complete_view.setSelections(question.answers, question.selections)
                        viewModel.isAuto.value = question.auto
                        edit_select_complete_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers + 1)

                        viewModel.spinnerAnswersPosition.value = min(Constants.SELECT_COMPLETE_MAX, question.answers.size)
                        viewModel.spinnerSelectsPosition.value = min(Constants.SELECT_COMPLETE_MAX - 2, question.selections.size + question.answers.size - 2)

                    }
                }
            }

            override fun onClickDeleteQuestion(data: Quest, position: Int) {

                val builder = AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
                builder.setTitle(getString(R.string.delete_question))
                builder.setMessage(getString(R.string.message_delete, data.problem))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->

                    if (data.imagePath != "") deleteFile(data.imagePath)

                    viewModel.deleteQuestion(data)
                    editAdapter.questions.removeAt(position)
                    editAdapter.notifyItemRemoved(position)
                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.create().show()
            }
        })
    }

    private fun showLayoutEdit() {

        viewModel.stateEditing.value = Constants.EDIT_QUESTION
        set_problem.isFocusable = true
        set_problem.requestFocus()
        button_add.visibility = View.VISIBLE

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        editAdapter.notifyDataSetChanged()

        if (resultCode != Activity.RESULT_OK) return
        if (resultData == null) return

        val bitmap = when (requestCode) {
            REQUEST_SAF_PICK_IMAGE -> getBitmapFromUri(resultData.data)
            else -> resultData.extras?.get("data") as Bitmap
        }

        try {

            val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_crop,
                    findViewById(R.id.layout_dialog_crop_image))

            val cropView = dialogLayout.findViewById<CropImageView>(R.id.cropImageView)
            cropView.imageBitmap = bitmap

            val builder = AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
            builder.setView(dialogLayout)
            builder.setTitle(getString(R.string.trim))
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setNegativeButton(android.R.string.cancel, null)

            val dialog = builder.show()

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {

                viewModel.imagePath = fileName
                button_image.setImageWithGlide(baseContext, cropView.croppedBitmap)

                lifecycleScope.launch{
                    viewModel.saveImage(cropView.croppedBitmap)
                }

                dialog.dismiss()
            }

        } catch (e: IOException) {
            e.printStackTrace()
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
        viewModel.stateEditing.value = Constants.NOT_EDITING
        button_cancel.visibility = View.GONE
        text_title.text = getString(R.string.add_question)
    }

    private fun addQuestion() {
        viewModel.addQuestion(
                onSuccess = {
                    reset()
                    Toast.makeText(baseContext, getString(R.string.msg_save), Toast.LENGTH_LONG).show()
                },
                onFailure = {
                    Toast.makeText(baseContext, it, Toast.LENGTH_LONG).show()
                })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)

        val searchView = menu.findItem(R.id.menu_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                editAdapter.searchWord = s
                editAdapter.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                editAdapter.searchWord = s
                editAdapter.notifyDataSetChanged()
                return false
            }
        })

        searchView.setOnCloseListener {
            editAdapter.searchWord = ""
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

                buttonCate.tag = viewModel.getTest(viewModel.testId).getCategory()

                if (viewModel.getTest(viewModel.testId).getCategory() == "") {

                    buttonCate.text = getString(R.string.category)
                } else {
                    buttonCate.text = viewModel.getTest(viewModel.testId).getCategory()
                }

                buttonCate.setOnClickListener {
                    val categoryEditor = CategoryEditor(this@EditActivity, buttonCate,
                            getCategories = { categoryViewModel.categories.value ?: emptyList() }
                            ,
                            addCategory = {
                                categoryViewModel.create(it)
                            },
                            deleteCategory = {
                                categoryViewModel.delete(it)
                            })
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

                name.setText(viewModel.getTest(viewModel.testId).title)

                colorChooser.setColorId(viewModel.getTest(viewModel.testId).color)

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

                        testViewModel.tests.find { it.id == viewModel.testId }?.let {
                            testViewModel.update(it, sb.toString(), colorChooser.getColorId(), buttonCate.tag.toString())
                        }

                        dialog.dismiss()
                        supportActionBar?.title = "${getString(R.string.title_activity_edit)}: ${viewModel.getTest().title}"

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

                i.putExtra("testId", viewModel.testId)
                startActivityForResult(i, 0)

                return true
            }

            item.itemId == R.id.action_reset_achievement -> {

                viewModel.resetAchievement()

                Toast.makeText(baseContext, getString(R.string.msg_reset_achievement), Toast.LENGTH_SHORT).show()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun showLayoutWrite() {
        viewModel.formatQuestion.value = Constants.WRITE
    }

    fun showLayoutComplete() {
        viewModel.formatQuestion.value = Constants.COMPLETE
    }

    fun showLayoutSelect() {
        viewModel.formatQuestion.value = Constants.SELECT
    }

    fun showLayoutSelectComplete() {
        viewModel.formatQuestion.value = Constants.SELECT_COMPLETE
    }

    private fun reset() {

        set_problem.setText("")
        set_problem.requestFocus()
        set_answer_write.setText("")
        set_explanation.setText("")
        viewModel.questionId = -1
        viewModel.imagePath = ""
        viewModel.order = -1
        button_image.setImageResource(R.drawable.ic_insert_photo_white_24dp)
        button_cancel.visibility = View.GONE

        button_add.text = getString(R.string.action_add)

        edit_select_view.reset()
        edit_complete_view.reset()
        edit_select_complete_view.reset()

        edit_select_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers)
        edit_select_complete_view.setAuto(sharedPreferenceManager.auto, sharedPreferenceManager.numOthers + 1)
    }

    private fun initViews() {

        if (sharedPreferenceManager.explanation) textInputLayout_explanation.visibility = View.VISIBLE

        button_expand.setOnClickListener {

            if (viewModel.stateEditing.value != Constants.NOT_EDITING) {

                hideLayoutEdit()

            } else {

                showLayoutEdit()

            }

            reset()
        }

        button_detail.setOnClickListener {
            viewModel.stateEditing.value = Constants.EDIT_CONFIG
        }

        radio_question.setOnCheckedChangeListener { _, checkedId ->
            val radio = findViewById<RadioButton>(checkedId)
            val tag = radio.tag
            if (tag is Int) viewModel.formatQuestion.value = tag
        }

        button_add.setOnClickListener { addQuestion() }

        button_cancel.setOnClickListener { cancelEditing() }

        button_image.setOnClickListener {
            val listDlg = AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
            listDlg.setItems(
                    if (viewModel.imagePath != "") resources.getStringArray(R.array.action_image) else resources.getStringArray(R.array.action_image).take(2).toTypedArray()
            ) { _, which ->

                when (which) {
                    0 -> { //撮影
                        if (ContextCompat.checkSelfPermission(this@EditActivity, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(this@EditActivity,
                                    arrayOf(Manifest.permission.CAMERA),
                                    REQUEST_PERMISSION_CAMERA)

                        } else {
                            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                                    takePictureIntent.resolveActivity(packageManager)?.also {
                                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                                    }
                                }
                            }
                        }
                    }
                    1 -> { //ギャラリー
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "image/*"
                        startActivityForResult(intent, REQUEST_SAF_PICK_IMAGE)
                    }
                    2 -> { //取り消し
                        viewModel.imagePath = ""
                        button_image.setImageResource(R.drawable.ic_insert_photo_white_24dp)
                    }
                }
            }
            listDlg.show()
        }

        recycler_view.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view.setHasFixedSize(true) // アイテムは固定サイズ
        recycler_view.adapter = editAdapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onSwiped(holder: RecyclerView.ViewHolder, position: Int) {
            }

            // ここで指定した方向にのみドラッグ可能
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                viewModel.sortManual(from, to, viewModel.testId)
                editAdapter.questions.swap(from, to)
                editAdapter.notifyItemMoved(from, to)

                return true
            }
        })

        touchHelper.attachToRecyclerView(recycler_view)
        recycler_view.addItemDecoration(touchHelper)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CAMERA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                            takePictureIntent.resolveActivity(packageManager)?.also {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                            }
                        }
                    }

                }
                return
            }
        }
    }

    companion object {
        private const val REQUEST_SAF_PICK_IMAGE = 10012
        private const val REQUEST_IMAGE_CAPTURE = 10013
        private const val REQUEST_PERMISSION_CAMERA = 10014
    }
}


