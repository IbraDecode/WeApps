package we.apps.media

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage

class MediaHandler(private val context: Context) {

    private val storage = FirebaseStorage.getInstance()

    fun uploadMedia(fileUri: Uri, path: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val ref = storage.reference.child(path + "/" + fileUri.lastPathSegment)
        ref.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                ref.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

