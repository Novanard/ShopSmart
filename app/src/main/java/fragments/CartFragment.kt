package fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import utility.CartAdapter
import utility.CartViewModel
import utility.Order
import utility.OrderItem

class CartFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartTotalPriceTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if the user is logged in
        if (!userIsLoggedIn()) {
            navigateToLoginFragment()
            return
        }

        // Check if the user is an admin
        checkIfUserIsAdmin { isAdmin ->
            if (isAdmin) {
                // Redirect admin users to the AdminPanelFragment
                (activity as? MainActivity)?.loadFragment(AdminPageFragment())
                return@checkIfUserIsAdmin
            }

            // Proceed with the cart functionality for non-admin users
            setupCart(view)
        }
    }

    private fun setupCart(view: View) {
        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        cartTotalPriceTextView = view.findViewById(R.id.cartTotalPrice)

        cartAdapter = CartAdapter(emptyList()) { item ->
            cartViewModel.removeItemFromCart(item)
            updateTotalCartPrice()
        }
        cartRecyclerView.adapter = cartAdapter
        cartRecyclerView.layoutManager = GridLayoutManager(context, 2)

        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
            cartAdapter.updateItems(cartItems)
            updateTotalCartPrice()
        })

        val checkoutButton = view.findViewById<Button>(R.id.checkoutButton)
        checkoutButton.setOnClickListener {
            initiateCheckout()
        }
    }

    private fun updateTotalCartPrice() {
        val totalPrice = cartViewModel.cartItems.value?.sumOf { it.first.price * it.second } ?: 0.0
        cartTotalPriceTextView.text = "Total Cart: $${String.format("%.2f", totalPrice)}"
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

    private fun navigateToMyOrdersFragment() {
        (activity as? MainActivity)?.loadFragment(MyOrdersFragment())
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

            val orderItems = cartItems.map { (item, quantity) ->
                OrderItem(item = item, quantity = quantity)
            }

            val order = Order(
                userId = email,
                items = orderItems,
                totalPrice = totalPrice,
                timestamp = System.currentTimeMillis(),
                isReady = false,
                isShipped = false,
                isDelivered = false
            )

            sendOrderToFirestore(order)
            navigateToMyOrdersFragment()
        }
    }

    private fun getCurrentUserEmail(callback: (String?) -> Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            callback(null)
            return
        }
        callback(firebaseUser.email)
    }

    private fun sendOrderToFirestore(order: Order) {
        val db = FirebaseFirestore.getInstance()
        val ordersCollection = db.collection("Orders")

        val orderData = hashMapOf(
            "userId" to order.userId,
            "items" to order.items.map {
                mapOf(
                    "item" to hashMapOf(
                        "name" to it.item.name,
                        "price" to it.item.price,
                        "quantity" to it.item.quantity,
                        "timesSold" to it.item.timesSold,
                        "department" to it.item.department,
                        "imageName" to it.item.imageName
                    ),
                    "quantity" to it.quantity
                )
            },
            "totalPrice" to order.totalPrice,
            "timestamp" to order.timestamp,
            "isReady" to order.isReady,
            "isShipped" to order.isShipped,
            "isDelivered" to order.isDelivered
        )

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

    private fun checkIfUserIsAdmin(callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(false)
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "user"
                callback(role == "admin")
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}