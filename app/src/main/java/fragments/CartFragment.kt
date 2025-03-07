package fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.R
import utility.CartViewModel
import utility.ItemAdapter

class CartFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: ItemAdapter
    private lateinit var cartViewModel: CartViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("OnVIEWCREATED", "REACHED SUCCESSFULLY")
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        cartAdapter = ItemAdapter(emptyList(), isInCartFragment = true)
        cartRecyclerView.adapter = cartAdapter
        cartRecyclerView.layoutManager = GridLayoutManager(context, 2)

        // Initialize CartViewModel
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)
        Log.d("CartFragment", "CartViewModel initialized: $cartViewModel")

        // Observe cartItems LiveData
        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
            Log.d("CartFragment", "Fetched cart items: ${cartItems.size}")
            cartAdapter.updateItems(cartItems)
        })
    }
}











