package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object DynamicLinkCreator {
    fun createDynamicLink(id: String): Uri {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://testmaker-1cb29.com/${id}"))
                .setDomainUriPrefix("https://testmaker.page.link")
                .setAndroidParameters(DynamicLink.AndroidParameters.Builder().setMinimumVersion(87).build())
                .setIosParameters(DynamicLink.IosParameters.Builder("jp.gr.java-conf.foobar.testmaker.service")
                        .setAppStoreId("1201200202")
                        .setMinimumVersion("2.1.5")
                        .build())
                .buildDynamicLink()

        return dynamicLink.uri
    }

    suspend fun createInviteGroupDynamicLink(groupId: String): Uri {

        return Firebase.dynamicLinks.shortLinkAsync {
            link = Uri.parse("https://testmaker-1cb29.com/groups/${groupId}")
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
}