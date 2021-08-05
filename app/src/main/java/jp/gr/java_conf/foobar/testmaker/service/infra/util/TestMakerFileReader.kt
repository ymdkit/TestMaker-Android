package jp.gr.java_conf.foobar.testmaker.service.infra.util

import android.app.Activity
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader

object TestMakerFileReader {

     // ファイル名 : ファイルの中身 のペアを返却します
     fun readFileFromUri(uri: Uri, activity: Activity): Pair<String, String> {
        runCatching {
            val cursor =
                activity.contentResolver.query(uri, null, null, null, null) ?: throw NullPointerException()
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            val title = cursor.getString(nameIndex)
            cursor.close()

            val inputStream = activity.contentResolver.openInputStream(uri) ?: throw NullPointerException()
            val text = inputStream.bufferedReader().use(BufferedReader::readText).replaceFirst("\uFEFF", "")
            inputStream.close()
            title to text
        }.onSuccess {
            return it
        }.onFailure {
            return "" to ""
        }
        return "" to ""
    }
}