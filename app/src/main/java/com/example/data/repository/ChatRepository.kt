package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before performing database operations.")
    }

    // --- USER PROFILE ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        val snapshot = docRef.snapshots().map { it.toObject(UserProfile::class.java) }
        emitAll(snapshot)
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val data = profile.copy(
                userId = uid,
                updatedAt = null
            )
            val docSnapshot = docRef.get().await()
            if (docSnapshot.exists()) {
                val existingCreatedAt = docSnapshot.getTimestamp("createdAt")
                docRef.set(
                    data.copy(createdAt = existingCreatedAt),
                    SetOptions.merge()
                ).await()
            } else {
                docRef.set(data).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateUserStatus(status: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("No user"))
        val docRef = db.collection("users").document(uid)
        return try {
            docRef.update(
                mapOf(
                    "status" to status,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeAllUsers(): Flow<List<UserProfile>> =
        db.collection("users")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(UserProfile::class.java)
            }

    // --- CONVERSATIONS ---

    fun observeConversations(userId: String): Flow<List<Conversation>> =
        db.collection("conversations")
            .whereArrayContains("participantIds", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Conversation::class.java)
                    .sortedByDescending { it.lastMessageTimestamp ?: it.updatedAt ?: it.createdAt }
            }

    suspend fun createDirectConversation(currentUserId: String, otherUser: UserProfile): Result<String> {
        val convsRef = db.collection("conversations")
        return try {
            // Check if conversation between these two already exists
            val existing = convsRef
                .whereEqualTo("type", "direct")
                .whereArrayContains("participantIds", currentUserId)
                .get()
                .await()
                .toObjects(Conversation::class.java)
                .firstOrNull { it.participantIds.contains(otherUser.userId) }

            if (existing != null) {
                return Result.success(existing.id)
            }

            val convId = UUID.randomUUID().toString()
            val conv = Conversation(
                id = convId,
                type = "direct",
                participantIds = listOf(currentUserId, otherUser.userId),
                name = otherUser.displayName,
                lastMessageText = "Started a conversation",
                lastMessageSenderId = currentUserId,
                lastMessageSenderName = auth.currentUser?.displayName ?: "User",
                createdAt = null,
                updatedAt = null
            )
            convsRef.document(convId).set(conv).await()
            Result.success(convId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, convsRef.path)
            Result.failure(e)
        }
    }

    suspend fun createGroupConversation(
        name: String,
        description: String,
        participantIds: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val convsRef = db.collection("conversations")
        return try {
            val convId = UUID.randomUUID().toString()
            val allMembers = (participantIds + uid).distinct()
            val conv = Conversation(
                id = convId,
                type = "group",
                participantIds = allMembers,
                name = name,
                groupDescription = description,
                adminIds = listOf(uid),
                lastMessageText = "Group created",
                lastMessageSenderId = uid,
                lastMessageSenderName = auth.currentUser?.displayName ?: "Admin",
                createdAt = null,
                updatedAt = null
            )
            convsRef.document(convId).set(conv).await()
            Result.success(convId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, convsRef.path)
            Result.failure(e)
        }
    }

    // --- MESSAGES ---

    fun observeMessages(conversationId: String, currentUserId: String): Flow<List<Message>> =
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .whereArrayContains("participantIds", currentUserId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Message::class.java)
                    .sortedBy { it.createdAt }
            }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        participantIds: List<String>,
        mediaType: String = "none",
        mediaUrl: String = "",
        mediaName: String = "",
        replyToMessageId: String = "",
        replyToText: String = "",
        replyToSender: String = ""
    ): Result<String> {
        val uid = requireUserId()
        val messagesRef = db.collection("conversations").document(conversationId).collection("messages")
        val convRef = db.collection("conversations").document(conversationId)
        return try {
            val messageId = UUID.randomUUID().toString()
            val senderName = auth.currentUser?.displayName ?: "User"
            val message = Message(
                id = messageId,
                conversationId = conversationId,
                senderId = uid,
                senderName = senderName,
                senderPhotoUrl = auth.currentUser?.photoUrl?.toString() ?: "",
                text = text,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                mediaName = mediaName,
                reactions = emptyMap(),
                replyToMessageId = replyToMessageId,
                replyToText = replyToText,
                replyToSender = replyToSender,
                isEdited = false,
                participantIds = participantIds,
                createdAt = null,
                updatedAt = null
            )

            // Write message document
            messagesRef.document(messageId).set(message).await()

            // Update parent conversation summary
            convRef.update(
                mapOf(
                    "lastMessageText" to if (mediaType != "none" && text.isBlank()) "[$mediaType]" else text,
                    "lastMessageSenderId" to uid,
                    "lastMessageSenderName" to senderName,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(messageId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, messagesRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleReaction(
        conversationId: String,
        messageId: String,
        emoji: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("conversations").document(conversationId).collection("messages").document(messageId)
        return try {
            val snapshot = docRef.get().await()
            val existing = snapshot.toObject(Message::class.java) ?: return Result.failure(Exception("Message not found"))
            val mutableReactions = existing.reactions.toMutableMap()
            if (mutableReactions[uid] == emoji) {
                mutableReactions.remove(uid)
            } else {
                mutableReactions[uid] = emoji
            }
            docRef.update(
                mapOf(
                    "reactions" to mutableReactions,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun editMessage(
        conversationId: String,
        messageId: String,
        newText: String
    ): Result<Unit> {
        val docRef = db.collection("conversations").document(conversationId).collection("messages").document(messageId)
        return try {
            docRef.update(
                mapOf(
                    "text" to newText,
                    "isEdited" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(
        conversationId: String,
        messageId: String
    ): Result<Unit> {
        val docRef = db.collection("conversations").document(conversationId).collection("messages").document(messageId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    private suspend fun <T> kotlinx.coroutines.flow.FlowCollector<T>.emitAll(flow: Flow<T>) {
        flow.collect { emit(it) }
    }
}
