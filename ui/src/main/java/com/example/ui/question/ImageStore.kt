package com.example.ui.question

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

// todo ファイルの置き場所を再考する
class ImageStore {

    // return file name
    fun saveImage(bitmap: Bitmap, context: Context): String{
        val c = Calendar.getInstance()
        val fileName = c.get(Calendar.YEAR)
            .toString() + "_" + (c.get(Calendar.MONTH) + 1) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_" + c.get(
            Calendar.HOUR_OF_DAY
        ) + "_" + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND) + "_" + c.get(Calendar.MILLISECOND) + ".png"

        val imageOptions = BitmapFactory.Options()
        imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
        try {

            val outStream = context.openFileOutput(fileName, 0)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return fileName
    }

}