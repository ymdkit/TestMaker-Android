package com.example.ui.answer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.QuestionImage
import com.example.ui.core.GlideApp
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun ContentBitmap(
    image: QuestionImage
) {
    val context = LocalContext.current

    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

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

    bitmap?.let {
        AndroidView(
            factory = { context ->
                PhotoView(context).apply {
                    this.setImageBitmap(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}