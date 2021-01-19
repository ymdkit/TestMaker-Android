package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogStartBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import org.koin.android.ext.android.inject
import java.util.*

class PlayConfigDialogFragment(private val test: Test, private val completion: (startPosition: String, limit: String) -> Unit) : BottomSheetDialogFragment() {

    private val sharedPreferenceManager: SharedPreferenceManager by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
                completion(setStartPosition.text.toString(), setLimit.text.toString())
            }
        }.root
    }
}