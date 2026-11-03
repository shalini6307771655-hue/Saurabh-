package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Conversation
import com.example.data.model.UserProfile
import com.example.ui.chat.ChatUiState
import com.example.ui.chat.NavigationTab
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SlateCard
import com.example.ui.theme.StatusAway
import com.example.ui.theme.StatusBusy
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: ChatUiState,
    onTabSelected: (NavigationTab) -> Unit,
    onConversationClick: (Conversation) -> Unit,
    onStartDirectChat: (UserProfile) -> Unit,
    onCreateGroup: (name: String, description: String, members: List<String>) -> Unit,
    onSearchChanged: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ObsidianBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF111827),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.activeTab == NavigationTab.CHATS,
                    onClick = { onTabSelected(NavigationTab.CHATS) },
                    icon = {
                        Icon(imageVector = Icons.Default.Forum, contentDescription = "Chats")
                    },
                    label = { Text("Chats") },
                    colors = navigationBarColors(),
                    modifier = Modifier.testTag("nav_tab_chats")
                )
                NavigationBarItem(
                    selected = state.activeTab == NavigationTab.GROUPS,
                    onClick = { onTabSelected(NavigationTab.GROUPS) },
                    icon = {
                        Icon(imageVector = Icons.Default.Group, contentDescription = "Groups")
                    },
                    label = { Text("Groups") },
                    colors = navigationBarColors(),
                    modifier = Modifier.testTag("nav_tab_groups")
                )
                NavigationBarItem(
                    selected = state.activeTab == NavigationTab.PEOPLE,
                    onClick = { onTabSelected(NavigationTab.PEOPLE) },
                    icon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "People")
                    },
                    label = { Text("People") },
                    colors = navigationBarColors(),
                    modifier = Modifier.testTag("nav_tab_people")
                )
                NavigationBarItem(
                    selected = state.activeTab == NavigationTab.SETTINGS,
                    onClick = { onTabSelected(NavigationTab.SETTINGS) },
                    icon = {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    },
                    label = { Text("Settings") },
                    colors = navigationBarColors(),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (state.activeTab == NavigationTab.CHATS || state.activeTab == NavigationTab.GROUPS) {
                FloatingActionButton(
                    onClick = {
                        if (state.activeTab == NavigationTab.GROUPS) {
                            showCreateGroupDialog = true
                        } else {
                            showNewChatDialog = true
                        }
                    },
                    containerColor = CyanPrimary,
                    contentColor = Color(0xFF0B0F19),
                    shape = CircleShape,
                    modifier = Modifier.testTag("new_chat_fab")
                ) {
                    Icon(
                        imageVector = if (state.activeTab == NavigationTab.GROUPS) Icons.Default.GroupAdd else Icons.Default.Add,
                        contentDescription = "New Chat",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Header
            DashboardHeader(
                profile = state.currentUserProfile,
                onProfileClick = onProfileClick
            )

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChanged,
                placeholder = {
                    Text(
                        text = "Search chats, people, or messages...",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8)
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_bar_input"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // Active Contacts Horizontal Carousel (Stories/Online strip)
            if (state.allUsers.isNotEmpty()) {
                OnlineContactsStrip(
                    users = state.allUsers,
                    onUserClick = onStartDirectChat
                )
            }

            // Main Content Area based on Selected Tab
            when (state.activeTab) {
                NavigationTab.CHATS -> {
                    val directChats = state.filteredConversations
                    ConversationsList(
                        conversations = directChats,
                        onConversationClick = onConversationClick,
                        onNewChatClick = { showNewChatDialog = true }
                    )
                }
                NavigationTab.GROUPS -> {
                    val groupChats = state.filteredConversations.filter { it.type == "group" }
                    GroupsList(
                        groups = groupChats,
                        onGroupClick = onConversationClick,
                        onCreateGroupClick = { showCreateGroupDialog = true }
                    )
                }
                NavigationTab.PEOPLE -> {
                    PeopleList(
                        users = state.allUsers,
                        onUserClick = onStartDirectChat
                    )
                }
                NavigationTab.SETTINGS -> {
                    // Rendered in MainActivity or ProfileScreen
                }
            }
        }

        // New Direct Chat Modal
        if (showNewChatDialog) {
            NewChatDialog(
                users = state.allUsers,
                onDismiss = { showNewChatDialog = false },
                onSelectUser = { user ->
                    showNewChatDialog = false
                    onStartDirectChat(user)
                },
                onCreateGroupInstead = {
                    showNewChatDialog = false
                    showCreateGroupDialog = true
                }
            )
        }

        // Create Group Modal
        if (showCreateGroupDialog) {
            CreateGroupDialog(
                users = state.allUsers,
                onDismiss = { showCreateGroupDialog = false },
                onCreate = { name, desc, members ->
                    showCreateGroupDialog = false
                    onCreateGroup(name, desc, members)
                }
            )
        }
    }
}

@Composable
private fun navigationBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = CyanPrimary,
    selectedTextColor = CyanPrimary,
    unselectedIconColor = Color(0xFF94A3B8),
    unselectedTextColor = Color(0xFF94A3B8),
    indicatorColor = CyanContainer
)

@Composable
private fun DashboardHeader(
    profile: UserProfile?,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "ChatFlow",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Welcome, ${profile?.displayName?.ifBlank { "User" } ?: "User"}",
                fontSize = 13.sp,
                color = CyanPrimary
            )
        }

        // User Avatar button with status badge
        Box(
            modifier = Modifier
                .size(46.dp)
                .clickable { onProfileClick() }
                .testTag("header_profile_button"),
            contentAlignment = Alignment.Center
        ) {
            if (!profile?.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profile?.photoUrl,
                    contentDescription = "Profile",
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
                        fontSize = 20.sp
                    )
                }
            }

            // Status indicator
            val statusColor = when (profile?.status) {
                "away" -> StatusAway
                "busy" -> StatusBusy
                "offline" -> StatusOffline
                else -> StatusOnline
            }

            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(2.dp, ObsidianBackground, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun OnlineContactsStrip(
    users: List<UserProfile>,
    onUserClick: (UserProfile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "Active Contacts",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 18.dp, bottom = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(users) { user ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onUserClick(user) }
                        .padding(horizontal = 4.dp)
                ) {
                    Box(modifier = Modifier.size(52.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CyanContainer, IndigoSecondary.copy(alpha = 0.5f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.displayName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        // Status dot
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(StatusOnline)
                                .border(2.dp, ObsidianBackground, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.displayName.split(" ").firstOrNull() ?: user.displayName,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationsList(
    conversations: List<Conversation>,
    onConversationClick: (Conversation) -> Unit,
    onNewChatClick: () -> Unit
) {
    if (conversations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No conversations yet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap below to connect with people on ChatFlow",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )
                Button(
                    onClick = onNewChatClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF0B0F19))
                ) {
                    Text(text = "Start a Chat", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversations, key = { it.id }) { conv ->
                ConversationItemCard(
                    conversation = conv,
                    onClick = { onConversationClick(conv) }
                )
            }
        }
    }
}

@Composable
private fun ConversationItemCard(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("conversation_card_${conversation.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (conversation.type == "group")
                                listOf(IndigoSecondary, CyanPrimary)
                            else
                                listOf(CyanPrimary, IndigoSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.name.take(1).uppercase(),
                    color = Color(0xFF0B0F19),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTime(conversation.lastMessageTimestamp?.toDate() ?: conversation.updatedAt?.toDate()),
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val snippet = if (conversation.lastMessageSenderName.isNotBlank() && conversation.type == "group") {
                        "${conversation.lastMessageSenderName}: ${conversation.lastMessageText}"
                    } else {
                        conversation.lastMessageText.ifBlank { "Tap to chat" }
                    }

                    Text(
                        text = snippet,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.type == "group") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "GROUP", color = CyanPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupsList(
    groups: List<Conversation>,
    onGroupClick: (Conversation) -> Unit,
    onCreateGroupClick: () -> Unit
) {
    if (groups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No group channels yet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Create a channel for family, team, or friends",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )
                Button(
                    onClick = onCreateGroupClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF0B0F19))
                ) {
                    Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Create New Group", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups, key = { it.id }) { group ->
                ConversationItemCard(
                    conversation = group,
                    onClick = { onGroupClick(group) }
                )
            }
        }
    }
}

@Composable
private fun PeopleList(
    users: List<UserProfile>,
    onUserClick: (UserProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users, key = { it.userId }) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick(user) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CyanContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.take(1).uppercase(),
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${user.username} • ${user.bio.ifBlank { "Available" }}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = { onUserClick(user) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF0B0F19))
                    ) {
                        Text(text = "Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NewChatDialog(
    users: List<UserProfile>,
    onDismiss: () -> Unit,
    onSelectUser: (UserProfile) -> Unit,
    onCreateGroupInstead: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text(text = "Start a Conversation", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onCreateGroupInstead,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanContainer, contentColor = CyanPrimary)
                ) {
                    Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Create Group Channel", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select contact:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (users.isEmpty()) {
                    Text(
                        text = "No other users registered yet. Invite someone or test with another account!",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        items(users) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectUser(user) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyanPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.displayName.take(1).uppercase(),
                                        color = Color(0xFF0B0F19),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = user.displayName, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(text = "@${user.username}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun CreateGroupDialog(
    users: List<UserProfile>,
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, members: List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var groupDesc by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text(text = "New Group Channel", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = groupDesc,
                    onValueChange = { groupDesc = it },
                    label = { Text("Description (Optional)") },
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Add Members (${selectedMembers.size} selected):",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    items(users) { user ->
                        val isChecked = selectedMembers.contains(user.userId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedMembers.remove(user.userId)
                                    else selectedMembers.add(user.userId)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedMembers.add(user.userId)
                                    else selectedMembers.remove(user.userId)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CyanPrimary,
                                    checkmarkColor = Color(0xFF0B0F19)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = user.displayName, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        onCreate(groupName, groupDesc, selectedMembers.toList())
                    }
                },
                enabled = groupName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF0B0F19))
            ) {
                Text(text = "Create", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

private fun formatTime(date: Date?): String {
    if (date == null) return ""
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(date)
}
