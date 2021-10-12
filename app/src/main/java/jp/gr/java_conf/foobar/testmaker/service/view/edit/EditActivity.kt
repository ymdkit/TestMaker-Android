package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.airbnb.epoxy.EpoxyTouchHelper
import jp.gr.java_conf.foobar.testmaker.service.ItemQuestionBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by keita on 2017/02/12.
 */

class EditActivity : BaseActivity() {

    private val testViewModel: TestViewModel by viewModel()
    private val service: CloudFunctionsService by inject()

    private val controller: EditController by lazy {
        EditController(this).apply {
            setOnClickListener(object : EditController.OnClickListener {
                override fun onClickQuestion(question: Question) {

                    if (actionMode != null) {
                        selectedQuestions = if (selectedQuestions.any { question.id == it.id }) {
                            selectedQuestions.filterNot { it.id == question.id }
                        } else {
                            selectedQuestions + listOf(question)
                        }
                    } else {

                        ListDialogFragment.newInstance(
                            question.question,
                            listOf(
                                DialogMenuItem(
                                    title = getString(R.string.edit),
                                    iconRes = R.drawable.ic_edit_white,
                                    action = { editQuestion(question) }),
                                DialogMenuItem(
                                    title = getString(R.string.copy_question),
                                    iconRes = R.drawable.ic_baseline_file_copy_24,
                                    action = { copyQuestion(question) }),
                                DialogMenuItem(
                                    title = getString(R.string.delete),
                                    iconRes = R.drawable.ic_delete_white,
                                    action = { deleteQuestion(question) })
                            )
                        ).show(supportFragmentManager, "TAG")

                    }
                }
            })
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater: MenuInflater = mode?.menuInflater ?: return false
            inflater.inflate(R.menu.menu_edit_selected, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean =
            when (item?.itemId) {
                R.id.action_delete -> {

                    ConfirmDangerDialogFragment.newInstance(
                        getString(
                            R.string.msg_delete_selected_questions,
                        ),
                        getString(R.string.button_delete_confirm)
                    ) {

                        testViewModel.delete(controller.selectedQuestions)
                        showToast(getString(R.string.msg_succes_delete_questions))
                        mode?.finish()

                    }.show(supportFragmentManager, "TAG")
                    true
                }
                else -> {
                    false
                }
            }


        override fun onDestroyActionMode(mode: ActionMode?) {
            controller.selectedQuestions = emptyList()
            actionMode = null
        }

    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityEditBinding>(
            this,
            R.layout.activity_edit
        )
    }

    private lateinit var test: Test
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
            test = it
        }

        binding.lifecycleOwner = this
        binding.recyclerView.adapter = controller.adapter

        binding.fab.setOnClickListener {
            EditQuestionActivity.startActivity(this, test.id)
        }

        createAd(binding.adView)

        initToolBar()
        initViews()

        testViewModel.testsLiveData.observeNonNull(this) {
            it.find { test.id == it.id }?.let {
                test = it
            }
            supportActionBar?.title = test.title
            controller.questions = test.questions.sortedBy { it.order }
        }
    }

    override fun onResume() {
        super.onResume()
        testViewModel.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)

        val searchView = menu.findItem(R.id.menu_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                controller.searchWord = s
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                controller.searchWord = s
                return false
            }
        })

        searchView.setOnCloseListener {
            controller.searchWord = ""
            false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_setting -> {
                EditTestActivity.startActivity(this, test.id)
                true
            }
            R.id.action_export -> {
                convertTestToCSV(test)
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_reset_achievement -> {
                test.let {
                    testViewModel.update(
                        Test.createFromRealmTest(
                            RealmTest.createFromTest(it).apply {
                                resetAchievement()
                            })
                    )
                }

                showToast(getString(R.string.msg_reset_achievement))
                true
            }
            R.id.action_select -> {
                if (actionMode == null) {
                    actionMode = startActionMode(actionModeCallback)
                } else {
                    controller.selectedQuestions = emptyList()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    private fun initViews() {
        EpoxyTouchHelper
            .initDragging(controller)
            .withRecyclerView(binding.recyclerView)
            .forVerticalList()
            .withTarget(ItemQuestionBindingModel_::class.java)
            .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<ItemQuestionBindingModel_>() {
                override fun onModelMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    modelBeingMoved: ItemQuestionBindingModel_,
                    itemView: View?
                ) {
                    val from = controller.adapter.getModelAtPosition(fromPosition)
                    val to = controller.adapter.getModelAtPosition(toPosition)

                    if (from is ItemQuestionBindingModel_ && to is ItemQuestionBindingModel_) {
                        testViewModel.swap(from.question(), to.question())
                    }
                }
            })
    }

    private fun convertTestToCSV(test: Test) {

        executeJobWithDialog(
            title = getString(R.string.converting),
            task = {
                withContext(Dispatchers.IO) {
                    service.testToText(test.escapedTest.copy(lang = if (Locale.getDefault().language == "ja") "ja" else "en"))
                }
            },
            onSuccess = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, it.text)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            },
            onFailure = {
                showErrorToast(it)
            }
        )
    }

    fun editQuestion(question: Question) {
        EditQuestionActivity.startActivity(this, test.id, question.id)
    }

    fun copyQuestion(question: Question) {
        testViewModel.insertAt(test, question.copy(), question.order)
    }

    fun deleteQuestion(question: Question) {
        ConfirmDangerDialogFragment.newInstance(
            getString(R.string.message_delete, question.question),
            getString(R.string.button_delete_confirm)
        ) {
            testViewModel.delete(question)
        }.show(supportFragmentManager, "TAG")
    }

    companion object {

        fun startActivity(activity: Activity, id: Long) {
            val intent = Intent(activity, EditActivity::class.java).apply {
                putExtra("id", id)
            }
            activity.startActivity(intent)
        }
    }
}


