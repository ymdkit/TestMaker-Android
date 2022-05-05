package jp.gr.java_conf.foobar.testmaker.service.view.result

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.History
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.HistoryRepository
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository
import jp.studyplus.android.sdk.PostCallback
import jp.studyplus.android.sdk.Studyplus
import jp.studyplus.android.sdk.StudyplusError
import jp.studyplus.android.sdk.record.StudyRecord
import jp.studyplus.android.sdk.record.StudyRecordAmountTotal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val testRepository: TestRepository,
    private val studyPlus: Studyplus
) : ViewModel() {

    enum class StudyPlusRecordStatus {
        READY,
        RECORDED
    }

    private var workbookId by Delegates.notNull<Long>()

    val test: Test by lazy {
        testRepository.get().find { it.id == workbookId } ?: throw NullPointerException()
    }

    val questions: List<Question> by lazy {
        testRepository.get().find { it.id == workbookId }?.questions?.filter { it.isSolved }
            ?: listOf()
    }

    val scoreList by lazy {
        listOf(questions.count { it.isCorrect },
            questions.count { !it.isCorrect }).map(Int::toFloat)
    }

    val scoreText by lazy { "${questions.count { it.isCorrect }}/${questions.size}" }

    val studyPlusRecordStatus = MutableStateFlow(StudyPlusRecordStatus.READY)

    fun setup(
        workbookId: Long
    ) {
        this.workbookId = workbookId
    }

    fun createAnswerHistory(user: FirebaseUser) {
        val test = testRepository.get().find { it.id == workbookId } ?: return
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

        studyPlusRecordStatus.value = StudyPlusRecordStatus.RECORDED

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