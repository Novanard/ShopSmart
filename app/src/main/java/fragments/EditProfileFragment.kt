package fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class EditProfileFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnUploadImage: Button
    private lateinit var btnSave: Button

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                ivProfileImage.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        etName = view.findViewById(R.id.etName)
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        btnUploadImage = view.findViewById(R.id.btnUploadImage)
        btnSave = view.findViewById(R.id.btnSave)

        // Load current user data
        loadCurrentUserData()

        // Set up image upload button
        btnUploadImage.setOnClickListener {
            openImagePicker()
        }

        // Set up save button
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isNotEmpty() || imageUri != null) {
                updateProfile(newName)
            } else {
                Toast.makeText(requireContext(), "No changes made", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCurrentUserData() {
        val user = auth.currentUser
        user?.let {
            etName.setText(it.displayName ?: "")
            it.photoUrl?.let { uri ->
                Glide.with(requireContext())
                    .load(uri)
                    .into(ivProfileImage)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun updateProfile(newName: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            // Upload new profile picture
            uploadProfileImage { imageUrl ->
                val profileUpdates = userProfileChangeRequest {
                    displayName = newName
                    photoUri = Uri.parse(imageUrl)
                }
                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            (activity as? MainActivity)?.loadFragment(UserProfileFragment())
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            // Only update the name
            val profileUpdates = userProfileChangeRequest {
                displayName = newName
            }
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Name updated successfully", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.loadFragment(UserProfileFragment())
                    } else {
                        Toast.makeText(requireContext(), "Failed to update name", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun uploadProfileImage(onSuccess: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val fileName = UUID.randomUUID().toString()
        val imageRef = storage.reference.child("profile_pictures/$userId/$fileName")

        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                }
        }
    }
}