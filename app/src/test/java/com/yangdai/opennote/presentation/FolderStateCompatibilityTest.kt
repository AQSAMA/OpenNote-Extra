package com.yangdai.opennote.presentation

import androidx.compose.runtime.saveable.SaverScope
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.presentation.component.main.folderDrawerIndex
import com.yangdai.opennote.presentation.component.main.isFolderDrawerItemSelected
import com.yangdai.opennote.presentation.screen.FolderEntitySaver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderStateCompatibilityTest {

    @Test
    fun folderDrawerIndex_returnsCorrectIndexOrNull() {
        val folders = listOf(
            FolderEntity(id = 10L, name = "Root") to 3,
            FolderEntity(id = 11L, name = "Child", parentId = 10L) to 1
        )

        assertEquals(2, folderDrawerIndex(10L, folders))
        assertEquals(3, folderDrawerIndex(11L, folders))
        assertNull(folderDrawerIndex(99L, folders))
    }

    @Test
    fun drawerSelection_usesFolderIdWithoutMatchingTrashIndex() {
        // Matching a folder ID must not make the Trash item look selected.
        assertFalse(
            isFolderDrawerItemSelected(
                selectedDrawerIndex = 1,
                selectedFolderId = 42L,
                folderId = 42L
            )
        )

        assertTrue(
            isFolderDrawerItemSelected(
                selectedDrawerIndex = 2,
                selectedFolderId = 42L,
                folderId = 42L
            )
        )
    }

    @Test
    fun folderEntitySaver_restoresLegacyTripleState() {
        val restored = FolderEntitySaver.restore(Triple(7L, "Legacy", 123))

        assertEquals(FolderEntity(id = 7L, name = "Legacy", color = 123), restored)
    }

    @Test
    fun folderEntitySaver_roundTripsParentId() {
        val folder = FolderEntity(id = 7L, name = "Child", color = 123, parentId = 2L)
        val saverScope = object : SaverScope {
            override fun canBeSaved(value: Any): Boolean = true
        }

        val saved = with(FolderEntitySaver) { saverScope.save(folder) }

        assertEquals(folder, saved?.let(FolderEntitySaver::restore))
    }

    @Test
    fun folderEntitySaver_rejectsUnexpectedSavedStateTypes() {
        assertNull(FolderEntitySaver.restore(listOf("wrong", "types", 123)))
    }
}
