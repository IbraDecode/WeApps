package we.apps.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import we.apps.databinding.ItemMessageBinding

class MessagesAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.binding.apply {
            if (message.deleted) {
                messageTextView.visibility = android.view.View.VISIBLE
                messageImageView.visibility = android.view.View.GONE
                messageMediaUrlTextView.visibility = android.view.View.GONE
                replyForwardLayout.visibility = android.view.View.GONE
                reactionsLayout.visibility = android.view.View.GONE
                messageTextView.text = "This message was deleted."
                messageTextView.setTextColor(holder.itemView.context.resources.getColor(android.R.color.darker_gray))
                return
            }

            // Handle message content
            when (message.type) {
                "text" -> {
                    messageTextView.visibility = android.view.View.VISIBLE
                    messageImageView.visibility = android.view.View.GONE
                    messageMediaUrlTextView.visibility = android.view.View.GONE
                    messageTextView.text = message.text
                    messageTextView.setTextColor(holder.itemView.context.resources.getColor(android.R.color.white))
                }
                "image" -> {
                    messageTextView.visibility = android.view.View.GONE
                    messageImageView.visibility = android.view.View.VISIBLE
                    messageMediaUrlTextView.visibility = android.view.View.GONE
                    Glide.with(holder.itemView.context).load(message.mediaUrl).into(messageImageView)
                }
                else -> {
                    // For other media types (video, audio, document), just show the URL for now
                    messageTextView.visibility = android.view.View.GONE
                    messageImageView.visibility = android.view.View.GONE
                    messageMediaUrlTextView.visibility = android.view.View.VISIBLE
                    messageMediaUrlTextView.text = message.mediaUrl
                }
            }

            // Handle replies and forwards
            if (message.repliedToMessageId != null) {
                replyForwardLayout.visibility = android.view.View.VISIBLE
                replyForwardIndicator.text = "Replied to:"
                // In a real app, you would fetch the content of the replied message
                repliedForwardedMessageText.text = "Original message (ID: ${message.repliedToMessageId})"
            } else if (message.forwardedFromMessageId != null) {
                replyForwardLayout.visibility = android.view.View.VISIBLE
                replyForwardIndicator.text = "Forwarded from:"
                // In a real app, you would fetch the content of the forwarded message
                repliedForwardedMessageText.text = "Original message (ID: ${message.forwardedFromMessageId})"
            } else {
                replyForwardLayout.visibility = android.view.View.GONE
            }

            // Handle reactions
            reactionsLayout.removeAllViews()
            message.reactions?.let { reactionsMap ->
                if (reactionsMap.isNotEmpty()) {
                    reactionsLayout.visibility = android.view.View.VISIBLE
                    reactionsMap.forEach { (userId, emoji) ->
                        val reactionTextView = android.widget.TextView(holder.itemView.context)
                        reactionTextView.text = emoji
                        reactionTextView.setPadding(8, 0, 8, 0)
                        reactionsLayout.addView(reactionTextView)
                    }
                } else {
                    reactionsLayout.visibility = android.view.View.GONE
                }
            }
        }
    }

    override fun getItemCount() = messages.size
}


