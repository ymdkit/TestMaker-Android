package jp.gr.java_conf.foobar.testmaker.service.view.group

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.GroupRepository

class GroupDetailViewModel(private val repository: GroupRepository) : ViewModel() {

    suspend fun getTests(groupId: String) = repository.getTests(groupId)

}