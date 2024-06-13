package com.example.spaceinterntask

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spaceinterntask.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Sign_Up_Activity : AppCompatActivity() {
    private val REQ_ONE_TAP = 2
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        All_Set_on_button_listener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                updateUI(user)
                            } else {

                                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } catch (e: ApiException) {

                Toast.makeText(this, "Sign-In Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun All_Set_on_button_listener(){

        binding.SigInButton.setOnClickListener {
            val name = binding.UserName.text.toString()
            val email = binding.EmailId.text.toString()
            val pass = binding.Password.text.toString()
            val confirm_pass = binding.ConfirmPassword.text.toString()

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm_pass)) {
                Toast.makeText(this, "Fill all your details", Toast.LENGTH_SHORT).show()
            } else {
                if (pass.length < 8 || confirm_pass.length < 8) {
                    Toast.makeText(this, "Password is weak", Toast.LENGTH_SHORT).show()
                } else {
                    if (pass == confirm_pass) {
                        auth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this.applicationContext, "Failed to register", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this.applicationContext, "Passwords don't match", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.SignInGoogleButton.setOnClickListener {
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.your_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                }
                .addOnFailureListener(this) { e ->
                    Toast.makeText(this, "Google Sign-In Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    Log.d("error of google",e.localizedMessage)
                }
        }
        binding.LoginLink.setOnClickListener{
            val intent = Intent(this, Login_Activity::class.java)
            startActivity(intent)
            finish()
        }
        binding.NameCard.setOnClickListener{
            binding.UserName.requestFocus()
        }
        binding.NameImage.setOnClickListener{
            binding.UserName.requestFocus()
        }
        binding.EmailCard.setOnClickListener {
            binding.EmailId.requestFocus()
        }
        binding.imageView.setOnClickListener {
            binding.EmailId.requestFocus()
        }
        binding.PasswordCard.setOnClickListener {
            binding.Password.requestFocus()
        }
        binding.imageView1.setOnClickListener {
            binding.Password.requestFocus()
        }
        binding.imageView12.setOnClickListener {
            binding.ConfirmPassword.requestFocus()
        }
        binding.ConfirmPasswordCard.setOnClickListener{}
        binding.ConfirmPassword.requestFocus()
    }
}
