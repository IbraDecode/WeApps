package we.apps.status

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import we.apps.databinding.ActivityUploadStatusBinding
import we.apps.media.MediaHandler

class UploadStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadStatusBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var mediaHandler: MediaHandler
    private var selectedMediaUri: Uri? = null
    private var mediaType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        mediaHandler = MediaHandler(this)

        binding.selectMediaButton.setOnClickListener { openMediaChooser() }
        binding.uploadStatusButton.setOnClickListener { uploadStatus() }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            selectedMediaUri = it.data?.data
            selectedMediaUri?.let { uri ->
                val mimeType = contentResolver.getType(uri)
                mediaType = when {
                    mimeType?.startsWith("image") == true -> "image"
                    mimeType?.startsWith("video") == true -> "video"
                    else -> "document" // Fallback for other types, though spec only mentions photo/video/text
                }
                binding.statusImageView.visibility = View.VISIBLE
                Glide.with(this).load(uri).into(binding.statusImageView)
            }
        }
    }

    private fun openMediaChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        pickMedia.launch(intent)
    }

    private fun uploadStatus() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val statusText = binding.statusEditText.text.toString().trim()

        if (statusText.isEmpty() && selectedMediaUri == null) {
            Toast.makeText(this, "Status cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val expiryTimestamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 hours from now

        if (selectedMediaUri != null && mediaType != null) {
            mediaHandler.uploadMedia(selectedMediaUri!!, "status_media") { mediaUrl ->
                val status = Status(
                    userId = userId,
                    mediaUrl = mediaUrl,
                    type = mediaType,
                    timestamp = System.currentTimeMillis(),
                    expiryTimestamp = expiryTimestamp
                )
                saveStatusToDatabase(status)
            } { e ->
                Toast.makeText(this, "Failed to upload media: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else if (statusText.isNotEmpty()) {
            val status = Status(
                userId = userId,
                text = statusText,
                type = "text",
                timestamp = System.currentTimeMillis(),
                expiryTimestamp = expiryTimestamp
            )
            saveStatusToDatabase(status)
        }
    }

    private fun saveStatusToDatabase(status: Status) {
        database.getReference("statuses").push().setValue(status)
            .addOnSuccessListener {
                Toast.makeText(this, "Status uploaded successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

