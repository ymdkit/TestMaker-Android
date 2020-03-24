package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.airbnb.epoxy.EpoxyTouchHelper
import jp.gr.java_conf.foobar.testmaker.service.CardQuestionBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by keita on 2017/02/12.
 */

open class EditActivity : BaseActivity(), EditController.OnClickListener {

    private val testViewModel: TestViewModel by viewModel()

    private val controller: EditController by lazy {
        EditController(this).apply {
            setOnClickListener(this@EditActivity)
        }
    }

    private val binding by lazy { DataBindingUtil.setContentView<ActivityEditBinding>(this, R.layout.activity_edit) }

    private lateinit var test: Test

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

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
            controller.questions = test.questions
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
                R.id.action_edit_pro -> {
                    EditProActivity.startActivity(this, test.id)
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
                .withTarget(CardQuestionBindingModel_::class.java)
                .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<CardQuestionBindingModel_>() {
                    override fun onModelMoved(fromPosition: Int, toPosition: Int, modelBeingMoved: CardQuestionBindingModel_, itemView: View?) {
                        val from = controller.adapter.getModelAtPosition(fromPosition)
                        val to = controller.adapter.getModelAtPosition(toPosition)

                        if (from is CardQuestionBindingModel_ && to is CardQuestionBindingModel_) {
                            testViewModel.swap(from.question(), to.question())
                        }
                    }
                })
    }

    override fun onClickEditQuestion(question: Question) {
        EditQuestionActivity.startActivity(this, test.id, question.id)
    }

    override fun onClickDeleteQuestion(question: Question) {
        AlertDialog.Builder(this@EditActivity, R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.delete_question))
                .setMessage(getString(R.string.message_delete, question.question))
                .setPositiveButton(android.R.string.ok) { _, _ ->
            if (question.imagePath != "") deleteFile(question.imagePath)
            testViewModel.delete(question)
        }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
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


