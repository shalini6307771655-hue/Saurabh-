package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val status: String = "online",
    @ServerTimestamp val lastSeen: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "displayName" to displayName,
            "username" to username,
            "email" to email,
            "photoUrl" to photoUrl,
            "bio" to bio,
            "status" to status,
            "lastSeen" to (lastSeen ?: com.google.firebase.firestore.FieldValue.serverTimestamp()),
            "createdAt" to (createdAt ?: com.google.firebase.firestore.FieldValue.serverTimestamp()),
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        ).filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val type: String = "direct", // "direct" or "group"
    val participantIds: List<String> = emptyList(),
    val name: String = "",
    val groupDescription: String = "",
    val groupIconUrl: String = "",
    val adminIds: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageSenderName: String = "",
    @ServerTimestamp val lastMessageTimestamp: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Message(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val text: String = "",
    val mediaType: String = "none", // "none", "image", "file", "audio"
    val mediaUrl: String = "",
    val mediaName: String = "",
    val reactions: Map<String, String> = emptyMap(), // userId -> emoji
    val replyToMessageId: String = "",
    val replyToText: String = "",
    val replyToSender: String = "",
    val isEdited: Boolean = false,
    val participantIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
