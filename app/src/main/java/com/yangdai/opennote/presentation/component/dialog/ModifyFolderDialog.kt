package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.presentation.util.getFolderDescendantIds
import kotlinx.coroutines.launch

@Composable
@Preview
fun ModifyFolderDialogPreview() {
    ModifyFolderDialog(
        folder = FolderEntity(),
        onDismissRequest = {},
        onModify = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyFolderDialog(
    folder: FolderEntity,
    folders: List<FolderEntity> = emptyList(),
    onDismissRequest: () -> Unit,
    onModify: (FolderEntity) -> Unit
) {

    var text by remember { mutableStateOf(folder.name) }
    var color by remember { mutableStateOf(folder.color) }
    var parentId by remember { mutableStateOf(folder.parentId) }
    var favorite by remember { mutableStateOf(folder.isFavorite) }
    val custom =
        color != null && !FolderEntity.folderColors.contains(Color(color!!))
    val initValue =
        if (folder.color == null) 0
        else if (custom) FolderEntity.folderColors.size + 1
        else FolderEntity.folderColors.indexOf(Color(folder.color)) + 1
    var selectedIndex by remember { mutableIntStateOf(initValue) }

    var showDialog by remember {
        mutableStateOf(false)
    }
    var showParentDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val blockedFolderIds = remember(folder.id, folders) {
        folder.id?.let { folderId ->
            getFolderDescendantIds(folders, folderId) + folderId
        } ?: emptySet()
    }
    val selectedParentName = folders.firstOrNull { it.id == parentId }?.name
        ?: stringResource(R.string.no_parent)

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.modify))
        },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.name)) },
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clickable { showParentDialog = true },
                    value = selectedParentName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(R.string.parent_folder)) }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = favorite,
                        onCheckedChange = { favorite = it }
                    )
                    Text(text = stringResource(R.string.favorite_folder))
                }
                LazyRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(FolderEntity.folderColors.size + 2) {
                        when (it) {
                            0 -> {
                                ColoredCircle2(selected = 0 == selectedIndex) {
                                    selectedIndex = 0
                                }
                            }

                            FolderEntity.folderColors.size + 1 -> {
                                ColoredCircle3(
                                    background = if (custom) Color(color!!) else Color.Black,
                                    selected = FolderEntity.folderColors.size + 1 == selectedIndex
                                ) {
                                    selectedIndex = FolderEntity.folderColors.size + 1
                                    showDialog = true
                                }
                            }

                            else -> {
                                ColoredCircle(
                                    color = FolderEntity.folderColors[it - 1],
                                    selected = it == selectedIndex,
                                    onClick = { selectedIndex = it }
                                )
                            }
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val haptic = LocalHapticFeedback.current
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)

                    color = when (selectedIndex) {
                        0 -> null
                        FolderEntity.folderColors.size + 1 -> color
                        else -> FolderEntity.folderColors[selectedIndex - 1].toArgb()
                    }

                    onModify(
                        FolderEntity(
                            id = folder.id,
                            name = text,
                            color = color,
                            parentId = parentId,
                            isFavorite = favorite
                        )
                    )

                    onDismissRequest()
                }
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )

    if (showDialog) {
        ColorPickerDialog(
            initialColor = if (custom) Color(color!!) else Color.White,
            sheetState = bottomSheetState,
            onDismissRequest = { showDialog = false }
        ) {
            color = it
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                if (!bottomSheetState.isVisible) {
                    showDialog = false
                }
            }
        }
    }

    if (showParentDialog) {
        FolderListDialog(
            hint = stringResource(R.string.parent_folder),
            oFolderId = parentId,
            folders = folders,
            rootLabel = stringResource(R.string.no_parent),
            excludedFolderIds = blockedFolderIds,
            onDismissRequest = { showParentDialog = false }
        ) {
            parentId = it
        }
    }
}

@Composable
fun ColoredCircle(color: Color, selected: Boolean, onClick: () -> Unit) {

    val background = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                if (selected)
                    drawCircle(
                        color = background
                    )
            }
            .clip(shape = CircleShape)
            .clickable(onClick = onClick)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(shape = CircleShape)
                .background(color = color)
        )
    }
}

@Composable
fun ColoredCircle2(selected: Boolean, onClick: () -> Unit) {

    val background = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                if (selected)
                    drawCircle(
                        color = background
                    )
            }
            .clip(shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "A")
    }
}

@Composable
fun ColoredCircle3(background: Color, selected: Boolean, onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .size(50.dp)
            .drawBehind {
                if (selected)
                    drawCircle(
                        color = background
                    )
            }
            .clip(shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Outlined.Colorize, contentDescription = "")
    }
}
