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
    private val isInCartFragment: Boolean, // Flag to check if we're in CartFragment
    private val onAddToCartClicked: ((Item, Int) -> Unit)? = null // Optional, only needed in ShopFragment
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view, isInCartFragment, onAddToCartClicked)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Pair<Item, Int>>) {
        items = newItems
        notifyDataSetChanged()  // Notify RecyclerView to refresh
    }

    class ItemViewHolder(
        itemView: View,
        private val isInCartFragment: Boolean,
        private val onAddToCartClicked: ((Item, Int) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        private val priceTextView: TextView = itemView.findViewById(R.id.itemPrice)
        private val quantityTextView: TextView = itemView.findViewById(R.id.itemQuantity)
        private val itemImageView: ImageView = itemView.findViewById(R.id.itemImage)

        // These views will only be used in ShopFragment
        private val quantityInput: EditText = itemView.findViewById(R.id.quantityInput)
        private val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)

        fun bind(cartItem: Pair<Item, Int>) {
            nameTextView.text = cartItem.first.name
            priceTextView.text = "Price: $${cartItem.first.price}"
            quantityTextView.text = "Quantity: ${cartItem.second}"

            val context = itemView.context
            Glide.with(context)
                .load(cartItem.first.imageName) // Assuming it's a URL
                .placeholder(R.drawable.shopsmart_transparent) // Placeholder image
                .error(R.drawable.shopsmart_transparent) // Fallback image if load fails
                .into(itemImageView)

            // If we are in the CartFragment, we don't show quantity input and add to cart button
            if (isInCartFragment) {
                quantityInput.visibility = View.GONE
                addToCartButton.visibility = View.GONE
            } else {
                // If we are in the ShopFragment, show quantity input and add to cart button
                addToCartButton.setOnClickListener {
                    val quantityText = quantityInput.text.toString()
                    val quantity = quantityText.toIntOrNull() ?: 0

                    if (quantity > 0 && onAddToCartClicked != null) {
                        onAddToCartClicked?.let { it1 -> it1(cartItem.first, quantity) }
                    } else {
                        quantityInput.error = "Enter a valid quantity"
                    }
                }
            }
        }
    }
}
