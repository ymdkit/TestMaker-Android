package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.stickyheader.StickyHeaderLinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.analytics.FirebaseAnalytics
import jp.gr.java_conf.foobar.testmaker.service.CardCategoryBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.ItemTestBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.LocalMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditActivity
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.online.UploadTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.LoadingDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class LocalMainFragment : Fragment() {

    private val localMainViewModel: LocalMainViewModel by viewModel()

    internal lateinit var mainController: MainController
    private val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val firebaseAnalytic: FirebaseAnalytics by inject()

    private var binding: LocalMainFragmentBinding? = null

    private val testViewModel: TestViewModel by sharedViewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()
    private val service: CloudFunctionsService by inject()

    private var selectedTest: Test? = null //ログイン時に一度画面から離れるので選択中の値を保持

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mainController = MainController(requireContext())
        mainController.setOnClickListener(object : MainController.OnClickListener {

            override fun onClickTest(test: Test) {
                ListDialogFragment(
                        test.title,
                        listOf(
                                DialogMenuItem(title = getString(R.string.play), iconRes = R.drawable.ic_play_arrow_white_24dp, action = { playTest(test) }),
                                DialogMenuItem(title = getString(R.string.edit), iconRes = R.drawable.ic_edit_white, action = { editTest(test) }),
                                DialogMenuItem(title = getString(R.string.delete), iconRes = R.drawable.ic_delete_white, action = { deleteTest(test) }),
                                DialogMenuItem(title = getString(R.string.share), iconRes = R.drawable.ic_share_white, action = { shareTest(test) })
                        )
                ).show(requireActivity().supportFragmentManager, "TAG")
            }

            override fun onClickCategoryMenu(category: Category) {

                ListDialogFragment(
                        category.name,
                        listOf(
                                DialogMenuItem(title = getString(R.string.edit_category_name), iconRes = R.drawable.ic_edit_white, action = { editCategory(category) }),
                                DialogMenuItem(title = getString(R.string.delete), iconRes = R.drawable.ic_delete_white, action = { deleteCategory(category) })
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

        return DataBindingUtil.inflate<LocalMainFragmentBinding>(inflater, R.layout.local_main_fragment, container, false).apply {
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
                    .withTargets(CardCategoryBindingModel_::class.java, ItemTestBindingModel_::class.java)
                    .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<EpoxyModel<*>>() {
                        override fun onModelMoved(fromPosition: Int, toPosition: Int, modelBeingMoved: EpoxyModel<*>?, itemView: View?) {
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
        firebaseAnalytic.logEvent("play", Bundle())

        if (test.questions.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.message_null_questions), Toast.LENGTH_SHORT).show()
        } else {
            initDialogPlayStart(test)
        }
    }

    private fun editTest(test: Test) {
        firebaseAnalytic.logEvent("edit", Bundle())
        EditActivity.startActivity(requireActivity(), test.id)
    }

    private fun deleteTest(test: Test) {
        firebaseAnalytic.logEvent("delete", Bundle())

        ConfirmDangerDialogFragment(getString(R.string.message_delete_exam, test.title)) {
            testViewModel.delete(test)
            categoryViewModel.refresh()
        }.show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun shareTest(test: Test) {
        ListDialogFragment(
                getString(R.string.title_dialog_share),
                listOf(
                        DialogMenuItem(title = getString(R.string.button_upload), iconRes = R.drawable.ic_baseline_cloud_upload_24, action = { uploadTest(test) }),
                        DialogMenuItem(title = getString(R.string.button_convert_to_csv), iconRes = R.drawable.ic_edit_white, action = { convertTestToCSV(test) })
                )
        ).show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun uploadTest(test: Test) {
        localMainViewModel.getUser()?.let {
            firebaseAnalytic.logEvent("upload_from_share_local", Bundle())
            UploadTestActivity.startActivityForResult(this, REQUEST_UPLOAD_TEST, test.id)
        } ?: run {
            login(test)
        }
    }

    private fun convertTestToCSV(test: Test) {

        var dialog: LoadingDialogFragment? = null
        val job = lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    service.testToText(test.escapedTest.copy(lang = if (Locale.getDefault().language == "ja") "ja" else "en"))
                }
            }.onSuccess {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, it.text)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }.onFailure {
                requireContext().showErrorToast(it)
            }
            withContext(Dispatchers.Main) {
                dialog?.dismiss()
            }
        }

        dialog = LoadingDialogFragment(
                title = getString(R.string.converting),
                onCanceled = {
                    job.cancel()
                }
        )
        dialog.show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun initDialogPlayStart(test: Test) {

        if (!sharedPreferenceManager.isShowPlaySettingDialog) {
            startAnswer(test, (test.startPosition + 1).toString(), test.limit.toString())
        } else {
            PlayConfigDialogFragment(test) { position, limit ->
                startAnswer(test, position, limit)
            }.show(requireActivity().supportFragmentManager, "TAG")
        }
    }

    private fun startAnswer(test: Test, start: String, limit: String) {

        var incorrect = false

        for (element in test.questions) if (!(element.isCorrect)) incorrect = true

        if (!incorrect && sharedPreferenceManager.refine) {

            Toast.makeText(requireContext(), getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show()

        } else if (limit == "") {

            Toast.makeText(requireContext(), getString(R.string.message_null_number), Toast.LENGTH_SHORT).show()

        } else if (start == "" || start.toInt() > test.questions.size || start.toInt() < 1) {

            Toast.makeText(requireContext(), getString(R.string.message_null_start), Toast.LENGTH_SHORT).show()

        } else {

            val result = test.copy(
                    limit = Integer.parseInt(limit),
                    startPosition = Integer.parseInt(start) - 1,
                    history = Calendar.getInstance().timeInMillis)

            testViewModel.update(result)

            PlayActivity.startActivity(requireActivity(), result.id)

        }
    }

    private fun login(test: Test) {

        selectedTest = test

        AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.login))
                .setMessage(getString(R.string.msg_not_login))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val providers = arrayListOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build())

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setTosAndPrivacyPolicyUrls(
                                            "https://testmaker-1cb29.firebaseapp.com/terms",
                                            "https://testmaker-1cb29.firebaseapp.com/privacy")
                                    .build(),
                            REQUEST_SIGN_IN_UPLOAD)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
    }

    private fun editCategory(category: Category) {

        val dialogLayout = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_edit_category_name, requireActivity().findViewById(R.id.layout_dialog_edit_category))
        val editCategory = dialogLayout.findViewById<EditText>(R.id.edit_category_name)
        editCategory.setText(category.name)
        val buttonSave = dialogLayout.findViewById<Button>(R.id.button_save)
        val dialog = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
                .setView(dialogLayout)
                .setTitle(getString(R.string.title_dialog_edit_category))
                .show()

        buttonSave.setOnClickListener {
            val categoryName = editCategory.text.toString()
            if (categoryName.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.message_shortage), Toast.LENGTH_SHORT).show()
            } else {

                testViewModel.renameAllInCategory(category.name, categoryName)
                categoryViewModel.update(category.copy(name = categoryName))
                dialog.dismiss()
            }
        }

    }

    private fun deleteCategory(category: Category) {

        ConfirmDangerDialogFragment(getString(R.string.message_delete_category, category.name)) {
            testViewModel.deleteAllInCategory(category.name)
            categoryViewModel.delete(category)
        }.show(requireActivity().supportFragmentManager, "TAG")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_IN_UPLOAD && resultCode == Activity.RESULT_OK) {
            selectedTest?.let {
                uploadTest(it)
                selectedTest = null
            }
        } else if (requestCode == REQUEST_UPLOAD_TEST && resultCode == Activity.RESULT_OK) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, data?.getStringExtra("result") ?: "")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

    }

    override fun onResume() {
        super.onResume()
        testViewModel.refresh()
        categoryViewModel.refresh()
    }

    companion object {
        const val REQUEST_SIGN_IN_UPLOAD = 54321
        const val REQUEST_UPLOAD_TEST = 54322
    }
}