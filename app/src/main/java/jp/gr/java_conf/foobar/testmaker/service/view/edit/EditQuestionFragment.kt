package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.isseiaoki.simplecropview.CropImageView
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

abstract class EditQuestionFragment : Fragment() {
    abstract val editQuestionViewModel: EditQuestionViewModel
    private val testViewModel: TestViewModel by sharedViewModel()

    private val fileName: String
        get() {
            val c = Calendar.getInstance()
            return c.get(Calendar.YEAR).toString() + "_" + (c.get(Calendar.MONTH) + 1) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND) + "_" + c.get(Calendar.MILLISECOND) + ".png"
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return
        if (data == null) return

        val bitmap = when (requestCode) {
            REQUEST_SAF_PICK_IMAGE -> getBitmapFromUri(data.data)
            else -> data.extras?.get("data") as Bitmap
        }

        try {

            val dialogLayout = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_crop,
                    requireActivity().findViewById(R.id.layout_dialog_crop_image))

            val cropView = dialogLayout.findViewById<CropImageView>(R.id.cropImageView)
            cropView.imageBitmap = bitmap

            AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
                    .setView(dialogLayout)
                    .setTitle(getString(R.string.trim))
                    .setPositiveButton(android.R.string.ok) { _, _ ->

                        val name = fileName
                        lifecycleScope.launch {
                            saveImage(name, cropView.croppedBitmap)
                            editQuestionViewModel.imagePath.postValue(name)
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>
                                            , grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CAMERA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takePicture()
                }
                return
            }
        }
    }

    protected fun saveQuestion() {
        requireContext().showToast(getString(R.string.msg_save))
        if (editQuestionViewModel.selectedQuestion.id == Question().id) {
            testViewModel.create(testViewModel.get(editQuestionViewModel.testId), editQuestionViewModel.createQuestion())
        } else {
            testViewModel.update(editQuestionViewModel.createQuestion())
            requireActivity().finish()
        }

        editQuestionViewModel.resetForm()
    }

    protected fun showAlertImage() {
        AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
                .setItems(
                        if (!editQuestionViewModel.imagePath.value.isNullOrEmpty()) resources.getStringArray(R.array.action_image) else resources.getStringArray(R.array.action_image).take(2).toTypedArray()
                ) { _, which ->

                    when (which) {
                        CAMERA -> {
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {

                                requestPermissions(
                                        arrayOf(Manifest.permission.CAMERA),
                                        REQUEST_PERMISSION_CAMERA)
                            } else {
                                takePicture()
                            }
                        }
                        GALLERY -> {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                            intent.addCategory(Intent.CATEGORY_OPENABLE)
                            intent.type = "image/*"
                            startActivityForResult(intent, REQUEST_SAF_PICK_IMAGE)
                        }
                        REMOVE -> {
                            editQuestionViewModel.imagePath.value = ""
                        }
                    }
                }
                .show()

    }

    private fun takePicture() {
        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri?): Bitmap {
        val parcelFileDescriptor = requireActivity().contentResolver.openFileDescriptor(uri!!, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun saveImage(fileName: String, bitmap: Bitmap) {
        val imageOptions = BitmapFactory.Options()
        imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
        try {

            val outStream = requireContext().openFileOutput(fileName, 0)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val REQUEST_PERMISSION_CAMERA = 10000
        const val REQUEST_SAF_PICK_IMAGE = 10001
        const val REQUEST_IMAGE_CAPTURE = 10002

        const val CAMERA = 0
        const val GALLERY = 1
        const val REMOVE = 2

    }

}