package jp.gr.java_conf.foobar.testmaker.service.infra.logger

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

class TestMakerLogger(private val analytics: FirebaseAnalytics) {

    fun logSearchEvent(term: String) =
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH) {
            param(FirebaseAnalytics.Param.SEARCH_TERM, term)
        }

    fun logCreateTestEvent(title: String, source: String) =
        analytics.logEvent("create_test"){
            param(FirebaseAnalytics.Param.ITEM_NAME, title)
            param(FirebaseAnalytics.Param.SOURCE, source)
        }

    fun logAnsweredTestEvent(title: String, count: Int) =
        analytics.logEvent("answered_test"){
            param(FirebaseAnalytics.Param.ITEM_NAME, title)
            param("count", count.toLong())
        }

    fun logEvent(eventName: String) = analytics.logEvent(eventName){}

}