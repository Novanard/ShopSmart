package fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.firestore.FirebaseFirestore
import utility.CartViewModel
import utility.Item
import utility.ItemAdapter

class HomePageFragment : Fragment() {
    private lateinit var shopNowButton: Button
    private lateinit var featuredRecyclerView: RecyclerView
    private lateinit var featuredAdapter: ItemAdapter
    private val database = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)

        shopNowButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(ShopFragment())
        }

        setupFeaturedProducts()
    }

    private fun findViews(view: View) {
        shopNowButton = view.findViewById(R.id.shopNowButton)
        featuredRecyclerView = view.findViewById(R.id.featuredProductsList)
    }

    private fun setupFeaturedProducts() {
        val cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        featuredAdapter = ItemAdapter(emptyList(), isInOrderDetailsFragment = false) { item, quantity ->
            cartViewModel.addItemToCart(item, quantity)
        }

        featuredRecyclerView.adapter = featuredAdapter
        featuredRecyclerView.layoutManager = GridLayoutManager(context, 2)

        loadFeaturedProducts()
    }


    private fun loadFeaturedProducts() {
        database.collection("Items")
            .orderBy("timesSold", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(4)
            .get()
            .addOnSuccessListener { documents ->
                val items = documents.map { doc ->
                    val item = Item(
                        name = doc.getString("name") ?: "",
                        price = (doc.get("price") as? Number)?.toDouble() ?: 0.0,
                        quantity = (doc.get("quantity") as? Number)?.toInt() ?: 0,  // Stock quantity
                        timesSold = (doc.get("timesSold") as? Number)?.toInt() ?: 0,
                        imageName = doc.getString("imageName") ?: "",
                        department = doc.getString("department") ?: ""
                    )
                    Pair(item, item.quantity) // Use actual stock quantity instead of 0
                }
                featuredAdapter.updateItems(items) // Pass updated list to adapter
            }
    }

}
