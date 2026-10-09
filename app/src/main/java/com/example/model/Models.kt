package com.example.model

enum class ProductCategory(val id: String, val nameEn: String, val nameBn: String, val iconName: String) {
    ALL("all", "All", "সকল পণ্য", "Category"),
    GADGETS("gadgets", "Gadgets", "গ্যাজেটস", "Devices"),
    FASHION("fashion", "Fashion", "ফ্যাশন", "Watch"),
    CLOTHING("clothing", "Clothing", "পোশাক", "Checkroom"),
    BEAUTY_CARE("beauty", "Beauty Care", "বিউটি কেয়ার", "Spa"),
    TOYS("toys", "Toys", "খেলনা", "SmartToy"),
    TOOLS("tools", "Tools", "টুলস ও যন্ত্রপাতি", "Build"),
    OTHER("other", "Other Products", "অন্যান্য পণ্য", "Category");

    companion object {
        fun fromId(id: String): ProductCategory {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class DeliveryArea(val id: String, val titleBn: String, val titleEn: String, val charge: Int, val estimateBn: String) {
    INSIDE_DHAKA("dhaka_inside", "ঢাকার ভিতরে", "Inside Dhaka", 60, "১-২ কার্যদিবস"),
    OUTSIDE_DHAKA("dhaka_outside", "ঢাকার বাইরে (সমগ্র বাংলাদেশ)", "Outside Dhaka (All Bangladesh)", 120, "৩-৪ কার্যদিবস")
}

enum class OrderStatus(val code: String, val titleBn: String, val titleEn: String, val colorHex: Long) {
    PENDING("pending", "অপেক্ষমান", "Pending", 0xFFD97706),
    CONFIRMED("confirmed", "নিশ্চিত", "Confirmed", 0xFF2563EB),
    SHIPPED("shipped", "শিপিংয়ে আছে", "Shipped", 0xFF7C3AED),
    DELIVERED("delivered", "ডেলিভার্ড", "Delivered", 0xFF059669),
    CANCELLED("cancelled", "বাতিল", "Cancelled", 0xFFDC2626);

    companion object {
        fun fromCode(code: String): OrderStatus {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: PENDING
        }
    }
}

data class CartItem(
    val productId: Long,
    val title: String,
    val titleBn: String,
    val price: Int,
    val originalPrice: Int,
    val imageUrl: String,
    val selectedSize: String,
    val selectedColor: String,
    val quantity: Int,
    val maxStock: Int
)

data class OrderItemDto(
    val productId: Long,
    val title: String,
    val titleBn: String,
    val price: Int,
    val quantity: Int,
    val size: String,
    val color: String
)
