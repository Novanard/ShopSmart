package utility
data class Order(
    var orderId: String = "", // Now includes Firestore document ID
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val timestamp: Long = 0L,
    var isReady: Boolean = false,
    var isShipped: Boolean = false,
    var isDelivered: Boolean = false
)
