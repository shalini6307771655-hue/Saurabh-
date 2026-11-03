package com.example.ui.chat

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.ui.theme.ChatBubbleIncoming
import com.example.ui.theme.ChatBubbleOutgoing
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.StatusOnline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    conversation: Conversation,
    messages: List<Message>,
    currentUserId: String,
    replyingTo: Message?,
    editingMessage: Message?,
    wallpaperTheme: String,
    onBackClick: () -> Unit,
    onSendMessage: (text: String, mediaType: String, mediaUrl: String, mediaName: String) -> Unit,
    onToggleReaction: (messageId: String, emoji: String) -> Unit,
    onStartReply: (Message) -> Unit,
    onCancelReply: () -> Unit,
    onStartEdit: (Message) -> Unit,
    onSaveEdit: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteMessage: (Message) -> Unit,
    onViewInfo: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf("") }
    var selectedMessageForMenu by remember { mutableStateOf<Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showEmojiQuickBar by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // When editing starts, populate input text
    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            inputText = editingMessage.text
        }
    }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val wallpaperGradient = when (wallpaperTheme) {
        "emerald" -> listOf(Color(0xFF031C18), Color(0xFF041212))
        "midnight" -> listOf(Color(0xFF091428), Color(0xFF040A14))
        "purple" -> listOf(Color(0xFF190D2E), Color(0xFF0C0717))
        else -> listOf(Color(0xFF0B0F19), Color(0xFF070B13))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(wallpaperGradient))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            ChatTopBar(
                conversation = conversation,
                onBackClick = onBackClick,
                onInfoClick = onViewInfo
            )

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyChatState(conversation.name)
                    }
                }

                items(messages, key = { it.id.ifBlank { it.text.hashCode().toString() } }) { message ->
                    val isMine = message.senderId == currentUserId
                    MessageBubble(
                        message = message,
                        isMine = isMine,
                        currentUserId = currentUserId,
                        onLongClick = { selectedMessageForMenu = message },
                        onReactionClick = { emoji -> onToggleReaction(message.id, emoji) }
                    )
                }
            }

            // Quick Emoji Reaction Bar (Popup or Bar)
            AnimatedVisibility(visible = showEmojiQuickBar) {
                EmojiQuickRow(
                    onEmojiSelected = { emoji ->
                        inputText += emoji
                    }
                )
            }

            // Replying to Message Banner
            AnimatedVisibility(visible = replyingTo != null) {
                if (replyingTo != null) {
                    ReplyBanner(
                        message = replyingTo,
                        onCancel = onCancelReply
                    )
                }
            }

            // Editing Message Banner
            AnimatedVisibility(visible = editingMessage != null) {
                if (editingMessage != null) {
                    EditBanner(
                        message = editingMessage,
                        onCancel = {
                            inputText = ""
                            onCancelEdit()
                        }
                    )
                }
            }

            // Bottom Input Bar
            ChatInputBar(
                text = inputText,
                onTextChanged = { inputText = it },
                isEditing = editingMessage != null,
                onEmojiClick = { showEmojiQuickBar = !showEmojiQuickBar },
                onAttachClick = { showAttachmentSheet = true },
                onSend = {
                    if (editingMessage != null) {
                        onSaveEdit(inputText)
                        inputText = ""
                    } else if (inputText.isNotBlank()) {
                        onSendMessage(inputText, "none", "", "")
                        inputText = ""
                    }
                },
                onVoiceRecord = {
                    // Send voice note simulation
                    onSendMessage("Voice Message (0:14)", "audio", "", "voice_note.m4a")
                    Toast.makeText(context, "Voice note sent", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Message Options Dialog (Long press)
        selectedMessageForMenu?.let { message ->
            MessageActionDialog(
                message = message,
                isMine = message.senderId == currentUserId,
                onDismiss = { selectedMessageForMenu = null },
                onReaction = { emoji ->
                    onToggleReaction(message.id, emoji)
                    selectedMessageForMenu = null
                },
                onReply = {
                    onStartReply(message)
                    selectedMessageForMenu = null
                },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(message.text))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    selectedMessageForMenu = null
                },
                onEdit = {
                    onStartEdit(message)
                    selectedMessageForMenu = null
                },
                onDelete = {
                    onDeleteMessage(message)
                    selectedMessageForMenu = null
                }
            )
        }

        // Attachment Sheet
        if (showAttachmentSheet) {
            AttachmentBottomSheet(
                onDismiss = { showAttachmentSheet = false },
                onSelectImage = {
                    onSendMessage("Shared photo", "image", "https://picsum.photos/600/400", "photo_${System.currentTimeMillis()}.jpg")
                    showAttachmentSheet = false
                },
                onSelectDocument = {
                    onSendMessage("Project_Specification.pdf", "file", "", "Project_Specification.pdf")
                    showAttachmentSheet = false
                },
                onSelectAudio = {
                    onSendMessage("Audio Recording (0:32)", "audio", "", "audio_track.mp3")
                    showAttachmentSheet = false
                }
            )
        }
    }
}

@Composable
private fun ChatTopBar(
    conversation: Conversation,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Surface(
        color = Color(0xFF111827).copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("chat_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Box(modifier = Modifier.size(42.dp)) {
                if (conversation.groupIconUrl.isNotBlank()) {
                    AsyncImage(
                        model = conversation.groupIconUrl,
                        contentDescription = conversation.name,
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
                            fontSize = 18.sp
                        )
                    }
                }

                // Online indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(StatusOnline)
                        .border(2.dp, Color(0xFF111827), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = conversation.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (conversation.type == "group")
                        "${conversation.participantIds.size} members"
                    else
                        "Online",
                    color = CyanPrimary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Conversation Info",
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: Message,
    isMine: Boolean,
    currentUserId: String,
    onLongClick: () -> Unit,
    onReactionClick: (String) -> Unit
) {
    val bubbleColor = if (isMine) ChatBubbleOutgoing else ChatBubbleIncoming
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        // Group sender name
        if (!isMine && message.senderName.isNotBlank()) {
            Text(
                text = message.senderName,
                color = CyanPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(min = 60.dp, max = 290.dp)
                .clip(shape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                // Reply quote preview
                if (message.replyToText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text(
                                text = message.replyToSender.ifBlank { "User" },
                                color = CyanPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = message.replyToText,
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Media Attachment Content
                when (message.mediaType) {
                    "image" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photo",
                                tint = CyanPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    "file" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "File",
                                tint = CyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.mediaName.ifBlank { "Document.pdf" },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    "audio" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = CyanPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Waveform bars simulation
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(12) { i ->
                                    val height = (8 + (i * 3) % 14).dp
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(height)
                                            .background(Color.White.copy(alpha = 0.8f))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "0:14",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Main Message Text
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp, Edited, Status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "edited ",
                            color = Color(0xFFCBD5E1).copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = formatTime(message.createdAt?.toDate()),
                        color = Color(0xFFCBD5E1).copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )

                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Reactions Row below bubble
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = if (isMine) 0.dp else 4.dp, end = if (isMine) 4.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Group by emoji
                val emojiCounts = message.reactions.values.groupingBy { it }.eachCount()
                emojiCounts.forEach { (emoji, count) ->
                    val myReaction = message.reactions[currentUserId] == emoji
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (myReaction) CyanContainer else Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (myReaction) CyanPrimary else Color(0xFF334155),
                                RoundedCornerShape(12.dp)
                            )
                            .combinedClickable(
                                onClick = { onReactionClick(emoji) }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$emoji $count",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(recipientName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mood,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Say hello to $recipientName!",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Send a message or media to start the conversation",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun EmojiQuickRow(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf("❤️", "👍", "😂", "🔥", "🎉", "😮", "🙏", "👏")
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            emojis.forEach { emoji ->
                TextButton(
                    onClick = { onEmojiSelected(emoji) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun ReplyBanner(
    message: Message,
    onCancel: () -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 2.dp, color = CyanPrimary, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to ${message.senderName}",
                    color = CyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.text,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Reply",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditBanner(
    message: Message,
    onCancel: () -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 2.dp, color = IndigoSecondary, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = IndigoSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Editing message",
                    color = IndigoSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.text,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Edit",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    isEditing: Boolean,
    onEmojiClick: () -> Unit,
    onAttachClick: () -> Unit,
    onSend: () -> Unit,
    onVoiceRecord: () -> Unit
) {
    Surface(
        color = Color(0xFF111827),
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onEmojiClick,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mood,
                    contentDescription = "Emoji",
                    tint = Color(0xFF94A3B8)
                )
            }

            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach Media",
                    tint = Color(0xFF94A3B8)
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = if (isEditing) "Edit message..." else "Message...",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (text.isNotBlank() || isEditing) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CyanPrimary)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF0B0F19),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onVoiceRecord,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .testTag("record_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record Voice",
                        tint = CyanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageActionDialog(
    message: Message,
    isMine: Boolean,
    onDismiss: () -> Unit,
    onReaction: (String) -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val quickEmojis = listOf("❤️", "👍", "😂", "🔥", "😮", "😢")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                quickEmojis.forEach { emoji ->
                    TextButton(onClick = { onReaction(emoji) }) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onReply)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Reply", color = Color.White)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCopy)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Copy text", color = Color.White)
                }

                if (isMine) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onEdit)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = IndigoSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Edit message", color = Color.White)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDelete)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Delete for everyone", color = MaterialTheme.colorScheme.error)
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
private fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onSelectImage: () -> Unit,
    onSelectDocument: () -> Unit,
    onSelectAudio: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text(text = "Share Content", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOption(
                    icon = Icons.Default.Image,
                    label = "Gallery",
                    color = CyanPrimary,
                    onClick = onSelectImage
                )
                AttachmentOption(
                    icon = Icons.Default.Description,
                    label = "Document",
                    color = IndigoSecondary,
                    onClick = onSelectDocument
                )
                AttachmentOption(
                    icon = Icons.Default.AudioFile,
                    label = "Audio",
                    color = Color(0xFF10B981),
                    onClick = onSelectAudio
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}

private fun formatTime(date: Date?): String {
    if (date == null) return ""
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(date)
}
