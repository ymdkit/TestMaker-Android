package com.example.usecase.model

import com.example.domain.model.SharedWorkbook

data class SharedWorkbookUseCaseModel(
    val id: String,
    val name: String,
    val userId: String,
    val userName: String,
    val comment: String,
    val questionListCount: Int,
    val downloadCount: Int,
    val isPublic: Boolean,
    val groupId: String?
) {
    companion object {
        fun fromSharedWorkbook(workbook: SharedWorkbook): SharedWorkbookUseCaseModel =
            SharedWorkbookUseCaseModel(
                id = workbook.id.value,
                name = workbook.name,
                userId = workbook.userId.value,
                userName = workbook.userName,
                comment = workbook.comment,
                questionListCount = workbook.questionListCount,
                downloadCount = workbook.downloadCount,
                isPublic = workbook.isPublic,
                groupId = workbook.groupId?.value ?: "",
            )
    }
}
