package utility

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
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
    private val isInOrderDetailsFragment: Boolean,
    private val onAddToCartClicked: ((Item, Int) -> Unit)? = null
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutRes = if (isInOrderDetailsFragment) {
            R.layout.item_layout_order_details  // Layout for OrderDetailsFragment
        } else {
            R.layout.item_layout  // Layout for ShopFragment
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ItemViewHolder(view, isInOrderDetailsFragment, onAddToCartClicked)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Pair<Item, Int>>) {
        items = newItems
        notifyDataSetChanged()
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
        private val totalTextView: TextView? = itemView.findViewById(R.id.itemTotal) // Used in OrderDetailsFragment

        fun bind(cartItem: Pair<Item, Int>) {
            val item = cartItem.first
            val quantity = cartItem.second

            nameTextView.text = item.name
            priceTextView.text = boldText("Price: ", "$${item.price}")
            quantityTextView.text = boldText("Qty: ", "$quantity")

            Glide.with(itemView.context)
                .load(item.imageName)
                .placeholder(R.drawable.shopsmart_transparent)
                .error(R.drawable.shopsmart_transparent)
                .into(itemImageView)

            if (isInOrderDetailsFragment) {
                val total = item.price * quantity
                totalTextView?.text = boldText("Total: ", "$${total}")
            }

            if (!isInOrderDetailsFragment) {
                val addToCartButton = itemView.findViewById<Button>(R.id.addToCartButton)
                val quantityInput = itemView.findViewById<EditText>(R.id.quantityInput)

                addToCartButton?.setOnClickListener {
                    val quantityText = quantityInput?.text.toString()
                    val enteredQuantity = quantityText.toIntOrNull() ?: 0

                    if (enteredQuantity > 0 && onAddToCartClicked != null) {
                        onAddToCartClicked.invoke(item, enteredQuantity)
                    } else {
                        quantityInput?.error = "Enter a valid quantity"
                    }
                }
            }
        }

        private fun boldText(boldPart: String, normalPart: String): SpannableStringBuilder {
            return SpannableStringBuilder().apply {
                append(boldPart)
                setSpan(StyleSpan(Typeface.BOLD), 0, boldPart.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                append(normalPart)
            }
        }
    }
}
