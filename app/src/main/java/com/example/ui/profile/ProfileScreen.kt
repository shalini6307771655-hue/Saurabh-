package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.StatusAway
import com.example.ui.theme.StatusBusy
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline

@Composable
fun ProfileScreen(
    profile: UserProfile?,
    darkTheme: Boolean,
    wallpaperTheme: String,
    soundEnabled: Boolean,
    previewEnabled: Boolean,
    onBackClick: () -> Unit,
    onStatusChange: (String) -> Unit,
    onUpdateProfile: (name: String, username: String, bio: String) -> Unit,
    onToggleDarkTheme: () -> Unit,
    onWallpaperChange: (String) -> Unit,
    onToggleSound: () -> Unit,
    onSignOut: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ObsidianBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Profile & Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Identity Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(86.dp)) {
                        if (!profile?.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile?.photoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(CyanPrimary, IndigoSecondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile?.displayName?.take(1)?.uppercase() ?: "U",
                                    color = Color(0xFF0B0F19),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                )
                            }
                        }

                        // Status badge button
                        val statusColor = when (profile?.status) {
                            "away" -> StatusAway
                            "busy" -> StatusBusy
                            "offline" -> StatusOffline
                            else -> StatusOnline
                        }
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                                .border(3.dp, Color(0xFF161F30), CircleShape)
                                .align(Alignment.BottomEnd)
                                .clickable { showStatusDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = profile?.displayName?.ifBlank { "User" } ?: "User",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "@${profile?.username?.ifBlank { "username" } ?: "username"}",
                        color = CyanPrimary,
                        fontSize = 14.sp
                    )

                    if (!profile?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile?.bio ?: "",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanContainer,
                                contentColor = CyanPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Status: ${profile?.status?.uppercase() ?: "ONLINE"}", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Sections
            Text(
                text = "APP SETTINGS",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Brightness4, contentDescription = null, tint = CyanPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Dark Mode (Default)", color = Color.White, fontSize = 14.sp)
                        }
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { onToggleDarkTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0B0F19),
                                checkedTrackColor = CyanPrimary
                            )
                        )
                    }

                    Divider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 10.dp))

                    // Sound Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = IndigoSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Message Sound Effects", color = Color.White, fontSize = 14.sp)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { onToggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0B0F19),
                                checkedTrackColor = IndigoSecondary
                            )
                        )
                    }

                    Divider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 10.dp))

                    // Wallpaper theme selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Chat Theme: ${wallpaperTheme.uppercase()}", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WallpaperChip("obsidian", "Obsidian", Color(0xFF0B0F19), wallpaperTheme == "obsidian") { onWallpaperChange("obsidian") }
                        WallpaperChip("midnight", "Midnight", Color(0xFF0F172A), wallpaperTheme == "midnight") { onWallpaperChange("midnight") }
                        WallpaperChip("emerald", "Emerald", Color(0xFF042F2E), wallpaperTheme == "emerald") { onWallpaperChange("emerald") }
                        WallpaperChip("purple", "Violet", Color(0xFF1E1B4B), wallpaperTheme == "purple") { onWallpaperChange("purple") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Log out")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Log Out of ChatFlow", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Edit Profile Dialog
        if (showEditDialog) {
            EditProfileDialog(
                currentName = profile?.displayName ?: "",
                currentUsername = profile?.username ?: "",
                currentBio = profile?.bio ?: "",
                onDismiss = { showEditDialog = false },
                onSave = { name, username, bio ->
                    showEditDialog = false
                    onUpdateProfile(name, username, bio)
                }
            )
        }

        // Status Selector Dialog
        if (showStatusDialog) {
            StatusSelectorDialog(
                currentStatus = profile?.status ?: "online",
                onDismiss = { showStatusDialog = false },
                onSelectStatus = { status ->
                    showStatusDialog = false
                    onStatusChange(status)
                }
            )
        }
    }
}

@Composable
private fun WallpaperChip(
    key: String,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(2.dp, if (isSelected) CyanPrimary else Color(0xFF334155), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = if (isSelected) CyanPrimary else Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentUsername: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (name: String, username: String, bio: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var username by remember { mutableStateOf(currentUsername) }
    var bio by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text(text = "Edit Profile", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF111827),
                        unfocusedContainerColor = Color(0xFF111827),
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username Handle (@)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF111827),
                        unfocusedContainerColor = Color(0xFF111827),
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Status / Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF111827),
                        unfocusedContainerColor = Color(0xFF111827),
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, username, bio) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF0B0F19))
            ) {
                Text(text = "Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun StatusSelectorDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onSelectStatus: (String) -> Unit
) {
    val statuses = listOf(
        Triple("online", "Online", StatusOnline),
        Triple("away", "Away", StatusAway),
        Triple("busy", "Do Not Disturb", StatusBusy),
        Triple("offline", "Invisible / Offline", StatusOffline)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text(text = "Set Presence Status", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { (key, label, color) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectStatus(key) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        if (currentStatus == key) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CyanPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = Color(0xFF94A3B8))
            }
        }
    )
}
