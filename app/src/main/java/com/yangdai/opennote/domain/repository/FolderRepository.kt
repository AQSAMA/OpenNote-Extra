package com.yangdai.opennote.domain.repository

import com.yangdai.opennote.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    fun getAllFolders(): Flow<List<FolderEntity>>

    fun getRootFolders(): Flow<List<FolderEntity>>

    fun getSubFolders(parentId: Long): Flow<List<FolderEntity>>

    suspend fun insertFolder(folderEntity: FolderEntity)

    suspend fun deleteFolder(folderEntity: FolderEntity)

    suspend fun deleteSubFolders(parentId: Long)

    suspend fun updateFolder(folderEntity: FolderEntity)

    suspend fun clearParentId(parentId: Long)

}