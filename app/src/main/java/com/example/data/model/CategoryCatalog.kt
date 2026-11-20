package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Games
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Healing
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.i18n.AppStrings
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class IconChoice(
  val key: String,
  val vector: ImageVector,
  val labelBn: String,
  val labelEn: String,
  val keywords: String = "",
)

data class IconGroup(
  val id: String,
  val labelBn: String,
  val labelEn: String,
  val icons: List<IconChoice>,
)

data class AccountPreset(
  val id: String,
  val nameBn: String,
  val nameEn: String,
  val method: PaymentMethod,
  val iconKey: String,
  val colorHex: Long,
)

object CategoryCatalog {

  val iconGroups: List<IconGroup> = listOf(
    IconGroup(
      id = "finance",
      labelBn = "অর্থ ও ব্যাংক",
      labelEn = "Finance & Bank",
      icons = listOf(
        IconChoice("wallet", Icons.Rounded.AccountBalanceWallet, "ওয়ালেট", "Wallet", "cash wallet purse"),
        IconChoice("bank", Icons.Rounded.AccountBalance, "ব্যাংক", "Bank", "bank savings account"),
        IconChoice("card", Icons.Rounded.CreditCard, "কার্ড", "Card", "credit debit visa mastercard"),
        IconChoice("mobile_pay", Icons.Rounded.PhoneAndroid, "মোবাইল পে", "Mobile Pay", "bkash nagad rocket upay"),
        IconChoice("cash", Icons.Rounded.Payments, "নগদ", "Cash", "money cash taka"),
        IconChoice("savings", Icons.Rounded.Savings, "সঞ্চয়", "Savings", "piggy bank save deposit"),
        IconChoice("trending", Icons.Rounded.TrendingUp, "বিনিয়োগ", "Investment", "stock mutual fund profit"),
        IconChoice("salary", Icons.Rounded.AttachMoney, "বেতন / আয়", "Salary / Income", "income salary bonus"),
        IconChoice("paid", Icons.Rounded.Paid, "মুদ্রা", "Currency", "coin taka dollar"),
        IconChoice("exchange", Icons.Rounded.CurrencyExchange, "লেনদেন", "Exchange", "transfer exchange"),
        IconChoice("calculator", Icons.Rounded.Calculate, "হিসাব", "Accounting", "ledger math tax"),
      ),
    ),
    IconGroup(
      id = "food",
      labelBn = "খাবার ও বাজার",
      labelEn = "Food & Dining",
      icons = listOf(
        IconChoice("food", Icons.Rounded.Restaurant, "রেস্তোরাঁ", "Dining", "meal lunch dinner restaurant"),
        IconChoice("groceries", Icons.Rounded.ShoppingCart, "বাজার", "Groceries", "bazaar market vegetables"),
        IconChoice("coffee", Icons.Rounded.Coffee, "চা / কফি", "Coffee", "tea cafe snack"),
        IconChoice("fastfood", Icons.Rounded.Fastfood, "ফাস্টফুড", "Fast Food", "burger pizza snacks"),
        IconChoice("bakery", Icons.Rounded.BakeryDining, "বেকারি", "Bakery", "bread cake breakfast"),
        IconChoice("kitchen", Icons.Rounded.Kitchen, "রান্নাঘর", "Kitchen", "cooking grocery pantry"),
      ),
    ),
    IconGroup(
      id = "transport",
      labelBn = "যাতায়াত ও গাড়ি",
      labelEn = "Transport & Auto",
      icons = listOf(
        IconChoice("transport", Icons.Rounded.DirectionsCar, "গাড়ি", "Car", "car auto drive"),
        IconChoice("bus", Icons.Rounded.DirectionsBus, "বাস", "Bus", "public transit bus"),
        IconChoice("taxi", Icons.Rounded.LocalTaxi, "ট্যাক্সি / রাইড", "Taxi / Ride", "uber pathao cng rickshaw"),
        IconChoice("bike", Icons.Rounded.TwoWheeler, "বাইক", "Bike", "motorcycle scooter"),
        IconChoice("train", Icons.Rounded.Train, "ট্রেন / মেট্রো", "Train", "rail metro"),
        IconChoice("fuel", Icons.Rounded.LocalGasStation, "জ্বালানি / তেল", "Fuel", "petrol octane gas"),
        IconChoice("flight", Icons.Rounded.Flight, "বিমান", "Flight", "air travel trip"),
        IconChoice("travel", Icons.Rounded.TravelExplore, "ভ্রমণ", "Travel", "tour vacation"),
        IconChoice("shipping", Icons.Rounded.LocalShipping, "ডেলিভারি", "Delivery", "courier parcel"),
      ),
    ),
    IconGroup(
      id = "home",
      labelBn = "বাসা ও বিল",
      labelEn = "Home & Utilities",
      icons = listOf(
        IconChoice("home", Icons.Rounded.Home, "বাসা ভাড়া", "Home / Rent", "house rent apartment"),
        IconChoice("apartment", Icons.Rounded.Apartment, "ফ্ল্যাট", "Apartment", "building flat"),
        IconChoice("utilities", Icons.Rounded.ReceiptLong, "বিল", "Bills", "receipt invoice utility"),
        IconChoice("electric", Icons.Rounded.Bolt, "বিদ্যুৎ", "Electricity", "power current bill"),
        IconChoice("water", Icons.Rounded.WaterDrop, "পানি / গ্যাস", "Water / Gas", "water bill gas"),
        IconChoice("wifi", Icons.Rounded.Wifi, "ইন্টারনেট", "Internet / WiFi", "broadband data recharge"),
        IconChoice("cleaning", Icons.Rounded.CleaningServices, "পরিচ্ছন্নতা", "Cleaning", "maid laundry homecare"),
        IconChoice("repair", Icons.Rounded.Handyman, "মেরামত", "Repairs", "maintenance tools plumber"),
        IconChoice("hotel", Icons.Rounded.Hotel, "হোটেল", "Hotel", "stay lodge"),
      ),
    ),
    IconGroup(
      id = "shopping",
      labelBn = "কেনাকাটা",
      labelEn = "Shopping",
      icons = listOf(
        IconChoice("shopping", Icons.Rounded.ShoppingBag, "শপিং", "Shopping", "mall purchase bag"),
        IconChoice("store", Icons.Rounded.Store, "দোকান", "Store", "shop superstore"),
        IconChoice("grocery_store", Icons.Rounded.LocalGroceryStore, "সুপারশপ", "Market", "supermarket basket"),
        IconChoice("clothes", Icons.Rounded.Checkroom, "পোশাক", "Clothing", "dress shirt shoes fashion"),
        IconChoice("gift", Icons.Rounded.CardGiftcard, "উপহার", "Gifts", "present birthday wedding"),
        IconChoice("redeem", Icons.Rounded.Redeem, "বোনাস / রিওয়ার্ড", "Rewards", "voucher coupon prize"),
      ),
    ),
    IconGroup(
      id = "health",
      labelBn = "স্বাস্থ্য ও যত্ন",
      labelEn = "Health & Care",
      icons = listOf(
        IconChoice("health", Icons.Rounded.LocalHospital, "হাসপাতাল", "Hospital", "doctor clinic checkup"),
        IconChoice("pharmacy", Icons.Rounded.LocalPharmacy, "ওষুধ", "Pharmacy", "medicine medical"),
        IconChoice("healing", Icons.Rounded.Healing, "চিকিৎসা", "Medical", "first aid therapy"),
        IconChoice("fitness", Icons.Rounded.FitnessCenter, "জিম ও ফিটনেস", "Fitness", "gym workout exercise"),
        IconChoice("spa", Icons.Rounded.Spa, "পার্সোনাল কেয়ার", "Personal Care", "salon beauty grooming"),
      ),
    ),
    IconGroup(
      id = "education",
      labelBn = "শিক্ষা ও কাজ",
      labelEn = "Education & Work",
      icons = listOf(
        IconChoice("education", Icons.Rounded.School, "শিক্ষা", "Education", "tuition school college"),
        IconChoice("book", Icons.Rounded.MenuBook, "বই", "Books", "study stationery notebook"),
        IconChoice("reading", Icons.Rounded.Book, "কোর্স", "Course", "library reading"),
        IconChoice("work", Icons.Rounded.Work, "চাকরি", "Work", "office job"),
        IconChoice("business", Icons.Rounded.BusinessCenter, "ব্যবসা", "Business", "client freelance company"),
      ),
    ),
    IconGroup(
      id = "tech",
      labelBn = "প্রযুক্তি ও বিনোদন",
      labelEn = "Tech & Leisure",
      icons = listOf(
        IconChoice("smartphone", Icons.Rounded.Smartphone, "মোবাইল", "Mobile", "phone recharge"),
        IconChoice("laptop", Icons.Rounded.LaptopMac, "ল্যাপটপ", "Laptop", "mac pc hardware"),
        IconChoice("computer", Icons.Rounded.Computer, "কম্পিউটার", "Computer", "desktop software"),
        IconChoice("camera", Icons.Rounded.CameraAlt, "ক্যামেরা / ফটো", "Camera", "photo media"),
        IconChoice("entertainment", Icons.Rounded.Movie, "সিনেমা", "Movies", "cinema theater netflix"),
        IconChoice("music", Icons.Rounded.MusicNote, "মিউজিক", "Music", "spotify audio concert"),
        IconChoice("headphones", Icons.Rounded.Headphones, "অডিও / গ্যাজেট", "Audio", "earphone gadget"),
        IconChoice("gaming", Icons.Rounded.SportsEsports, "গেমিং", "Gaming", "ps5 xbox game"),
        IconChoice("games", Icons.Rounded.Games, "খেলা", "Games", "hobby fun"),
        IconChoice("subscriptions", Icons.Rounded.Subscriptions, "সাবস্ক্রিপশন", "Subscriptions", "monthly ott membership"),
      ),
    ),
    IconGroup(
      id = "family",
      labelBn = "পরিবার ও অন্যান্য",
      labelEn = "Family & Misc",
      icons = listOf(
        IconChoice("family", Icons.Rounded.Favorite, "পরিবার", "Family", "home care love"),
        IconChoice("kids", Icons.Rounded.ChildCare, "শিশু", "Kids", "baby child diaper"),
        IconChoice("pets", Icons.Rounded.Pets, "পোষা প্রাণী", "Pets", "cat dog vet"),
        IconChoice("charity", Icons.Rounded.VolunteerActivism, "দান / সাহায্য", "Charity", "zakat sadaqah donation"),
        IconChoice("trophy", Icons.Rounded.EmojiEvents, "পুরস্কার", "Award", "bonus prize win"),
        IconChoice("other", Icons.Rounded.Category, "অন্যান্য", "Other", "misc general tag"),
      ),
    ),
  )

  val allIconChoices: List<IconChoice> = iconGroups.flatMap { it.icons }

  val iconOptions: List<Pair<String, ImageVector>> = allIconChoices.map { it.key to it.vector }

  val colorOptions: List<Long> = listOf(
    0xFF0E7C5B,
    0xFF0B5D4B,
    0xFF10B981,
    0xFF65A30D,
    0xFF0891B2,
    0xFF0284C7,
    0xFF2563EB,
    0xFF4F46E5,
    0xFF7C3AED,
    0xFF9333EA,
    0xFFDB2777,
    0xFFE11D48,
    0xFFDC2626,
    0xFFEA580C,
    0xFFD97706,
    0xFFCA8A04,
    0xFF475569,
    0xFF334155,
  )

  val accountPresets: List<AccountPreset> = listOf(
    AccountPreset("preset_cash", "নগদ ওয়ালেট", "Cash Wallet", PaymentMethod.CASH, "wallet", 0xFF0E7C5B),
    AccountPreset("preset_bank", "ব্যাংক অ্যাকাউন্ট", "Bank Account", PaymentMethod.BANK, "bank", 0xFF2563EB),
    AccountPreset("preset_mobile", "মোবাইল ব্যাংকিং", "Mobile Pay", PaymentMethod.MOBILE, "mobile_pay", 0xFFDB2777),
    AccountPreset("preset_card", "কার্ড অ্যাকাউন্ট", "Card Account", PaymentMethod.CARD, "card", 0xFFD97706),
    AccountPreset("preset_savings", "সেভিংস / ডিপিএস", "Savings / DPS", PaymentMethod.BANK, "savings", 0xFF0891B2),
  )

  fun builtInCategories(strings: AppStrings): List<CategoryItem> = listOf(
    CategoryItem("food", strings.catFood, "food", 0xFF0E7C5B, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("groceries", strings.catGroceries, "groceries", 0xFF65A30D, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("transport", strings.catTransport, "transport", 0xFF2563EB, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("utilities", strings.catUtilities, "utilities", 0xFFD97706, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("shopping", strings.catShopping, "shopping", 0xFFDB2777, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("health", strings.catHealth, "health", 0xFFDC2626, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("education", strings.catEducation, "education", 0xFF4F46E5, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("entertainment", strings.catEntertainment, "entertainment", 0xFF7C3AED, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("family", strings.catFamily, "family", 0xFFEA580C, flowType = CategoryFlowType.EXPENSE),
    CategoryItem("other", strings.catOther, "other", 0xFF0891B2, flowType = CategoryFlowType.EXPENSE),
  )

  fun builtInIncomeCategories(strings: AppStrings): List<CategoryItem> = listOf(
    CategoryItem("inc_salary", strings.catSalary, "salary", 0xFF0E7C5B, flowType = CategoryFlowType.INCOME),
    CategoryItem("inc_business", strings.catBusiness, "business", 0xFF2563EB, flowType = CategoryFlowType.INCOME),
    CategoryItem("inc_investment", strings.catInvestment, "trending", 0xFF7C3AED, flowType = CategoryFlowType.INCOME),
    CategoryItem("inc_other", strings.catOtherIncome, "savings", 0xFF0891B2, flowType = CategoryFlowType.INCOME),
  )

  fun mergeCategories(
    strings: AppStrings,
    customCategories: List<CustomCategory>,
    includeIncome: Boolean = false,
  ): List<CategoryItem> {
    val builtInExpense = builtInCategories(strings)
    val builtInIncome = if (includeIncome) builtInIncomeCategories(strings) else emptyList()
    val custom = customCategories
      .filter { cat ->
        val flow = CategoryFlowType.fromWire(cat.categoryType)
        includeIncome || flow == CategoryFlowType.EXPENSE
      }
      .map {
        CategoryItem(
          id = it.id,
          name = it.name,
          iconKey = it.iconKey,
          colorHex = it.colorHex,
          isCustom = true,
          flowType = CategoryFlowType.fromWire(it.categoryType),
        )
      }
    return builtInExpense + builtInIncome + custom
  }

  fun iconFor(key: String): ImageVector = when (key) {
    "tag" -> Icons.Rounded.Category
    else -> allIconChoices.firstOrNull { it.key == key }?.vector ?: Icons.Rounded.Category
  }
}

object DateFormatUtils {
  fun monthKeyFromMillis(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    return String.format(Locale.US, "%04d-%02d", year, month)
  }

  fun currentMonthKey(): String = monthKeyFromMillis(System.currentTimeMillis())

  fun shiftMonthKey(monthKey: String, deltaMonths: Int): String {
    val parts = monthKey.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, 1)
      add(Calendar.MONTH, deltaMonths)
    }
    return monthKeyFromMillis(cal.timeInMillis)
  }

  fun monthRangeMillis(monthKey: String): Pair<Long, Long> {
    val parts = monthKey.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
    val start = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val end = (start.clone() as Calendar).apply {
      add(Calendar.MONTH, 1)
    }
    return start.timeInMillis to (end.timeInMillis - 1L)
  }

  fun periodRangeMillis(filter: PeriodFilter, customMonthKey: String): Pair<Long, Long> {
    val now = Calendar.getInstance()
    return when (filter) {
      PeriodFilter.THIS_MONTH -> monthRangeMillis(currentMonthKey())
      PeriodFilter.LAST_MONTH -> monthRangeMillis(shiftMonthKey(currentMonthKey(), -1))
      PeriodFilter.CUSTOM_MONTH -> monthRangeMillis(customMonthKey)
      PeriodFilter.THIS_QUARTER -> {
        val currentQuarterStartMonth = (now.get(Calendar.MONTH) / 3) * 3
        val start = Calendar.getInstance().apply {
          set(Calendar.YEAR, now.get(Calendar.YEAR))
          set(Calendar.MONTH, currentQuarterStartMonth)
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 3) }
        start.timeInMillis to (end.timeInMillis - 1L)
      }
      PeriodFilter.THIS_YEAR -> {
        val start = Calendar.getInstance().apply {
          set(Calendar.YEAR, now.get(Calendar.YEAR))
          set(Calendar.MONTH, Calendar.JANUARY)
          set(Calendar.DAY_OF_MONTH, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.YEAR, 1) }
        start.timeInMillis to (end.timeInMillis - 1L)
      }
    }
  }

  fun daysInMonth(monthKey: String): Int {
    val parts = monthKey.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  fun formatMonthLabel(monthKey: String, langCode: String): String {
    val parts = monthKey.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    val locale = Locale.forLanguageTag(langCode)
    val sdf = SimpleDateFormat("MMMM yyyy", locale)
    return sdf.format(cal.time)
  }

  fun formatShortMonthLabel(monthKey: String, langCode: String): String {
    val parts = monthKey.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    val locale = Locale.forLanguageTag(langCode)
    return SimpleDateFormat("MMM", locale).format(cal.time)
  }

  fun formatDayKey(millis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
  }

  fun formatDayHeader(millis: Long, todayLabel: String, yesterdayLabel: String, langCode: String): String {
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    val now = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
      isSameDay(target, now) -> todayLabel
      isSameDay(target, yesterday) -> yesterdayLabel
      else -> SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag(langCode)).format(Date(millis))
    }
  }

  fun formatDateCompact(millis: Long, langCode: String): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag(langCode)).format(Date(millis))
  }

  fun formatTimeOnly(millis: Long, langCode: String, use24Hour: Boolean): String {
    val pattern = if (use24Hour) "HH:mm" else "hh:mm a"
    return SimpleDateFormat(pattern, Locale.forLanguageTag(langCode)).format(Date(millis))
  }

  fun formatDateTime(millis: Long, langCode: String, use24Hour: Boolean = false): String {
    val pattern = if (use24Hour) "dd MMM yyyy, HH:mm" else "dd MMM yyyy, hh:mm a"
    return SimpleDateFormat(pattern, Locale.forLanguageTag(langCode)).format(Date(millis))
  }

  fun isToday(millis: Long): Boolean {
    if (millis <= 0L) return false
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    return isSameDay(target, Calendar.getInstance())
  }

  private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
      a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

object CurrencyFormat {
  fun format(amount: Double, symbol: String): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
      maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
      minimumFractionDigits = 0
    }
    return "$symbol${nf.format(amount)}"
  }

  fun formatSigned(amount: Double, symbol: String): String {
    return if (amount < 0.0) {
      "-${format(-amount, symbol)}"
    } else {
      format(amount, symbol)
    }
  }
}
