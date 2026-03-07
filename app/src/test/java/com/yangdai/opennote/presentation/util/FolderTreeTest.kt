package com.yangdai.opennote.presentation.util

import com.yangdai.opennote.data.local.entity.FolderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderTreeTest {

    @Test
    fun flattenFolderTree_respectsExpandedParents() {
        val root = FolderEntity(id = 1L, name = "Root")
        val child = FolderEntity(id = 2L, name = "Child", parentId = 1L)
        val grandChild = FolderEntity(id = 3L, name = "Grand Child", parentId = 2L)

        val flattened = flattenFolderTree(
            folderNoteCounts = listOf(
                root to 4,
                child to 2,
                grandChild to 1
            ),
            expandedFolderIds = setOf(1L, 2L)
        )

        assertEquals(listOf(1L, 2L, 3L), flattened.mapNotNull { it.folder.id })
        assertEquals(listOf(0, 1, 2), flattened.map { it.depth })
        assertEquals(listOf(4, 2, 1), flattened.map { it.noteCount })
    }

    @Test
    fun getFolderDescendantIds_collectsNestedChildren() {
        val folders = listOf(
            FolderEntity(id = 1L, name = "Root"),
            FolderEntity(id = 2L, name = "Child A", parentId = 1L),
            FolderEntity(id = 3L, name = "Child B", parentId = 1L),
            FolderEntity(id = 4L, name = "Grand Child", parentId = 2L)
        )

        val descendants = getFolderDescendantIds(folders, 1L)

        assertEquals(setOf(2L, 3L, 4L), descendants)
        assertTrue(4L in descendants)
    }
}
