package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.model.ProductCategory
import com.example.ui.components.ProductCard
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.FormatUtils

@Composable
fun HomeScreen(
    viewModel: StoreViewModel,
    onNavigateToAdmin: () -> Unit
) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val checkoutForm by viewModel.checkoutForm.collectAsStateWithLifecycle()
    val viewingProduct by viewModel.viewingProduct.collectAsStateWithLifecycle()

    // Dialog controllers
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showTrackDialog by remember { mutableStateOf(false) }
    var confirmedOrderToShow by remember { mutableStateOf<OrderEntity?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            StoreHeaderBar(
                cartCount = cartCount,
                onOpenCart = { showCheckoutDialog = true },
                onOpenTrack = { showTrackDialog = true },
                onOpenAdmin = onNavigateToAdmin,
                onToggleSearch = { isSearchExpanded = !isSearchExpanded }
            )
        },
        bottomBar = {
            // Floating sticky cart bar when cart is not empty
            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("floating_cart_bar"),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${FormatUtils.toBengaliDigits(cartCount)} টি পণ্য কার্টে আছে",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = FormatUtils.formatTaka(cartSubtotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Button(
                            onClick = { showCheckoutDialog = true },
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("checkout_floating_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "চেকআউট করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("products_grid"),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input Row (Collapsible or Active)
            if (isSearchExpanded || searchQuery.isNotBlank()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("পণ্য খুঁজুন (যেমন: ঘড়ি, পাঞ্জাবি, ড্রোন, টি৯০০)...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("home_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Promotional Hero Banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                StoreHeroBanner()
            }

            // Store Perks / Trust Badges Row
            item(span = { GridItemSpan(maxLineSpan) }) {
                StoreTrustHighlights()
            }

            // Categories Selector
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ক্যাটেগরি সমূহ (Categories)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ProductCategory.entries) { category ->
                            val isSelected = selectedCategory == category
                            CategoryChip(
                                category = category,
                                isSelected = isSelected,
                                onClick = { viewModel.selectCategory(category) }
                            )
                        }
                    }
                }
            }

            // Section Header: Products List
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == ProductCategory.ALL) "জনপ্রিয় পণ্যসমূহ (${FormatUtils.toBengaliDigits(products.size)} টি)"
                        else "${selectedCategory.nameBn} (${FormatUtils.toBengaliDigits(products.size)} টি)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "অনুসন্ধান ফলাফল",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Empty state if no products found
            if (products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো পণ্য পাওয়া যায়নি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অনুগ্রহ করে অন্য কোনো কি-ওয়ার্ড দিয়ে খুঁজুন বা অন্য ক্যাটেগরি বেছে নিন।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                // Product Cards Grid
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onProductClick = { viewModel.viewProductDetails(product) },
                        onAddToCart = { viewModel.addToCart(product) }
                    )
                }
            }
        }
    }

    // Product Detail Dialog
    viewingProduct?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { viewModel.viewProductDetails(null) },
            onAddToCart = { prod, size, color, qty ->
                viewModel.addToCart(prod, size, color, qty)
            },
            onBuyNow = { prod, size, color, qty ->
                viewModel.addToCart(prod, size, color, qty)
                showCheckoutDialog = true
            }
        )
    }

    // Cart Checkout Sheet
    if (showCheckoutDialog) {
        CartCheckoutDialog(
            cartItems = cartItems,
            checkoutForm = checkoutForm,
            onQuantityChange = { item, delta -> viewModel.updateCartQuantity(item, delta) },
            onRemoveItem = { item -> viewModel.removeFromCart(item) },
            onNameChange = { viewModel.updateCustomerName(it) },
            onPhoneChange = { viewModel.updateCustomerPhone(it) },
            onAddressChange = { viewModel.updateDeliveryAddress(it) },
            onAreaChange = { viewModel.updateDeliveryArea(it) },
            onNotesChange = { viewModel.updateOrderNotes(it) },
            onSubmitOrder = { onSuccess ->
                viewModel.submitCheckout { order ->
                    onSuccess(order)
                }
            },
            onDismiss = { showCheckoutDialog = false },
            onOrderSuccess = { order ->
                showCheckoutDialog = false
                confirmedOrderToShow = order
            }
        )
    }

    // Order Success Confirmation Dialog
    confirmedOrderToShow?.let { order ->
        OrderSuccessDialog(
            order = order,
            onTrackOrder = { orderNumber ->
                viewModel.updateTrackSearchQuery(orderNumber)
                viewModel.trackOrder()
                showTrackDialog = true
            },
            onDismiss = { confirmedOrderToShow = null }
        )
    }

    // Order Tracking Dialog
    if (showTrackDialog) {
        val trackQuery by viewModel.trackSearchQuery.collectAsStateWithLifecycle()
        val trackedOrders by viewModel.trackedOrders.collectAsStateWithLifecycle()
        val isTrackSearching by viewModel.isTrackSearching.collectAsStateWithLifecycle()

        OrderTrackDialog(
            searchQuery = trackQuery,
            orders = trackedOrders,
            isSearching = isTrackSearching,
            onQueryChange = { viewModel.updateTrackSearchQuery(it) },
            onSearch = { viewModel.trackOrder() },
            onDismiss = { showTrackDialog = false }
        )
    }
}

@Composable
private fun StoreHeaderBar(
    cartCount: Int,
    onOpenCart: () -> Unit,
    onOpenTrack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onToggleSearch: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_trendcart_icon),
                        contentDescription = "Trend Cart Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Trend Cart BD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Store",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "ট্রেন্ড কার্ট বিডি • অনলাইন শপ",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Action Icons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onToggleSearch,
                    modifier = Modifier.testTag("toggle_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onOpenTrack,
                    modifier = Modifier.testTag("order_track_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Track Order",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onOpenAdmin,
                    modifier = Modifier.testTag("admin_panel_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Panel",
                        tint = Color.White
                    )
                }

                // Cart Icon with Badge
                IconButton(
                    onClick = onOpenCart,
                    modifier = Modifier.testTag("cart_top_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = FormatUtils.toBengaliDigits(cartCount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shopping Cart",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreHeroBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 8.5f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = "Mega Sale Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay with attractive Bangladeshi shopping copy
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            ),
                            startY = 100f
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "মেগা ধামাকা অফার • ৭০% পর্যন্ত ছাড়",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "ট্রেন্ডিং গ্যাজেট ও ফ্যাশন কালেকশন",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreTrustHighlights() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrustItem(
            icon = Icons.Default.LocalShipping,
            title = "ক্যাশ অন ডেলিভারি",
            desc = "পণ্য পেয়ে টাকা দিন",
            modifier = Modifier.weight(1f)
        )
        TrustItem(
            icon = Icons.Default.CheckCircle,
            title = "১০০% অরিজিনাল",
            desc = "কোয়ালিটি চেক করা",
            modifier = Modifier.weight(1f)
        )
        TrustItem(
            icon = Icons.Default.Verified,
            title = "সমগ্র বাংলাদেশ",
            desc = "দ্রুত হোম ডেলিভারি",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TrustItem(
    icon: ImageVector,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    text = desc,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: ProductCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (category) {
        ProductCategory.ALL -> Icons.Default.Category
        ProductCategory.GADGETS -> Icons.Default.Devices
        ProductCategory.FASHION -> Icons.Default.Watch
        ProductCategory.CLOTHING -> Icons.Default.Checkroom
        ProductCategory.BEAUTY_CARE -> Icons.Default.Spa
        ProductCategory.TOYS -> Icons.Default.SmartToy
        ProductCategory.TOOLS -> Icons.Default.Build
        ProductCategory.OTHER -> Icons.Default.ShoppingBag
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category.nameEn,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.nameBn,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
