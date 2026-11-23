package com.example.businessos.ui.pos

import com.example.businessos.ui.components.FullScreenForm
import com.example.businessos.data.model.Money
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.ui.text.style.TextOverflow
import com.example.businessos.ui.util.AppLanguageManager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.local.entity.CustomerEntity
import com.example.businessos.data.local.entity.ItemEntity
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.PriceType
import com.example.businessos.data.model.Role
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.CardBackground
import com.example.businessos.ui.theme.CardBorder
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.DarkBorder
import com.example.businessos.ui.theme.DarkSurface
import com.example.businessos.ui.theme.DarkSurfaceElevated
import com.example.businessos.ui.theme.StatusGreen
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TealAccent
import com.example.businessos.ui.theme.TextMuted
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.businessos.ui.components.StockImageStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    onNavigateBack: () -> Unit,
    onSwitchWorkspace: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val items by viewModel.items.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showCartSheet by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val totalAmount = Money(cart.sumOf { it.subtotal.cents })
    val totalQty = cart.sumOf { it.qty }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        floatingActionButton = {
            if (session?.activeRole != Role.CASHIER) {
                FloatingActionButton(
                    onClick = { showAddItemDialog = true },
                    containerColor = AmberAccent,
                    contentColor = TextOnAccent
                ) {
                    Icon(Icons.Default.Add, contentDescription = AppLanguageManager.tr("Add Inventory Item", "Ongeza bidhaa"))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BusinessOsTopBar(
                title = AppLanguageManager.tr("Sales", "Mauzo"),
                businessName = session?.activeBusinessName,
                role = session?.activeRole,
                onSwitchWorkspace = onSwitchWorkspace
            )

            // Search Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(AppLanguageManager.tr("Search shoes, spares, items...", "Tafuta bidhaa..."), color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmberAccent) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = AppLanguageManager.tr("Clear", "Futa"), tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Low Stock Alert Banner (Clean White/Dark card styling)
            val lowStockItems = remember(items) { items.filter { it.stockQty <= it.minStockLevel } }
            if (lowStockItems.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StatusRed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppLanguageManager.tr("Low Stock Alert: ${lowStockItems.size} items", "Onyo la Akiba: Bidhaa ${lowStockItems.size}"),
                                color = StatusRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = lowStockItems.take(3).joinToString(", ") { "${it.name} (${it.stockQty})" } + if (lowStockItems.size > 3) "..." else "",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // View Mode Toggle (Grid vs List)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredItems.size} " + AppLanguageManager.tr("Products", "Bidhaa"),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isGridView) AmberAccent else Color.Transparent)
                            .clickable { isGridView = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = "Grid",
                                tint = if (isGridView) TextOnAccent else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Grid",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGridView) TextOnAccent else TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isGridView) AmberAccent else Color.Transparent)
                            .clickable { isGridView = false }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ViewList,
                                contentDescription = "List",
                                tint = if (!isGridView) TextOnAccent else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "List",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isGridView) TextOnAccent else TextSecondary
                            )
                        }
                    }
                }
            }

            // Items Display (Grid or List)
            if (filteredItems.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) AppLanguageManager.tr("No Items in Inventory", "Hakuna Bidhaa") else AppLanguageManager.tr("No matching items", "Hakuna matokeo"),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppLanguageManager.tr("Tap the + button to add products to your stall.", "Gusa kitufe cha + kuongeza bidhaa dukani."),
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val inCart = cart.find { it.item.id == item.id }
                        PosItemGridCard(
                            item = item,
                            inCartQty = inCart?.qty ?: 0,
                            onAddToCart = { viewModel.addToCart(item) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val inCart = cart.find { it.item.id == item.id }
                        PosItemRow(
                            item = item,
                            inCartQty = inCart?.qty ?: 0,
                            onAddToCart = { viewModel.addToCart(item) }
                        )
                    }
                }
            }

            // Bottom Floating Cart Bar
            if (cart.isNotEmpty()) {
                Surface(
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.clickable { showCartSheet = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$totalQty",
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = totalAmount.formatCompact(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = AppLanguageManager.tr("Tap to view items & toggle prices", "Gusa kuona bidhaa na kubadilisha bei"),
                                    fontSize = 12.sp,
                                    color = TealAccent
                                )
                            }
                        }

                        Button(
                            onClick = { showCheckoutSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAccent,
                                contentColor = TextOnAccent
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(AppLanguageManager.tr("Checkout", "Lipa"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Cart details modal sheet
        if (showCartSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                containerColor = DarkSurface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                CartSheetContent(
                    cart = cart,
                    role = session?.activeRole ?: Role.CASHIER,
                    onQtyDelta = { id, delta -> viewModel.updateCartQty(id, delta) },
                    onTogglePriceType = { id -> viewModel.togglePriceType(id) },
                    onOverridePrice = { id, newPrice -> viewModel.overrideItemPrice(id, Money.fromShillingsDouble(newPrice)) },
                    onCheckout = {
                        showCartSheet = false
                        showCheckoutSheet = true
                    },
                    onClear = {
                        viewModel.clearCart()
                        showCartSheet = false
                    }
                )
            }
        }

        // Checkout Bottom Sheet
        if (showCheckoutSheet) {
            CheckoutDialog(
                totalAmount = totalAmount,
                customers = customers,
                onDismiss = { showCheckoutSheet = false },
                onConfirm = { method, customerId, mpesaCode ->
                    viewModel.checkout(method, customerId, mpesaCode) {
                        showCheckoutSheet = false
                    }
                }
            )
        }

        // Add Item Dialog
        if (showAddItemDialog) {
            val context = LocalContext.current
            AddItemDialog(
                onDismiss = { showAddItemDialog = false },
                onAdd = { name, retail, wholesale, stock, minStock, photo ->
                    viewModel.addItemToInventory(name, Money.fromShillingsDouble(retail), Money.fromShillingsDouble(wholesale), stock.toLong(), minStock) { newItem ->
                        if (photo != null) {
                            StockImageStorage.saveCustomImage(context, newItem.id, photo)
                        }
                    }
                    showAddItemDialog = false
                }
            )
        }
    }
}

@Composable
fun PosItemGridCard(
    item: ItemEntity,
    inCartQty: Int,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddToCart() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (inCartQty > 0) AmberAccent else CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                contentAlignment = Alignment.Center
            ) {
                com.example.businessos.ui.components.StockItemThumbnail(
                    itemId = item.id,
                    itemName = item.name,
                    size = 85.dp
                )
                if (inCartQty > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AmberAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$inCartQty",
                            fontWeight = FontWeight.Bold,
                            color = TextOnAccent,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "KSh ${item.retailPrice.toInt()}",
                color = StatusGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${AppLanguageManager.tr("Stock", "Akiba")}: ${item.stockQty}" + if (item.stockQty <= item.minStockLevel) " (${AppLanguageManager.tr("low", "chini")})" else "",
                    color = if (item.stockQty <= item.minStockLevel) StatusRed else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = if (item.stockQty <= item.minStockLevel) FontWeight.Bold else FontWeight.Normal
                )

                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.AddCircle,
                        contentDescription = AppLanguageManager.tr("Add", "Ongeza"),
                        tint = AmberAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PosItemRow(
    item: ItemEntity,
    inCartQty: Int,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddToCart() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (inCartQty > 0) AmberAccent.copy(alpha = 0.5f) else CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.example.businessos.ui.components.StockItemThumbnail(
                itemId = item.id,
                itemName = item.name,
                size = 50.dp
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AppLanguageManager.tr("Retail: KSh ${item.retailPrice.toInt()}", "Rejareja: KSh ${item.retailPrice.toInt()}"),
                        color = StatusGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppLanguageManager.tr("Wholesale: KSh ${item.wholesalePrice.toInt()}", "Jumla: KSh ${item.wholesalePrice.toInt()}"),
                        color = AmberAccent,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${AppLanguageManager.tr("Stock", "Akiba")}: ${item.stockQty}" + if (item.stockQty <= item.minStockLevel) " (${AppLanguageManager.tr("low", "chini")})" else "",
                        color = if (item.stockQty <= item.minStockLevel) StatusRed else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (item.stockQty <= item.minStockLevel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (inCartQty > 0) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AmberAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$inCartQty",
                        fontWeight = FontWeight.Bold,
                        color = TextOnAccent
                    )
                }
            } else {
                IconButton(onClick = onAddToCart) {
                    Icon(Icons.Default.Add, contentDescription = AppLanguageManager.tr("Add", "Ongeza"), tint = AmberAccent)
                }
            }
        }
    }
}

@Composable
fun CartSheetContent(
    cart: List<CartItem>,
    role: Role,
    onQtyDelta: (String, Int) -> Unit,
    onTogglePriceType: (String) -> Unit,
    onOverridePrice: (String, Double) -> Unit,
    onCheckout: () -> Unit,
    onClear: () -> Unit
) {
    var overrideTargetId by remember { mutableStateOf<String?>(null) }
    var overridePriceInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppLanguageManager.tr("Current Sale Items", "Bidhaa za mauzo haya"),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClear) {
                Text(AppLanguageManager.tr("Clear All", "Futa zote"), color = StatusRed)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cart, key = { it.item.id }) { cartItem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cartItem.item.name,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = cartItem.subtotal.formatCompact(),
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (cartItem.priceType == PriceType.RETAIL) StatusGreen.copy(alpha = 0.14f) else AmberAccent.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onTogglePriceType(cartItem.item.id) }
                            ) {
                                Text(
                                    text = if (cartItem.priceType == PriceType.RETAIL) AppLanguageManager.tr("Retail @ ${cartItem.unitPrice.formatCompact()}", "Rejareja @ ${cartItem.unitPrice.formatCompact()}") else AppLanguageManager.tr("Wholesale @ ${cartItem.unitPrice.formatCompact()}", "Jumla @ ${cartItem.unitPrice.formatCompact()}"),
                                    color = if (cartItem.priceType == PriceType.RETAIL) StatusGreen else AmberAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            if (role != Role.CASHIER) {
                                IconButton(onClick = {
                                    overrideTargetId = cartItem.item.id
                                    overridePriceInput = cartItem.unitPrice.toString()
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = AppLanguageManager.tr("Override", "Badilisha bei"), tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onQtyDelta(cartItem.item.id, -1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = AppLanguageManager.tr("Minus", "Punguza"), tint = TextPrimary, modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    text = "${cartItem.qty}",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(onClick = { onQtyDelta(cartItem.item.id, 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = AppLanguageManager.tr("Plus", "Ongeza"), tint = AmberAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(AppLanguageManager.tr("Proceed to Payment", "Endelea kulipa"), fontWeight = FontWeight.Bold)
        }
    }

    if (overrideTargetId != null) {
        FullScreenForm(
            onDismissRequest = { overrideTargetId = null },
            containerColor = DarkSurface,
            title = { Text(AppLanguageManager.tr("Manager Price Override", "Badilisha bei (meneja)"), color = TextPrimary) },
            text = {
                Column {
                    Text(AppLanguageManager.tr("Enter custom unit price (will be audit logged).", "Weka bei mpya (itarekodiwa)."), color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = overridePriceInput,
                        onValueChange = { overridePriceInput = it },
                        label = { Text(AppLanguageManager.tr("Price (KSh)", "Bei (KSh)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = overridePriceInput.toDoubleOrNull()
                        if (parsed != null && parsed >= 0) {
                            onOverridePrice(overrideTargetId!!, parsed)
                        }
                        overrideTargetId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
                ) {
                    Text(AppLanguageManager.tr("Apply", "Tumia"))
                }
            },
            dismissButton = {
                TextButton(onClick = { overrideTargetId = null }) {
                    Text(AppLanguageManager.tr("Cancel", AppLanguageManager.tr("Cancel", "Ghairi")), color = TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    totalAmount: Money,
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (method: PaymentMethod, customerId: String?, mpesaCode: String?) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var mpesaCode by remember { mutableStateOf("") }
    var mpesaSmsInput by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Checkout • ${totalAmount.formatCompact()}",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(16.dp))) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF18221D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(AppLanguageManager.tr("Amount to pay", "Kiasi cha kulipa"), style = MaterialTheme.typography.labelSmall, color = TealAccent)
                            Text(totalAmount.formatCompact(), style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppLanguageManager.tr("Payment method", "Njia ya malipo"),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethodPill(
                        label = AppLanguageManager.tr("Cash", "Taslimu"),
                        isSelected = selectedMethod == PaymentMethod.CASH,
                        onClick = { selectedMethod = PaymentMethod.CASH },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodPill(
                        label = AppLanguageManager.tr("M-Pesa", "M-Pesa"),
                        isSelected = selectedMethod == PaymentMethod.MPESA,
                        onClick = { selectedMethod = PaymentMethod.MPESA },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethodPill(
                        label = AppLanguageManager.tr("Split", "Gawanya"),
                        isSelected = selectedMethod == PaymentMethod.SPLIT,
                        onClick = { selectedMethod = PaymentMethod.SPLIT },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodPill(
                        label = AppLanguageManager.tr("On credit", "Deni"),
                        isSelected = selectedMethod == PaymentMethod.CREDIT,
                        onClick = { selectedMethod = PaymentMethod.CREDIT },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedMethod == PaymentMethod.MPESA || selectedMethod == PaymentMethod.SPLIT) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Color(0xFF132B20),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = AppLanguageManager.tr("Paste M-Pesa SMS (fills in for you)", "Bandika SMS ya M-Pesa (inajaza yenyewe)"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = mpesaSmsInput,
                                onValueChange = { sms ->
                                    mpesaSmsInput = sms
                                    val parsed = com.example.businessos.ui.components.MpesaParser.parse(sms)
                                    if (parsed != null && parsed.code.isNotBlank()) {
                                        mpesaCode = parsed.code
                                    }
                                },
                                placeholder = { Text(AppLanguageManager.tr("Paste the M-Pesa SMS here...", "Bandika SMS ya M-Pesa hapa..."), fontSize = 14.sp) },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StatusGreen,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mpesaCode,
                        onValueChange = { mpesaCode = it.uppercase() },
                        label = { Text(AppLanguageManager.tr("M-Pesa confirmation code", "Msimbo wa M-Pesa")) },
                        placeholder = { Text(AppLanguageManager.tr("e.g. QKH719JXZ", "mf. QKH719JXZ")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusGreen,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                if (selectedMethod == PaymentMethod.CREDIT) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = AppLanguageManager.tr("Customer who owes", "Mteja anayedaiwa"),
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: "Select Customer",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberAccent,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            customers.forEach { cust ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(cust.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text(AppLanguageManager.tr("${cust.phone} - Owes: KSh ${cust.balance.toInt()}", "${cust.phone} - Deni: KSh ${cust.balance.toInt()}"), fontSize = 12.sp, color = TextSecondary)
                                        }
                                    },
                                    onClick = {
                                        selectedCustomer = cust
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedMethod, selectedCustomer?.id, mpesaCode.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text(AppLanguageManager.tr("Confirm Sale", "Thibitisha mauzo"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppLanguageManager.tr("Cancel", AppLanguageManager.tr("Cancel", "Ghairi")), color = TextSecondary)
            }
        }
    )
}

@Composable
fun PaymentMethodPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) AmberAccent else DarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isSelected) AmberAccent else DarkBorder),
        modifier = modifier
            .clickable { onClick() }
            .height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) TextOnAccent else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, retail: Double, wholesale: Double, stock: Int, minStock: Int, photo: Bitmap?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var retailPrice by remember { mutableStateOf("") }
    var wholesalePrice by remember { mutableStateOf("") }
    var stockQty by remember { mutableStateOf("10") }
    var minStockLevel by remember { mutableStateOf("3") }
    var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedPhoto = bitmap
        }
    }

    val stockPresets = remember {
        listOf(
            "Safari Boots Size 42" to Pair(2500.0, 1800.0),
            "Pure Leather Belt" to Pair(800.0, 500.0),
            "Leather Jacket" to Pair(6500.0, 4500.0),
            "Engine Oil 20W-50 4L" to Pair(3200.0, 2600.0),
            "Front Brake Pads" to Pair(3500.0, 2400.0),
            "Tubeless Tyre 195/65R15" to Pair(8500.0, 7200.0),
            "Paint Thinner 5L" to Pair(1500.0, 1100.0)
        )
    }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Column {
                Text(AppLanguageManager.tr("Add item", "Ongeza bidhaa"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(AppLanguageManager.tr("Photos stay inside the app only", "Picha huhifadhiwa ndani ya programu pekee"), color = TextMuted, fontSize = 12.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Camera capture section
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (capturedPhoto != null) StatusGreen else DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { cameraLauncher.launch(null) }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bmp = capturedPhoto
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = AppLanguageManager.tr("Item photo", "Picha ya bidhaa"),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(AppLanguageManager.tr("Photo taken", "Picha imepigwa"), color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(AppLanguageManager.tr("Tap here to change the photo", "Gusa hapa kubadilisha picha"), color = TextSecondary, fontSize = 12.sp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = AppLanguageManager.tr("Camera", "Kamera"), tint = AmberAccent)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(AppLanguageManager.tr("Take a photo of the item", "Piga picha ya bidhaa"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(AppLanguageManager.tr("Saved inside the app, not in your gallery", "Inahifadhiwa ndani ya programu, si kwenye gallery"), color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text(
                    text = AppLanguageManager.tr("Quick pick:", "Chagua haraka:"),
                    fontSize = 12.sp,
                    color = AmberAccent,
                    fontWeight = FontWeight.Bold
                )

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stockPresets) { (presetName, prices) ->
                        Surface(
                            color = DarkSurfaceElevated,
                            border = BorderStroke(1.dp, if (name == presetName) AmberAccent else DarkBorder),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.clickable {
                                name = presetName
                                retailPrice = prices.first.toInt().toString()
                                wholesalePrice = prices.second.toInt().toString()
                            }
                        ) {
                            Text(
                                text = presetName,
                                fontSize = 12.sp,
                                color = if (name == presetName) AmberAccent else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppLanguageManager.tr("Item name", "Jina la bidhaa")) },
                    placeholder = { Text(AppLanguageManager.tr("e.g. Safari boots size 42", "mf. Viatu vya safari size 42")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = retailPrice,
                        onValueChange = { retailPrice = it },
                        label = { Text(AppLanguageManager.tr("Retail price", "Bei ya rejareja")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = wholesalePrice,
                        onValueChange = { wholesalePrice = it },
                        label = { Text(AppLanguageManager.tr("Wholesale price", "Bei ya jumla")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockQty,
                        onValueChange = { stockQty = it },
                        label = { Text(AppLanguageManager.tr("Starting quantity", "Idadi ya mwanzo")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = minStockLevel,
                        onValueChange = { minStockLevel = it },
                        label = { Text(AppLanguageManager.tr("Warn me at or below", "Nionye ikiwa chini ya")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = retailPrice.toDoubleOrNull() ?: 0.0
                    val w = wholesalePrice.toDoubleOrNull() ?: 0.0
                    val s = stockQty.toIntOrNull() ?: 0
                    val m = minStockLevel.toIntOrNull() ?: 3
                    if (name.isNotBlank() && r > 0) {
                        onAdd(name, r, w, s, m, capturedPhoto)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text(AppLanguageManager.tr("Save item", "Hifadhi bidhaa"), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi"), color = TextSecondary) }
        }
    )
}
