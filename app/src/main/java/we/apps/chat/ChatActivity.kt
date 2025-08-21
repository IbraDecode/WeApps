package we.apps.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import we.apps.data.local.AppDatabase
import we.apps.databinding.ActivityChatBinding
import we.apps.media.MediaHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var mediaHandler: MediaHandler
    private lateinit var appDatabase: AppDatabase
    private val messagesList = mutableListOf<Message>()

    private var receiverId: String? = null
    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        mediaHandler = MediaHandler(this)

        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "weapps-db"
        ).build()

        receiverId = intent.getStringExtra("receiverId")
        // In a real app, you would determine the chatId based on sender and receiver IDs
        // For simplicity, let\"s assume a fixed chat ID for now or generate one.
        // This needs to be robust for private and group chats.
        chatId = "chat_example_123"

        messagesAdapter = MessagesAdapter(messagesList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messagesAdapter

        binding.sendButton.setOnClickListener { sendMessage() }
        binding.attachMediaButton.setOnClickListener { openMediaChooser() }
        binding.recordVoiceNoteButton.setOnClickListener { recordVoiceNote() }

        loadMessagesFromLocalDb()
        listenForMessages()
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val selectedMediaUri = it.data?.data
            selectedMediaUri?.let { uri ->
                uploadMedia(uri, "image") // Assuming image for now, will need to determine type
            }
        }
    }

    private fun openMediaChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // Allow all media types for now
        pickMedia.launch(intent)
    }

    private fun recordVoiceNote() {
        Toast.makeText(this, "Voice note recording not yet implemented", Toast.LENGTH_SHORT).show()
        // TODO: Implement voice note recording and upload
    }

    private fun uploadMedia(fileUri: Uri, type: String) {
        val senderId = auth.currentUser?.uid
        if (senderId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        mediaHandler.uploadMedia(fileUri, "chat_media",
            onSuccess = { mediaUrl ->
                val message = Message(
                    senderId = senderId,
                    receiverId = receiverId,
                    mediaUrl = mediaUrl,
                    type = type,
                    timestamp = System.currentTimeMillis()
                )
                chatId?.let {
                    database.getReference("chats").child(it).push().setValue(message)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Media sent", Toast.LENGTH_SHORT).show()
                            lifecycleScope.launch(Dispatchers.IO) {
                                appDatabase.messageDao().insertMessage(message)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to send media: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            onFailure = { e ->
                Toast.makeText(this, "Failed to upload media: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun sendMessage() {
        val messageText = binding.messageEditText.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val senderId = auth.currentUser?.uid
        if (senderId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val message = Message(
            senderId = senderId,
            receiverId = receiverId,
            text = messageText,
            type = "text",
            timestamp = System.currentTimeMillis()
        )

        chatId?.let {
            database.getReference("chats").child(it).push().setValue(message)
                .addOnSuccessListener {
                    binding.messageEditText.text.clear()
                    binding.chatRecyclerView.scrollToPosition(messagesList.size - 1)
                    lifecycleScope.launch(Dispatchers.IO) {
                        appDatabase.messageDao().insertMessage(message)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteMessage(messageId: String) {
        chatId?.let {
            database.getReference("chats").child(it).child(messageId).child("deleted").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch(Dispatchers.IO) {
                        // Optionally, update the local database to mark as deleted
                        // For simplicity, we are not updating the local database for deleted messages here
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to delete message: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadMessagesFromLocalDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val localMessages = appDatabase.messageDao().getAllMessages()
            withContext(Dispatchers.Main) {
                messagesList.clear()
                messagesList.addAll(localMessages)
                messagesAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messagesList.size - 1)
            }
        }
    }

    private fun listenForMessages() {
        chatId?.let {
            database.getReference("chats").child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messagesList.clear()
                        val newMessages = mutableListOf<Message>()
                        for (messageSnapshot in snapshot.children) {
                            val message = messageSnapshot.getValue(Message::class.java)
                            message?.let { msg ->
                                newMessages.add(msg)
                                lifecycleScope.launch(Dispatchers.IO) {
                                    appDatabase.messageDao().insertMessage(msg)
                                }
                            }
                        }
                        messagesList.addAll(newMessages)
                        messagesAdapter.notifyDataSetChanged()
                        binding.chatRecyclerView.scrollToPosition(messagesList.size - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ChatActivity, "Failed to load messages: ${error.message}", Toast.LENGTH_SHORT).show()
                        // If Firebase fails, rely on local database
                        loadMessagesFromLocalDb()
                    }
                })
        }
    }
}

