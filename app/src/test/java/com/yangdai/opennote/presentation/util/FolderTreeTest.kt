package com.yangdai.opennote.presentation.util

import com.yangdai.opennote.data.local.entity.FolderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderTreeTest {

    @Test
    fun `buildFolderTreeItems returns nested folders when parent expanded`() {
        val root = FolderEntity(id = 1, name = "Root")
        val child = FolderEntity(id = 2, name = "Child", parentId = 1)
        val grandChild = FolderEntity(id = 3, name = "GrandChild", parentId = 2)

        val items = buildFolderTreeItems(
            folders = listOf(root, child, grandChild),
            expandedFolderIds = setOf(1, 2)
        )

        assertEquals(listOf(1L, 2L, 3L), items.mapNotNull { it.folder.id })
        assertEquals(listOf(0, 1, 2), items.map { it.depth })
    }

    @Test
    fun `getDescendantFolderIds includes all descendants`() {
        val folders = listOf(
            FolderEntity(id = 1, name = "A"),
            FolderEntity(id = 2, name = "B", parentId = 1),
            FolderEntity(id = 3, name = "C", parentId = 2),
            FolderEntity(id = 4, name = "D")
        )

        val descendants = getDescendantFolderIds(folders, 1)

        assertEquals(setOf(1L, 2L, 3L), descendants)
        assertFalse(descendants.contains(4L))
    }

    @Test
    fun `getAvailableParentFolders excludes current folder and descendants`() {
        val root = FolderEntity(id = 1, name = "Root")
        val child = FolderEntity(id = 2, name = "Child", parentId = 1)
        val grandChild = FolderEntity(id = 3, name = "GrandChild", parentId = 2)
        val other = FolderEntity(id = 4, name = "Other")

        val available = getAvailableParentFolders(
            folders = listOf(root, child, grandChild, other),
            currentFolder = child
        )

        val availableIds = available.mapNotNull { it.id }.toSet()
        assertTrue(availableIds.contains(1L))
        assertTrue(availableIds.contains(4L))
        assertFalse(availableIds.contains(2L))
        assertFalse(availableIds.contains(3L))
    }
}
