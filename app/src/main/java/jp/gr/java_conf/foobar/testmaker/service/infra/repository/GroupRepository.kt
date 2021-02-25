package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource

class GroupRepository(private val dataSource: RemoteDataSource) {

    suspend fun getGroups(userId: String) = dataSource.getGroups(userId)

}