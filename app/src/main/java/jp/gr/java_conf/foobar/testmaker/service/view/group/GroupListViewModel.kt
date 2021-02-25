package jp.gr.java_conf.foobar.testmaker.service.view.group

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.GroupRepository

class GroupListViewModel(private val repository: GroupRepository) : ViewModel() {

    suspend fun getGroups(userId: String) = repository.getGroups(userId)

    suspend fun createGroup(userId: String, name: String): Group {
        val group = Group(userId = userId, name = name)
        return repository.createGroup(group)
    }

    suspend fun joinGroup(userId: String, group: Group) = repository.joinGroup(userId, group)

    fun createUser(user: FirebaseUser) = repository.createUser(user)

}