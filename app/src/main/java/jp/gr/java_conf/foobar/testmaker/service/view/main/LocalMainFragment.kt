package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.stickyheader.StickyHeaderLinearLayoutManager
import jp.gr.java_conf.foobar.testmaker.service.CardCategoryBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.ItemTestBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.LocalMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.DynamicLinksCreator
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditActivity
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.online.UploadTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.play.AnswerWorkbookActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class LocalMainFragment : Fragment() {

    private val mainController by lazy { MainController(requireContext()) }
    private val sharedPreferenceManager: SharedPreferenceManager by inject()

    private var binding: LocalMainFragmentBinding? = null

    private val testViewModel: TestViewModel by sharedViewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()
    private val dynamicLinksCreator: DynamicLinksCreator by inject()
    private val logger: TestMakerLogger by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mainController.setOnClickListener(object : MainController.OnClickListener {

            override fun onClickTest(test: Test) {
                ListDialogFragment.newInstance(
                    test.title,
                    listOf(
                        DialogMenuItem(
                            title = getString(R.string.play),
                            iconRes = R.drawable.ic_play_arrow_white_24dp,
                            action = { playTest(test) }),
                        DialogMenuItem(
                            title = getString(R.string.edit),
                            iconRes = R.drawable.ic_edit_white,
                            action = { editTest(test) }),
                        DialogMenuItem(
                            title = getString(R.string.delete),
                            iconRes = R.drawable.ic_delete_white,
                            action = { deleteTest(test) }),
                        DialogMenuItem(
                            title = getString(R.string.share),
                            iconRes = R.drawable.ic_share_white,
                            action = { uploadTest(test) })
                    )
                ).show(requireActivity().supportFragmentManager, "TAG")
            }

            override fun onClickCategoryMenu(category: Category) {

                ListDialogFragment.newInstance(
                    category.name,
                    listOf(
                        DialogMenuItem(
                            title = getString(R.string.edit_category_name),
                            iconRes = R.drawable.ic_edit_white,
                            action = { editCategory(category) }),
                        DialogMenuItem(
                            title = getString(R.string.delete),
                            iconRes = R.drawable.ic_delete_white,
                            action = { deleteCategory(category) })
                    )
                ).show(requireActivity().supportFragmentManager, "TAG")
            }
        })

        categoryViewModel.categories.observeNonNull(this) {
            mainController.categories = it
        }

        testViewModel.testsLiveData.observeNonNull(this) {
            mainController.tests = it
        }

        return DataBindingUtil.inflate<LocalMainFragmentBinding>(
            inflater,
            R.layout.local_main_fragment,
            container,
            false
        ).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner

            fab.setOnClickListener {
                EditTestActivity.startActivity(requireActivity())
            }

            recyclerView.layoutManager = StickyHeaderLinearLayoutManager(requireContext())
            recyclerView.adapter = mainController.adapter

            EpoxyTouchHelper
                .initDragging(mainController)
                .withRecyclerView(recyclerView)
                .forVerticalList()
                .withTargets(
                    CardCategoryBindingModel_::class.java,
                    ItemTestBindingModel_::class.java
                )
                .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<EpoxyModel<*>>() {
                    override fun onModelMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        modelBeingMoved: EpoxyModel<*>?,
                        itemView: View?
                    ) {
                        val from = mainController.adapter.getModelAtPosition(fromPosition)
                        val to = mainController.adapter.getModelAtPosition(toPosition)

                        if (from is ItemTestBindingModel_ && to is ItemTestBindingModel_) {
                            testViewModel.swap(from.test(), to.test())
                        } else if (from is CardCategoryBindingModel_ && to is CardCategoryBindingModel_) {
                            categoryViewModel.swap(from.category(), to.category())
                        }
                    }
                })
        }.root
    }

    private fun playTest(test: Test) {
        if (test.questions.isEmpty()) {
            requireContext().showToast(getString(R.string.message_null_questions))
            return
        }
        initDialogPlayStart(test)
    }

    private fun editTest(test: Test) {
        EditActivity.startActivity(requireActivity(), test.id)
    }

    private fun deleteTest(test: Test) {
        ConfirmDangerDialogFragment.newInstance(
            title = getString(R.string.message_delete_exam, test.title),
            buttonText = getString(R.string.button_delete_confirm),
            completion = {
                testViewModel.delete(test)
                categoryViewModel.refresh()
                requireContext().showToast(getString(R.string.msg_success_delete_test))
            }
        ).show(childFragmentManager, ConfirmDangerDialogFragment::class.java.simpleName)
    }

    private fun uploadTest(test: Test) {
        logger.logEvent("upload_from_share_local")
        UploadTestActivity.startActivityForResult(this, REQUEST_UPLOAD_TEST, test.id)
    }

    private fun initDialogPlayStart(test: Test) {

        if (!sharedPreferenceManager.isShowPlaySettingDialog) {
            startAnswer(test, (test.startPosition), test.limit)
        } else {

            childFragmentManager.setFragmentResultListener(
                REQUEST_PLAY_CONFIG,
                viewLifecycleOwner
            ) { requestKey, bundle ->
                if (requestKey != REQUEST_PLAY_CONFIG) return@setFragmentResultListener

                val position = bundle.getInt(PlayConfigDialogFragment.RESULT_START_POSITION)
                val limit = bundle.getInt(PlayConfigDialogFragment.RESULT_LIMIT)
                startAnswer(test, (position - 1).coerceAtLeast(0), limit)
            }

            PlayConfigDialogFragment.newInstance(test, REQUEST_PLAY_CONFIG)
                .show(childFragmentManager, "TAG")
        }
    }

    private fun startAnswer(test: Test, start: Int, limit: Int) {

        var incorrect = false

        for (element in test.questions) if (!(element.isCorrect)) incorrect = true

        if (!incorrect && sharedPreferenceManager.refine) {
            requireContext().showToast(getString(R.string.message_null_wrongs))
        } else {

            val result = test.copy(
                limit = limit,
                startPosition = start,
                history = Calendar.getInstance().timeInMillis
            )

            testViewModel.update(result)

            AnswerWorkbookActivity.startActivity(requireActivity(), result.id)

//            PlayActivity.startActivity(requireActivity(), result.id)

        }
    }

    private fun editCategory(category: Category) {

        EditTextDialogFragment.newInstance(
            title = getString(R.string.title_dialog_edit_category),
            defaultText = category.name,
            hint = getString(R.string.hint_category_name)
        )
        { text ->
            testViewModel.renameAllInCategory(category.name, text)
            categoryViewModel.update(category.copy(name = text))
        }.show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun deleteCategory(category: Category) {
        ConfirmDangerDialogFragment.newInstance(
            title = getString(R.string.message_delete_category, category.name),
            buttonText = getString(R.string.button_delete_confirm),
            completion = {
                testViewModel.deleteAllInCategory(category.name)
                categoryViewModel.delete(category)
                requireContext().showToast(getString(R.string.msg_success_delete_category))
            }
        ).show(childFragmentManager, ConfirmDangerDialogFragment::class.java.simpleName)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_UPLOAD_TEST && resultCode == Activity.RESULT_OK) {

            val documentId = data?.getStringExtra(EXTRA_DOCUMENT_ID) ?: return
            val testName = data.getStringExtra(EXTRA_TEST_NAME) ?: return

            requireActivity().executeJobWithDialog(
                title = getString(R.string.msg_creating_share_test_link),
                task = {
                    dynamicLinksCreator.createShareTestDynamicLinks(documentId)
                },
                onSuccess = {
                    it.shortLink?.let {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                getString(R.string.msg_share_test, testName, it)
                            )
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }
                },
                onFailure = {
                    requireContext().showToast(getString(R.string.msg_failure_share_test))
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        testViewModel.refresh()
        categoryViewModel.refresh()
    }

    companion object {
        const val REQUEST_UPLOAD_TEST = 54322
        const val REQUEST_PLAY_CONFIG = "request_play_config"

        const val EXTRA_DOCUMENT_ID = "documentId"
        const val EXTRA_TEST_NAME = "testName"
    }
}