package we.apps.status

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import we.apps.databinding.ActivityViewStatusBinding

class ViewStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewStatusBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val statusId = intent.getStringExtra("statusId")
        if (statusId == null) {
            Toast.makeText(this, "Status ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadStatus(statusId)
    }

    private fun loadStatus(statusId: String) {
        database.getReference("statuses").child(statusId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.getValue(Status::class.java)
                    if (status != null) {
                        displayStatus(status)
                        updateViewers(statusId)
                    } else {
                        Toast.makeText(this@ViewStatusActivity, "Status not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ViewStatusActivity, "Failed to load status: ${error.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    private fun displayStatus(status: Status) {
        when (status.type) {
            "text" -> {
                binding.statusTextView.visibility = View.VISIBLE
                binding.statusMediaView.visibility = View.GONE
                binding.statusTextView.text = status.text
            }
            "image" -> {
                binding.statusTextView.visibility = View.GONE
                binding.statusMediaView.visibility = View.VISIBLE
                Glide.with(this).load(status.mediaUrl).into(binding.statusMediaView)
            }
            "video" -> {
                // TODO: Implement video playback
                binding.statusTextView.visibility = View.VISIBLE
                binding.statusMediaView.visibility = View.GONE
                binding.statusTextView.text = "Video status: ${status.mediaUrl}"
                Toast.makeText(this, "Video playback not yet implemented", Toast.LENGTH_SHORT).show()
            }
        }
        // Ensure viewers is not null before accessing size
        updateViewersCount(status.viewers?.size ?: 0)
    }

    private fun updateViewers(statusId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("statuses").child(statusId).child("viewers").child(userId).setValue(true)
        }
    }

    private fun updateViewersCount(count: Int) {
        binding.viewersCountTextView.text = "Viewers: $count"
    }
}


