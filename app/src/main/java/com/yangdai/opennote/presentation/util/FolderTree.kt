package com.yangdai.opennote.presentation.util

import com.yangdai.opennote.data.local.entity.FolderEntity

data class FolderTreeItem(
    val folder: FolderEntity,
    val depth: Int,
    val hasChildren: Boolean,
    val noteCount: Int
)

fun flattenFolderTree(
    folderNoteCounts: List<Pair<FolderEntity, Int>>,
    expandedFolderIds: Set<Long>
): List<FolderTreeItem> {
    val folders = folderNoteCounts.map { it.first }
    val noteCounts = folderNoteCounts.associate { (folder, count) -> folder.id to count }
    return flattenFolders(folders, noteCounts, expandedFolderIds)
}

fun flattenFolders(
    folders: List<FolderEntity>,
    noteCounts: Map<Long?, Int> = emptyMap(),
    expandedFolderIds: Set<Long> = folders.mapNotNull { it.id }.toSet()
): List<FolderTreeItem> {
    val folderIds = folders.mapNotNull { it.id }.toSet()
    val childrenByParent = folders
        .groupBy { it.parentId }
        .mapValues { (_, children) -> children.sortedWith(folderComparator) }

    val roots = folders
        .filter { it.parentId == null || it.parentId !in folderIds }
        .sortedWith(folderComparator)

    val flattenedFolders = mutableListOf<FolderTreeItem>()
    val visited = mutableSetOf<Long>()

    fun addFolder(folder: FolderEntity, depth: Int) {
        val folderId = folder.id ?: return
        if (!visited.add(folderId)) return

        val children = childrenByParent[folderId].orEmpty().filter { it.id != folderId }
        flattenedFolders += FolderTreeItem(
            folder = folder,
            depth = depth,
            hasChildren = children.isNotEmpty(),
            noteCount = noteCounts[folderId] ?: 0
        )

        if (folderId in expandedFolderIds) {
            children.forEach { child ->
                addFolder(child, depth + 1)
            }
        }
    }

    roots.forEach { root ->
        addFolder(root, 0)
    }

    folders
        .sortedWith(folderComparator)
        .filter { folder -> folder.id != null && folder.id !in visited }
        .forEach { orphanedFolder ->
            addFolder(orphanedFolder, 0)
        }

    return flattenedFolders
}

fun getFolderDescendantIds(
    folders: List<FolderEntity>,
    folderId: Long
): Set<Long> {
    val descendants = mutableSetOf<Long>()
    val childrenByParent = folders.groupBy { it.parentId }

    fun collectChildren(parentId: Long) {
        childrenByParent[parentId].orEmpty().forEach { child ->
            val childId = child.id ?: return@forEach
            if (descendants.add(childId)) {
                collectChildren(childId)
            }
        }
    }

    collectChildren(folderId)
    return descendants
}

private val folderComparator = compareBy<FolderEntity> { it.name.lowercase() }
    .thenBy { it.id ?: Long.MAX_VALUE }
