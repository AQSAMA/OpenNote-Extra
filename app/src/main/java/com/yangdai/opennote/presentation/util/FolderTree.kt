package com.yangdai.opennote.presentation.util

import com.yangdai.opennote.data.local.entity.FolderEntity

data class FolderTreeItem(
    val folder: FolderEntity,
    val depth: Int,
    val hasChildren: Boolean
)

fun getDescendantFolderIds(
    folders: List<FolderEntity>,
    rootFolderId: Long?
): Set<Long> {
    if (rootFolderId == null) return emptySet()
    val descendants = linkedSetOf<Long>()
    val childrenByParent = folders.groupBy { it.parentId }

    fun collect(folderId: Long, depth: Int) {
        if (depth > FolderEntity.MAX_FOLDER_DEPTH) return
        if (!descendants.add(folderId)) return
        childrenByParent[folderId]?.forEach { child ->
            child.id?.let { collect(it, depth + 1) }
        }
    }

    collect(rootFolderId, 0)
    return descendants
}

fun buildFolderTreeItems(
    folders: List<FolderEntity>,
    expandedFolderIds: Set<Long> = emptySet(),
    showChildren: Boolean = true
): List<FolderTreeItem> {
    if (folders.isEmpty()) return emptyList()

    val folderById = folders.associateBy { it.id }
    val childrenByParent = folders.groupBy { parent ->
        parent.parentId.takeIf { parentId -> folderById.containsKey(parentId) }
    }
    val visited = mutableSetOf<Long>()
    val result = mutableListOf<FolderTreeItem>()

    fun append(parentId: Long?, depth: Int) {
        if (depth > FolderEntity.MAX_FOLDER_DEPTH) return
        val children = childrenByParent[parentId].orEmpty()
            .sortedBy { it.name.lowercase() }

        children.forEach { folder ->
            val id = folder.id ?: return@forEach
            if (!visited.add(id)) return@forEach
            val hasChildren = childrenByParent[id].isNullOrEmpty().not()
            result += FolderTreeItem(folder = folder, depth = depth, hasChildren = hasChildren)

            if (showChildren && hasChildren && expandedFolderIds.contains(id)) {
                append(id, depth + 1)
            }
        }
    }

    append(parentId = null, depth = 0)
    return result
}

fun getAvailableParentFolders(
    folders: List<FolderEntity>,
    currentFolder: FolderEntity
): List<FolderEntity> {
    val currentId = currentFolder.id ?: return folders
    val blockedIds = getDescendantFolderIds(folders, currentId) + currentId
    return folders.filter { folder ->
        folder.id !in blockedIds
    }
}
