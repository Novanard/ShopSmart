package utility

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CartViewModel : ViewModel() {

    // Use MutableLiveData for cartItems
    private val _cartItems = MutableLiveData<MutableList<Pair<Item, Int>>>()
    val cartItems: MutableLiveData<MutableList<Pair<Item, Int>>> get() = _cartItems

    init {
        // Initialize the list
        _cartItems.value = mutableListOf()
    }

    // Add item to the cart
    fun addItemToCart(item: Item, quantity: Int) {
        val currentItems = _cartItems.value ?: mutableListOf()
        val existingItem = currentItems.find { it.first == item }

        if (existingItem != null) {
            val index = currentItems.indexOf(existingItem)
            currentItems[index] = existingItem.copy(first = item, second = existingItem.second + quantity)
        } else {
            currentItems.add(Pair(item, quantity))
        }

        // Update LiveData
        _cartItems.value = currentItems

        // Log cart items after adding
        Log.d("CartViewModel", "Cart items: ${currentItems.size}")
    }

    // Get current cart items
    fun getCartItems(): List<Pair<Item, Int>> {
        return _cartItems.value ?: emptyList()
    }
}