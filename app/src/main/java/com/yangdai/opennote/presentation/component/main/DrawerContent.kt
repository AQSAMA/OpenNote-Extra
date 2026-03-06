package com.yangdai.opennote.presentation.component.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.data.local.entity.FolderEntity
import com.yangdai.opennote.presentation.navigation.Screen
import com.yangdai.opennote.presentation.navigation.Screen.Folders
import com.yangdai.opennote.presentation.navigation.Screen.Settings

@Composable
fun DrawerContent(
    folderNoteCounts: List<Pair<FolderEntity, Int>>,
    showLock: Boolean,
    selectedDrawerIndex: Int,
    onLockClick: () -> Unit,
    navigateTo: (Screen) -> Unit,
    onDrawerItemClicked: (Int, FolderEntity) -> Unit
) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        if (showLock)
            IconButton(onClick = onLockClick) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "lock",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

        IconButton(onClick = { navigateTo(Settings) }) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Open Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    DrawerItem(
        icon = Icons.Outlined.Book,
        label = stringResource(R.string.all_notes),
        isSelected = selectedDrawerIndex == 0,
        onClick = { onDrawerItemClicked(0, FolderEntity()) }
    )

    DrawerItem(
        icon = Icons.Outlined.Delete,
        label = stringResource(R.string.trash),
        isSelected = selectedDrawerIndex == 1,
        onClick = { onDrawerItemClicked(1, FolderEntity()) }
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

    // Record whether the folder list is expanded
    var isFoldersExpended by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(folderNoteCounts) {
        isFoldersExpended = folderNoteCounts.isNotEmpty()
    }

    DrawerItem(
        icon = if (!isFoldersExpended) Icons.AutoMirrored.Outlined.KeyboardArrowRight else Icons.Outlined.KeyboardArrowDown,
        label = stringResource(R.string.folders),
        badge = folderNoteCounts.size.toString(),
        isSelected = false,
        onClick = { isFoldersExpended = !isFoldersExpended }
    )

    AnimatedVisibility(visible = isFoldersExpended) {
        Column {
            // Build folder hierarchy: show root folders first, then their children
            val rootFolders = folderNoteCounts.filter { it.first.parentId == null }
            rootFolders.forEach { pair ->
                key(pair.first.id) {
                    DrawerFolderWithChildren(
                        folder = pair.first,
                        noteCount = pair.second,
                        allFolderNoteCounts = folderNoteCounts,
                        selectedDrawerIndex = selectedDrawerIndex,
                        onFolderClicked = { clickedIndex, folderEntity ->
                            onDrawerItemClicked(clickedIndex + 2, folderEntity)
                        },
                        depth = 0
                    )
                }
            }
        }
    }

    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(NavigationDrawerItemDefaults.ItemPadding),
        onClick = { navigateTo(Folders) }
    ) {
        Text(text = stringResource(R.string.manage_folders), textAlign = TextAlign.Center)
    }
}

@Composable
private fun DrawerFolderWithChildren(
    folder: FolderEntity,
    noteCount: Int,
    allFolderNoteCounts: List<Pair<FolderEntity, Int>>,
    selectedDrawerIndex: Int,
    onFolderClicked: (Int, FolderEntity) -> Unit,
    depth: Int
) {
    val children = allFolderNoteCounts.filter { it.first.parentId == folder.id }
    val hasChildren = children.isNotEmpty()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val folderIndex = allFolderNoteCounts.indexOf(folder to noteCount)
    val isSelected = selectedDrawerIndex == folderIndex + 2

    Row(
        modifier = Modifier.padding(
            start = NavigationDrawerItemDefaults.ItemPadding.calculateLeftPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr
            ) + (depth * 16).dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasChildren) {
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = if (!isExpanded) Icons.AutoMirrored.Outlined.KeyboardArrowRight
                    else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Toggle expand",
                    tint = folder.color?.let { Color(it) }
                        ?: MaterialTheme.colorScheme.primary
                )
            }
        }

        NavigationDrawerItem(
            modifier = Modifier.padding(
                end = NavigationDrawerItemDefaults.ItemPadding.calculateRightPadding(
                    androidx.compose.ui.unit.LayoutDirection.Ltr
                )
            ),
            icon = {
                if (!hasChildren) {
                    Icon(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        imageVector = Icons.Outlined.FolderOpen,
                        tint = folder.color?.let { Color(it) }
                            ?: MaterialTheme.colorScheme.primary,
                        contentDescription = "Folder Icon"
                    )
                }
            },
            label = {
                Text(
                    text = folder.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            badge = {
                Text(
                    text = noteCount.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            shape = MaterialTheme.shapes.medium,
            selected = isSelected,
            onClick = { onFolderClicked(folderIndex, folder) }
        )
    }

    AnimatedVisibility(visible = isExpanded && hasChildren) {
        Column {
            children.forEach { childPair ->
                key(childPair.first.id) {
                    DrawerFolderWithChildren(
                        folder = childPair.first,
                        noteCount = childPair.second,
                        allFolderNoteCounts = allFolderNoteCounts,
                        selectedDrawerIndex = selectedDrawerIndex,
                        onFolderClicked = onFolderClicked,
                        depth = depth + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    label: String,
    badge: String = "",
    isSelected: Boolean,
    onClick: () -> Unit
) = NavigationDrawerItem(
    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    icon = {
        Icon(
            modifier = Modifier.padding(horizontal = 4.dp),
            imageVector = icon,
            tint = iconTint,
            contentDescription = "Leading Icon"
        )
    },
    label = {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    },
    badge = {
        Text(
            text = badge,
            style = MaterialTheme.typography.labelMedium
        )
    },
    shape = MaterialTheme.shapes.medium,
    selected = isSelected,
    onClick = onClick
)