package fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import utility.Order
import utility.OrderAdapter

class MyOrdersFragment : Fragment() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView)
        orderAdapter = OrderAdapter(emptyList())
        ordersRecyclerView.adapter = orderAdapter
        ordersRecyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch and display the user's orders
        fetchUserOrders()
    }

    private fun fetchUserOrders() {
        val userId = FirebaseAuth.getInstance().currentUser?.email
        if (userId == null) {
            Log.e("MyOrdersFragment", "User is not logged in")
            return
        }

        Log.d("MyOrdersFragment", "Fetching orders for user: $userId")

        val db = FirebaseFirestore.getInstance()
        db.collection("Orders")
            .whereEqualTo("userId", userId) // Fetch orders for the current user
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("MyOrdersFragment", "No orders found for user: $userId")
                } else {
                    val orders = documents.toObjects(Order::class.java)
                    Log.d("MyOrdersFragment", "Fetched ${orders.size} orders: $orders")
                    orderAdapter.updateOrders(orders)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MyOrdersFragment", "Error fetching orders", e)
            }
    }
}