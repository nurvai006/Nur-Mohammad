package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.repository.StoreRepository
import com.example.model.CartItem
import com.example.model.DeliveryArea
import com.example.model.OrderItemDto
import com.example.model.OrderStatus
import com.example.model.ProductCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckoutFormState(
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val deliveryArea: DeliveryArea = DeliveryArea.INSIDE_DHAKA,
    val orderNotes: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StoreRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = StoreRepository(database.productDao(), database.orderDao())
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Products & Filtering
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _selectedCategory,
        _searchQuery
    ) { products, category, query ->
        val catFiltered = if (category == ProductCategory.ALL) {
            products
        } else {
            products.filter { it.category.equals(category.id, ignoreCase = true) }
        }

        if (query.isBlank()) {
            catFiltered
        } else {
            val q = query.trim().lowercase()
            catFiltered.filter {
                it.title.lowercase().contains(q) ||
                it.titleBn.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.descriptionBn.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartCount: StateFlow<Int> = MutableStateFlow(0).apply {
        viewModelScope.launch {
            cartItems.collect { list ->
                value = list.sumOf { it.quantity }
            }
        }
    }

    val cartSubtotal: StateFlow<Int> = MutableStateFlow(0).apply {
        viewModelScope.launch {
            cartItems.collect { list ->
                value = list.sumOf { it.price * it.quantity }
            }
        }
    }

    // Checkout
    private val _checkoutForm = MutableStateFlow(CheckoutFormState())
    val checkoutForm: StateFlow<CheckoutFormState> = _checkoutForm.asStateFlow()

    private val _lastPlacedOrder = MutableStateFlow<OrderEntity?>(null)
    val lastPlacedOrder: StateFlow<OrderEntity?> = _lastPlacedOrder.asStateFlow()

    // Order Tracking
    private val _trackSearchQuery = MutableStateFlow("")
    val trackSearchQuery: StateFlow<String> = _trackSearchQuery.asStateFlow()

    private val _trackedOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val trackedOrders: StateFlow<List<OrderEntity>> = _trackedOrders.asStateFlow()

    private val _isTrackSearching = MutableStateFlow(false)
    val isTrackSearching: StateFlow<Boolean> = _isTrackSearching.asStateFlow()

    // Admin
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminSelectedStatus = MutableStateFlow("all")
    val adminSelectedStatus: StateFlow<String> = _adminSelectedStatus.asStateFlow()

    private val _adminPinInput = MutableStateFlow("")
    val adminPinInput: StateFlow<String> = _adminPinInput.asStateFlow()

    private val _adminPinError = MutableStateFlow<String?>(null)
    val adminPinError: StateFlow<String?> = _adminPinError.asStateFlow()

    // Currently inspected product for detail dialog
    private val _viewingProduct = MutableStateFlow<ProductEntity?>(null)
    val viewingProduct: StateFlow<ProductEntity?> = _viewingProduct.asStateFlow()

    // Filtered admin orders
    val adminFilteredOrders: StateFlow<List<OrderEntity>> = combine(
        allOrders,
        _adminSelectedStatus
    ) { orders, status ->
        if (status == "all") orders
        else orders.filter { it.status.equals(status, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product and Search Actions
    fun selectCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun viewProductDetails(product: ProductEntity?) {
        _viewingProduct.value = product
    }

    // Cart Actions
    fun addToCart(
        product: ProductEntity,
        selectedSize: String = "",
        selectedColor: String = "",
        quantity: Int = 1
    ) {
        if (product.stock <= 0) return

        val size = if (selectedSize.isNotBlank()) selectedSize else product.sizes.split(",").firstOrNull()?.trim() ?: "Default"
        val color = if (selectedColor.isNotBlank()) selectedColor else product.colors.split(",").firstOrNull()?.trim() ?: "Default"

        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.productId == product.id && it.selectedSize == size && it.selectedColor == color
        }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stock)
            currentList[existingIndex] = existing.copy(quantity = newQty)
        } else {
            currentList.add(
                CartItem(
                    productId = product.id,
                    title = product.title,
                    titleBn = product.titleBn,
                    price = product.price,
                    originalPrice = product.originalPrice,
                    imageUrl = product.imageUrl,
                    selectedSize = size,
                    selectedColor = color,
                    quantity = quantity.coerceAtMost(product.stock),
                    maxStock = product.stock
                )
            )
        }
        _cartItems.value = currentList
    }

    fun updateCartQuantity(item: CartItem, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst {
            it.productId == item.productId && it.selectedSize == item.selectedSize && it.selectedColor == item.selectedColor
        }
        if (index >= 0) {
            val newQty = currentList[index].quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                val clamped = newQty.coerceAtMost(item.maxStock)
                currentList[index] = currentList[index].copy(quantity = clamped)
            }
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(item: CartItem) {
        val currentList = _cartItems.value.toMutableList()
        currentList.removeAll {
            it.productId == item.productId && it.selectedSize == item.selectedSize && it.selectedColor == item.selectedColor
        }
        _cartItems.value = currentList
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Checkout Form Actions
    fun updateCustomerName(name: String) {
        _checkoutForm.value = _checkoutForm.value.copy(customerName = name, errorMessage = null)
    }

    fun updateCustomerPhone(phone: String) {
        _checkoutForm.value = _checkoutForm.value.copy(customerPhone = phone, errorMessage = null)
    }

    fun updateDeliveryAddress(address: String) {
        _checkoutForm.value = _checkoutForm.value.copy(deliveryAddress = address, errorMessage = null)
    }

    fun updateDeliveryArea(area: DeliveryArea) {
        _checkoutForm.value = _checkoutForm.value.copy(deliveryArea = area, errorMessage = null)
    }

    fun updateOrderNotes(notes: String) {
        _checkoutForm.value = _checkoutForm.value.copy(orderNotes = notes)
    }

    fun submitCheckout(onSuccess: (OrderEntity) -> Unit) {
        val state = _checkoutForm.value
        val name = state.customerName.trim()
        val phone = state.customerPhone.trim()
        val address = state.deliveryAddress.trim()
        val area = state.deliveryArea

        if (name.isBlank()) {
            _checkoutForm.value = state.copy(errorMessage = "অনুগ্রহ করে আপনার নাম লিখুন (Please enter your name)")
            return
        }

        // Validate Bangladeshi phone number: 11 digits starting with 01
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length != 11 || !cleanPhone.startsWith("01")) {
            _checkoutForm.value = state.copy(errorMessage = "সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)")
            return
        }

        if (address.length < 10) {
            _checkoutForm.value = state.copy(errorMessage = "অনুগ্রহ করে পূর্ণ ডেলিভারি ঠিকানা বিস্তারিত লিখুন (জেলা, থানা, এলাকা)")
            return
        }

        val cart = _cartItems.value
        if (cart.isEmpty()) {
            _checkoutForm.value = state.copy(errorMessage = "আপনার শপিং কার্ট খালি রয়েছে")
            return
        }

        val subtotal = cart.sumOf { it.price * it.quantity }
        val deliveryFee = area.charge
        val grandTotal = subtotal + deliveryFee

        val items = cart.map {
            OrderItemDto(
                productId = it.productId,
                title = it.title,
                titleBn = it.titleBn,
                price = it.price,
                quantity = it.quantity,
                size = it.selectedSize,
                color = it.selectedColor
            )
        }

        _checkoutForm.value = state.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val order = repository.placeOrder(
                    customerName = name,
                    customerPhone = cleanPhone,
                    deliveryAddress = address,
                    deliveryArea = area.titleBn,
                    deliveryFee = deliveryFee,
                    orderNotes = state.orderNotes,
                    items = items,
                    subtotal = subtotal,
                    grandTotal = grandTotal
                )

                _lastPlacedOrder.value = order
                clearCart()
                _checkoutForm.value = CheckoutFormState() // Reset
                onSuccess(order)
            } catch (e: Exception) {
                _checkoutForm.value = _checkoutForm.value.copy(
                    isSubmitting = false,
                    errorMessage = "অর্ডার সম্পন্ন করতে সমস্যা হয়েছে: ${e.localizedMessage}"
                )
            }
        }
    }

    // Order Tracking
    fun updateTrackSearchQuery(query: String) {
        _trackSearchQuery.value = query
    }

    fun trackOrder() {
        val q = _trackSearchQuery.value.trim()
        if (q.isBlank()) return

        _isTrackSearching.value = true
        viewModelScope.launch {
            try {
                // If query is an order number
                val single = repository.findOrderByNumber(q)
                if (single != null) {
                    _trackedOrders.value = listOf(single)
                } else {
                    // Search by phone
                    val byPhone = repository.getOrdersByCustomerPhone(q)
                    _trackedOrders.value = byPhone
                }
            } finally {
                _isTrackSearching.value = false
            }
        }
    }

    // Admin Panel Actions
    fun updateAdminPinInput(pin: String) {
        _adminPinInput.value = pin
        _adminPinError.value = null
    }

    fun loginAdmin(): Boolean {
        val pin = _adminPinInput.value.trim()
        // Default secure PIN is 1234 or custom
        return if (pin == "1234" || pin == "adminbd" || pin == "trendcart2026") {
            _isAdminLoggedIn.value = true
            _adminPinInput.value = ""
            _adminPinError.value = null
            true
        } else {
            _adminPinError.value = "ভুল পিন কোড! সঠিক পিন লিখুন (Default: 1234)"
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    fun setAdminSelectedStatus(status: String) {
        _adminSelectedStatus.value = status
    }

    fun updateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun deleteOrder(orderId: Long) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
        }
    }

    fun saveProduct(
        id: Long = 0,
        title: String,
        titleBn: String,
        category: String,
        price: Int,
        originalPrice: Int,
        stock: Int,
        imageUrl: String,
        sizes: String,
        colors: String,
        description: String,
        descriptionBn: String,
        isFeatured: Boolean
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                title = title.trim(),
                titleBn = titleBn.trim().ifBlank { title.trim() },
                category = category.trim().lowercase(),
                price = price,
                originalPrice = originalPrice,
                stock = stock,
                imageUrl = imageUrl.trim().ifBlank { "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600&auto=format&fit=crop&q=80" },
                sizes = sizes.trim().ifBlank { "Standard" },
                colors = colors.trim().ifBlank { "Standard" },
                description = description.trim(),
                descriptionBn = descriptionBn.trim().ifBlank { description.trim() },
                isFeatured = isFeatured
            )
            if (id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun quickUpdateStock(productId: Long, newStock: Int) {
        viewModelScope.launch {
            repository.updateProductStock(productId, newStock)
        }
    }
}
