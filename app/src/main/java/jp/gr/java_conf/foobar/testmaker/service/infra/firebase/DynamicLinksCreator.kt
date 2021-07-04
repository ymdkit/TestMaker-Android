package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.content.Context
import android.net.Uri
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DynamicLinksCreator(private val context: Context) {

    suspend fun createShareTestDynamicLinks(documentId: String) =
            createDynamicLinks(link = Uri.parse("https://testmaker-1cb29.com/${documentId}"))

    suspend fun createInviteGroupDynamicLinks(groupId: String) =
            createDynamicLinks(link = Uri.parse("https://testmaker-1cb29.com/groups/${groupId}"))

    private suspend fun createDynamicLinks(link: Uri) = withContext(Dispatchers.Default) {
        Firebase.dynamicLinks.shortLinkAsync {
            this.link = link
            domainUriPrefix = ("https://testmaker.page.link")

            socialMetaTagParameters {
                imageUrl = Uri.parse("https://ankimaker.com/img/ogp.png")
                title = context.getString(R.string.app_name)
                description = context.getString(R.string.app_description)
            }

            androidParameters {
                minimumVersion = 87
            }
            iosParameters("jp.gr.java-conf.foobar.testmaker.service") {
                appStoreId = "1201200202"
                minimumVersion = "2.1.5"
            }
        }.await()
    }
}