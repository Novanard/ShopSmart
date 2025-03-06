package utility

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopsmart.R

class ItemAdapter(private var items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Item>) {
        Log.d("RecyclerViewDebug", "Updating RecyclerView with ${newItems.size} items")  // 🔍 Debug Log
        items = newItems
        notifyDataSetChanged()
    }


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemName)
        private val priceTextView: TextView = itemView.findViewById(R.id.itemPrice)
        private val quantityTextView: TextView = itemView.findViewById(R.id.itemQuantity)
        private val itemImageView: ImageView = itemView.findViewById(R.id.itemImage)

        fun bind(item: Item) {
            nameTextView.text = item.name
            priceTextView.text = "Price: $${item.price}"
            quantityTextView.text = "Available: ${item.quantity}"

            //Load image from drawable dynamically
            val context = itemView.context
            val resId = context.resources.getIdentifier(item.imageName, "drawable", context.packageName)
            if (resId != 0) {
                itemImageView.setImageResource(resId)
            } else {
                itemImageView.setImageResource(R.drawable.shopsmart_transparent) // Default placeholder
            }
        }
    }
}
