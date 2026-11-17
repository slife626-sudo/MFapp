package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.i18n.AppStrings
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CategoryCatalog {
  val iconOptions: List<Pair<String, ImageVector>> = listOf(
    "food" to Icons.Rounded.Restaurant,
    "transport" to Icons.Rounded.DirectionsCar,
    "groceries" to Icons.Rounded.ShoppingCart,
    "utilities" to Icons.Rounded.ReceiptLong,
    "shopping" to Icons.Rounded.ShoppingBag,
    "health" to Icons.Rounded.LocalHospital,
    "education" to Icons.Rounded.School,
    "entertainment" to Icons.Rounded.Movie,
    "family" to Icons.Rounded.Favorite,
    "home" to Icons.Rounded.Home,
    "pets" to Icons.Rounded.Pets,
    "fitness" to Icons.Rounded.FitnessCenter,
    "travel" to Icons.Rounded.TravelExplore,
    "wallet" to Icons.Rounded.AccountBalanceWallet,
    "other" to Icons.Rounded.Category,
  )

  val colorOptions: List<Long> = listOf(
    0xFF0E7C5B,
    0xFF2563EB,
    0xFFD97706,
    0xFFDC2626,
    0xFF7C3AED,
    0xFF0891B2,
    0xFFDB2777,
    0xFF4F46E5,
    0xFF65A30D,
    0xFFEA580C,
  )

  fun builtInCategories(strings: AppStrings): List<CategoryItem> = listOf(
    CategoryItem("food", strings.catFood, "food", 0xFF0E7C5B),
    CategoryItem("groceries", strings.catGroceries, "groceries", 0xFF65A30D),
    CategoryItem("transport", strings.catTransport, "transport", 0xFF2563EB),
    CategoryItem("utilities", strings.catUtilities, "utilities", 0xFFD97706),
    CategoryItem("shopping", strings.catShopping, "shopping", 0xFFDB2777),
    CategoryItem("health", strings.catHealth, "health", 0xFFDC2626),
    CategoryItem("education", strings.catEducation, "education", 0xFF4F46E5),
    CategoryItem("entertainment", strings.catEntertainment, "entertainment", 0xFF7C3AED),
    CategoryItem("family", strings.catFamily, "family", 0xFFEA580C),
    CategoryItem("other", strings.catOther, "other", 0xFF0891B2),
  )

  fun mergeCategories(
    strings: AppStrings,
    customCategories: List<CustomCategory>,
  ): List<CategoryItem> {
    val builtIn = builtInCategories(strings)
    val custom = customCategories.map {
      CategoryItem(
        id = it.id,
        name = it.name,
        iconKey = it.iconKey,
        colorHex = it.colorHex,
        isCustom = true,
      )
    }
    return builtIn + custom
  }

  fun iconFor(key: String): ImageVector =
    iconOptions.firstOrNull { it.first == key }?.second ?: Icons.Rounded.Category
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

  fun formatDateTime(millis: Long, langCode: String): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.forLanguageTag(langCode)).format(Date(millis))
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
}
