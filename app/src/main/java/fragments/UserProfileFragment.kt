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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Fetch user role and setup UI
        val userId = auth.currentUser?.uid
        if (userId != null) {
            fetchUserRole(userId, view)
        } else {
            setupUI(view, "user") // Default to user role if user is not authenticated
        }

        return view
    }

    private fun fetchUserRole(userId: String, view: View) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")?.trim()?.lowercase() ?: "user"
                Log.d("UserProfile", "User role retrieved: $role")

                requireActivity().runOnUiThread {
                    if (role == "admin") {
                        // Redirect to AdminPageFragment
                        (activity as? MainActivity)?.loadFragment(AdminPageFragment())
                    } else {
                        // Setup UI for regular user
                        setupUI(view, "user")
                    }
                }
            }
            .addOnFailureListener {
                Log.e("UserProfile", "Failed to fetch user role, defaulting to user profile.")
                requireActivity().runOnUiThread {
                    setupUI(view, "user")
                }
            }
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
                (activity as? MainActivity)?.loadFragment(EditProfileFragment())
            }

            logoutButton.setOnClickListener {
                logoutUser()
            }
        }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.loadFragment(LoginFragment())
    }
}