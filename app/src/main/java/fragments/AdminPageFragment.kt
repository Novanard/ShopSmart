package fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R

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

        btnViewOrders.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(AdminOrdersFragment())
        }
    }
}
