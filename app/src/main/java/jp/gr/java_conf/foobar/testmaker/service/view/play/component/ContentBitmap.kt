package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.storage.FirebaseStorage
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.modules.GlideApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun ContentBitmap(modifier: Modifier = Modifier,imageUrl: String){
    val context = LocalContext.current

    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit){

        if (imageUrl.contains("/")){
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child(imageUrl)
            val target = GlideApp.with(context).asBitmap().load(storageRef).submit()

            coroutineScope{
                try {
                    withContext(Dispatchers.IO){
                        bitmap = target.get()
                    }
                } catch (e: Exception) {
                    Log.d(this.javaClass.name,"${e.message}")
                }
            }
        }else{
            try {
                val file = context.getFileStreamPath(imageUrl)
                bitmap =  BitmapFactory.decodeFile(file.absolutePath)
            } catch (e: IOException) {
                Log.d(this.javaClass.name,"${e.message}")
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
    } ?: run {
        Image(
            modifier = modifier.padding(16.dp),
            painter = painterResource(id = R.drawable.ic_insert_photo_white_24dp),
            contentDescription = ""
        )
    }
}