package fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import utility.CartAdapter
import utility.CartViewModel
import utility.ItemAdapter
import utility.Order

class CartFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartViewModel: CartViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if the user is logged in
        if (!userIsLoggedIn()) {
            // Redirect to LoginFragment if the user is not logged in
            navigateToLoginFragment()
            return
        }

        // Initialize RecyclerView and Adapter
        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        cartAdapter = CartAdapter(emptyList()) { item ->
            cartViewModel.removeItemFromCart(item)
        }
        cartRecyclerView.adapter = cartAdapter
        cartRecyclerView.layoutManager = GridLayoutManager(context, 2)

        // Initialize CartViewModel
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        // Observe cartItems LiveData
        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
            cartAdapter.updateItems(cartItems)
        })

        // Initialize Checkout Button and set click listener
        val checkoutButton = view.findViewById<Button>(R.id.checkoutButton)
        checkoutButton.setOnClickListener {
            initiateCheckout() // Call the checkout function when the button is clicked
        }
    }
    private fun initiateCheckout() {
        if (userIsLoggedIn()) {
            Log.d("CartFragment", "User is logged in, proceeding to checkout")
            proceedToCheckout()
        } else {
            Log.d("CartFragment", "User is not logged in, navigating to login fragment")
            navigateToLoginFragment()
        }
    }

    private fun userIsLoggedIn(): Boolean {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser != null
    }

    private fun navigateToLoginFragment() {
        (activity as? MainActivity)?.loadFragment(LoginFragment())
    }
    private fun proceedToCheckout() {
        getCurrentUserEmail { email ->
            if (email == null) {
                Log.e("CartFragment", "User email not found")
                Toast.makeText(requireContext(), "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
                return@getCurrentUserEmail
            }

            val cartItems = cartViewModel.cartItems.value ?: emptyList()
            val totalPrice = cartItems.sumOf { it.first.price * it.second }

            // Create an Order object
            val order = Order(
                userId = email, // Use the user's email instead of uid
                items = cartItems,
                totalPrice = totalPrice,
                timestamp = System.currentTimeMillis()
            )

            sendOrderToFirestore(order)
        }
    }
    private fun getCurrentUserId(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser?.uid
    }
    private fun getCurrentUserEmail(callback: (String?) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            callback(null) // User is not authenticated
            return
        }
        val email = firebaseUser.email
        callback(email)
    }
    private fun sendOrderToFirestore(order: Order) {
        val db = FirebaseFirestore.getInstance()
        val ordersCollection = db.collection("Orders")
        val orderData = hashMapOf(
            "userId" to order.userId,
            "items" to order.items.map { it.first.name to it.second }, // Convert items to a map of itemId to quantity
            "totalPrice" to order.totalPrice,
            "timestamp" to order.timestamp
        )

        // Add the order to Firestore
        ordersCollection.add(orderData)
            .addOnSuccessListener { documentReference ->
                Log.d("CartFragment", "Order sent to Firestore with ID: ${documentReference.id}")
                cartViewModel.clearCart()
                Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("CartFragment", "Error sending order to Firestore", e)
                Toast.makeText(requireContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show()
            }

    }
}











