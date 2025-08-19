package we.apps.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderId: String? = null,
    val receiverId: String? = null,
    val text: String? = null,
    val mediaUrl: String? = null,
    val type: String? = "text", // "text", "image", "video", "audio", "document"
    val timestamp: Long? = null,
    val repliedToMessageId: String? = null,
    val forwardedFromMessageId: String? = null,
    val reactions: Map<String, String>? = null, // Map of userId to emoji reaction
    val deleted: Boolean = false
)


