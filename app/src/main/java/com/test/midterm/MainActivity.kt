package com.test.midterm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easylearn.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {
    private var signInButton: SignInButton? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val TAG = "MainActivity"
    private var mAuth: FirebaseAuth? = null
    private var btnSignOut: Button? = null
    private val RC_SIGN_IN = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signInButton = findViewById(R.id.sign_in_button)
        mAuth = FirebaseAuth.getInstance()
        btnSignOut = findViewById(R.id.sign_out_button)
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        signInButton.setOnClickListener(View.OnClickListener { signIn() })
        btnSignOut.setOnClickListener(View.OnClickListener {
            mGoogleSignInClient.signOut()
            Toast.makeText(this@MainActivity, "You are Logged Out", Toast.LENGTH_SHORT).show()
            btnSignOut.setVisibility(View.INVISIBLE)
        })
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val acc = completedTask.getResult(
                ApiException::class.java
            )
            Toast.makeText(this@MainActivity, "Signed In Successfully", Toast.LENGTH_SHORT).show()
            FirebaseGoogleAuth(acc)
        } catch (e: ApiException) {
            Toast.makeText(this@MainActivity, "Sign In Failed", Toast.LENGTH_SHORT).show()
            FirebaseGoogleAuth(null)
        }
    }

    private fun FirebaseGoogleAuth(acct: GoogleSignInAccount?) { //check if the account is null
        if (acct != null) {
            val authCredential: AuthCredential =
                GoogleAuthProvider.getCredential(acct.idToken, null)
            mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, object :
                OnCompleteListener<AuthResult?> {
                override fun onComplete(task: Task<AuthResult?>) {
                    if (task.isSuccessful()) {
                        Toast.makeText(this@MainActivity, "Successful", Toast.LENGTH_SHORT).show()
                        val user: FirebaseUser = mAuth.getCurrentUser()
                        updateUI(user)
                    } else {
                        Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "acc failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(fUser: FirebaseUser?) {
        btnSignOut!!.visibility = View.VISIBLE
        val account: GoogleSignInAccount =
            GoogleSignIn.getLastSignedInAccount(applicationContext)
        if (account != null) {
            val personName = account.displayName
            val personGivenName = account.givenName
            val personFamilyName = account.familyName
            val personEmail = account.email
            val personId = account.id
            val personPhoto = account.photoUrl
            Toast.makeText(this@MainActivity, personName + personEmail, Toast.LENGTH_SHORT).show()
        }
    }
}