package utility

data class Order(
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val timestamp: Long = 0L,
    val isReady: Boolean = false,
    val isShipped: Boolean = false,
    val isDelivered: Boolean = false
)
