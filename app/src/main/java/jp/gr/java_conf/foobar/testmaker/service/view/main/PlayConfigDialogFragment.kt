package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogStartBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import org.koin.android.ext.android.inject
import java.util.*

class PlayConfigDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val ARG_TEST = "test"
        const val ARG_REQUEST_KEY = "request_key"

        const val RESULT_LIMIT = "result_limit"
        const val RESULT_START_POSITION = "result_start_position"

        fun newInstance(test: Test, requestKey: String): PlayConfigDialogFragment =
                PlayConfigDialogFragment().apply {
                    arguments = bundleOf(
                            ARG_TEST to test,
                            ARG_REQUEST_KEY to requestKey
                    )
                }
    }

    private val sharedPreferenceManager: SharedPreferenceManager by inject()

    private val test: Test by lazy {
        arguments?.getParcelable<Test>(ARG_TEST) ?: throw RuntimeException("test does not exist")
    }
    private val requestKey: String by lazy {
        arguments?.getString(ARG_REQUEST_KEY) ?: throw RuntimeException("requestKey does not exist")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return DataBindingUtil.inflate<DialogStartBinding>(inflater, R.layout.dialog_start, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

            title = test.title

            setLimit.setText(test.limit.toString())
            setStartPosition.setText((test.startPosition + 1).toString())

            checkRandom.isChecked = sharedPreferenceManager.random
            checkRandom.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.random = isChecked }

            checkReverse.isChecked = sharedPreferenceManager.reverse
            checkReverse.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.reverse = isChecked }

            checkManual.isChecked = sharedPreferenceManager.manual
            checkManual.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.manual = isChecked }

            checkAudio.isChecked = sharedPreferenceManager.audio
            checkAudio.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.audio = isChecked }

            checkRefine.isChecked = sharedPreferenceManager.refine
            checkRefine.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.refine = isChecked }

            checkAlwaysReview.isChecked = sharedPreferenceManager.alwaysReview
            checkAlwaysReview.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.alwaysReview = isChecked }

            checkCaseInsensitive.isChecked = sharedPreferenceManager.isCaseInsensitive
            checkCaseInsensitive.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.isCaseInsensitive = isChecked }
            if (Locale.getDefault().language != "en") checkCaseInsensitive.visibility = View.GONE

            buttonStart.setOnClickListener {
                if (setStartPosition.text.isNullOrBlank()) {
                    requireContext().showToast(getString(R.string.message_null_start))
                    return@setOnClickListener
                }
                if (setLimit.text.isNullOrBlank()) {
                    requireContext().showToast(getString(R.string.message_null_number))
                    return@setOnClickListener
                }

                setFragmentResult(requestKey, bundleOf(
                        RESULT_LIMIT to setLimit.text.toString().toInt(),
                        RESULT_START_POSITION to setStartPosition.text.toString().toInt()
                ))
                dismiss()
            }
        }.root
    }
}