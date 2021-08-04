package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.airbnb.epoxy.EpoxyTouchHelper
import jp.gr.java_conf.foobar.testmaker.service.ItemQuestionBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by keita on 2017/02/12.
 */

class EditActivity : BaseActivity() {

    private val testViewModel: TestViewModel by viewModel()

    private val controller: EditController by lazy {
        EditController(this).apply {
            setOnClickListener(object : EditController.OnClickListener {
                override fun onClickQuestion(question: Question) {
                    ListDialogFragment.newInstance(
                            question.question,
                            listOf(
                                    DialogMenuItem(title = getString(R.string.edit), iconRes = R.drawable.ic_edit_white, action = { editQuestion(question) }),
                                    DialogMenuItem(title = getString(R.string.copy_question), iconRes = R.drawable.ic_baseline_file_copy_24, action = { copyQuestion(question) }),
                                    DialogMenuItem(title = getString(R.string.delete), iconRes = R.drawable.ic_delete_white, action = { deleteQuestion(question) })
                            )
                    ).show(supportFragmentManager, "TAG")
                }
            })
        }
    }

    private val binding by lazy { DataBindingUtil.setContentView<ActivityEditBinding>(this, R.layout.activity_edit) }

    private lateinit var test: Test

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
                android.R.id.home -> {
                    finish()
                    true
                }
                R.id.action_reset_achievement -> {
                    test.let {
                        testViewModel.update(Test.createFromRealmTest(RealmTest.createFromTest(it).apply {
                            resetAchievement()
                        }))
                    }

                    showToast(getString(R.string.msg_reset_achievement))
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
                    override fun onModelMoved(fromPosition: Int, toPosition: Int, modelBeingMoved: ItemQuestionBindingModel_, itemView: View?) {
                        val from = controller.adapter.getModelAtPosition(fromPosition)
                        val to = controller.adapter.getModelAtPosition(toPosition)

                        if (from is ItemQuestionBindingModel_ && to is ItemQuestionBindingModel_) {
                            testViewModel.swap(from.question(), to.question())
                        }
                    }
                })
    }

    fun editQuestion(question: Question) {
        EditQuestionActivity.startActivity(this, test.id, question.id)
    }

    fun copyQuestion(question: Question) {
        testViewModel.insertAt(test, question.copy(), question.order)
    }

    fun deleteQuestion(question: Question) {
        ConfirmDangerDialogFragment.newInstance(getString(R.string.message_delete, question.question),
            getString(R.string.button_delete_confirm)) {
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


