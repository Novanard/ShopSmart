package fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R

class UserProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val userName: TextView = view.findViewById(R.id.userName)
        val myOrdersButton: Button = view.findViewById(R.id.btnMyOrders)
        val uploadPhotoButton: Button = view.findViewById(R.id.btnUploadPhoto)
        val logoutButton: Button = view.findViewById(R.id.btnLogout)

        // Dummy data (Replace with actual user data)
        userName.text = "John Doe"

        myOrdersButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(MyOrdersFragment())
        }

        uploadPhotoButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(LoginFragment())
        }

        logoutButton.setOnClickListener {
            // TODO: Handle logout logic
        }

        return view
    }
}
