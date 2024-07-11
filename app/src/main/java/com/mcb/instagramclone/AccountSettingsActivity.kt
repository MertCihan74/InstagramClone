package com.mcb.instagramclone

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.mcb.instagramclone.Model.User
import com.squareup.picasso.Picasso

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var fullNameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var profileImageView: ImageView
    private var checker = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        fullNameEditText = findViewById(R.id.full_name_profile_frag)
        usernameEditText = findViewById(R.id.username_profile_frag)
        bioEditText = findViewById(R.id.bio_profile_frag)
        profileImageView = findViewById(R.id.profile_image_view_profile_frag)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logoutButton: Button = findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val saveButton: ImageView = findViewById(R.id.save_infor_profile_btn)
        saveButton.setOnClickListener {
            if (checker == "clicked") {
                // Handle if any additional actions needed when "clicked"
            } else {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(fullNameEditText.text.toString()) -> {
                Toast.makeText(this, "Please write full name first", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(usernameEditText.text.toString()) -> {
                Toast.makeText(this, "Please write username first", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(bioEditText.text.toString()) -> {
                Toast.makeText(this, "Please write your bio first", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
                val userMap = HashMap<String, Any>()
                userMap["fullname"] = fullNameEditText.text.toString().toLowerCase()
                userMap["username"] = usernameEditText.text.toString().toLowerCase()
                userMap["bio"] = bioEditText.text.toString().toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)
                Toast.makeText(this, "Account information has been updated successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(profileImageView)
                        usernameEditText.setText(user.getUsername())
                        fullNameEditText.setText(user.getFullname())
                        bioEditText.setText(user.getBio())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("AccountSettingsActivity", "userInfo onCancelled: ${databaseError.message}")
            }
        })
    }
}
