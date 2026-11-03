package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class ChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedUser_success() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ChatRepository(firestore)

        val profile = UserProfile(
            userId = aliceUid,
            displayName = "Alice Wonderland",
            username = "alice_w",
            bio = "Coding Android Apps",
            status = "online"
        )

        val saveResult = withTimeout(TIMEOUT_MS) {
            repository.saveUserProfile(profile)
        }
        assertTrue("Save user profile should succeed", saveResult.isSuccess)

        val retrieved = withTimeout(TIMEOUT_MS) {
            firestore.collection("users").document(aliceUid).get().await()
        }
        assertEquals("Alice Wonderland", retrieved.getString("displayName"))
    }

    @Test
    fun createConversation_and_sendMessage_success() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = "bob_user_id"
        val repository = ChatRepository(firestore)

        val bobProfile = UserProfile(
            userId = bobUid,
            displayName = "Bob Smith",
            username = "bob_s"
        )

        val convResult = withTimeout(TIMEOUT_MS) {
            repository.createDirectConversation(aliceUid, bobProfile)
        }
        assertTrue("Create conversation should succeed", convResult.isSuccess)
        val convId = convResult.getOrThrow()
        assertNotNull(convId)

        val sendResult = withTimeout(TIMEOUT_MS) {
            repository.sendMessage(
                conversationId = convId,
                text = "Hello Bob!",
                participantIds = listOf(aliceUid, bobUid)
            )
        }
        assertTrue("Send message should succeed", sendResult.isSuccess)
    }

    @Test
    fun unauthenticated_access_rejected() = runBlocking {
        auth.signOut()
        val repository = ChatRepository(firestore)

        val testProfile = UserProfile(
            userId = "anon_user",
            displayName = "Anon",
            username = "anon"
        )

        val result = repository.saveUserProfile(testProfile)
        assertTrue("Unauthenticated save profile must fail", result.isFailure)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val TIMEOUT_MS = 5000L
    }
}
