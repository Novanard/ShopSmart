package fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.firestore.FirebaseFirestore
import utility.ItemAdapter
import utility.Order

class OrderDetailsFragment : Fragment() {

    private lateinit var orderId: String
    private lateinit var orderItemsRecyclerView: RecyclerView
    private lateinit var orderTotal: TextView
    private lateinit var orderStatus: TextView
    private lateinit var orderTimestamp: TextView
    private lateinit var btnBackToOrders: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get orderId from arguments
        orderId = arguments?.getString("orderId") ?: ""

        // Debugging log
        Log.d("OrderDetailsFragment", "Received orderId: $orderId")

        if (orderId.isEmpty()) {
            Log.e("OrderDetailsFragment", "Error: orderId is empty!")
            return
        }

        // Initialize UI elements
        orderItemsRecyclerView = view.findViewById(R.id.orderItemsRecyclerView)
        orderTotal = view.findViewById(R.id.orderTotal)
        orderStatus = view.findViewById(R.id.orderStatus)
        orderTimestamp = view.findViewById(R.id.orderTimestamp)
        btnBackToOrders= view.findViewById(R.id.btnBackToMyOrders)

        // Fetch order details
        fetchOrderDetails()
        btnBackToOrders.setOnClickListener{
            (activity as? MainActivity)?.loadFragment(MyOrdersFragment())
        }
    }

    private fun fetchOrderDetails() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Orders").document(orderId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val order = document.toObject(Order::class.java)
                    order?.let {
                        updateUI(it)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("OrderDetailsFragment", "Error fetching order details", e)
            }
    }

    private fun updateUI(order: Order) {
        orderTotal.text = "Total: $${order.totalPrice}"
        orderStatus.text = "Status: ${if (order.isDelivered) "Delivered" else "Processing"}"
        orderTimestamp.text = "Date: ${java.util.Date(order.timestamp)}"

        // Convert order items to the format used by ItemAdapter
        val orderItemsList = order.items.map { Pair(it.item, it.quantity) }

        val orderAdapter = ItemAdapter(orderItemsList, true)

        orderItemsRecyclerView.adapter = orderAdapter
        orderItemsRecyclerView.layoutManager = GridLayoutManager(context,2)
    }

}
