package fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.firestore.FirebaseFirestore
import utility.Order
import utility.OrderAdapter

class AdminOrdersFragment : Fragment() {

    private lateinit var recyclerViewOrders: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var orderList: MutableList<Order>
    private lateinit var adminPageButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders)
        recyclerViewOrders.layoutManager = LinearLayoutManager(requireContext())
        adminPageButton=view.findViewById(R.id.btnBackToAdmin)
        orderList = mutableListOf()
        orderAdapter = OrderAdapter(orderList)
        recyclerViewOrders.adapter = orderAdapter

        fetchAllOrders()
        adminPageButton.setOnClickListener{
            (activity as MainActivity).loadFragment((AdminPageFragment()))
        }
    }

    private fun fetchAllOrders() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Orders")
            .get()
            .addOnSuccessListener { documents ->
                orderList.clear()
                for (document in documents) {
                    try {
                        val order = document.toObject(Order::class.java)
                        order.orderId = document.id
                        orderList.add(order)
                    } catch (e: Exception) {
                        Log.e("AdminOrders", "Error parsing order: ${e.message}")
                    }
                }
                orderAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("AdminOrders", "Failed to fetch orders: ${e.message}")
                Toast.makeText(requireContext(), "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
