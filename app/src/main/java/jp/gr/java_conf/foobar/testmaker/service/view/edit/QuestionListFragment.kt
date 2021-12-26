package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.airbnb.epoxy.EpoxyTouchHelper
import com.google.android.gms.ads.AdRequest
import jp.gr.java_conf.foobar.testmaker.service.ItemQuestionBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentQuestionListBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
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

class QuestionListFragment : Fragment() {

    private val testViewModel: TestViewModel by viewModel()
    private val service: CloudFunctionsService by inject()
    private val logger: TestMakerLogger by inject()
    private val sharedPreferenceManager: SharedPreferenceManager by inject()

    private val controller: EditController by lazy {
        EditController(requireContext()).apply {
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
                        ).show(childFragmentManager, "TAG")

                    }
                }
            })
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater: MenuInflater = mode?.menuInflater ?: return false
            inflater.inflate(R.menu.menu_edit_selected, menu)
            // ContextMenu の背景色に合わせてアイコンの色を変更
            menu?.forEach {
                val drawable = it.icon
                DrawableCompat.setTint(
                    drawable,
                    ContextCompat.getColor(requireContext(), R.color.colorText)
                )
                it.icon = drawable
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.action_move -> {
                    if (controller.selectedQuestions.isEmpty()) {
                        requireContext().showToast(getString(R.string.msg_empty_selected_questions))
                        return true
                    }

                    ListDialogFragment.newInstance(
                        getString(R.string.msg_move_questions),
                        testViewModel.tests.filterNot { it.id == test.id }.map {
                            DialogMenuItem(
                                title = it.title,
                                iconRes = R.drawable.ic_baseline_description_24,
                                action = {
                                    testViewModel.move(controller.selectedQuestions, it)
                                    requireContext().showToast(
                                        getString(
                                            R.string.msg_succes_move_questions,
                                            it.title
                                        )
                                    )
                                    logger.logEvent("move_questions")
                                    mode?.finish()
                                }
                            )
                        }
                    ).show(childFragmentManager, "TAG")
                    return true
                }
                R.id.action_copy -> {
                    if (controller.selectedQuestions.isEmpty()) {
                        requireContext().showToast(getString(R.string.msg_empty_selected_questions))
                        return true
                    }

                    ListDialogFragment.newInstance(
                        getString(R.string.msg_copy_questions),
                        testViewModel.tests.filterNot { it.id == test.id }.map {
                            DialogMenuItem(
                                title = it.title,
                                iconRes = R.drawable.ic_baseline_description_24,
                                action = {
                                    testViewModel.copy(controller.selectedQuestions, it)
                                    requireContext().showToast(
                                        getString(
                                            R.string.msg_succes_copy_questions,
                                            it.title
                                        )
                                    )
                                    logger.logEvent("copy_questions")
                                    mode?.finish()
                                }
                            )
                        }
                    ).show(childFragmentManager, "TAG")
                    return true
                }
                R.id.action_delete -> {

                    if (controller.selectedQuestions.isEmpty()) {
                        requireContext().showToast(getString(R.string.msg_empty_selected_questions))
                        return true
                    }

                    ConfirmDangerDialogFragment.newInstance(
                        getString(
                            R.string.msg_delete_selected_questions,
                        ),
                        getString(R.string.button_delete_confirm)
                    ) {

                        testViewModel.delete(controller.selectedQuestions)
                        requireContext().showToast(getString(R.string.msg_succes_delete_questions))
                        logger.logEvent("delete_questions")
                        mode?.finish()

                    }.show(childFragmentManager, "TAG")
                    return true
                }
                else -> {
                    return false
                }
            }
        }


        override fun onDestroyActionMode(mode: ActionMode?) {
            controller.selectedQuestions = emptyList()
            actionMode = null
        }

    }

    private lateinit var binding: FragmentQuestionListBinding

    private val args: QuestionListFragmentArgs by navArgs()
    private lateinit var test: Test

    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        test = testViewModel.tests.find { it.id == args.workbookId }!!

        testViewModel.testsLiveData.observeNonNull(this) {
            it.find { test.id == it.id }?.let {
                test = it
            }
            controller.questions = test.questions.sortedBy { it.order }
        }

        return DataBindingUtil.inflate<FragmentQuestionListBinding>(
            inflater,
            R.layout.fragment_question_list,
            container,
            false
        ).apply {
            binding = this

            recyclerView.adapter = controller.adapter

            fab.setOnClickListener {
                CreateQuestionActivity.startActivity(requireActivity(), test.id)
            }

            toolbar.setupWithNavController(
                findNavController(),
                AppBarConfiguration(findNavController().graph)
            )

            if (sharedPreferenceManager.isRemovedAd) {
                adView.visibility = View.GONE
            } else {
                adView.loadAd(AdRequest.Builder().build())
            }

            initViews()

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_setting -> {
                    findNavController().navigate(
                        QuestionListFragmentDirections.actionQuestionListToEditWorkbook(
                            workbookId = test.id
                        )
                    )
                    true
                }
                R.id.action_export -> {
                    convertTestToCSV(test)
                    true
                }
                android.R.id.home -> {
                    findNavController().popBackStack()
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

                    requireContext().showToast(getString(R.string.msg_reset_achievement))
                    true
                }
                R.id.action_select -> {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback)
                    } else {
                        controller.selectedQuestions = emptyList()
                    }
                    true
                }
                else -> {
                    super.onOptionsItemSelected(it)
                }
            }
        }

        val searchView = binding.toolbar.menu.findItem(R.id.menu_search).actionView as SearchView
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
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }

    override fun onResume() {
        super.onResume()
        testViewModel.refresh()
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

        requireActivity().executeJobWithDialog(
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
                requireContext().showErrorToast(it)
            }
        )
    }

    fun editQuestion(question: Question) {
        EditQuestionActivity.startActivity(requireActivity(), test.id, question.id)
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
        }.show(childFragmentManager, "TAG")
    }

    companion object {

        fun startActivity(activity: Activity, id: Long) {
            val intent = Intent(activity, QuestionListFragment::class.java).apply {
                putExtra("id", id)
            }
            activity.startActivity(intent)
        }
    }
}


