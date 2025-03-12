package fragments

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
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var isAdminRedirected = false  // Prevents UI setup if redirected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        if (userId != null) {
            fetchUserRole(userId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (isAdminRedirected) {
            null // Prevents inflating the layout if redirected
        } else {
            inflater.inflate(R.layout.fragment_user_profile, container, false)
        }
    }

    private fun fetchUserRole(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")?.trim()?.lowercase() ?: "user"
                Log.d("UserProfile", "User role retrieved: $role")

                if (role == "admin") {
                    isAdminRedirected = true
                    requireActivity().runOnUiThread {
                        (activity as? MainActivity)?.loadFragment(AdminPageFragment())
                    }
                } else {
                    isAdminRedirected = false // Normal user, allow profile page to load
                }
            }
            .addOnFailureListener {
                Log.e("UserProfile", "Failed to fetch user role, defaulting to user profile.")
                isAdminRedirected = false // Allow profile page to load
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isAdminRedirected) {
            setupUI(view)
        }
    }

    private fun setupUI(view: View) {
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
            (activity as? MainActivity)?.loadFragment(EditProfileFragment())
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.loadFragment(LoginFragment())
    }
}