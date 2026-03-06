package com.yangdai.opennote.presentation.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.animateItem
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.presentation.component.TopBarTitle
import com.yangdai.opennote.presentation.component.dialog.ModifyFolderDialog
import com.yangdai.opennote.presentation.component.dialog.WarningDialog
import com.yangdai.opennote.presentation.event.FolderEvent
import com.yangdai.opennote.presentation.util.FolderTreeIndent
import com.yangdai.opennote.presentation.util.flattenFolderTree
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    navigateUp: () -> Unit
) {
    val folderNoteCounts by sharedViewModel.folderWithNoteCountsFlow.collectAsStateWithLifecycle()
    val folders = remember(folderNoteCounts) { folderNoteCounts.map { it.first } }

    var showAddFolderDialog by rememberSaveable { mutableStateOf(false) }
    var expandedFolderIds by rememberSaveable { mutableStateOf<List<Long>>(emptyList()) }
    var initializedFolderExpansion by rememberSaveable { mutableStateOf(false) }

    val foldersWithChildren = remember(folders) {
        folders.filter { folder ->
            val folderId = folder.id
            folderId != null && folders.any { it.parentId == folderId }
        }.mapNotNull { it.id }
    }
    val flattenedFolders = remember(folderNoteCounts, expandedFolderIds) {
        flattenFolderTree(folderNoteCounts, expandedFolderIds.toSet())
    }

    LaunchedEffect(foldersWithChildren) {
        if (!initializedFolderExpansion && foldersWithChildren.isNotEmpty()) {
            expandedFolderIds = foldersWithChildren
            initializedFolderExpansion = true
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    TopBarTitle(title = stringResource(id = R.string.folders))
                },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = "Create New Folder"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(scrolledContainerColor = TopAppBarDefaults.topAppBarColors().containerColor),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = paddingValues
        ) {
            items(flattenedFolders, key = { it.folder.id ?: Long.MIN_VALUE }) { item ->
                FolderItem(
                    folder = item.folder,
                    folders = folders,
                    notesCountInFolder = item.noteCount,
                    depth = item.depth,
                    hasChildren = item.hasChildren,
                    isExpanded = item.folder.id in expandedFolderIds,
                    onExpandToggle = item.folder.id?.let { folderId ->
                        {
                            expandedFolderIds = if (folderId in expandedFolderIds) {
                                expandedFolderIds - folderId
                            } else {
                                expandedFolderIds + folderId
                            }
                        }
                    },
                    onModify = { folderEntity ->
                        sharedViewModel.onFolderEvent(FolderEvent.UpdateFolder(folderEntity))
                    },
                    onDelete = {
                        sharedViewModel.onFolderEvent(FolderEvent.DeleteFolder(item.folder))
                    },
                    onToggleFavorite = { folderEntity ->
                        sharedViewModel.onFolderEvent(
                            FolderEvent.UpdateFolder(folderEntity.copy(isFavorite = !folderEntity.isFavorite))
                        )
                    }
                )
            }
        }

        if (showAddFolderDialog) {
            ModifyFolderDialog(
                folder = FolderEntity(),
                folders = folders,
                onDismissRequest = { showAddFolderDialog = false }
            ) {
                sharedViewModel.onFolderEvent(FolderEvent.AddFolder(it))
            }
        }
    }
}

@Composable
fun FolderItem(
    folder: FolderEntity,
    folders: List<FolderEntity>,
    notesCountInFolder: Int,
    depth: Int,
    hasChildren: Boolean,
    isExpanded: Boolean,
    onExpandToggle: (() -> Unit)?,
    onModify: (FolderEntity) -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: (FolderEntity) -> Unit,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    var showModifyDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    val folderColor by remember(folder, colorScheme) {
        mutableStateOf(if (folder.color != null) Color(folder.color) else colorScheme.primary)
    }

    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    var showContextMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    LaunchedEffect(isHovered) {
        delay(100L)
        showContextMenu = isHovered
    }

    SwipeToDismissBox(
        state = dismissState,
        onDismiss = { dismissDirection ->
            when (dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    showModifyDialog = true
                    scope.launch { dismissState.reset() }
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    showWarningDialog = true
                    scope.launch { dismissState.reset() }
                }

                SwipeToDismissBoxValue.Settled -> {}
            }
        },
        backgroundContent = {
            val direction = dismissState.targetValue
            val progress = dismissState.progress
            val iconOffset = 30.dp * (progress * 1.5f)
            val backgroundColor = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> folderColor.copy(alpha = 0.1f)
                SwipeToDismissBoxValue.EndToStart -> colorScheme.errorContainer
                SwipeToDismissBoxValue.Settled -> Color.Unspecified
            }

            val cornerLeftRadius =
                if (direction == SwipeToDismissBoxValue.StartToEnd) 16.dp * (progress * 6f) else 0.dp
            val cornerRightRadius =
                if (direction == SwipeToDismissBoxValue.EndToStart) 16.dp * (progress * 6f) else 0.dp
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(
                        shape = RoundedCornerShape(
                            topStart = cornerLeftRadius,
                            topEnd = cornerRightRadius,
                            bottomStart = cornerLeftRadius,
                            bottomEnd = cornerRightRadius
                        )
                    )
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp)
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd)
                    Icon(
                        imageVector = Icons.Outlined.DriveFileRenameOutline,
                        contentDescription = null,
                        tint = folderColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(30.dp)
                            .offset { IntOffset(x = iconOffset.roundToPx(), y = 0) }
                    )
                if (direction == SwipeToDismissBoxValue.EndToStart)
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = colorScheme.onErrorContainer,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(30.dp)
                            .offset { IntOffset(x = -iconOffset.roundToPx(), y = 0) }
                    )
            }
        },
        modifier = Modifier
            .padding(bottom = 16.dp)
            .animateItem()
            .clip(CardDefaults.elevatedShape)
            .hoverable(interactionSource)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showContextMenu = true
                    }
                )
            }
    ) {
        ElevatedCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(folderColor.copy(alpha = 0.1f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (depth > 0) {
                    Spacer(modifier = Modifier.width((depth * FolderTreeIndent.value).dp))
                }
                if (hasChildren && onExpandToggle != null) {
                    IconButton(onClick = onExpandToggle) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowDown
                            else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = folderColor
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder",
                    tint = folderColor,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = folderColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val text =
                        "$notesCountInFolder${
                            if (notesCountInFolder == 1 || notesCountInFolder == 0)
                                stringResource(R.string.note)
                            else stringResource(R.string.notes)
                        }"
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        )
                    )
                }
                IconButton(onClick = { onToggleFavorite(folder) }) {
                    Icon(
                        imageVector = if (folder.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(R.string.favorite_folder),
                        tint = if (folder.isFavorite) folderColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.modify)) },
                leadingIcon = {
                    Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null)
                },
                onClick = {
                    showModifyDialog = true
                    showContextMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(
                            if (folder.isFavorite) R.string.remove_from_favorites
                            else R.string.favorite_folder
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        if (folder.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = null
                    )
                },
                onClick = {
                    onToggleFavorite(folder)
                    showContextMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                },
                onClick = {
                    showWarningDialog = true
                    showContextMenu = false
                }
            )
        }
    }

    if (showWarningDialog) {
        WarningDialog(
            message = stringResource(R.string.deleting_a_folder_will_move_its_notes_to_trash_and_remove_its_subfolders_do_you_want_to_continue),
            onDismissRequest = { showWarningDialog = false },
            onConfirm = onDelete
        )
    }

    if (showModifyDialog) {
        ModifyFolderDialog(
            folder = folder,
            folders = folders,
            onDismissRequest = { showModifyDialog = false }
        ) {
            onModify(it)
        }
    }
}
