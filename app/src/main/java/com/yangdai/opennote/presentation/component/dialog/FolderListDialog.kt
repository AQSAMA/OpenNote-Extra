package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity


@Composable
fun FolderListDialog(
    hint: String = "",
    oFolderId: Long?,
    folders: List<FolderEntity>,
    onDismissRequest: () -> Unit,
    onSelect: (Long?) -> Unit
) {

    var selectedFolderId by remember { mutableStateOf(oFolderId) }

    // Build hierarchical folder list
    val hierarchicalFolders = remember(folders) {
        buildHierarchicalFolderList(folders)
    }

    AlertDialog(
        title = { Text(text = hint) },
        text = {
            Column {
                HorizontalDivider(Modifier.fillMaxWidth())
                LazyColumn(modifier = Modifier.fillMaxWidth()) {

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFolderId = null
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                modifier = Modifier
                                    .padding(start = 0.dp, end = 16.dp)
                                    .padding(vertical = 16.dp),
                                selected = null == selectedFolderId,
                                onClick = null
                            )

                            Icon(
                                tint = MaterialTheme.colorScheme.onSurface,
                                imageVector = Icons.Outlined.FolderOpen,
                                contentDescription = ""
                            )

                            Text(
                                text = stringResource(id = R.string.all_notes),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                    items(hierarchicalFolders) { (folder, depth) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFolderId = folder.id
                                }
                                .padding(start = (depth * 24).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                modifier = Modifier
                                    .padding(start = 0.dp, end = 16.dp)
                                    .padding(vertical = 16.dp),
                                selected = folder.id == selectedFolderId,
                                onClick = null
                            )

                            Icon(
                                imageVector = Icons.Outlined.FolderOpen,
                                tint = if (folder.color != null) Color(folder.color) else MaterialTheme.colorScheme.onSurface,
                                contentDescription = "Leading Icon"
                            )

                            Text(
                                text = folder.name,
                                modifier = Modifier.padding(start = 16.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth())
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            val haptic = LocalHapticFeedback.current
            Button(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onSelect(selectedFolderId)
                onDismissRequest()
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    )
}

private fun buildHierarchicalFolderList(
    folders: List<FolderEntity>,
    parentId: Long? = null,
    depth: Int = 0,
    visited: MutableSet<Long?> = mutableSetOf()
): List<Pair<FolderEntity, Int>> {
    if (!visited.add(parentId) || depth > 10) return emptyList()
    val allIds = folders.mapNotNull { it.id }.toSet()
    val result = mutableListOf<Pair<FolderEntity, Int>>()
    val children = if (parentId == null) {
        // At root level, include orphaned folders whose parentId doesn't match any existing folder
        folders.filter { it.parentId == null || it.parentId !in allIds }
    } else {
        folders.filter { it.parentId == parentId }
    }
    for (child in children) {
        result.add(child to depth)
        result.addAll(buildHierarchicalFolderList(folders, child.id, depth + 1, visited))
    }
    return result
}

@Composable
@Preview
fun FolderListDialogPreview() {
    FolderListDialog(
        hint = "Select a folder",
        oFolderId = 1,
        folders = listOf(
            FolderEntity(1, "Folder 1", null),
            FolderEntity(2, "Folder 2", null),
            FolderEntity(3, "Folder 3", null)
        ),
        onDismissRequest = {},
        onSelect = {}
    )
}
