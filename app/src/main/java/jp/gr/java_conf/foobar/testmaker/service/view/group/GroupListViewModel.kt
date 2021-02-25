package jp.gr.java_conf.foobar.testmaker.service.view.group

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.GroupRepository

class GroupListViewModel(private val repository: GroupRepository) : ViewModel() {

    suspend fun getGroups(userId: String) = repository.getGroups(userId)

}