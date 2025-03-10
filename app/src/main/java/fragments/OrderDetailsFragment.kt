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
            // Create a SpannableString to bold a specific part of a string only
        val totalText = SpannableString("Total: $${order.totalPrice}")
        totalText.setSpan(StyleSpan(Typeface.BOLD), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        orderTotal.text = totalText

        // Same for Status
        val statusText = SpannableString("Status: ${if (order.isDelivered) "Delivered" else "Processing"}")
        statusText.setSpan(StyleSpan(Typeface.BOLD), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        orderStatus.text = statusText

        // Same for Date
        val dateText = SpannableString("Date: ${java.util.Date(order.timestamp)}")
        dateText.setSpan(StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        orderTimestamp.text = dateText


        // Convert order items to the format used by ItemAdapter
        val orderItemsList = order.items.map { Pair(it.item, it.quantity) }

        val orderAdapter = ItemAdapter(orderItemsList, true)

        orderItemsRecyclerView.layoutManager = LinearLayoutManager(context)
        orderItemsRecyclerView.adapter = orderAdapter
    }

}
