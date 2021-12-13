package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogCropBinding
import kotlinx.coroutines.launch

//todo fragmentの再生成を考慮する（ViewModel 等で bitmap を保持する）
class CropImageDialogFragment(
    private val bitmap: Bitmap,
    private val onCrop: (Bitmap) -> Unit
): DialogFragment() {

    private lateinit var binding: DialogCropBinding

    // Activity の再生成が起きた場合にダイアログを削除する
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null){
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_crop, null, false)
        binding.run {
            cropImageView.imageBitmap = bitmap
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.trim))
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->

                lifecycleScope.launch {
                    onCrop(binding.cropImageView.croppedBitmap)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}