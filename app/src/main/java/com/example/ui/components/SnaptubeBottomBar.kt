package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SnaptubeRed
import com.example.ui.theme.SnaptubeYellow
import com.example.ui.viewmodel.SnaptubeNavTab

@Composable
fun SnaptubeBottomBar(
    currentTab: SnaptubeNavTab,
    activeDownloadsCount: Int,
    onTabSelected: (SnaptubeNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Tab 1: YouTube Content View (Prioritized Main Screen)
        NavigationBarItem(
            selected = currentTab == SnaptubeNavTab.HOME,
            onClick = { onTabSelected(SnaptubeNavTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == SnaptubeNavTab.HOME) Icons.Filled.Subscriptions else Icons.Outlined.Subscriptions,
                    contentDescription = "YouTube",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "YouTube",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == SnaptubeNavTab.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = SnaptubeYellow,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("tab_youtube")
        )

        // Tab 2: Navegador
        NavigationBarItem(
            selected = currentTab == SnaptubeNavTab.BROWSER,
            onClick = { onTabSelected(SnaptubeNavTab.BROWSER) },
            icon = {
                Icon(
                    imageVector = if (currentTab == SnaptubeNavTab.BROWSER) Icons.Filled.Language else Icons.Outlined.Language,
                    contentDescription = "Navegador",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Navegador",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == SnaptubeNavTab.BROWSER) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = SnaptubeYellow,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("tab_navegador")
        )

        // Tab 3: Mis Archivos
        NavigationBarItem(
            selected = currentTab == SnaptubeNavTab.MY_FILES,
            onClick = { onTabSelected(SnaptubeNavTab.MY_FILES) },
            icon = {
                BadgedBox(
                    badge = {
                        if (activeDownloadsCount > 0) {
                            Badge(
                                containerColor = SnaptubeRed,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "$activeDownloadsCount",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == SnaptubeNavTab.MY_FILES) Icons.Filled.Folder else Icons.Outlined.Folder,
                        contentDescription = "Mis Archivos",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "Mis Archivos",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == SnaptubeNavTab.MY_FILES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = SnaptubeYellow,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("tab_mis_archivos")
        )

        // Tab 4: Yo / Herramientas
        NavigationBarItem(
            selected = currentTab == SnaptubeNavTab.ME,
            onClick = { onTabSelected(SnaptubeNavTab.ME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == SnaptubeNavTab.ME) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Yo",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Yo",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == SnaptubeNavTab.ME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = SnaptubeYellow,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("tab_yo")
        )
    }
}
