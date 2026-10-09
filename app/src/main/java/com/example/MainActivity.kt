package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StoreViewModel

enum class ScreenState {
    CUSTOMER_STORE,
    ADMIN_PANEL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val storeViewModel: StoreViewModel = viewModel()
                    var currentScreen by remember { mutableStateOf(ScreenState.CUSTOMER_STORE) }

                    when (currentScreen) {
                        ScreenState.CUSTOMER_STORE -> {
                            HomeScreen(
                                viewModel = storeViewModel,
                                onNavigateToAdmin = {
                                    currentScreen = ScreenState.ADMIN_PANEL
                                }
                            )
                        }

                        ScreenState.ADMIN_PANEL -> {
                            val isLoggedIn by storeViewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
                            val pinInput by storeViewModel.adminPinInput.collectAsStateWithLifecycle()
                            val pinError by storeViewModel.adminPinError.collectAsStateWithLifecycle()
                            val orders by storeViewModel.adminFilteredOrders.collectAsStateWithLifecycle()
                            val allOrdersList by storeViewModel.allOrders.collectAsStateWithLifecycle()
                            val products by storeViewModel.allProducts.collectAsStateWithLifecycle()
                            val statusFilter by storeViewModel.adminSelectedStatus.collectAsStateWithLifecycle()

                            AdminDashboardScreen(
                                isLoggedIn = isLoggedIn,
                                pinInput = pinInput,
                                pinError = pinError,
                                orders = orders,
                                allOrdersList = allOrdersList,
                                products = products,
                                selectedStatusFilter = statusFilter,
                                onPinChange = { storeViewModel.updateAdminPinInput(it) },
                                onLogin = { storeViewModel.loginAdmin() },
                                onLogout = { storeViewModel.logoutAdmin() },
                                onFilterStatus = { storeViewModel.setAdminSelectedStatus(it) },
                                onUpdateOrderStatus = { id, status -> storeViewModel.updateOrderStatus(id, status) },
                                onDeleteOrder = { id -> storeViewModel.deleteOrder(id) },
                                onSaveProduct = { id, title, titleBn, cat, price, orig, stock, img, sizes, colors, desc, descBn, featured ->
                                    storeViewModel.saveProduct(id, title, titleBn, cat, price, orig, stock, img, sizes, colors, desc, descBn, featured)
                                },
                                onDeleteProduct = { id -> storeViewModel.deleteProduct(id) },
                                onQuickStockChange = { id, newStock -> storeViewModel.quickUpdateStock(id, newStock) },
                                onBackToStore = {
                                    currentScreen = ScreenState.CUSTOMER_STORE
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
