package utility

import com.google.firebase.firestore.PropertyName

data class Order(
    var orderId: String = "", // Now includes Firestore document ID
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val timestamp: Long = 0L,
    @get:PropertyName("isReady") @set:PropertyName("isReady")
    var isReady: Boolean = false,

    @get:PropertyName("isShipped") @set:PropertyName("isShipped")
    var isShipped: Boolean = false,

    @get:PropertyName("isDelivered") @set:PropertyName("isDelivered")
    var isDelivered: Boolean = false
)
