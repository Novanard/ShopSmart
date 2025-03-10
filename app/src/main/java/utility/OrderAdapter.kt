package utility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fragments.OrderDetailsFragment
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private var orders: List<Order>) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.orderId)
        val orderTotal: TextView = itemView.findViewById(R.id.orderTotal)
        val orderTimestamp: TextView = itemView.findViewById(R.id.orderTimestamp)
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        val btnViewOrder: Button = itemView.findViewById(R.id.btnViewOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        // Set Order Details
        holder.orderId.text = "Order ID: ${order.orderId}"
        holder.orderTotal.text = "Total: $${order.totalPrice}"
        holder.orderTimestamp.text = "Date: ${formatTimestamp(order.timestamp)}"
        holder.orderStatus.text = "Status: ${buildOrderStatus(order)}"

        // Handle "View Order" button click
        holder.btnViewOrder.setOnClickListener {
            val activity = holder.itemView.context as? FragmentActivity
            if (activity != null) {
                checkUserRoleAndOpenFragment(activity, order.orderId)
            }
        }
    }

    override fun getItemCount() = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    private fun buildOrderStatus(order: Order): String {
        return when {
            order.isDelivered -> "Delivered"
            order.isShipped -> "Shipped"
            order.isReady -> "Ready"
            else -> "Processing"
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


    private fun checkUserRoleAndOpenFragment(activity: FragmentActivity, orderId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "user" // Default to "user"
                openOrderDetailsFragment(activity, orderId, role)
            }
            .addOnFailureListener {
                openOrderDetailsFragment(activity, orderId, "user") // Default to user view on failure
            }
    }

    private fun openOrderDetailsFragment(activity: FragmentActivity, orderId: String, role: String) {
        val fragment = OrderDetailsFragment()
        val bundle = Bundle()
        bundle.putString("orderId", orderId)
        bundle.putString("role", role)
        fragment.arguments = bundle

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
