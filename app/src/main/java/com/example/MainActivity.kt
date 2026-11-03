package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.credentials.CredentialManager
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.chat.NavigationTab
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.ChatFlowTheme
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ObsidianBackground
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatFlowApp()
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun ChatFlowApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    var darkTheme by remember { mutableStateOf(true) }

    ChatFlowTheme(darkTheme = darkTheme) {
        Crossfade(targetState = user, label = "AuthGateTransition") { activeUser ->
            if (activeUser == null) {
                AuthScreen()
            } else {
                MainContent(
                    currentUserId = activeUser.uid,
                    darkTheme = darkTheme,
                    onToggleDarkTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}

@Composable
fun MainContent(
    currentUserId: String,
    darkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: ChatViewModel = viewModel(
        key = currentUserId,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val databaseId = context.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                return ChatViewModel(ChatRepository(db), currentUserId) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialize user profile in Firestore if it doesn't exist
    LaunchedEffect(currentUserId) {
        val authUser = Firebase.auth.currentUser
        if (authUser != null && uiState.currentUserProfile == null) {
            val emailHandle = authUser.email?.substringBefore("@") ?: "user_${currentUserId.take(5)}"
            val initialProfile = UserProfile(
                userId = currentUserId,
                displayName = authUser.displayName?.ifBlank { "User" } ?: "User",
                username = emailHandle.lowercase().replace(".", "_"),
                email = authUser.email ?: "",
                photoUrl = authUser.photoUrl?.toString() ?: "",
                bio = "Hello! I am using ChatFlow",
                status = "online"
            )
            val db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
            ChatRepository(db).saveUserProfile(initialProfile)
        }
    }

    // Show status snackbars
    LaunchedEffect(uiState.statusMessage, uiState.error) {
        val msg = uiState.statusMessage ?: uiState.error
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val activeConv = uiState.activeConversation
            when {
                activeConv != null -> {
                    ChatScreen(
                        conversation = activeConv,
                        messages = uiState.activeMessages,
                        currentUserId = currentUserId,
                        replyingTo = uiState.replyingToMessage,
                        editingMessage = uiState.editingMessage,
                        wallpaperTheme = uiState.chatWallpaper,
                        onBackClick = { viewModel.closeConversation() },
                        onSendMessage = { text, mediaType, mediaUrl, mediaName ->
                            viewModel.sendMessage(text, mediaType, mediaUrl, mediaName)
                        },
                        onToggleReaction = { messageId, emoji ->
                            viewModel.toggleReaction(messageId, emoji)
                        },
                        onStartReply = { message ->
                            viewModel.setReplyingTo(message)
                        },
                        onCancelReply = { viewModel.setReplyingTo(null) },
                        onStartEdit = { message ->
                            viewModel.startEditingMessage(message)
                        },
                        onSaveEdit = { newText ->
                            viewModel.saveEditedMessage(newText)
                        },
                        onCancelEdit = { viewModel.cancelEditing() },
                        onDeleteMessage = { message ->
                            viewModel.deleteMessage(message)
                        },
                        onViewInfo = {
                            viewModel.setViewingGroupInfo(true)
                        }
                    )
                }

                uiState.activeTab == NavigationTab.SETTINGS -> {
                    ProfileScreen(
                        profile = uiState.currentUserProfile,
                        darkTheme = darkTheme,
                        wallpaperTheme = uiState.chatWallpaper,
                        soundEnabled = uiState.soundNotificationsEnabled,
                        previewEnabled = uiState.previewNotificationsEnabled,
                        onBackClick = { viewModel.onSelectTab(NavigationTab.CHATS) },
                        onStatusChange = { newStatus -> viewModel.updateUserStatus(newStatus) },
                        onUpdateProfile = { name, username, bio -> viewModel.updateProfile(name, username, bio) },
                        onToggleDarkTheme = onToggleDarkTheme,
                        onWallpaperChange = { newTheme -> viewModel.setChatWallpaper(newTheme) },
                        onToggleSound = { viewModel.toggleSounds() },
                        onSignOut = {
                            signOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = {
                                    // Firebase sign out triggers authStateFlow -> unauthenticated screen
                                },
                                scope = coroutineScope
                            )
                        }
                    )
                }

                else -> {
                    DashboardScreen(
                        state = uiState,
                        onTabSelected = { tab -> viewModel.onSelectTab(tab) },
                        onConversationClick = { conv -> viewModel.openConversation(conv) },
                        onStartDirectChat = { otherUser -> viewModel.startDirectChat(otherUser) },
                        onCreateGroup = { name, desc, members -> viewModel.createGroupChat(name, desc, members) },
                        onSearchChanged = { query -> viewModel.onSearchQueryChanged(query) },
                        onProfileClick = { viewModel.onSelectTab(NavigationTab.SETTINGS) }
                    )
                }
            }
        }
    }
}
