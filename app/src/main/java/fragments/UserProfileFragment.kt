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
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val myOrdersButton: Button = view.findViewById(R.id.btnMyOrders)
        val uploadPhotoButton: Button = view.findViewById(R.id.btnUploadPhoto)
        val logoutButton: Button = view.findViewById(R.id.btnLogout)

        val user = auth.currentUser
        userName.text = user?.displayName ?: "User"

        val imageUrl = user?.photoUrl
        Log.d("UserProfile", "Loaded user profile URL: $imageUrl")

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

        checkUserRole()

        return view
    }


    private fun checkUserRole() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "undefined"
                    Log.d("UserProfile", "Retrieved user role: $role")  // Log the actual value

                    if (role.trim().lowercase() == "admin") {  // Ensure no whitespace issues
                        Log.d("UserProfile", "User is admin, redirecting to AdminPageFragment")
                        (activity as? MainActivity)?.loadFragment(AdminPageFragment())
                    } else {
                        Log.d("UserProfile", "User is NOT admin, staying on profile page")
                    }
                } else {
                    Log.e("UserProfile", "User document does not exist!")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserProfile", "Error fetching user role: ${exception.message}")
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
            Log.d("UserProfile", "Uploading image for userId: $userId")

            if (userId == null) {
                Log.e("UserProfile", "User is not authenticated!")
                Toast.makeText(requireContext(), "User is not authenticated!", Toast.LENGTH_SHORT).show()
                return
            }

            val fileName = UUID.randomUUID().toString()
            val imageRef = storageRef.child("profile_pictures/$userId/$fileName")

            Log.d("UserProfile", "Uploading image to path: profile_pictures/$userId/$fileName")

            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("UserProfile", "Image uploaded successfully. Image URL: $uri")

                        val user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            photoUri = uri
                        }

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("UserProfile", "Profile photo updated successfully")
                                    Toast.makeText(requireContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show()
                                    Glide.with(requireContext())
                                        .load(uri)
                                        .into(requireView().findViewById(R.id.profileImage))
                                } else {
                                    Log.e("UserProfile", "Failed to update profile photo: ${task.exception?.message}")
                                    Toast.makeText(requireContext(), "Failed to update profile photo!", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("UserProfile", "Error uploading profile image: ${exception.message}")
                    Toast.makeText(requireContext(), "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        auth.signOut()
        Log.d("UserProfile", "User logged out successfully")
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.loadFragment(LoginFragment())
    }
}
