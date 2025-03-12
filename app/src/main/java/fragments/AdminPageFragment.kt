package fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth

class AdminPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnViewOrders: Button = view.findViewById(R.id.btnViewOrders)
        val btnAddProduct: Button = view.findViewById(R.id.btnAddProduct)
        val btnLogOut: Button = view.findViewById(R.id.btnLogOut)

        btnViewOrders.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(AdminOrdersFragment())
        }

        btnLogOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // Navigate back to the login after logging out
            (activity as? MainActivity)?.loadFragment(LoginFragment())
        }

        btnAddProduct.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(FragmentAddProduct())
        }
    }
}