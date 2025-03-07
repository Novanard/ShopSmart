package utility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.R

class CartAdapter(
    private var items: List<Pair<Item, Int>>,
    private val onDeleteClicked: (Item) -> Unit // Lambda to handle delete clicks
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemImage: ImageView = view.findViewById(R.id.itemImage)
        private val itemName: TextView = view.findViewById(R.id.itemName)
        private val itemPrice: TextView = view.findViewById(R.id.itemPrice)
        private val itemQuantity: TextView = view.findViewById(R.id.itemQuantity)
        private val deleteButton: Button = view.findViewById(R.id.deleteButton)

        fun bind(item: Pair<Item, Int>) {
            // Bind data to views
            itemName.text = item.first.name
            itemPrice.text = "Price: $${item.first.price}"
            itemQuantity.text = "Qty: ${item.second}"

            // Load product image
            val context = itemView.context
            val resId = context.resources.getIdentifier(item.first.imageName, "drawable", context.packageName)
            if (resId != 0) {
                itemImage.setImageResource(resId)
            } else {
                itemImage.setImageResource(R.drawable.shopsmart_transparent) // Default placeholder
            }

            // Handle delete button click
            deleteButton.setOnClickListener {
                onDeleteClicked(item.first)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    // Update the items list
    fun updateItems(newItems: List<Pair<Item, Int>>) {
        items = newItems
        notifyDataSetChanged() // Refresh the RecyclerView
    }
}