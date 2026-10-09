package com.example.data.repository

import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.local.ProductDao
import com.example.data.local.ProductEntity
import com.example.model.OrderItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class StoreRepository(
    private val productDao: ProductDao,
    private val orderDao: OrderDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return if (category == "all") {
            productDao.getAllProducts()
        } else {
            productDao.getProductsByCategory(category)
        }
    }

    fun getProductById(id: Long): Flow<ProductEntity?> {
        return productDao.getProductById(id)
    }

    suspend fun getOrdersByCustomerPhone(phone: String): List<OrderEntity> = withContext(Dispatchers.IO) {
        orderDao.searchOrders(phone.trim())
    }

    suspend fun findOrderByNumber(orderNumber: String): OrderEntity? = withContext(Dispatchers.IO) {
        orderDao.getOrderByOrderNumber(orderNumber.trim())
    }

    suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(productId: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(productId)
    }

    suspend fun updateProductStock(productId: Long, newStock: Int) = withContext(Dispatchers.IO) {
        productDao.updateStock(productId, newStock.coerceAtLeast(0))
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, newStatus)
    }

    suspend fun deleteOrder(orderId: Long) = withContext(Dispatchers.IO) {
        orderDao.deleteOrderById(orderId)
    }

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryArea: String,
        deliveryFee: Int,
        orderNotes: String,
        items: List<OrderItemDto>,
        subtotal: Int,
        grandTotal: Int
    ): OrderEntity = withContext(Dispatchers.IO) {
        // Generate unique order number e.g. TCB-2610-8429
        val datePrefix = SimpleDateFormat("yyMM", Locale.US).format(Date())
        val randomDigits = Random.nextInt(1000, 9999)
        val orderNumber = "TCB-$datePrefix-$randomDigits"

        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("productId", item.productId)
                put("title", item.title)
                put("titleBn", item.titleBn)
                put("price", item.price)
                put("quantity", item.quantity)
                put("size", item.size)
                put("color", item.color)
            }
            jsonArray.put(obj)

            // Reduce product stock in database
            val product = productDao.getProductDirect(item.productId)
            if (product != null) {
                val updatedStock = (product.stock - item.quantity).coerceAtLeast(0)
                productDao.updateStock(product.id, updatedStock)
            }
        }

        val order = OrderEntity(
            orderNumber = orderNumber,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            deliveryAddress = deliveryAddress.trim(),
            deliveryArea = deliveryArea,
            deliveryFee = deliveryFee,
            orderNotes = orderNotes.trim(),
            itemsJson = jsonArray.toString(),
            subtotal = subtotal,
            grandTotal = grandTotal,
            paymentMethod = "Cash on Delivery (ক্যাশ অন ডেলিভারি)",
            status = "pending",
            createdAt = System.currentTimeMillis()
        )

        val id = orderDao.insertOrder(order)
        order.copy(id = id)
    }

    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val count = productDao.getProductCount()
        if (count == 0) {
            val initialList = listOf(
                ProductEntity(
                    title = "T900 Ultra Smart Watch 2.09\" Full HD",
                    titleBn = "টি৯০০ আল্ট্রা স্মার্ট ওয়াচ ২.০৯ ইঞ্চি ফুল এইচডি",
                    category = "gadgets",
                    price = 1250,
                    originalPrice = 1950,
                    stock = 25,
                    imageUrl = "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600&auto=format&fit=crop&q=80",
                    sizes = "Standard",
                    colors = "Orange, Black, Silver",
                    description = "Bluetooth Calling, Wireless Charging, Multiple Sports Modes, Heart Rate & Blood Oxygen Monitor, IP68 Waterproof.",
                    descriptionBn = "ব্লুটুথ কলিং, ওয়্যারলেস চার্জিং, হার্ট রেট ও ব্লাড অক্সিজেন মনিটর, আইপি৬৮ ওয়াটারপ্রুফ সুবিধা।",
                    isFeatured = true
                ),
                ProductEntity(
                    title = "M10 Wireless Gaming TWS Earbuds",
                    titleBn = "এম১০ ওয়্যারলেস গেমিং ব্লুটুথ ইয়ারবাডস",
                    category = "gadgets",
                    price = 650,
                    originalPrice = 1100,
                    stock = 38,
                    imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600&auto=format&fit=crop&q=80",
                    sizes = "Standard",
                    colors = "Black, Cyan",
                    description = "2000mAh Powerbank Charging Case, 9D Hifi Stereo Sound, Low Latency Gaming Mode, Smart Touch Control.",
                    descriptionBn = "২০০০ মিলিঅ্যাম্পিয়ার চার্জিং কেস (পাওয়ারব্যাংক হিসেবেও ব্যবহারযোগ্য), চমৎকার বেস এবং ক্লিয়ার সাউন্ড।",
                    isFeatured = true
                ),
                ProductEntity(
                    title = "Curren Chronograph Luxury Men's Watch",
                    titleBn = "কারেন লাক্সারি মেনস ক্রোনোগ্রাফ রিস্ট ওয়াচ",
                    category = "fashion",
                    price = 1850,
                    originalPrice = 2800,
                    stock = 18,
                    imageUrl = "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=600&auto=format&fit=crop&q=80",
                    sizes = "Standard Free Size",
                    colors = "Gold Black, Silver Blue, Full Black",
                    description = "Original Quartz Movement, Genuine Leather Strap, Functional Chronograph Sub-dials, 30M Water Resistant.",
                    descriptionBn = "আসল কোয়ার্টজ মুভমেন্ট, খাঁটি চামড়ার বেল্ট, কার্যকরী ক্রোনোগ্রাফ সাব-ডায়াল এবং ৩০ মিটার পর্যন্ত ওয়াটার রেজিস্ট্যান্ট।",
                    isFeatured = true
                ),
                ProductEntity(
                    title = "Polarized Classic Aviator Sunglasses",
                    titleBn = "পোলারাইজড ক্ল্যাসিক এভিয়েটর সানগ্লাস",
                    category = "fashion",
                    price = 750,
                    originalPrice = 1400,
                    stock = 32,
                    imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600&auto=format&fit=crop&q=80",
                    sizes = "Standard",
                    colors = "Black, Golden, Silver Mirror",
                    description = "UV400 100% Protection Lens, Ultra Lightweight Alloy Metal Frame, Anti-Glare Driving Eyewear with Hard Box.",
                    descriptionBn = "ইউভি৪০০ শতভাগ সূর্যের অতিবেগুনী রশ্মি প্রতিরোধক, হালকা অ্যালয় ফ্রেম এবং সাথে রয়েছে মজবুত বক্স।",
                    isFeatured = false
                ),
                ProductEntity(
                    title = "Premium Semi-Drop Cotton Panjabi",
                    titleBn = "প্রিমিয়াম কটন সেমি-ড্রপ পাঞ্জাবি",
                    category = "clothing",
                    price = 1650,
                    originalPrice = 2400,
                    stock = 22,
                    imageUrl = "https://images.unsplash.com/photo-1609357605129-26f69add5d6e?w=600&auto=format&fit=crop&q=80",
                    sizes = "38, 40, 42, 44",
                    colors = "Navy Blue, Emerald Green, Maroon, White",
                    description = "100% Combed Cotton, Modern Minimal Collar Embroidery, Snap Buttons, Extremely Breathable & Elegant Fit.",
                    descriptionBn = "১০০% প্রিমিয়াম কম্বড কটন, নিখুঁত কলার এমব্রয়ডারি, স্ন্যাপ বাটন এবং মার্জিত আরামদায়ক ফিটিং।",
                    isFeatured = true
                ),
                ProductEntity(
                    title = "Export Quality Pique Cotton Polo Shirt",
                    titleBn = "এক্সপোর্ট কোয়ালিটি পিকে কটন পোলো শার্ট",
                    category = "clothing",
                    price = 590,
                    originalPrice = 850,
                    stock = 45,
                    imageUrl = "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600&auto=format&fit=crop&q=80",
                    sizes = "M, L, XL, XXL",
                    colors = "Black, White, Olive Green, Royal Blue",
                    description = "220 GSM Pique Fabric, Anti-Shrink, Colorfast Guaranteed, High Quality Rib Collar & Cuffs.",
                    descriptionBn = "২২০ জিএসএম প্রিমিয়াম পিকে কটন, রঙ ওঠার কোনো ভয় নেই, প্রিমিয়াম কলার ও নিখুঁত ফিনিশিং।",
                    isFeatured = false
                ),
                ProductEntity(
                    title = "Bioaqua Vitamin C Brightening Essence 30ml",
                    titleBn = "বায়োয়াকুয়া ভিটামিন সি ব্রাইটনিং সিরাম ৩০ মি.লি.",
                    category = "beauty",
                    price = 450,
                    originalPrice = 690,
                    stock = 30,
                    imageUrl = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80",
                    sizes = "30ml",
                    colors = "Standard",
                    description = "Enriched with Pure Vitamin C & Hyaluronic Acid, Fades Dark Spots, Evens Skin Tone and Restores Youthful Glow.",
                    descriptionBn = "খাঁটি ভিটামিন সি ও হায়ালুরোনিক অ্যাসিড সমৃদ্ধ, মেছতা ও দাগ দূর করে ত্বক উজ্জ্বল ও কোমল রাখে।",
                    isFeatured = false
                ),
                ProductEntity(
                    title = "Herbal Hair Growth & Anti-Hairfall Oil 200ml",
                    titleBn = "অরগানিক ভেষজ হেয়ার গ্রোথ অয়েল ২০০ মি.লি.",
                    category = "beauty",
                    price = 580,
                    originalPrice = 890,
                    stock = 25,
                    imageUrl = "https://images.unsplash.com/photo-1608248597359-0a86d262a0aa?w=600&auto=format&fit=crop&q=80",
                    sizes = "200ml",
                    colors = "Herbal",
                    description = "Made with 18 Ayurvedic Herbs including Onion Seed, Bhringraj, Rosemary & Amla. Stops Hairfall in 14 Days.",
                    descriptionBn = "১৮টি প্রাকৃতিক ভেষজ উপাদানে তৈরি, চুল পড়া বন্ধ করে এবং নতুন চুল গজাতে দারুণ কার্যকর।",
                    isFeatured = false
                ),
                ProductEntity(
                    title = "Remote Control 4WD Monster Rock Crawler",
                    titleBn = "রিমোট কন্ট্রোল ফোর-হুইল ড্রাইভ মনস্টার ট্রাক",
                    category = "toys",
                    price = 1450,
                    originalPrice = 2200,
                    stock = 15,
                    imageUrl = "https://images.unsplash.com/photo-1594787318286-3d835c1d207f?w=600&auto=format&fit=crop&q=80",
                    sizes = "Scale 1:18",
                    colors = "Red, Blue, Green",
                    description = "2.4GHz Wireless Remote, Independent Shock Absorbers, High Grip Rubber Off-road Tires, Rechargeable Battery.",
                    descriptionBn = "উচ্চ ক্ষমতার সাসপেনশন, যে কোনো রুক্ষ রাস্তায় চলার উপযোগী, সাথে রিচার্জেবল ব্যাটারি ও রিমোট।",
                    isFeatured = true
                ),
                ProductEntity(
                    title = "Kids 10.5\" LCD Writing & Drawing Tablet",
                    titleBn = "বাচ্চাদের ১০.৫ ইঞ্চি এলসিডি ড্রয়িং ট্যাবলেট",
                    category = "toys",
                    price = 390,
                    originalPrice = 650,
                    stock = 50,
                    imageUrl = "https://images.unsplash.com/photo-1584824486509-112e4181ff6b?w=600&auto=format&fit=crop&q=80",
                    sizes = "10.5 Inch",
                    colors = "Blue, Pink, Black",
                    description = "Paperless Drawing & Writing Board for Kids. Eyes Protection Non-Radiation Screen, One-Touch Clear Button.",
                    descriptionBn = "চোখের কোনো ক্ষতি করে না, কাগজের অপচয় ছাড়া শিশুদের লেখা ও আঁকার চমৎকার ডিজিটাল বোর্ড।",
                    isFeatured = false
                ),
                ProductEntity(
                    title = "12V Cordless Electric Drill & Screwdriver Set",
                    titleBn = "১২ ভোল্ট কর্ডলেস ইলেকট্রিক ড্রিল ও টুল কিট",
                    category = "tools",
                    price = 2250,
                    originalPrice = 3300,
                    stock = 14,
                    imageUrl = "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600&auto=format&fit=crop&q=80",
                    sizes = "Kit with 24 Accessories",
                    colors = "Orange & Black",
                    description = "12V Rechargeable Lithium Battery, Dual Speed Adjustment, High Torque Gear, Built-in LED Working Light.",
                    descriptionBn = "বাসাবাড়ি ও প্রফেশনাল কাজের জন্য আদর্শ, শক্তিশালী রিচার্জেবল ব্যাটারি এবং ২৪ পিস এক্সেসরিজ সেট।",
                    isFeatured = true
                ),
                ProductEntity(
                    title = "Stainless Steel 14-in-1 Multitool Plier",
                    titleBn = "স্টেইনলেস স্টিল ১৪-ইন-১ মাল্টিটুল প্লায়ার্স",
                    category = "tools",
                    price = 720,
                    originalPrice = 1100,
                    stock = 24,
                    imageUrl = "https://images.unsplash.com/photo-1586864387967-d02ef85d93e8?w=600&auto=format&fit=crop&q=80",
                    sizes = "Pocket Size",
                    colors = "Matte Black",
                    description = "Heavy Duty Carbon Stainless Steel, Wire Cutter, Knife, Saw, Bottle Opener, Screwdriver with Oxford Carry Pouch.",
                    descriptionBn = "মজবুত স্টিল দিয়ে তৈরি, পকেট সাইজ, ভ্রমণের সময় বা জরুরি প্রয়োজনে যেকোনো মেরামতে সহায়ক।",
                    isFeatured = false
                ),
                ProductEntity(
                    title = "Portable USB Rechargeable Juice Blender 380ml",
                    titleBn = "পোর্টেবল ইউএসবি রিচার্জেবল জুসার ব্লেন্ডার",
                    category = "other",
                    price = 850,
                    originalPrice = 1350,
                    stock = 28,
                    imageUrl = "https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=600&auto=format&fit=crop&q=80",
                    sizes = "380ml",
                    colors = "Mint Green, Sky Blue, Soft Pink",
                    description = "6-Blade 3D Fast Motor, USB Magnetic Charging, Food Grade BPA Free, Ideal for Shakes, Smoothies & Baby Food.",
                    descriptionBn = "৬টি ধারালো ব্লেড, সহজে বহনযোগ্য, যেকোনো জায়গায় তাজা ফলের জুস বা স্মুদি তৈরি করা যায়।",
                    isFeatured = true
                )
            )
            productDao.insertAll(initialList)
        }
    }
}
