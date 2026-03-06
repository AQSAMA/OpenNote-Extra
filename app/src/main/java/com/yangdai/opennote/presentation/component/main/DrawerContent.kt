package com.yangdai.opennote.presentation.component.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.yangdai.opennote.presentation.util.flattenFolderTree

@Composable
fun DrawerContent(
    folderNoteCounts: List<Pair<FolderEntity, Int>>,
    showLock: Boolean,
    selectedDrawerIndex: Int,
    selectedFolderId: Long?,
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

    var isFoldersExpended by rememberSaveable { mutableStateOf(false) }
    var expandedFolderIds by rememberSaveable { mutableStateOf<List<Long>>(emptyList()) }
    var initializedFolderExpansion by rememberSaveable { mutableStateOf(false) }

    val folders = remember(folderNoteCounts) { folderNoteCounts.map { it.first } }
    val foldersWithChildren = remember(folders) {
        folders.filter { folder ->
            val folderId = folder.id
            folderId != null && folders.any { it.parentId == folderId }
        }.mapNotNull { it.id }
    }
    val flattenedFolders = remember(folderNoteCounts, expandedFolderIds) {
        flattenFolderTree(folderNoteCounts, expandedFolderIds.toSet())
    }

    LaunchedEffect(folderNoteCounts) {
        isFoldersExpended = folderNoteCounts.isNotEmpty()
        if (!initializedFolderExpansion && foldersWithChildren.isNotEmpty()) {
            expandedFolderIds = foldersWithChildren
            initializedFolderExpansion = true
        }
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
            flattenedFolders.forEach { item ->
                DrawerItem(
                    icon = Icons.Outlined.FolderOpen,
                    iconTint = item.folder.color?.let { Color(it) }
                        ?: MaterialTheme.colorScheme.primary,
                    label = item.folder.name,
                    badge = item.noteCount.toString(),
                    indentLevel = item.depth,
                    expandIcon = when {
                        !item.hasChildren -> null
                        item.folder.id in expandedFolderIds -> Icons.Outlined.KeyboardArrowDown
                        else -> Icons.AutoMirrored.Outlined.KeyboardArrowRight
                    },
                    onExpandToggle = item.folder.id?.let { folderId ->
                        {
                            expandedFolderIds = if (folderId in expandedFolderIds) {
                                expandedFolderIds - folderId
                            } else {
                                expandedFolderIds + folderId
                            }
                        }
                    },
                    isSelected = selectedDrawerIndex != 0 && selectedDrawerIndex != 1
                            && selectedFolderId == item.folder.id,
                    onClick = { onDrawerItemClicked(2, item.folder) }
                )
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
private fun DrawerItem(
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    label: String,
    badge: String = "",
    isSelected: Boolean,
    indentLevel: Int = 0,
    expandIcon: ImageVector? = null,
    onExpandToggle: (() -> Unit)? = null,
    onClick: () -> Unit
) = NavigationDrawerItem(
    modifier = Modifier
        .fillMaxWidth()
        .padding(
            start = 12.dp + (indentLevel * 16).dp,
            top = 2.dp,
            end = 12.dp,
            bottom = 2.dp
        ),
    icon = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (expandIcon != null && onExpandToggle != null) {
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = expandIcon,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = null
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Icon(
                imageVector = icon,
                tint = iconTint,
                contentDescription = "Leading Icon"
            )
        }
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
