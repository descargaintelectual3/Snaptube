package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SnaptubeRed
import com.example.ui.theme.SnaptubeYellow

@Composable
fun SnaptubeTopBar(
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onDirectDownloadUrl: (String) -> Unit = {},
    activeDownloadsCount: Int,
    onSearchClick: () -> Unit = {},
    onPasteLinkClick: () -> Unit,
    onDownloadsBadgeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val isUrl = searchQuery.trim().run {
        startsWith("http://", ignoreCase = true) ||
        startsWith("https://", ignoreCase = true) ||
        contains(".com", ignoreCase = true) ||
        contains("youtu", ignoreCase = true) ||
        contains("tiktok", ignoreCase = true) ||
        contains("instagram", ignoreCase = true)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Top branding row - Snaptube VIP Sin Anuncios
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Snaptube Yellow Brand Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SnaptubeYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Snaptube",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Snaptube",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(6.dp))

                // AdBlock VIP badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "VIP • Sin Anuncios",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Paste Link Direct Downloader
                IconButton(
                    onClick = {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            onQueryChange(clip)
                            if (clip.startsWith("http://") || clip.startsWith("https://") || clip.contains("youtu")) {
                                onDirectDownloadUrl(clip)
                            }
                        } else {
                            onPasteLinkClick()
                        }
                    },
                    modifier = Modifier
                        .testTag("paste_link_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Pegar Enlace",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Download Manager Quick Access
                IconButton(
                    onClick = onDownloadsBadgeClick,
                    modifier = Modifier
                        .testTag("downloads_badge_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    BadgedBox(
                        badge = {
                            if (activeDownloadsCount > 0) {
                                Badge(
                                    containerColor = SnaptubeRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "$activeDownloadsCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Descargas Activas",
                            tint = if (activeDownloadsCount > 0) SnaptubeYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Global Interactive Search & Direct Download Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("global_search_bar")
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = if (searchQuery.isNotEmpty()) SnaptubeYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                if (searchQuery.isNotEmpty()) {
                                    if (isUrl) onDirectDownloadUrl(searchQuery) else onSearchSubmit(searchQuery)
                                } else {
                                    onSearchClick()
                                }
                            }
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSearchClick() },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Buscar en YouTube o pegar enlace...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("global_search_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(SnaptubeYellow),
                            keyboardOptions = KeyboardOptions(
                                imeAction = if (isUrl) ImeAction.Go else ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { onSearchSubmit(searchQuery) },
                                onGo = {
                                    if (isUrl) onDirectDownloadUrl(searchQuery) else onSearchSubmit(searchQuery)
                                }
                            )
                        )
                    }

                    // Clear button if user has entered text
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar texto",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Direct Download button when a link is identified
                    AnimatedVisibility(
                        visible = isUrl,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Button(
                            onClick = { onDirectDownloadUrl(searchQuery) },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("direct_download_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SnaptubeYellow,
                                contentColor = Color.Black
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Descargar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
