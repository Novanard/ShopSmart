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

class HomePageFragment : Fragment() {
    private lateinit var shopNowButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        val user = FirebaseAuth.getInstance().currentUser
        shopNowButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(ShopFragment())
        }
    }
     private   fun findViews(view: View) {
            shopNowButton = view.findViewById(R.id.shopNowButton)
        }
    }

