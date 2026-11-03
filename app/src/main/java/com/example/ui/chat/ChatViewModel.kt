package com.example.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val currentUserId: String,
    val currentUserProfile: UserProfile? = null,
    val conversations: List<Conversation> = emptyList(),
    val filteredConversations: List<Conversation> = emptyList(),
    val allUsers: List<UserProfile> = emptyList(),
    val activeConversation: Conversation? = null,
    val activeMessages: List<Message> = emptyList(),
    val searchQuery: String = "",
    val activeTab: NavigationTab = NavigationTab.CHATS,
    val replyingToMessage: Message? = null,
    val editingMessage: Message? = null,
    val isCreatingGroup: Boolean = false,
    val isViewingGroupInfo: Boolean = false,
    val isViewingProfileEdit: Boolean = false,
    val isAttachingMedia: Boolean = false,
    val chatWallpaper: String = "obsidian", // obsidian, emerald, midnight, purple
    val soundNotificationsEnabled: Boolean = true,
    val previewNotificationsEnabled: Boolean = true,
    val darkTheme: Boolean = true,
    val error: String? = null,
    val statusMessage: String? = null
)

enum class NavigationTab {
    CHATS, GROUPS, PEOPLE, SETTINGS
}

class ChatViewModel(
    private val repository: ChatRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(currentUserId = currentUserId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Observe current user's profile
        viewModelScope.launch {
            repository.observeUserProfile(currentUserId)
                .catch { Log.e(TAG, "Error observing user profile", it) }
                .collect { profile ->
                    _uiState.value = _uiState.value.copy(
                        currentUserProfile = profile ?: UserProfile(
                            userId = currentUserId,
                            displayName = "User",
                            username = "user_${currentUserId.take(5)}"
                        )
                    )
                }
        }

        // Observe all users for search and new chat
        viewModelScope.launch {
            repository.observeAllUsers()
                .catch { Log.e(TAG, "Error observing users", it) }
                .collect { users ->
                    _uiState.value = _uiState.value.copy(
                        allUsers = users.filter { it.userId != currentUserId }
                    )
                }
        }

        // Observe conversations for the current user
        viewModelScope.launch {
            repository.observeConversations(currentUserId)
                .catch { Log.e(TAG, "Error observing conversations", it) }
                .collect { convs ->
                    val query = _uiState.value.searchQuery
                    val filtered = filterConvs(convs, query)
                    _uiState.value = _uiState.value.copy(
                        conversations = convs,
                        filteredConversations = filtered
                    )
                }
        }
    }

    private fun filterConvs(convs: List<Conversation>, query: String): List<Conversation> {
        if (query.isBlank()) return convs
        return convs.filter { conv ->
            conv.name.contains(query, ignoreCase = true) ||
            conv.lastMessageText.contains(query, ignoreCase = true)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredConversations = filterConvs(_uiState.value.conversations, query)
        )
    }

    fun onSelectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun openConversation(conversation: Conversation) {
        _uiState.value = _uiState.value.copy(
            activeConversation = conversation,
            replyingToMessage = null,
            editingMessage = null
        )
        // Start observing messages for this conversation
        viewModelScope.launch {
            repository.observeMessages(conversation.id, currentUserId)
                .catch { Log.e(TAG, "Error observing messages for ${conversation.id}", it) }
                .collect { messages ->
                    if (_uiState.value.activeConversation?.id == conversation.id) {
                        _uiState.value = _uiState.value.copy(activeMessages = messages)
                    }
                }
        }
    }

    fun closeConversation() {
        _uiState.value = _uiState.value.copy(
            activeConversation = null,
            activeMessages = emptyList(),
            replyingToMessage = null,
            editingMessage = null
        )
    }

    fun startDirectChat(otherUser: UserProfile) {
        viewModelScope.launch {
            val result = repository.createDirectConversation(currentUserId, otherUser)
            result.onSuccess { convId ->
                val newConv = Conversation(
                    id = convId,
                    type = "direct",
                    participantIds = listOf(currentUserId, otherUser.userId),
                    name = otherUser.displayName
                )
                openConversation(newConv)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Failed to start chat")
            }
        }
    }

    fun createGroupChat(name: String, description: String, selectedMemberIds: List<String>) {
        viewModelScope.launch {
            val result = repository.createGroupConversation(name, description, selectedMemberIds)
            result.onSuccess { convId ->
                _uiState.value = _uiState.value.copy(
                    isCreatingGroup = false,
                    statusMessage = "Group \"$name\" created!"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.localizedMessage ?: "Failed to create group")
            }
        }
    }

    fun sendMessage(
        text: String,
        mediaType: String = "none",
        mediaUrl: String = "",
        mediaName: String = ""
    ) {
        val conv = _uiState.value.activeConversation ?: return
        if (text.isBlank() && mediaType == "none") return

        val replying = _uiState.value.replyingToMessage

        viewModelScope.launch {
            repository.sendMessage(
                conversationId = conv.id,
                text = text,
                participantIds = conv.participantIds,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                mediaName = mediaName,
                replyToMessageId = replying?.id ?: "",
                replyToText = replying?.text ?: "",
                replyToSender = replying?.senderName ?: ""
            )
            _uiState.value = _uiState.value.copy(
                replyingToMessage = null,
                isAttachingMedia = false
            )
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        val conv = _uiState.value.activeConversation ?: return
        viewModelScope.launch {
            repository.toggleReaction(conv.id, messageId, emoji)
        }
    }

    fun startEditingMessage(message: Message) {
        _uiState.value = _uiState.value.copy(editingMessage = message)
    }

    fun saveEditedMessage(newText: String) {
        val conv = _uiState.value.activeConversation ?: return
        val msg = _uiState.value.editingMessage ?: return
        if (newText.isBlank()) return
        viewModelScope.launch {
            repository.editMessage(conv.id, msg.id, newText)
            _uiState.value = _uiState.value.copy(editingMessage = null)
        }
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(editingMessage = null)
    }

    fun deleteMessage(message: Message) {
        val conv = _uiState.value.activeConversation ?: return
        viewModelScope.launch {
            repository.deleteMessage(conv.id, message.id)
        }
    }

    fun setReplyingTo(message: Message?) {
        _uiState.value = _uiState.value.copy(replyingToMessage = message)
    }

    fun setCreatingGroup(open: Boolean) {
        _uiState.value = _uiState.value.copy(isCreatingGroup = open)
    }

    fun setViewingGroupInfo(open: Boolean) {
        _uiState.value = _uiState.value.copy(isViewingGroupInfo = open)
    }

    fun setViewingProfileEdit(open: Boolean) {
        _uiState.value = _uiState.value.copy(isViewingProfileEdit = open)
    }

    fun setAttachingMedia(open: Boolean) {
        _uiState.value = _uiState.value.copy(isAttachingMedia = open)
    }

    fun updateUserStatus(status: String) {
        viewModelScope.launch {
            repository.updateUserStatus(status)
        }
    }

    fun updateProfile(displayName: String, username: String, bio: String) {
        val current = _uiState.value.currentUserProfile ?: return
        val updated = current.copy(
            displayName = displayName,
            username = username,
            bio = bio
        )
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            _uiState.value = _uiState.value.copy(
                isViewingProfileEdit = false,
                statusMessage = "Profile updated!"
            )
        }
    }

    fun setChatWallpaper(theme: String) {
        _uiState.value = _uiState.value.copy(chatWallpaper = theme)
    }

    fun toggleDarkTheme() {
        _uiState.value = _uiState.value.copy(darkTheme = !_uiState.value.darkTheme)
    }

    fun toggleSounds() {
        _uiState.value = _uiState.value.copy(soundNotificationsEnabled = !_uiState.value.soundNotificationsEnabled)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, error = null)
    }

    private companion object {
        const val TAG = "ChatViewModel"
    }
}
