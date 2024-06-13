package com.example.spaceinterntask

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spaceinterntask.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login_Activity : AppCompatActivity() {
    private val REQ_ONE_TAP = 2
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        on_click_login_function()
        focus_on_ids()
        google_login()

        oneTapClient  = Identity.getSignInClient(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            try {
                val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = googleCredential.googleIdToken
                when {
                    idToken != null -> {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    updateUI(user)
                                } else {
                                    Log.d( "signInWithCredential-fail", task.exception.toString())
                                    updateUI(null)

                                }
                            }
                    }
                    else -> {
                        if (idToken != null) {
                            Log.d("No ID token!",idToken)
                        }
                    }
                }
            } catch (e: ApiException) {
                Log.e("Google sign in failed", e.toString())
            }
        }
    }
    private fun google_login(){
        binding.InGoogleButton.setOnClickListener {

            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.your_web_client_id))
                        .setFilterByAuthorizedAccounts(true)
                        .build()
                )
                .build()

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    startIntentSenderForResult(result.pendingIntent.intentSender,
                        REQ_ONE_TAP, null, 0, 0, 0, null)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun on_click_login_function() {
        binding.LogInButton.setOnClickListener {
            if (binding.EmailId.text.isEmpty() && binding.Password.text.isEmpty()) {
                Toast.makeText(this.applicationContext, "EmailId and Password is empty", Toast.LENGTH_SHORT).show()
            } else {
                val email= binding.EmailId.text
                val pass= binding.Password.text

                auth.signInWithEmailAndPassword(email.toString(), pass.toString())
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this.applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
        }
    }

    private fun focus_on_ids() {
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
        binding.SiginLink.setOnClickListener{
            val intent = Intent(this, Sign_Up_Activity::class.java)
            startActivity(intent)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            //Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show()
        }
    }
}
