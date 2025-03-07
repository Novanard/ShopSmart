package utility

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.R

class OrderAdapter(private var orders: List<Order>) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.orderId)
        val orderTotal: TextView = itemView.findViewById(R.id.orderTotal)
        val orderTimestamp: TextView = itemView.findViewById(R.id.orderTimestamp)
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderId.text = "Order ID: ${order.timestamp}"
        holder.orderTotal.text = "Total: $${order.totalPrice}"
        holder.orderTimestamp.text = "Date: ${java.util.Date(order.timestamp)}"
        holder.orderStatus.text = "Status: ${buildOrderStatus(order)}"
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
}
