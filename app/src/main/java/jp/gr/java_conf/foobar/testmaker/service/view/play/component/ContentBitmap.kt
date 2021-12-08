package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import java.io.IOException

@Composable
fun ContentBitmap(modifier: Modifier = Modifier,imageUrl: String){
    val context = LocalContext.current

    val bitmap = getBitmap(context, imageUrl)

    bitmap?.let {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "",
            modifier = modifier.padding(16.dp)
        )
    } ?: run {
        Image(
            modifier = modifier.padding(16.dp),
            painter = painterResource(id = R.drawable.ic_insert_photo_white_24dp),
            contentDescription = ""
        )
    }
}

fun getBitmap(context: Context, imageUrl: String): Bitmap? {
    return try {
        val file = context.getFileStreamPath(imageUrl)
        return BitmapFactory.decodeFile(file.absolutePath)
    } catch (e: IOException) {
        null
    }
}