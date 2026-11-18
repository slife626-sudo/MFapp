package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

enum class PaymentMethod(val wireValue: String) {
  CASH("CASH"),
  CARD("CARD"),
  MOBILE("MOBILE"),
  BANK("BANK");

  companion object {
    fun fromWire(value: String?): PaymentMethod =
      entries.firstOrNull { it.wireValue == value } ?: CASH
  }
}

enum class CurrencyOption(val code: String, val symbol: String, val label: String) {
  BDT("BDT", "৳", "BDT (৳)"),
  USD("USD", "$", "USD ($)"),
  INR("INR", "₹", "INR (₹)"),
  EUR("EUR", "€", "EUR (€)"),
  GBP("GBP", "£", "GBP (£)");

  companion object {
    fun fromCode(code: String?): CurrencyOption =
      entries.firstOrNull { it.code == code } ?: BDT
  }
}

@Keep
@IgnoreExtraProperties
data class Expense(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val amount: Double = 0.0,
  val categoryId: String = "food",
  val paymentMethod: String = PaymentMethod.CASH.wireValue,
  val note: String = "",
  val dateMillis: Long = 0L,
  val monthKey: String = "",
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Income(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val amount: Double = 0.0,
  val paymentMethod: String = PaymentMethod.CASH.wireValue,
  val note: String = "",
  val dateMillis: Long = 0L,
  val monthKey: String = "",
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AccountTransfer(
  @DocumentId val id: String = "",
  val userId: String = "",
  val fromMethod: String = PaymentMethod.BANK.wireValue,
  val toMethod: String = PaymentMethod.CASH.wireValue,
  val amount: Double = 0.0,
  val note: String = "",
  val dateMillis: Long = 0L,
  val monthKey: String = "",
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Payable(
  @DocumentId val id: String = "",
  val userId: String = "",
  val personName: String = "",
  val amount: Double = 0.0,
  val paidAmount: Double = 0.0,
  val paymentMethod: String = PaymentMethod.CASH.wireValue,
  val note: String = "",
  val dateMillis: Long = 0L,
  val monthKey: String = "",
  val settled: Boolean = false,
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class PayablePayment(
  @DocumentId val id: String = "",
  val userId: String = "",
  val payableId: String = "",
  val personName: String = "",
  val amount: Double = 0.0,
  val paymentMethod: String = PaymentMethod.CASH.wireValue,
  val note: String = "",
  val dateMillis: Long = 0L,
  val monthKey: String = "",
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Receivable(
  @DocumentId val id: String = "",
  val userId: String = "",
  val personName: String = "",
  val amount: Double = 0.0,
  val collectedAmount: Double = 0.0,
  val paymentMethod: String = PaymentMethod.CASH.wireValue,
  val note: String = "",
  val dateMillis: Long = 0L,
  val monthKey: String = "",
  val settled: Boolean = false,
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ReceivablePayment(
  @DocumentId val id: String = "",
  val userId: String = "",
  val receivableId: String = "",
  val personName: String = "",
  val amount: Double = 0.0,
  val paymentMethod: String = PaymentMethod.CASH.wireValue,
  val note: String = "",
  val dateMillis: Long = 0L,
  val monthKey: String = "",
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class MarketList(
  @DocumentId val id: String = "",
  val ownerUid: String = "",
  val ownerEmail: String = "",
  val ownerName: String = "",
  val title: String = "",
  val sharedWithEmail: String = "",
  val memberEmails: List<String> = emptyList(),
  val note: String = "",
  val completed: Boolean = false,
  val dateMillis: Long = 0L,
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class MarketListItem(
  @DocumentId val id: String = "",
  val listId: String = "",
  val ownerUid: String = "",
  val memberEmails: List<String> = emptyList(),
  val addedByEmail: String = "",
  val name: String = "",
  val quantity: String = "",
  val estimatedPrice: Double = 0.0,
  val bought: Boolean = false,
  val note: String = "",
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CustomCategory(
  @DocumentId val id: String = "",
  val userId: String = "",
  val name: String = "",
  val iconKey: String = "tag",
  val colorHex: Long = 0xFF0E7C5B,
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class MonthlyBudget(
  @DocumentId val id: String = "",
  val userId: String = "",
  val monthKey: String = "",
  val totalLimit: Double = 0.0,
  val categoryLimits: Map<String, Double> = emptyMap(),
  val currencyCode: String = CurrencyOption.BDT.code,
  @com.google.firebase.firestore.ServerTimestamp val createdAt: Timestamp? = null,
  @com.google.firebase.firestore.ServerTimestamp val updatedAt: Timestamp? = null,
)

data class CategoryItem(
  val id: String,
  val name: String,
  val iconKey: String,
  val colorHex: Long,
  val isCustom: Boolean = false,
)

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}
