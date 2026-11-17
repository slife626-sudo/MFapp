package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.CustomCategory
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentMethod
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class SyncSnapshot<T>(
  val items: List<T>,
  val hasPendingWrites: Boolean,
  val isFromCache: Boolean,
)

class ExpenseRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireMatchingUserId(expectedUserId: String): String {
    val uid = auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
    return uid
  }

  fun observeExpenses(userId: String): Flow<SyncSnapshot<Expense>> {
    val path = "users/$userId/expenses"
    return db.collection("users").document(userId).collection("expenses")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        val list = snapshot.toObjects(
          Expense::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedByDescending { it.dateMillis }
        SyncSnapshot(
          items = list,
          hasPendingWrites = snapshot.metadata.hasPendingWrites(),
          isFromCache = snapshot.metadata.isFromCache,
        )
      }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
          handleFirestoreError(exceptionToLog, OperationType.LIST, path)
        }
        throw error
      }
  }

  fun observeIncomes(userId: String): Flow<SyncSnapshot<Income>> {
    val path = "users/$userId/incomes"
    return db.collection("users").document(userId).collection("incomes")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        val list = snapshot.toObjects(
          Income::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedByDescending { it.dateMillis }
        SyncSnapshot(
          items = list,
          hasPendingWrites = snapshot.metadata.hasPendingWrites(),
          isFromCache = snapshot.metadata.isFromCache,
        )
      }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
          handleFirestoreError(exceptionToLog, OperationType.LIST, path)
        }
        throw error
      }
  }

  fun observeCustomCategories(userId: String): Flow<List<CustomCategory>> {
    val path = "users/$userId/categories"
    return db.collection("users").document(userId).collection("categories")
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          CustomCategory::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedBy { it.name.lowercase() }
      }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
          handleFirestoreError(exceptionToLog, OperationType.LIST, path)
        }
        throw error
      }
  }

  fun observeBudgets(userId: String): Flow<List<MonthlyBudget>> {
    val path = "users/$userId/budgets"
    return db.collection("users").document(userId).collection("budgets")
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          MonthlyBudget::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
          handleFirestoreError(exceptionToLog, OperationType.LIST, path)
        }
        throw error
      }
  }

  suspend fun addExpense(
    userId: String,
    title: String,
    amount: Double,
    categoryId: String,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ): Result<Unit> {
    var docPath = "users/$userId/expenses"
    return try {
      val uid = requireMatchingUserId(userId)
      val cleanTitle = title.trim().take(120)
      val cleanCategory = categoryId.trim().take(64)
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(cleanTitle.isNotEmpty()) { "Title cannot be empty" }
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }
      require(cleanCategory.isNotEmpty()) { "Category cannot be empty" }

      val docRef = db.collection("users").document(uid).collection("expenses").document()
      docPath = docRef.path
      val payload = mapOf(
        "userId" to uid,
        "title" to cleanTitle,
        "amount" to amount,
        "categoryId" to cleanCategory,
        "paymentMethod" to paymentMethod.wireValue,
        "note" to cleanNote,
        "dateMillis" to validDateMillis,
        "monthKey" to monthKey,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateExpense(
    userId: String,
    expenseId: String,
    title: String,
    amount: Double,
    categoryId: String,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ): Result<Unit> {
    val docPath = "users/$userId/expenses/$expenseId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(expenseId.isNotBlank()) { "Expense ID required" }
      val cleanTitle = title.trim().take(120)
      val cleanCategory = categoryId.trim().take(64)
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(cleanTitle.isNotEmpty()) { "Title cannot be empty" }
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      val docRef = db.collection("users").document(uid).collection("expenses").document(expenseId)
      docRef.update(
        mapOf(
          "title" to cleanTitle,
          "amount" to amount,
          "categoryId" to cleanCategory,
          "paymentMethod" to paymentMethod.wireValue,
          "note" to cleanNote,
          "dateMillis" to validDateMillis,
          "monthKey" to monthKey,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> {
    val docPath = "users/$userId/expenses/$expenseId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(expenseId.isNotBlank()) { "Expense ID required" }
      db.collection("users").document(uid).collection("expenses").document(expenseId)
        .delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun addIncome(
    userId: String,
    title: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ): Result<Unit> {
    var docPath = "users/$userId/incomes"
    return try {
      val uid = requireMatchingUserId(userId)
      val cleanTitle = title.trim().take(120)
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(cleanTitle.isNotEmpty()) { "Title cannot be empty" }
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      val docRef = db.collection("users").document(uid).collection("incomes").document()
      docPath = docRef.path
      val payload = mapOf(
        "userId" to uid,
        "title" to cleanTitle,
        "amount" to amount,
        "paymentMethod" to paymentMethod.wireValue,
        "note" to cleanNote,
        "dateMillis" to validDateMillis,
        "monthKey" to monthKey,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateIncome(
    userId: String,
    incomeId: String,
    title: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
  ): Result<Unit> {
    val docPath = "users/$userId/incomes/$incomeId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(incomeId.isNotBlank()) { "Income ID required" }
      val cleanTitle = title.trim().take(120)
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(cleanTitle.isNotEmpty()) { "Title cannot be empty" }
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      val docRef = db.collection("users").document(uid).collection("incomes").document(incomeId)
      docRef.update(
        mapOf(
          "title" to cleanTitle,
          "amount" to amount,
          "paymentMethod" to paymentMethod.wireValue,
          "note" to cleanNote,
          "dateMillis" to validDateMillis,
          "monthKey" to monthKey,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deleteIncome(userId: String, incomeId: String): Result<Unit> {
    val docPath = "users/$userId/incomes/$incomeId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(incomeId.isNotBlank()) { "Income ID required" }
      db.collection("users").document(uid).collection("incomes").document(incomeId)
        .delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun addCustomCategory(
    userId: String,
    name: String,
    iconKey: String,
    colorHex: Long,
  ): Result<Unit> {
    var docPath = "users/$userId/categories"
    return try {
      val uid = requireMatchingUserId(userId)
      val cleanName = name.trim().take(40)
      require(cleanName.isNotEmpty()) { "Category name cannot be empty" }
      val docRef = db.collection("users").document(uid).collection("categories").document()
      docPath = docRef.path
      docRef.set(
        mapOf(
          "userId" to uid,
          "name" to cleanName,
          "iconKey" to iconKey.trim().take(32).ifEmpty { "other" },
          "colorHex" to colorHex,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deleteCustomCategory(userId: String, categoryId: String): Result<Unit> {
    val docPath = "users/$userId/categories/$categoryId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(categoryId.isNotBlank()) { "Category ID required" }
      db.collection("users").document(uid).collection("categories").document(categoryId)
        .delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun saveMonthlyBudget(
    userId: String,
    monthKey: String,
    totalLimit: Double,
    categoryLimits: Map<String, Double>,
    currencyCode: String,
    alreadyExists: Boolean,
  ): Result<Unit> {
    val docPath = "users/$userId/budgets/$monthKey"
    return try {
      val uid = requireMatchingUserId(userId)
      require(monthKey == "default" || monthKey.matches(Regex("^[0-9]{4}-[0-9]{2}$"))) {
        "Invalid monthKey"
      }
      val safeTotal = totalLimit.coerceIn(0.0, 1_000_000_000.0)
      val safeCategoryLimits = categoryLimits
        .filter { it.key.isNotBlank() && it.value >= 0.0 && it.value <= 1_000_000_000.0 }
        .entries.take(30).associate { it.key to it.value }
      val safeCurrency = currencyCode.trim().take(8).ifEmpty { "BDT" }

      val docRef = db.collection("users").document(uid).collection("budgets").document(monthKey)
      if (alreadyExists) {
        docRef.update(
          mapOf(
            "totalLimit" to safeTotal,
            "categoryLimits" to safeCategoryLimits,
            "currencyCode" to safeCurrency,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      } else {
        docRef.set(
          mapOf(
            "userId" to uid,
            "monthKey" to monthKey,
            "totalLimit" to safeTotal,
            "categoryLimits" to safeCategoryLimits,
            "currencyCode" to safeCurrency,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  suspend fun setCloudSyncEnabled(enabled: Boolean): Result<Unit> {
    return try {
      if (enabled) {
        db.enableNetwork().await()
      } else {
        db.disableNetwork().await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun importBackup(
    userId: String,
    expenses: List<Expense>,
    customCategories: List<CustomCategory>,
    budgets: List<MonthlyBudget>,
    existingCategoryIds: Set<String>,
    existingBudgetIds: Set<String>,
    incomes: List<Income> = emptyList(),
  ): Result<Int> {
    return try {
      val uid = requireMatchingUserId(userId)
      var importedCount = 0

      for (cat in customCategories) {
        val safeId = sanitizeId(cat.id)
        if (safeId.isNotEmpty() && cat.name.isNotBlank()) {
          val docRef = db.collection("users").document(uid).collection("categories").document(safeId)
          if (safeId in existingCategoryIds) {
            docRef.update(
              mapOf(
                "name" to cat.name.trim().take(40),
                "iconKey" to cat.iconKey.trim().take(32).ifEmpty { "other" },
                "colorHex" to cat.colorHex,
                "updatedAt" to FieldValue.serverTimestamp(),
              )
            ).await()
          } else {
            docRef.set(
              mapOf(
                "userId" to uid,
                "name" to cat.name.trim().take(40),
                "iconKey" to cat.iconKey.trim().take(32).ifEmpty { "other" },
                "colorHex" to cat.colorHex,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
              )
            ).await()
          }
        }
      }

      for (budget in budgets) {
        val mk = budget.monthKey
        if (mk == "default" || mk.matches(Regex("^[0-9]{4}-[0-9]{2}$"))) {
          saveMonthlyBudget(
            userId = uid,
            monthKey = mk,
            totalLimit = budget.totalLimit,
            categoryLimits = budget.categoryLimits,
            currencyCode = budget.currencyCode,
            alreadyExists = mk in existingBudgetIds,
          )
        }
      }

      for (exp in expenses) {
        val safeTitle = exp.title.trim().take(120)
        if (safeTitle.isEmpty() || exp.amount <= 0.0) continue
        val validDate = if (exp.dateMillis > 0L) exp.dateMillis else System.currentTimeMillis()
        val monthKey = DateFormatUtils.monthKeyFromMillis(validDate)
        val safeId = sanitizeId(exp.id)
        val colRef = db.collection("users").document(uid).collection("expenses")
        val docRef = if (safeId.isNotEmpty()) colRef.document(safeId) else colRef.document()
        docRef.set(
          mapOf(
            "userId" to uid,
            "title" to safeTitle,
            "amount" to exp.amount.coerceIn(0.01, 1_000_000_000.0),
            "categoryId" to exp.categoryId.trim().take(64).ifEmpty { "other" },
            "paymentMethod" to PaymentMethod.fromWire(exp.paymentMethod).wireValue,
            "note" to exp.note.trim().take(300),
            "dateMillis" to validDate,
            "monthKey" to monthKey,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
        importedCount++
      }

      for (inc in incomes) {
        val safeTitle = inc.title.trim().take(120)
        if (safeTitle.isEmpty() || inc.amount <= 0.0) continue
        val validDate = if (inc.dateMillis > 0L) inc.dateMillis else System.currentTimeMillis()
        val monthKey = DateFormatUtils.monthKeyFromMillis(validDate)
        val safeId = sanitizeId(inc.id)
        val colRef = db.collection("users").document(uid).collection("incomes")
        val docRef = if (safeId.isNotEmpty()) colRef.document(safeId) else colRef.document()
        docRef.set(
          mapOf(
            "userId" to uid,
            "title" to safeTitle,
            "amount" to inc.amount.coerceIn(0.01, 1_000_000_000.0),
            "paymentMethod" to PaymentMethod.fromWire(inc.paymentMethod).wireValue,
            "note" to inc.note.trim().take(300),
            "dateMillis" to validDate,
            "monthKey" to monthKey,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
        importedCount++
      }

      Result.success(importedCount)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "users/$userId/backup_import")
      Result.failure(e)
    }
  }

  private fun sanitizeId(raw: String): String =
    raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
}
