package we.apps

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import we.apps.chat.ChatActivity
import we.apps.databinding.ActivityMainBinding
import we.apps.status.UploadStatusActivity
import we.apps.status.ViewStatusActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(IosEmojiProvider())
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openChatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            // For simplicity, passing a dummy receiverId. In a real app, this would be dynamic.
            intent.putExtra("receiverId", "dummyReceiverId")
            startActivity(intent)
        }

        binding.uploadStatusButton.setOnClickListener {
            startActivity(Intent(this, UploadStatusActivity::class.java))
        }

        binding.viewStatusButton.setOnClickListener {
            val intent = Intent(this, ViewStatusActivity::class.java)
            // For simplicity, passing a dummy statusId. In a real app, this would be dynamic.
            intent.putExtra("statusId", "dummyStatusId")
            startActivity(intent)
        }
    }
}

