package fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.R
import com.google.firebase.firestore.FirebaseFirestore
import utility.CartViewModel
import utility.Item
import utility.ItemAdapter

class ShopFragment : Fragment() {

    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var database: FirebaseFirestore
    private lateinit var cartViewModel: CartViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop, container, false)

        itemsRecyclerView = view.findViewById(R.id.itemsRecyclerView)
        itemAdapter = ItemAdapter(emptyList(), isInCartFragment = false) { item, quantity ->
            cartViewModel.addItemToCart(item, quantity)
        }
        itemsRecyclerView.adapter = itemAdapter
        itemsRecyclerView.layoutManager = GridLayoutManager(context, 2)

        // Initialize Firestore and ViewModel
        database = FirebaseFirestore.getInstance()
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        // Load items for default department on start
        loadItemsForDepartment("Vegetables")

        // Find department buttons
        val btnVegetables = view.findViewById<Button>(R.id.btnVegetables)
        val btnButchery = view.findViewById<Button>(R.id.btnButchery)
        val btnBakery = view.findViewById<Button>(R.id.btnBakery)
        val btnHomeTools = view.findViewById<Button>(R.id.btnHomeTools)

        // Set click listeners to load the correct department's items
        btnVegetables.setOnClickListener { loadItemsForDepartment("Vegetables") }
        btnButchery.setOnClickListener { loadItemsForDepartment("Butchery") }
        btnBakery.setOnClickListener { loadItemsForDepartment("Bakery") }
        btnHomeTools.setOnClickListener { loadItemsForDepartment("HomeTools") }


        return view
    }

// Function to load items from Firestore for a specific department
    private fun loadItemsForDepartment(department: String) {
        val items = mutableListOf<Pair<Item, Int>>() // A list of Pair<Item, Int>

        database.collection("Items")
            .whereEqualTo("department", department)
            .get()
            .addOnSuccessListener { documents ->
                // Convert Firestore documents into Item objects and pair them with a quantity (defaulting to 0)
                documents.forEach { doc ->
                    val item = Item(
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

                    // Add the Item to the list paired with a quantity (in this case, defaulting to quantity 0)
                    items.add(Pair(item, 0))  // You can replace '0' with any actual quantity if available
                }

                // Now that items are properly paired with quantities, update the adapter
                Log.d("FirestoreDebug", "Updating Adapter with ${items.size} items")
                itemAdapter.updateItems(items) // Update the RecyclerView adapter
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "Firestore fetch failed: ", e)
            }
    }

}

