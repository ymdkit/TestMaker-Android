package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.net.Uri
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object DynamicLinksCreator {

    suspend fun createShareTestDynamicLinks(documentId: String): Uri =
            createDynamicLinks(link = Uri.parse("https://testmaker-1cb29.com/${documentId}"))

    suspend fun createInviteGroupDynamicLinks(groupId: String): Uri =
            createDynamicLinks(link = Uri.parse("https://testmaker-1cb29.com/groups/${groupId}"))

    private suspend fun createDynamicLinks(link: Uri) =
            Firebase.dynamicLinks.shortLinkAsync {
                this.link = link
                domainUriPrefix = ("https://testmaker.page.link")
                androidParameters {
                    minimumVersion = 87
                }
                iosParameters("jp.gr.java-conf.foobar.testmaker.service") {
                    appStoreId = "1201200202"
                    minimumVersion = "2.1.5"
                }
                navigationInfoParameters {
                    forcedRedirectEnabled = true
                }
            }.await().shortLink ?: Uri.parse("")
}