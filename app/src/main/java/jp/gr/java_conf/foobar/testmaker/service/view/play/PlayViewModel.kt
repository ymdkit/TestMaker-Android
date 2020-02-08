package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class PlayViewModel(private val repository: TestMakerRepository) : ViewModel() {

    var testId: Long = -1

    fun getTest(): Test = repository.getTest(testId)
    fun resetSolving() = repository.resetSolving(testId)
    fun updateCorrect(quest: Quest, correct: Boolean) = repository.updateCorrect(quest, correct)
    fun updateSolving(quest: Quest, solving: Boolean) = repository.updateSolving(quest, solving)
    suspend fun loadImage(imagePath: String) = repository.loadImage(imagePath)


}