package utility
data class Order(
    val userId: String,
    val items: List<Pair<Item, Int>>,
    val totalPrice: Double,
    val timestamp: Long
)
