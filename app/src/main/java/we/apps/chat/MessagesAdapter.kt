package we.apps.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.weapps.R

class MessagesAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageTextView)
        val messageImageView: ImageView = view.findViewById(R.id.messageImageView)
        val messageMediaUrlTextView: TextView = view.findViewById(R.id.messageMediaUrlTextView)
        val replyForwardLayout: LinearLayout = view.findViewById(R.id.replyForwardLayout)
        val replyForwardIndicator: TextView = view.findViewById(R.id.replyForwardIndicator)
        val repliedForwardedMessageText: TextView = view.findViewById(R.id.repliedForwardedMessageText)
        val reactionsLayout: LinearLayout = view.findViewById(R.id.reactionsLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        if (message.deleted) {
            holder.messageText.visibility = View.VISIBLE
            holder.messageImageView.visibility = View.GONE
            holder.messageMediaUrlTextView.visibility = View.GONE
            holder.replyForwardLayout.visibility = View.GONE
            holder.reactionsLayout.visibility = View.GONE
            holder.messageText.text = "This message was deleted."
            holder.messageText.setTextColor(holder.itemView.context.resources.getColor(android.R.color.darker_gray))
            return
        }

        // Handle message content
        when (message.type) {
            "text" -> {
                holder.messageText.visibility = View.VISIBLE
                holder.messageImageView.visibility = View.GONE
                holder.messageMediaUrlTextView.visibility = View.GONE
                holder.messageText.text = message.text
                holder.messageText.setTextColor(holder.itemView.context.resources.getColor(android.R.color.white))
            }
            "image" -> {
                holder.messageText.visibility = View.GONE
                holder.messageImageView.visibility = View.VISIBLE
                holder.messageMediaUrlTextView.visibility = View.GONE
                Glide.with(holder.itemView.context).load(message.mediaUrl).into(holder.messageImageView)
            }
            else -> {
                // For other media types (video, audio, document), just show the URL for now
                holder.messageText.visibility = View.GONE
                holder.messageImageView.visibility = View.GONE
                holder.messageMediaUrlTextView.visibility = View.VISIBLE
                holder.messageMediaUrlTextView.text = message.mediaUrl
            }
        }

        // Handle replies and forwards
        if (message.repliedToMessageId != null) {
            holder.replyForwardLayout.visibility = View.VISIBLE
            holder.replyForwardIndicator.text = "Replied to:"
            // In a real app, you would fetch the content of the replied message
            holder.repliedForwardedMessageText.text = "Original message (ID: ${message.repliedToMessageId})"
        } else if (message.forwardedFromMessageId != null) {
            holder.replyForwardLayout.visibility = View.VISIBLE
            holder.replyForwardIndicator.text = "Forwarded from:"
            // In a real app, you would fetch the content of the forwarded message
            holder.repliedForwardedMessageText.text = "Original message (ID: ${message.forwardedFromMessageId})"
        } else {
            holder.replyForwardLayout.visibility = View.GONE
        }

        // Handle reactions
        holder.reactionsLayout.removeAllViews()
        message.reactions?.let { reactionsMap ->
            if (reactionsMap.isNotEmpty()) {
                holder.reactionsLayout.visibility = View.VISIBLE
                reactionsMap.forEach { (userId, emoji) ->
                    val reactionTextView = TextView(holder.itemView.context)
                    reactionTextView.text = emoji
                    reactionTextView.setPadding(8, 0, 8, 0)
                    holder.reactionsLayout.addView(reactionTextView)
                }
            } else {
                holder.reactionsLayout.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = messages.size
}

