package com.instagramcloneapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.mcb.instagramclone.AccountSettingsActivity
import com.mcb.instagramclone.Model.User
import com.mcb.instagramclone.R
import com.mcb.instagramclone.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        profileId = pref?.getString("profileId", "none") ?: "none"

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (profileId == firebaseUser.uid) {
            binding.editAccountSettingsBtn.text = "Edit Profile"
        } else {
            checkFollowAndFollowingButtonStatus()
        }

        binding.editAccountSettingsBtn.setOnClickListener {
            val buttonText = binding.editAccountSettingsBtn.text.toString()
            when (buttonText) {
                "Edit Profile" -> startActivity(Intent(requireContext(), AccountSettingsActivity::class.java))
                "Follow" -> followUser()
                "Following" -> unfollowUser()
            }
        }

        getFollowers()
        getFollowings()
        userInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser.uid.let { uid ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(uid)
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(profileId).exists()) {
                    binding.editAccountSettingsBtn.text = "Following"
                } else {
                    binding.editAccountSettingsBtn.text = "Follow"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ProfileFragment", "checkFollowAndFollowingButtonStatus onCancelled: ${databaseError.message}")
            }
        })
    }

    private fun followUser() {
        val currentUser = firebaseUser.uid
        val followRef = FirebaseDatabase.getInstance().reference.child("Follow")

        followRef.child(currentUser).child("Following").child(profileId).setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                followRef.child(profileId).child("Followers").child(currentUser).setValue(true).addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        Log.d("ProfileFragment", "Successfully followed user")
                    } else {
                        Log.e("ProfileFragment", "Failed to follow user: ${task2.exception?.message}")
                    }
                }
            } else {
                Log.e("ProfileFragment", "Failed to follow user: ${task.exception?.message}")
            }
        }
    }

    private fun unfollowUser() {
        val currentUser = firebaseUser.uid
        val followRef = FirebaseDatabase.getInstance().reference.child("Follow")

        followRef.child(currentUser).child("Following").child(profileId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                followRef.child(profileId).child("Followers").child(currentUser).removeValue().addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        Log.d("ProfileFragment", "Successfully unfollowed user")
                    } else {
                        Log.e("ProfileFragment", "Failed to unfollow user: ${task2.exception?.message}")
                    }
                }
            } else {
                Log.e("ProfileFragment", "Failed to unfollow user: ${task.exception?.message}")
            }
        }
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    binding.totalFollowers.text = dataSnapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ProfileFragment", "getFollowers onCancelled: ${databaseError.message}")
            }
        })
    }

    private fun getFollowings() {
        val followingsRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    binding.totalFollowing.text = dataSnapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ProfileFragment", "getFollowings onCancelled: ${databaseError.message}")
            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(binding.proImageProfileFrag)
                        binding.profileFragmentUsername.text = user.getUsername()
                        binding.fullNameProfileFrag.text = user.getFullname()
                        binding.bioProfileFrag.text = user.getBio()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ProfileFragment", "userInfo onCancelled: ${databaseError.message}")
            }
        })
    }

    override fun onStop() {
        super.onStop()
        saveProfileId()
    }

    override fun onPause() {
        super.onPause()
        saveProfileId()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveProfileId()
    }

    private fun saveProfileId() {
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }
}
