package fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.R
import com.google.firebase.firestore.FirebaseFirestore
import utility.Item
import utility.ItemAdapter

class ShopFragment : Fragment() {

    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop, container, false)

        itemsRecyclerView = view.findViewById(R.id.itemsRecyclerView)
        itemAdapter = ItemAdapter(emptyList())
        itemsRecyclerView.layoutManager = LinearLayoutManager(context)
        itemsRecyclerView.adapter = itemAdapter

        database = FirebaseFirestore.getInstance()

        // Buttons for departments
        view.findViewById<Button>(R.id.btnVegetables).setOnClickListener {
            loadItemsForDepartment("Vegetables")
        }
        view.findViewById<Button>(R.id.btnButchery).setOnClickListener {
            loadItemsForDepartment("Butchery")
        }
        view.findViewById<Button>(R.id.btnBakery).setOnClickListener {
            loadItemsForDepartment("Bakery")
        }

        // Load default department on start
        loadItemsForDepartment("Vegetables")

        return view
    }

    private fun loadItemsForDepartment(department: String) {
        database.collection("Items")
            .whereEqualTo("department", department)
            .get()
            .addOnSuccessListener { documents ->
                val items = documents.map { doc ->
                    Item(
                        name = doc.getString("name") ?: "",
                        price = when (val priceValue = doc.get("price")) {
                            is Number -> priceValue.toDouble()
                            else -> 0.0
                        },
                        quantity = when (val quantityValue = doc.get("quantity")) {
                            is Number -> quantityValue.toInt()
                            else -> 0
                        },
                        timesSold = when (val timesSoldValue = doc.get("timesSold")) {
                            is Number -> timesSoldValue.toInt()
                            else -> 0
                        },
                        imageName = doc.getString("imageName") ?: "",
                        department = doc.getString("department") ?: ""
                    )
                }
                Log.d("FirestoreDebug", "Updating Adapter with ${items.size} items")
                itemAdapter.updateItems(items)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "Firestore fetch failed: ", e)
            }
    }

}
