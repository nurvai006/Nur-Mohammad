package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.model.OrderStatus
import com.example.model.ProductCategory
import com.example.util.FormatUtils
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    isLoggedIn: Boolean,
    pinInput: String,
    pinError: String?,
    orders: List<OrderEntity>,
    allOrdersList: List<OrderEntity>,
    products: List<ProductEntity>,
    selectedStatusFilter: String,
    onPinChange: (String) -> Unit,
    onLogin: () -> Boolean,
    onLogout: () -> Unit,
    onFilterStatus: (String) -> Unit,
    onUpdateOrderStatus: (Long, String) -> Unit,
    onDeleteOrder: (Long) -> Unit,
    onSaveProduct: (Long, String, String, String, Int, Int, Int, String, String, String, String, String, Boolean) -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onQuickStockChange: (Long, Int) -> Unit,
    onBackToStore: () -> Unit
) {
    BackHandler {
        onBackToStore()
    }

    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Orders, 1: Products, 2: FCM & Settings
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        // Admin Login Screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("এডমিন প্যানেল লগইন", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackToStore) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to store")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .testTag("admin_login_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "সুরক্ষিত এডমিন লগইন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "ট্রেন্ড কার্ট বিডি অ্যাডমিনিস্ট্রেটর অ্যাক্সেস",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = onPinChange,
                            label = { Text("এডমিন পিন কোড (Default: 1234)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pin_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (pinError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pinError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_login_submit_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("প্রবেশ করুন (Login)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "ডিফল্ট পিন কোড: 1234",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        return
    }

    // Authenticated Admin Dashboard
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("এডমিন ড্যাশবোর্ড", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Trend Cart BD • Store Management", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToStore) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to store")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Lock, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Analytics Metric Cards (Summary)
            val totalRevenue = allOrdersList.filter { it.status == "delivered" || it.status == "confirmed" || it.status == "shipped" }
                .sumOf { it.grandTotal }
            val totalOrdersCount = allOrdersList.size
            val pendingOrdersCount = allOrdersList.count { it.status == "pending" }
            val deliveredOrdersCount = allOrdersList.count { it.status == "delivered" }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    MetricCard(
                        title = "মোট বিক্রয় (Revenue)",
                        value = FormatUtils.formatTaka(totalRevenue),
                        icon = Icons.Default.Payment,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    MetricCard(
                        title = "মোট অর্ডার (Total)",
                        value = "${FormatUtils.toBengaliDigits(totalOrdersCount)} টি",
                        icon = Icons.Default.ShoppingBag,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    MetricCard(
                        title = "অপেক্ষমান (Pending)",
                        value = "${FormatUtils.toBengaliDigits(pendingOrdersCount)} টি",
                        icon = Icons.Default.Refresh,
                        color = Color(0xFFD97706)
                    )
                }
                item {
                    MetricCard(
                        title = "ডেলিভার্ড (Delivered)",
                        value = "${FormatUtils.toBengaliDigits(deliveredOrdersCount)} টি",
                        icon = Icons.Default.Check,
                        color = Color(0xFF059669)
                    )
                }
            }

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("অর্ডার সমূহ (${FormatUtils.toBengaliDigits(orders.size)})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("পণ্য ব্যবস্থাপনা (${FormatUtils.toBengaliDigits(products.size)})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("FCM পুশ ও সেটিংস", fontWeight = FontWeight.Bold) }
                )
            }

            // Tab Content
            when (activeTab) {
                0 -> {
                    // Orders Management Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Status Filter Chips
                        val filterList = listOf(
                            "all" to "সকল (${FormatUtils.toBengaliDigits(allOrdersList.size)})",
                            "pending" to "পেন্ডিং (${FormatUtils.toBengaliDigits(allOrdersList.count { it.status == "pending" })})",
                            "confirmed" to "কনফার্মড (${FormatUtils.toBengaliDigits(allOrdersList.count { it.status == "confirmed" })})",
                            "shipped" to "শিপিং (${FormatUtils.toBengaliDigits(allOrdersList.count { it.status == "shipped" })})",
                            "delivered" to "ডেলিভার্ড (${FormatUtils.toBengaliDigits(allOrdersList.count { it.status == "delivered" })})",
                            "cancelled" to "বাতিল (${FormatUtils.toBengaliDigits(allOrdersList.count { it.status == "cancelled" })})"
                        )

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterList) { (key, label) ->
                                FilterChip(
                                    selected = selectedStatusFilter == key,
                                    onClick = { onFilterStatus(key) },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }

                        if (orders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "এই ফিল্টারে কোনো অর্ডার নেই",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(orders, key = { it.id }) { order ->
                                    AdminOrderCard(
                                        order = order,
                                        onUpdateStatus = { status -> onUpdateOrderStatus(order.id, status) },
                                        onDelete = { onDeleteOrder(order.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Products Management Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "দোকানের সকল পণ্য তালিকা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showAddProductDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("admin_add_product_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন পণ্য")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(products, key = { it.id }) { product ->
                                AdminProductItem(
                                    product = product,
                                    onEdit = { productToEdit = product },
                                    onDelete = { onDeleteProduct(product.id) },
                                    onStockChange = { delta ->
                                        onQuickStockChange(product.id, product.stock + delta)
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // FCM Push Notifications & Store Settings Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "FCM ওয়েব ও মোবাইল পুশ নোটিফিকেশন",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "গ্রাহক যখন নতুন অর্ডার প্লেস করবেন, তখন দোকানদার সরাসরি মোবাইলে পুশ নোটিফিকেশন পাবেন।",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "১. Firebase Console-এ Cloud Messaging সক্রিয় করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "২. VAPID Web Push Key জেনারেট করে অ্যাডমিনে সেট করুন", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "৩. নতুন অর্ডার এন্ট্রি হলে স্বয়ংক্রিয় টপিক 'store_orders' এ নোটিফিকেশন পাঠানো হয়।", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "টেস্ট নোটিফিকেশন: নতুন অর্ডার এসেছে!", Toast.LENGTH_LONG).show()
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("টেস্ট পুশ নোটিফিকেশন টেস্ট করুন")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Store Info
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "দোকানের ডেলিভারি চার্জ সেটিংস:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• ঢাকার ভিতরে ডেলিভারি চার্জ: ৬০ টাকা (১-২ কার্যদিবস)", fontSize = 13.sp)
                                Text("• ঢাকার বাইরে সমগ্র বাংলাদেশ: ১২০ টাকা (৩-৪ কার্যদিবস)", fontSize = 13.sp)
                                Text("• পেমেন্ট পলিসি: Cash on Delivery (ক্যাশ অন ডেলিভারি)", fontSize = 13.sp)
                                Text("• ডাটাবেজ: Persistent Local SQLite/Room DB", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddProductDialog || productToEdit != null) {
        val editing = productToEdit
        ProductEditDialog(
            product = editing,
            onDismiss = {
                showAddProductDialog = false
                productToEdit = null
            },
            onSave = { id, title, titleBn, cat, price, origPrice, stock, img, sizes, colors, desc, descBn, isFeatured ->
                onSaveProduct(id, title, titleBn, cat, price, origPrice, stock, img, sizes, colors, desc, descBn, isFeatured)
                showAddProductDialog = false
                productToEdit = null
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(86.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = color
            )
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: OrderEntity,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val currentStatus = OrderStatus.fromCode(order.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order Number and Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Order Number", order.orderNumber)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "অর্ডার নম্বর কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(currentStatus.colorHex).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = currentStatus.titleBn,
                        color = Color(currentStatus.colorHex),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Customer Details with Direct Call Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "গ্রাহক: ${order.customerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "ফোন: ${order.customerPhone}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "ঠিকানা: ${order.deliveryAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(text = "এলাকা: ${order.deliveryArea}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    if (order.orderNotes.isNotBlank()) {
                        Text(text = "নোট: ${order.orderNotes}", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Customer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items breakdown
            val itemsList = try {
                val array = JSONArray(order.itemsJson)
                (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    "${obj.optString("titleBn", obj.optString("title"))} (${obj.optString("size")}/${obj.optString("color")}) x ${FormatUtils.toBengaliDigits(obj.optInt("quantity"))}"
                }.joinToString(", ")
            } catch (_: Exception) {
                "আইটেম সমূহ"
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "পণ্য: $itemsList", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "মোট মূল্য (COD): ${FormatUtils.formatTaka(order.grandTotal)} (ডেলিভারি চার্জ: ${FormatUtils.formatTaka(order.deliveryFee)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Management Action Buttons
            Text(text = "স্ট্যাটাস পরিবর্তন করুন:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (order.status != "confirmed") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("confirmed") },
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("কনফার্ম", fontSize = 11.sp)
                    }
                }

                if (order.status != "shipped") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("shipped") },
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("শিপ করুন", fontSize = 11.sp)
                    }
                }

                if (order.status != "delivered") {
                    Button(
                        onClick = { onUpdateStatus("delivered") },
                        modifier = Modifier.weight(1f).height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("ডেলিভার্ড", fontSize = 11.sp)
                    }
                }

                if (order.status != "cancelled") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("cancelled") },
                        modifier = Modifier.weight(0.8f).height(34.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("বাতিল", fontSize = 11.sp)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminProductItem(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStockChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.titleBn.ifBlank { product.title },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = "ক্যাটেগরি: ${ProductCategory.fromId(product.category).nameBn} | ${FormatUtils.formatTaka(product.price)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Stock Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(text = "স্টক: ", fontSize = 11.sp)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onStockChange(-1) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("-", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = FormatUtils.toBengaliDigits(product.stock),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onStockChange(1) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ProductEditDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (Long, String, String, String, Int, Int, Int, String, String, String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var titleBn by remember { mutableStateOf(product?.titleBn ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "gadgets") }
    var priceText by remember { mutableStateOf((product?.price ?: 1000).toString()) }
    var origPriceText by remember { mutableStateOf((product?.originalPrice ?: 1500).toString()) }
    var stockText by remember { mutableStateOf((product?.stock ?: 20).toString()) }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var sizes by remember { mutableStateOf(product?.sizes ?: "M, L, XL") }
    var colors by remember { mutableStateOf(product?.colors ?: "Black, White") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var descriptionBn by remember { mutableStateOf(product?.descriptionBn ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxSize(0.9f)
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (product == null) "নতুন পণ্য যোগ করুন" else "পণ্য সম্পাদনা করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = titleBn,
                        onValueChange = { titleBn = it },
                        label = { Text("বাংলা টাইটেল *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("ইংরেজি টাইটেল") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("ক্যাটেগরি নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(ProductCategory.entries.filter { it != ProductCategory.ALL }) { cat ->
                            FilterChip(
                                selected = category == cat.id,
                                onClick = { category = cat.id },
                                label = { Text(cat.nameBn, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("বিক্রয় মূল্য (৳) *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = origPriceText,
                            onValueChange = { origPriceText = it },
                            label = { Text("আসল মূল্য (৳)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("স্টক *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("ছবির লিঙ্ক (Image URL)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sizes,
                        onValueChange = { sizes = it },
                        label = { Text("সাইজ সমূহ (কমা দিয়ে লিখুন)") },
                        placeholder = { Text("M, L, XL, Free Size") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = colors,
                        onValueChange = { colors = it },
                        label = { Text("রঙ সমূহ (কমা দিয়ে লিখুন)") },
                        placeholder = { Text("Black, Blue, Red") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descriptionBn,
                        onValueChange = { descriptionBn = it },
                        label = { Text("বাংলা বিস্তারিত বিবরণ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                Button(
                    onClick = {
                        val price = priceText.toIntOrNull() ?: 100
                        val origPrice = origPriceText.toIntOrNull() ?: price
                        val stock = stockText.toIntOrNull() ?: 10
                        onSave(
                            product?.id ?: 0L,
                            title,
                            titleBn,
                            category,
                            price,
                            origPrice,
                            stock,
                            imageUrl,
                            sizes,
                            colors,
                            description,
                            descriptionBn,
                            product?.isFeatured ?: false
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("সংরক্ষণ করুন (Save Product)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
