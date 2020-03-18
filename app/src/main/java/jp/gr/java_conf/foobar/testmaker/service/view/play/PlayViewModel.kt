package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class PlayViewModel(private val repository: TestMakerRepository) : ViewModel() {

    suspend fun loadImage(imagePath: String) = repository.loadImage(imagePath)
}