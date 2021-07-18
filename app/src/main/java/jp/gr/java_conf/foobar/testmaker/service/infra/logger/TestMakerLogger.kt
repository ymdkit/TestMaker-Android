package jp.gr.java_conf.foobar.testmaker.service.infra.logger

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

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

    fun logAnsweredTestEvent(test: Test, count: Int) =
        analytics.logEvent("answered_test"){
            param(FirebaseAnalytics.Param.ITEM_NAME, test.title)
            param(FirebaseAnalytics.Param.SOURCE, test.source)
            param("count", count.toLong())
        }

    fun logUploadTestEvent(test: Test, destination: String) =
        analytics.logEvent("upload_test"){
            param(FirebaseAnalytics.Param.ITEM_NAME, test.title)
            param(FirebaseAnalytics.Param.SOURCE, test.source)
            param(FirebaseAnalytics.Param.DESTINATION, destination)
        }

    fun logCreateQuestion(question: Question, source: String) =
        analytics.logEvent("create_question"){
            param(FirebaseAnalytics.Param.ITEM_NAME, question.question)
            param(FirebaseAnalytics.Param.SOURCE, source)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, question.type.toLong())
            param("is_auto", question.isAutoGenerateOthers.toString())
            param("is_check_order", question.isCheckOrder.toString())
            param("is_use_image", question.imagePath.isNotEmpty().toString())
            param("is_use_explanation", question.explanation.isNotEmpty().toString())
        }

    fun logAnswerQuestion(question: Question) =
        analytics.logEvent("answer_question"){
            param(FirebaseAnalytics.Param.ITEM_NAME, question.question)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, question.type.toLong())
            param("is_auto", question.isAutoGenerateOthers.toString())
            param("is_check_order", question.isCheckOrder.toString())
            param("is_use_image", question.imagePath.isNotEmpty().toString())
            param("is_use_explanation", question.explanation.isNotEmpty().toString())
            param("is_correct", question.isCorrect.toString())
        }

    fun logEvent(eventName: String) = analytics.logEvent(eventName){}

}