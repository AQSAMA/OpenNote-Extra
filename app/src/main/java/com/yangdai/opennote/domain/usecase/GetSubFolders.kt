package com.yangdai.opennote.domain.usecase

import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow

class GetSubFolders(
    private val repository: FolderRepository
) {

    operator fun invoke(parentId: Long): Flow<List<FolderEntity>> =
        repository.getSubFolders(parentId)
}
