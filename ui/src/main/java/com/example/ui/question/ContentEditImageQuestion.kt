package com.example.ui.question

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.core.QuestionImage
import com.example.ui.R
import com.example.ui.core.GlideApp
import com.example.ui.core.item.ClickableListItem
import com.example.ui.core.showToast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun ContentEditImageQuestion(
    image: QuestionImage,
    fragmentManager: FragmentManager,
    onValueChange: (QuestionImage) -> Unit
) {
    val context = LocalContext.current

    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }
    var showingDialog by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {

                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(it, "r")
                val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
                BitmapFactory.decodeFileDescriptor(fileDescriptor)?.let {
                    CropImageDialogFragment(
                        bitmap = it,
                        onCrop = { newBitmap ->
                            bitmap = newBitmap
                        }
                    ).show(fragmentManager, "")
                }
                parcelFileDescriptor.close()
            }
        }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap: Bitmap? ->
            bitmap?.let {
                CropImageDialogFragment(
                    bitmap = it,
                    onCrop = { newBitmap ->
                        val path = ImageStore().saveImage(newBitmap, context = context)
                        onValueChange(QuestionImage.LocalImage(path = path))
                    }
                ).show(fragmentManager, "")
            }
        }
    )

    val askCameraPermitLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted: Boolean ->
            if (isGranted) {
                CameraLauncher().takePicture(context) {
                    takePictureLauncher.launch(null)
                }
            } else {
                context.showToast(
                    context.getString(R.string.msg_camera_not_granted),
                    Toast.LENGTH_LONG
                )
            }
        }
    )

    LaunchedEffect(image) {
        when (image) {
            is QuestionImage.Empty -> {
                bitmap = null
            }
            is QuestionImage.FireStoreImage -> {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(image.ref)
                val target = GlideApp.with(context).asBitmap().load(storageRef).submit()

                withContext(Dispatchers.IO) {
                    try {
                        bitmap = (target.get())
                    } catch (e: Exception) {
                        Log.d(this.javaClass.name, "${e.message}")
                    }
                }
            }
            is QuestionImage.LocalImage -> {
                try {
                    val file = context.getFileStreamPath(image.path)
                    bitmap = BitmapFactory.decodeFile(file.absolutePath)
                } catch (e: IOException) {
                    Log.d(this.javaClass.name, "${e.message}")
                }
            }
        }
    }

    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(bottom = 8.dp),
        onClick = { showingDialog = true },
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
        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "")
        } ?: run {
            Icon(Icons.Default.AddAPhoto, contentDescription = "add photo")
        }
    }
    if (showingDialog) {


        Dialog(onDismissRequest = { showingDialog = false }) {
            Surface(shape = RoundedCornerShape(8.dp)) {
                Column(
                    modifier = Modifier
                        .padding(
                            start = 24.dp,
                            top = 32.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.title_image_menu),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ClickableListItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Camera,
                                    contentDescription = "camera"
                                )
                            },
                            text = stringResource(id = R.string.button_take_photo),
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    )
                                    != PackageManager.PERMISSION_GRANTED
                                ) {
                                    askCameraPermitLauncher.launch(Manifest.permission.CAMERA)
                                } else {
                                    showingDialog = false
                                    CameraLauncher().takePicture(context) {
                                        takePictureLauncher.launch(null)
                                    }
                                }
                            }
                        )
                        ClickableListItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Photo,
                                    contentDescription = "gallery"
                                )
                            },
                            text = stringResource(id = R.string.button_select_gallery),
                            onClick = {
                                showingDialog = false
                                launcher.launch("image/*")
                            }
                        )
                        ClickableListItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "trash"
                                )
                            },
                            text = stringResource(id = R.string.button_delete_image),
                            onClick = {
                                showingDialog = false
                                onValueChange(QuestionImage.Empty)
                            }
                        )
                    }
                }
            }
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