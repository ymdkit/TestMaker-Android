package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    @ApplicationContext val context: Context,
) {

    // todo 画像のアップロード
    private fun uploadImage(localPath: String, remotePath: String): String {
        val storage = FirebaseStorage.getInstance()

        val storageRef = storage.reference.child(remotePath)

        val baos = ByteArrayOutputStream()
        val imageOptions = BitmapFactory.Options()
        imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
        runCatching {
            context.openFileInput(localPath)
        }.onSuccess {
            val bitmap = BitmapFactory.decodeStream(it, null, imageOptions)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()
            storageRef.putBytes(data)
            return remotePath
        }.onFailure {
            return ""
        }
        return ""
    }

    sealed class FirebasePostResponse {
        object Divided : FirebasePostResponse()
        data class Failure(val message: String?) : FirebasePostResponse()
        data class Success(val documentId: String) : FirebasePostResponse()
    }

}
