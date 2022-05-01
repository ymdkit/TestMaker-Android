package jp.gr.java_conf.foobar.testmaker.service.view.group

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.GroupRepository
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val repository: GroupRepository
) : ViewModel() {

    suspend fun getGroups(userId: String) = repository.getGroups(userId)

    suspend fun createGroup(userId: String, groupName: String): Group =
        repository.createGroup(userId, groupName)


    suspend fun joinGroup(userId: String, group: Group) =
        repository.joinGroup(userId, group, group.id)

    fun createUser(user: FirebaseUser) = repository.createUser(user)

}