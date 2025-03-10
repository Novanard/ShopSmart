package utility

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopsmart.R

class ItemAdapter(
    private var items: List<Pair<Item, Int>>,
    private val isInOrderDetailsFragment: Boolean,  // To check if we are in OrderDetailsFragment
    private val onAddToCartClicked: ((Item, Int) -> Unit)? = null  // Lambda for ShopFragment
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Inflate different layout depending on the fragment
        val layoutRes = if (isInOrderDetailsFragment) {
            R.layout.item_layout_order_details  // Layout with Price, Quantity, and Total for OrderDetailsFragment
        } else {
            R.layout.item_layout  // Regular layout for ShopFragment
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ItemViewHolder(view, isInOrderDetailsFragment, onAddToCartClicked)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Pair<Item, Int>>) {
        items = newItems
        notifyDataSetChanged()  // Refresh RecyclerView
    }

    class ItemViewHolder(
        itemView: View,
        private val isInOrderDetailsFragment: Boolean,
        private val onAddToCartClicked: ((Item, Int) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        private val priceTextView: TextView = itemView.findViewById(R.id.itemPrice)
        private val quantityTextView: TextView = itemView.findViewById(R.id.itemQuantity)
        private val itemImageView: ImageView = itemView.findViewById(R.id.itemImage)
        private val totalTextView: TextView? = itemView.findViewById(R.id.itemTotal) // Will be used only in OrderDetailsFragment

        fun bind(cartItem: Pair<Item, Int>) {
            nameTextView.text = cartItem.first.name
            priceTextView.text = "Price: $${cartItem.first.price}"
            quantityTextView.text = "Qty: ${cartItem.second}"

            // Load image using Glide or similar
            Glide.with(itemView.context)
                .load(cartItem.first.imageName)  // Assuming the image name or URL
                .placeholder(R.drawable.shopsmart_transparent)
                .into(itemImageView)

            // If in OrderDetailsFragment, calculate and display total price
            if (isInOrderDetailsFragment) {
                val total = cartItem.first.price * cartItem.second
                totalTextView?.text = "Total: $${total}"  // Show total price only in OrderDetailsFragment
            }

            // Handle Add to Cart button only in the ShopFragment
            if (!isInOrderDetailsFragment) {
                itemView.findViewById<Button>(R.id.addToCartButton)?.setOnClickListener {
                    val quantityText = itemView.findViewById<EditText>(R.id.quantityInput)?.text.toString()
                    val quantity = quantityText.toIntOrNull() ?: 0

                    if (quantity > 0 && onAddToCartClicked != null) {
                        onAddToCartClicked?.let { it1 -> it1(cartItem.first, quantity) }
                    } else {
                        itemView.findViewById<EditText>(R.id.quantityInput)?.error = "Enter a valid quantity"
                    }
                }
            }
        }
    }
}
