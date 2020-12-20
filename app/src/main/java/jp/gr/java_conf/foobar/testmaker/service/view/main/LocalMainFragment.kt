package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyTouchHelper
import com.firebase.ui.auth.AuthUI
import com.google.firebase.analytics.FirebaseAnalytics
import jp.gr.java_conf.foobar.testmaker.service.CardCategoryBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.CardTestBindingModel_
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.LocalMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
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

    private var selectedTest: RealmTest? = null //ログイン時に一度画面から離れるので選択中の値を保持

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mainController = MainController(requireContext())
        mainController.setOnClickListener(object : MainController.OnClickListener {

            override fun onClickPlayTest(test: Test) {

                firebaseAnalytic.logEvent("play", Bundle())

                if (test.questions.isEmpty()) {

                    Toast.makeText(requireContext(), getString(R.string.message_null_questions), Toast.LENGTH_SHORT).show()

                } else {

                    initDialogPlayStart(test)

                }
            }

            override fun onClickEditTest(test: Test) {

                firebaseAnalytic.logEvent("edit", Bundle())

                EditActivity.startActivity(requireActivity(), test.id)

            }

            override fun onClickDeleteTest(test: Test) {

                firebaseAnalytic.logEvent("delete", Bundle())

                val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
                builder.setTitle(getString(R.string.delete_exam))
                builder.setMessage(getString(R.string.message_delete_exam, test.title))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    testViewModel.delete(test)
                    categoryViewModel.refresh()
                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.create().show()
            }

            override fun onClickShareTest(test: Test) {

                AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
                        .setTitle(getString(R.string.title_dialog_share))
                        .setItems(resources.getStringArray(R.array.action_share)) { dialog, which ->

                            when (which) {
                                0 -> { //リンクの共有
                                    localMainViewModel.getUser()?.let {
                                        dialog.dismiss()
                                        uploadTest(RealmTest.createFromTest(test))
                                    } ?: run {
                                        login(RealmTest.createFromTest(test))
                                    }
                                }

                                1 -> { //テキスト変換
                                    lifecycleScope.launch {
                                        val progress = AlertDialog.Builder(requireContext())
                                                .setTitle(getString(R.string.converting))
                                                .setView(LayoutInflater.from(requireContext()).inflate(R.layout.dialog_progress, requireActivity().findViewById(R.id.layout_progress))).create()

                                        progress.show()
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
                                        progress.dismiss()
                                    }
                                }
                            }

                        }.show()
            }
        })

        categoryViewModel.hasTestsCategories.observeNonNull(this) {
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

            recyclerView.adapter = mainController.adapter

            EpoxyTouchHelper
                    .initDragging(mainController)
                    .withRecyclerView(recyclerView)
                    .forVerticalList()
                    .forAllModels()
                    .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<EpoxyModel<*>>() {
                        override fun onModelMoved(fromPosition: Int, toPosition: Int, modelBeingMoved: EpoxyModel<*>?, itemView: View?) {
                            val from = mainController.adapter.getModelAtPosition(fromPosition)
                            val to = mainController.adapter.getModelAtPosition(toPosition)

                            if (from is CardTestBindingModel_ && to is CardTestBindingModel_) {
                                testViewModel.swap(from.test(), to.test())
                            } else if (from is CardCategoryBindingModel_ && to is CardCategoryBindingModel_) {
                                categoryViewModel.swap(from.category(), to.category())
                            }
                        }
                    })
        }.root
    }

    private fun uploadTest(test: RealmTest) {
        UploadTestActivity.startActivityForResult(this, REQUEST_UPLOAD_TEST, test.id)
    }

    private fun initDialogPlayStart(test: Test) {

        val dialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_start, requireActivity().findViewById(R.id.layout_dialog_start))

        val editLimit = dialogLayout.findViewById<EditText>(R.id.set_limit)
        editLimit.setText(test.limit.toString())

        val editStart = dialogLayout.findViewById<EditText>(R.id.set_start_position)
        editStart.setText((test.startPosition + 1).toString())

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
        if (Locale.getDefault().language != "en") checkCaseInsensitive.visibility = View.GONE

        val buttonStart = dialogLayout.findViewById<Button>(R.id.button_start)
        buttonStart.setOnClickListener {
            startAnswer(test, editStart.text.toString(), editLimit.text.toString())
        }

        val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
        builder.setView(dialogLayout)
        builder.setTitle(getString(R.string.way))
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setNegativeButton(android.R.string.cancel, null)

        if (!sharedPreferenceManager.isShowPlaySettingDialog) {
            startAnswer(test, editStart.text.toString(), editLimit.text.toString())
        } else {
            val dialog = builder.show()
            hideDefaultButtonsFromDialog(dialog)

        }


    }

    private fun hideDefaultButtonsFromDialog(dialog: AlertDialog) {

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        if (positiveButton != null) positiveButton.visibility = View.GONE

        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        if (negativeButton != null) negativeButton.visibility = View.GONE

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

    private fun login(test: RealmTest) {

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