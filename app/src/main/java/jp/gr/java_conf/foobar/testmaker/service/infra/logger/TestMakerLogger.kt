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

    fun logCreateTestEvent(test: Test) =
        analytics.logEvent("create_test"){
            param(FirebaseAnalytics.Param.ITEM_NAME, test.title)
        }

    fun logCreateQuestionEvent(question: Question) =
        analytics.logEvent("create_question"){
            param("question", question.question)
            param("answer", question.answer)
            param("answers", question.answers.joinToString(","))
            param("others", question.others.joinToString(","))
            param("is_auto", question.isAutoGenerateOthers.toString())
            param("is_check_complete", question.isCheckOrder.toString())
            param("is_use_image", question.imagePath.isNotEmpty().toString())
            param("type", question.type.toString())
            param("explanation", question.explanation)
        }

    fun logEvent(eventName: String) = analytics.logEvent(eventName){}

}