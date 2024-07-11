package com.mcb.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signUpButton: Button = findViewById(R.id.signup_btn)
        signUpButton.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullNameEditText: EditText = findViewById(R.id.fullname_signup)
        val userNameEditText: EditText = findViewById(R.id.username_signup)
        val emailEditText: EditText = findViewById(R.id.email_signup)
        val passwordEditText: EditText = findViewById(R.id.password_signup)

        val fullName = fullNameEditText.text.toString()
        val userName = userNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        val progressDialog = ProgressDialog(this@SignUpActivity)

        if (fullName.isEmpty() || userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }
        else{

            progressDialog.setTitle("Signing up")
            progressDialog.setMessage("Please wait...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
        }
        val mAuth:FirebaseAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    saveUserInfo(fullName, userName, email, progressDialog)
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    // You can add additional user info to the database if needed
                } else {
                    // If sign in fails, display a message to the user.
                    val message=task.exception!!.toString()
                    Toast.makeText(this, "Authentication failed: ${message}", Toast.LENGTH_SHORT).show()
                    mAuth.signOut()
                    progressDialog.dismiss()
                }
            }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID= FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap=HashMap<String,Any>()
        userMap["uid"]=currentUserID
        userMap["fullname"]=fullName.toLowerCase()
        userMap["username"]=userName.toLowerCase()
        userMap["email"]=email
        userMap["bio"]="Hi it's a Instagram clone app"
        userMap["image"]="https://firebasestorage.googleapis.com/v0/b/instagram-clone-6a668.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=9015a2e5-0185-47c7-822e-3bb3c293383f"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Authentication failed: ${message}", Toast.LENGTH_SHORT)
                        .show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}
