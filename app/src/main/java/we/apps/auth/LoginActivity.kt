package we.apps.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import we.apps.MainActivity
import we.apps.databinding.ActivityLoginBinding
import we.apps.profile.ProfileSetupActivity
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@LoginActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                Toast.makeText(this@LoginActivity, "Code sent", Toast.LENGTH_SHORT).show()
            }
        }

        binding.sendOtpButton.setOnClickListener { sendOtp() }
        binding.verifyOtpButton.setOnClickListener { verifyOtp() }
    }

    private fun sendOtp() {
        val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
        if (phoneNumber.isEmpty()) {
            binding.phoneNumberEditText.error = "Enter phone number"
            return
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtp() {
        val otp = binding.otpEditText.text.toString().trim()
        if (otp.isEmpty()) {
            binding.otpEditText.error = "Enter OTP"
            return
        }

        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            signInWithPhoneAuthCredential(credential)
        } else {
            Toast.makeText(this, "Verification ID is null. Resend OTP.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)
                        userRef.get().addOnSuccessListener { dataSnapshot ->
                            if (dataSnapshot.exists()) {
                                // User profile exists, go to MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                            } else {
                                // New user, go to ProfileSetupActivity
                                startActivity(Intent(this, ProfileSetupActivity::class.java))
                            }
                            finish()
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to check user profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User not logged in after authentication", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

