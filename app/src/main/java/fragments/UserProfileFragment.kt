package fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class UserProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private val db = FirebaseFirestore.getInstance()

    private val PICK_IMAGE_REQUEST = 71
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return inflater.inflate(R.layout.fragment_user_profile, container, false)

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")?.trim()?.lowercase() ?: "user"
                Log.d("UserProfile", "User role retrieved: $role")

                requireActivity().runOnUiThread {
                    if (role == "admin") {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, AdminPageFragment()) // ✅ Fully replace the fragment
                            .commit()
                    } else {
                        // ✅ If user, just load the normal profile view
                        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
                        setupUI(view, "user")
                        container?.removeAllViews()
                        container?.addView(view)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("UserProfile", "Failed to fetch user role, defaulting to user profile.")
                requireActivity().runOnUiThread {
                    val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
                    setupUI(view, "user")
                    container?.removeAllViews()
                    container?.addView(view)
                }
            }

        return null
    }


    private fun setupUI(view: View, role: String) {
        if (role == "admin") {
            val btnViewOrders = view.findViewById<Button>(R.id.btnViewOrders)
            btnViewOrders.setOnClickListener {
                (activity as? MainActivity)?.loadFragment(AdminOrdersFragment())
            }
        } else {
            val profileImage = view.findViewById<ImageView>(R.id.profileImage)
            val userName = view.findViewById<TextView>(R.id.userName)
            val myOrdersButton = view.findViewById<Button>(R.id.btnMyOrders)
            val uploadPhotoButton = view.findViewById<Button>(R.id.btnUploadPhoto)
            val logoutButton = view.findViewById<Button>(R.id.btnLogout)

            val user = auth.currentUser
            userName.text = user?.displayName ?: "User"

            val imageUrl = user?.photoUrl
            if (imageUrl != null) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.blank_profile_img)
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.blank_profile_img)
            }

            myOrdersButton.setOnClickListener {
                (activity as? MainActivity)?.loadFragment(MyOrdersFragment())
            }

            uploadPhotoButton.setOnClickListener {
                openImagePicker()
            }

            logoutButton.setOnClickListener {
                logoutUser()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            imageUri = data?.data
            val profileImage: ImageView = requireView().findViewById(R.id.profileImage)
            profileImage.setImageURI(imageUri)

            uploadProfileImageToFirebase()
        }
    }

    private fun uploadProfileImageToFirebase() {
        if (imageUri != null) {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(requireContext(), "User is not authenticated!", Toast.LENGTH_SHORT).show()
                return
            }

            val fileName = UUID.randomUUID().toString()
            val imageRef = storageRef.child("profile_pictures/$userId/$fileName")

            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            photoUri = uri
                        }

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(requireContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show()
                                    Glide.with(requireContext())
                                        .load(uri)
                                        .into(requireView().findViewById(R.id.profileImage))
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update profile photo!", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error uploading image!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.loadFragment(LoginFragment())
    }
}
