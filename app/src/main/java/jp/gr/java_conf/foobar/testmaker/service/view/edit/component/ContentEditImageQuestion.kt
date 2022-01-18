package jp.gr.java_conf.foobar.testmaker.service.view.edit.component

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.firebase.storage.FirebaseStorage
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.modules.GlideApp
import jp.gr.java_conf.foobar.testmaker.service.view.edit.CropImageDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun ContentEditImageQuestion(
    imageUrl: String,
    fragmentManager: FragmentManager,
    value: Bitmap?,
    onValueChange: (Bitmap?) -> Unit
){
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {

            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(it, "r")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            BitmapFactory.decodeFileDescriptor(fileDescriptor)?.let {
                CropImageDialogFragment(
                    bitmap = it,
                    onCrop = { newBitmap ->
                        onValueChange(newBitmap)
                    }
                ).show(fragmentManager, "")
            }
            parcelFileDescriptor.close()
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap: Bitmap? ->
            bitmap?.let{
                CropImageDialogFragment(
                    bitmap = it,
                    onCrop = { newBitmap ->
                        onValueChange(newBitmap)
                    }
                ).show(fragmentManager, "")
            }
        }
    )

    val askCameraPermitLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
        , onResult = { isGranted: Boolean ->
            if (isGranted) {
                CameraLauncher().takePicture(context) {
                    takePictureLauncher.launch(null)
                }
            } else {
                context.showToast(context.getString(R.string.msg_camera_not_granted), Toast.LENGTH_LONG)
            }
        }
    )

    LaunchedEffect(Unit){
        if(imageUrl.isNotEmpty()){
            if (imageUrl.contains("/")){
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(imageUrl)
                val target = GlideApp.with(context).asBitmap().load(storageRef).submit()

                try {
                    withContext(Dispatchers.IO){
                        onValueChange(target.get())
                    }
                } catch (e: Exception) {
                    Log.d(this.javaClass.name,"${e.message}")
                }

            }else{
                try {
                    val file = context.getFileStreamPath(imageUrl)
                    onValueChange(BitmapFactory.decodeFile(file.absolutePath))
                } catch (e: IOException) {
                    Log.d(this.javaClass.name,"${e.message}")
                }
            }
        }
    }

    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(bottom = 8.dp),
        onClick = {
            ListDialogFragment.newInstance(
                context.getString(R.string.title_image_menu),
                listOf(
                    DialogMenuItem(
                        title = context.getString(R.string.button_take_photo),
                        iconRes = R.drawable.ic_baseline_camera_alt_24,
                        action = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                )
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                askCameraPermitLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                CameraLauncher().takePicture(context) {
                                    takePictureLauncher.launch(null)
                                }
                            }
                        }),
                    DialogMenuItem(
                        title = context.getString(R.string.button_select_gallery),
                        iconRes = R.drawable.ic_insert_photo_white_24dp,
                        action = {
                            launcher.launch("image/*")
                        }),
                    DialogMenuItem(
                        title = context.getString(R.string.button_delete_image),
                        iconRes = R.drawable.ic_delete_white,
                        action = {
                            onValueChange(null)
                        })
                )
            ).show(
                fragmentManager,
                "TAG"
            )
        },
        border = BorderStroke(
            ButtonDefaults.OutlinedBorderSize,
            MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
        )
    )
    {
        Text(
            stringResource(id = R.string.button_add_image),
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.weight(weight = 1f, fill = true))
        value?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "")
        } ?: run {
            Icon(Icons.Default.AddAPhoto, contentDescription = "add photo")
        }
    }
}

class CameraLauncher {
    fun takePicture(context: Context, onLaunch: () -> Unit) {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            onLaunch()
        }
    }
}