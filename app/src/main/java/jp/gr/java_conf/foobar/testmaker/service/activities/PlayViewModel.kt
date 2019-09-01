package jp.gr.java_conf.foobar.testmaker.service.activities

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class PlayViewModel(private val repository: TestMakerRepository) : ViewModel() {

    fun getTest(testId: Long): Test = repository.getTest(testId)
    fun resetSolving(testId: Long) = repository.resetSolving(testId)
    fun updateCorrect(quest: Quest, correct: Boolean) = repository.updateCorrect(quest, correct)
    fun updateSolving(quest: Quest, solving: Boolean) = repository.updateSolving(quest, solving)
    fun loadImage(imagePath: String, setImage: (Bitmap) -> Unit) = repository.loadImage(imagePath, setImage)


}