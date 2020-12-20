package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

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
}