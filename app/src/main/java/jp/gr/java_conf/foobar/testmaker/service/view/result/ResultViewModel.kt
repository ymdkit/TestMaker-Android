package jp.gr.java_conf.foobar.testmaker.service.view.result

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.History
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.HistoryRepository
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository
import jp.studyplus.android.sdk.PostCallback
import jp.studyplus.android.sdk.Studyplus
import jp.studyplus.android.sdk.StudyplusError
import jp.studyplus.android.sdk.record.StudyRecord
import jp.studyplus.android.sdk.record.StudyRecordAmountTotal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultViewModel(
    private val testId: Long,
    private val repository: HistoryRepository,
    private val testRepository: TestRepository,
    private val studyPlus: Studyplus
) : ViewModel() {

    val test = testRepository.get().find { it.id == testId } ?: throw NullPointerException()

    val questions =
        testRepository.get().find { it.id == testId }?.questions?.filter { it.isSolved }
            ?: throw NullPointerException()

    val scoreList = listOf(questions.count { it.isCorrect },
        questions.count { !it.isCorrect }).map(Int::toFloat)

    val scoreText = "${questions.count { it.isCorrect }}/${questions.size}"

    fun createAnswerHistory(user: FirebaseUser) {
        val test = testRepository.get().find { it.id == testId } ?: return
        if (test.documentId.isEmpty()) return

        val history = History(
            userId = user.uid,
            userName = user.displayName ?: "",
            numCorrect = questions.count { it.isCorrect },
            numSolved = questions.size
        )

        viewModelScope.launch {
            repository.createHistory(test.documentId, history)
        }
    }

    fun createStudyPlusRecord(duration: Long, context: Context) {

        if (!studyPlus.isAuthenticated()) return

        val record = StudyRecord(
            duration = (duration / 1000).toInt(),
            amount = StudyRecordAmountTotal(questions.size),
            comment = "${test.title} で勉強しました"
        )

        viewModelScope.launch(Dispatchers.Default) {
            studyPlus.postRecord(record,
                object : PostCallback {
                    override fun onSuccess() {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                context.getString(R.string.msg_upload_study_plus),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                    override fun onFailure(e: StudyplusError) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                context.getString(R.string.msg_upload_study_plus),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }

}