package we.apps.status

data class Status(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String? = null,
    val mediaUrl: String? = null,
    val text: String? = null,
    val type: String? = "text", // "text", "image", "video"
    val timestamp: Long? = null,
    val expiryTimestamp: Long? = null,
    val viewers: Map<String, Boolean>? = null // Map of userId to true if viewed
)


