package fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

    private var userRole: String? = "user" // Default to user

    // Admin buttons (Only used if role == "admin")
    private lateinit var btnOrderReady: Button
    private lateinit var btnOrderShipped: Button
    private lateinit var btnOrderDelivered: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arguments?.let {
            orderId = it.getString("orderId") ?: ""
            userRole = it.getString("role") ?: "user" // Default to "user"
        }

        // Debugging log
        Log.d("OrderDetailsFragment", "Received orderId: $orderId, userRole: $userRole")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // ✅ Load different XML layout based on userRole
        val layoutRes = if (userRole == "admin") R.layout.order_details_admin else R.layout.fragment_order_details
        return inflater.inflate(layoutRes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (orderId.isEmpty()) {
            Log.e("OrderDetailsFragment", "Error: orderId is empty!")
            return
        }

        // Initialize UI elements
        orderItemsRecyclerView = view.findViewById(R.id.orderItemsRecyclerView)
        orderTotal = view.findViewById(R.id.orderTotal)
        orderStatus = view.findViewById(R.id.orderStatus)
        orderTimestamp = view.findViewById(R.id.orderTimestamp)
        btnBackToOrders = view.findViewById(R.id.btnBackToMyOrders)


        if (userRole == "admin") {
            btnOrderReady = view.findViewById(R.id.btnOrderReady)
            btnOrderShipped = view.findViewById(R.id.btnOrderShipped)
            btnOrderDelivered = view.findViewById(R.id.btnOrderDelivered)

            btnOrderReady.setOnClickListener { updateOrderStatus("isReady", true, "Ready") }
            btnOrderShipped.setOnClickListener { updateOrderStatus("isShipped", true, "Shipped") }
            btnOrderDelivered.setOnClickListener { updateOrderStatus("isDelivered", true, "Delivered") }
        }

        // Fetch order details
        fetchOrderDetails()

        btnBackToOrders.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(AdminOrdersFragment())
        }
    }

    private fun fetchOrderDetails() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Orders").document(orderId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("OrderDetailsFragment", "Firestore Document: ${document.data}") // ✅ Debugging log

                    // ✅ Explicitly force Firestore to map correctly
                    val order = document.toObject(Order::class.java)

                    if (order != null) {
                        Log.d("OrderDetailsFragment", "Mapped Order: isReady=${order.isReady}, isShipped=${order.isShipped}, isDelivered=${order.isDelivered}")
                        updateUI(order)
                    } else {
                        Log.e("OrderDetailsFragment", "Failed to map Firestore document to Order object!")
                    }
                } else {
                    Log.e("OrderDetailsFragment", "Order document does not exist!")
                }
            }
            .addOnFailureListener { e ->
                Log.e("OrderDetailsFragment", "Error fetching order details", e)
            }
    }




    private fun updateUI(order: Order) {
        // Create a SpannableString to bold a specific part of a string only
        val totalText = SpannableString("Total: $${order.totalPrice}")
        totalText.setSpan(StyleSpan(Typeface.BOLD), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        orderTotal.text = totalText

        // Status formatting
        Log.d("OrderDetailsFragment", "Updating UI with order: isReady=${order.isReady}, isShipped=${order.isShipped}, isDelivered=${order.isDelivered}")
        val statusText = SpannableString("Status: ${getOrderStatus(order)}")
        statusText.setSpan(StyleSpan(Typeface.BOLD), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        orderStatus.text = statusText
        // Date formatting
        val dateText = SpannableString("Date: ${java.util.Date(order.timestamp)}")
        dateText.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        orderTimestamp.text = dateText

        // Convert order items to the format used by ItemAdapter
        val orderItemsList = order.items.map { Pair(it.item, it.quantity) }

        val orderAdapter = ItemAdapter(orderItemsList, true)
        orderItemsRecyclerView.layoutManager = LinearLayoutManager(context)
        orderItemsRecyclerView.adapter = orderAdapter
    }

    private fun getOrderStatus(order: Order): String {
        return when {
            order.isDelivered -> "Delivered"
            order.isShipped -> "Shipped"
            order.isReady -> "Ready"
            else -> "Processing"
        }
    }


    private fun updateOrderStatus(field: String, value: Boolean, statusText: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Orders").document(orderId)
            .update(field, value)
            .addOnSuccessListener {
                Log.d("OrderDetailsFragment", "Order $field updated to $value")

                // ✅ Update UI immediately
                orderStatus.text = "Status: $statusText"

                Toast.makeText(requireContext(), "Order status updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("OrderDetailsFragment", "Failed to update order status", e)
                Toast.makeText(requireContext(), "Failed to update order status!", Toast.LENGTH_SHORT).show()
            }
    }
}
