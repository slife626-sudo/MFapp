package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.AccountTransfer
import com.example.data.model.CategoryFlowType
import com.example.data.model.CurrencyOption
import com.example.data.model.CustomAccount
import com.example.data.model.CustomCategory
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.MonthlyBudget
import com.example.data.model.Payable
import com.example.data.model.PayablePayment
import com.example.data.model.PaymentMethod
import com.example.data.model.Receivable
import com.example.data.model.ReceivablePayment
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

  fun firestoreInstance(): FirebaseFirestore = db

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

  fun observeTransfers(userId: String): Flow<SyncSnapshot<AccountTransfer>> {
    val path = "users/$userId/transfers"
    return db.collection("users").document(userId).collection("transfers")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        val list = snapshot.toObjects(
          AccountTransfer::class.java,
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

  fun observePayables(userId: String): Flow<SyncSnapshot<Payable>> {
    val path = "users/$userId/payables"
    return db.collection("users").document(userId).collection("payables")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        val list = snapshot.toObjects(
          Payable::class.java,
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

  fun observePayablePayments(userId: String): Flow<SyncSnapshot<PayablePayment>> {
    val path = "users/$userId/payablePayments"
    return db.collection("users").document(userId).collection("payablePayments")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        val list = snapshot.toObjects(
          PayablePayment::class.java,
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

  fun observeReceivables(userId: String): Flow<SyncSnapshot<Receivable>> {
    val path = "users/$userId/receivables"
    return db.collection("users").document(userId).collection("receivables")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        val list = snapshot.toObjects(
          Receivable::class.java,
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

  fun observeReceivablePayments(userId: String): Flow<SyncSnapshot<ReceivablePayment>> {
    val path = "users/$userId/receivablePayments"
    return db.collection("users").document(userId).collection("receivablePayments")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        val list = snapshot.toObjects(
          ReceivablePayment::class.java,
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
    accountId: String? = null,
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
      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val payload = buildMap<String, Any> {
        put("userId", uid)
        put("title", cleanTitle)
        put("amount", amount)
        put("categoryId", cleanCategory)
        put("paymentMethod", paymentMethod.wireValue)
        if (cleanAccountId != null) put("accountId", cleanAccountId)
        put("note", cleanNote)
        put("dateMillis", validDateMillis)
        put("monthKey", monthKey)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }
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
    accountId: String? = null,
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

      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val docRef = db.collection("users").document(uid).collection("expenses").document(expenseId)
      docRef.update(
        mapOf(
          "title" to cleanTitle,
          "amount" to amount,
          "categoryId" to cleanCategory,
          "paymentMethod" to paymentMethod.wireValue,
          "accountId" to (cleanAccountId ?: FieldValue.delete()),
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
    categoryId: String? = null,
    accountId: String? = null,
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

      val cleanCategory = categoryId?.trim()?.take(64)?.takeIf { it.isNotEmpty() }
      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val docRef = db.collection("users").document(uid).collection("incomes").document()
      docPath = docRef.path
      val payload = buildMap<String, Any> {
        put("userId", uid)
        put("title", cleanTitle)
        put("amount", amount)
        if (cleanCategory != null) put("categoryId", cleanCategory)
        put("paymentMethod", paymentMethod.wireValue)
        if (cleanAccountId != null) put("accountId", cleanAccountId)
        put("note", cleanNote)
        put("dateMillis", validDateMillis)
        put("monthKey", monthKey)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }
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
    categoryId: String? = null,
    accountId: String? = null,
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

      val cleanCategory = categoryId?.trim()?.take(64)?.takeIf { it.isNotEmpty() }
      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val docRef = db.collection("users").document(uid).collection("incomes").document(incomeId)
      docRef.update(
        mapOf(
          "title" to cleanTitle,
          "amount" to amount,
          "categoryId" to (cleanCategory ?: FieldValue.delete()),
          "paymentMethod" to paymentMethod.wireValue,
          "accountId" to (cleanAccountId ?: FieldValue.delete()),
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

  suspend fun addTransfer(
    userId: String,
    fromMethod: PaymentMethod,
    toMethod: PaymentMethod,
    amount: Double,
    note: String,
    dateMillis: Long,
    fromAccountId: String? = null,
    toAccountId: String? = null,
  ): Result<Unit> {
    var docPath = "users/$userId/transfers"
    return try {
      val uid = requireMatchingUserId(userId)
      val cleanFromAcc = fromAccountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val cleanToAcc = toAccountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val distinctCustomAccounts = cleanFromAcc != null && cleanToAcc != null && cleanFromAcc != cleanToAcc
      require(fromMethod != toMethod || distinctCustomAccounts) {
        "Source and destination accounts must differ"
      }
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      val docRef = db.collection("users").document(uid).collection("transfers").document()
      docPath = docRef.path
      val payload = buildMap<String, Any> {
        put("userId", uid)
        put("fromMethod", fromMethod.wireValue)
        put("toMethod", toMethod.wireValue)
        if (cleanFromAcc != null) put("fromAccountId", cleanFromAcc)
        if (cleanToAcc != null) put("toAccountId", cleanToAcc)
        put("amount", amount)
        put("note", cleanNote)
        put("dateMillis", validDateMillis)
        put("monthKey", monthKey)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateTransfer(
    userId: String,
    transferId: String,
    fromMethod: PaymentMethod,
    toMethod: PaymentMethod,
    amount: Double,
    note: String,
    dateMillis: Long,
    fromAccountId: String? = null,
    toAccountId: String? = null,
  ): Result<Unit> {
    val docPath = "users/$userId/transfers/$transferId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(transferId.isNotBlank()) { "Transfer ID required" }
      val cleanFromAcc = fromAccountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val cleanToAcc = toAccountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val distinctCustomAccounts = cleanFromAcc != null && cleanToAcc != null && cleanFromAcc != cleanToAcc
      require(fromMethod != toMethod || distinctCustomAccounts) {
        "Source and destination accounts must differ"
      }
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      db.collection("users").document(uid).collection("transfers").document(transferId)
        .update(
          mapOf(
            "fromMethod" to fromMethod.wireValue,
            "toMethod" to toMethod.wireValue,
            "fromAccountId" to (cleanFromAcc ?: FieldValue.delete()),
            "toAccountId" to (cleanToAcc ?: FieldValue.delete()),
            "amount" to amount,
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

  suspend fun deleteTransfer(userId: String, transferId: String): Result<Unit> {
    val docPath = "users/$userId/transfers/$transferId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(transferId.isNotBlank()) { "Transfer ID required" }
      db.collection("users").document(uid).collection("transfers").document(transferId)
        .delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun addPayable(
    userId: String,
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
    dueDateMillis: Long? = null,
    accountId: String? = null,
  ): Result<Unit> {
    var docPath = "users/$userId/payables"
    return try {
      val uid = requireMatchingUserId(userId)
      val cleanName = personName.trim().take(120)
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(cleanName.isNotEmpty()) { "Person name cannot be empty" }
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val cleanDue = dueDateMillis?.takeIf { it > 0L }
      val docRef = db.collection("users").document(uid).collection("payables").document()
      docPath = docRef.path
      val payload = buildMap<String, Any> {
        put("userId", uid)
        put("personName", cleanName)
        put("amount", amount)
        put("paidAmount", 0.0)
        put("paymentMethod", paymentMethod.wireValue)
        if (cleanAccountId != null) put("accountId", cleanAccountId)
        put("note", cleanNote)
        put("dateMillis", validDateMillis)
        if (cleanDue != null) put("dueDateMillis", cleanDue)
        put("monthKey", monthKey)
        put("settled", false)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updatePayable(
    userId: String,
    payable: Payable,
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
    dueDateMillis: Long? = payable.dueDateMillis,
    accountId: String? = payable.accountId,
  ): Result<Unit> {
    val docPath = "users/$userId/payables/${payable.id}"
    return try {
      val uid = requireMatchingUserId(userId)
      require(payable.id.isNotBlank()) { "Payable ID required" }
      val cleanName = personName.trim().take(120)
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(cleanName.isNotEmpty()) { "Person name cannot be empty" }
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }
      val clampedPaid = payable.paidAmount.coerceIn(0.0, 1_000_000_000.0)
      val isSettledNow = clampedPaid + 0.001 >= amount
      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val cleanDue = dueDateMillis?.takeIf { it > 0L }

      db.collection("users").document(uid).collection("payables").document(payable.id)
        .update(
          mapOf(
            "personName" to cleanName,
            "amount" to amount,
            "paidAmount" to clampedPaid,
            "paymentMethod" to paymentMethod.wireValue,
            "accountId" to (cleanAccountId ?: FieldValue.delete()),
            "note" to cleanNote,
            "dateMillis" to validDateMillis,
            "dueDateMillis" to (cleanDue ?: FieldValue.delete()),
            "monthKey" to monthKey,
            "settled" to isSettledNow,
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

  suspend fun deletePayable(
    userId: String,
    payableId: String,
    relatedPaymentIds: List<String>,
  ): Result<Unit> {
    val docPath = "users/$userId/payables/$payableId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(payableId.isNotBlank()) { "Payable ID required" }
      val batch = db.batch()
      batch.delete(db.collection("users").document(uid).collection("payables").document(payableId))
      for (payId in relatedPaymentIds) {
        if (payId.isNotBlank()) {
          batch.delete(db.collection("users").document(uid).collection("payablePayments").document(payId))
        }
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun addPayablePayment(
    userId: String,
    payable: Payable,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
    currentPaidSum: Double,
    accountId: String? = null,
  ): Result<Unit> {
    val docPath = "users/$userId/payablePayments"
    return try {
      val uid = requireMatchingUserId(userId)
      require(payable.id.isNotBlank()) { "Payable ID required" }
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      val newPaid = (currentPaidSum + amount).coerceIn(0.0, 1_000_000_000.0)
      val isSettledNow = newPaid + 0.001 >= payable.amount
      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }

      val batch = db.batch()
      val paymentRef = db.collection("users").document(uid).collection("payablePayments").document()
      val paymentPayload = buildMap<String, Any> {
        put("userId", uid)
        put("payableId", payable.id)
        put("personName", payable.personName.trim().take(120).ifEmpty { "Lender" })
        put("amount", amount)
        put("paymentMethod", paymentMethod.wireValue)
        if (cleanAccountId != null) put("accountId", cleanAccountId)
        put("note", cleanNote)
        put("dateMillis", validDateMillis)
        put("monthKey", monthKey)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }
      batch.set(paymentRef, paymentPayload)
      val payableRef = db.collection("users").document(uid).collection("payables").document(payable.id)
      batch.update(
        payableRef,
        mapOf(
          "paidAmount" to newPaid,
          "settled" to isSettledNow,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deletePayablePayment(
    userId: String,
    payment: PayablePayment,
    parentPayable: Payable?,
    newPaidSumAfterDelete: Double,
  ): Result<Unit> {
    val docPath = "users/$userId/payablePayments/${payment.id}"
    return try {
      val uid = requireMatchingUserId(userId)
      require(payment.id.isNotBlank()) { "Payment ID required" }
      val batch = db.batch()
      batch.delete(db.collection("users").document(uid).collection("payablePayments").document(payment.id))
      if (parentPayable != null && parentPayable.id.isNotBlank()) {
        val safePaid = newPaidSumAfterDelete.coerceIn(0.0, 1_000_000_000.0)
        val isSettledNow = safePaid + 0.001 >= parentPayable.amount
        batch.update(
          db.collection("users").document(uid).collection("payables").document(parentPayable.id),
          mapOf(
            "paidAmount" to safePaid,
            "settled" to isSettledNow,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun addReceivable(
    userId: String,
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
    dueDateMillis: Long? = null,
    accountId: String? = null,
  ): Result<Unit> {
    var docPath = "users/$userId/receivables"
    return try {
      val uid = requireMatchingUserId(userId)
      val cleanName = personName.trim().take(120)
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(cleanName.isNotEmpty()) { "Person name cannot be empty" }
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val cleanDue = dueDateMillis?.takeIf { it > 0L }
      val docRef = db.collection("users").document(uid).collection("receivables").document()
      docPath = docRef.path
      val payload = buildMap<String, Any> {
        put("userId", uid)
        put("personName", cleanName)
        put("amount", amount)
        put("collectedAmount", 0.0)
        put("paymentMethod", paymentMethod.wireValue)
        if (cleanAccountId != null) put("accountId", cleanAccountId)
        put("note", cleanNote)
        put("dateMillis", validDateMillis)
        if (cleanDue != null) put("dueDateMillis", cleanDue)
        put("monthKey", monthKey)
        put("settled", false)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateReceivable(
    userId: String,
    receivable: Receivable,
    personName: String,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
    dueDateMillis: Long? = receivable.dueDateMillis,
    accountId: String? = receivable.accountId,
  ): Result<Unit> {
    val docPath = "users/$userId/receivables/${receivable.id}"
    return try {
      val uid = requireMatchingUserId(userId)
      require(receivable.id.isNotBlank()) { "Receivable ID required" }
      val cleanName = personName.trim().take(120)
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(cleanName.isNotEmpty()) { "Person name cannot be empty" }
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }
      val clampedCollected = receivable.collectedAmount.coerceIn(0.0, 1_000_000_000.0)
      val isSettledNow = clampedCollected + 0.001 >= amount
      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
      val cleanDue = dueDateMillis?.takeIf { it > 0L }

      db.collection("users").document(uid).collection("receivables").document(receivable.id)
        .update(
          mapOf(
            "personName" to cleanName,
            "amount" to amount,
            "collectedAmount" to clampedCollected,
            "paymentMethod" to paymentMethod.wireValue,
            "accountId" to (cleanAccountId ?: FieldValue.delete()),
            "note" to cleanNote,
            "dateMillis" to validDateMillis,
            "dueDateMillis" to (cleanDue ?: FieldValue.delete()),
            "monthKey" to monthKey,
            "settled" to isSettledNow,
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

  suspend fun deleteReceivable(
    userId: String,
    receivableId: String,
    relatedPaymentIds: List<String>,
  ): Result<Unit> {
    val docPath = "users/$userId/receivables/$receivableId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(receivableId.isNotBlank()) { "Receivable ID required" }
      val batch = db.batch()
      batch.delete(db.collection("users").document(uid).collection("receivables").document(receivableId))
      for (payId in relatedPaymentIds) {
        if (payId.isNotBlank()) {
          batch.delete(db.collection("users").document(uid).collection("receivablePayments").document(payId))
        }
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun addReceivablePayment(
    userId: String,
    receivable: Receivable,
    amount: Double,
    paymentMethod: PaymentMethod,
    note: String,
    dateMillis: Long,
    currentCollectedSum: Double,
    accountId: String? = null,
  ): Result<Unit> {
    val docPath = "users/$userId/receivablePayments"
    return try {
      val uid = requireMatchingUserId(userId)
      require(receivable.id.isNotBlank()) { "Receivable ID required" }
      val cleanNote = note.trim().take(300)
      val validDateMillis = if (dateMillis > 0L) dateMillis else System.currentTimeMillis()
      val monthKey = DateFormatUtils.monthKeyFromMillis(validDateMillis)
      require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount out of valid range" }

      val newCollected = (currentCollectedSum + amount).coerceIn(0.0, 1_000_000_000.0)
      val isSettledNow = newCollected + 0.001 >= receivable.amount
      val cleanAccountId = accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }

      val batch = db.batch()
      val paymentRef = db.collection("users").document(uid).collection("receivablePayments").document()
      val paymentPayload = buildMap<String, Any> {
        put("userId", uid)
        put("receivableId", receivable.id)
        put("personName", receivable.personName.trim().take(120).ifEmpty { "Borrower" })
        put("amount", amount)
        put("paymentMethod", paymentMethod.wireValue)
        if (cleanAccountId != null) put("accountId", cleanAccountId)
        put("note", cleanNote)
        put("dateMillis", validDateMillis)
        put("monthKey", monthKey)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
      }
      batch.set(paymentRef, paymentPayload)
      val receivableRef = db.collection("users").document(uid).collection("receivables").document(receivable.id)
      batch.update(
        receivableRef,
        mapOf(
          "collectedAmount" to newCollected,
          "settled" to isSettledNow,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deleteReceivablePayment(
    userId: String,
    payment: ReceivablePayment,
    parentReceivable: Receivable?,
    newCollectedSumAfterDelete: Double,
  ): Result<Unit> {
    val docPath = "users/$userId/receivablePayments/${payment.id}"
    return try {
      val uid = requireMatchingUserId(userId)
      require(payment.id.isNotBlank()) { "Payment ID required" }
      val batch = db.batch()
      batch.delete(db.collection("users").document(uid).collection("receivablePayments").document(payment.id))
      if (parentReceivable != null && parentReceivable.id.isNotBlank()) {
        val safeCollected = newCollectedSumAfterDelete.coerceIn(0.0, 1_000_000_000.0)
        val isSettledNow = safeCollected + 0.001 >= parentReceivable.amount
        batch.update(
          db.collection("users").document(uid).collection("receivables").document(parentReceivable.id),
          mapOf(
            "collectedAmount" to safeCollected,
            "settled" to isSettledNow,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.commit().await()
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
    categoryType: CategoryFlowType = CategoryFlowType.EXPENSE,
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
          "categoryType" to categoryType.wireValue,
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

  suspend fun updateCustomCategory(
    userId: String,
    categoryId: String,
    name: String,
    iconKey: String,
    colorHex: Long,
    categoryType: CategoryFlowType,
  ): Result<Unit> {
    val docPath = "users/$userId/categories/$categoryId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(categoryId.isNotBlank()) { "Category ID required" }
      val cleanName = name.trim().take(40)
      require(cleanName.isNotEmpty()) { "Category name cannot be empty" }
      db.collection("users").document(uid).collection("categories").document(categoryId)
        .update(
          mapOf(
            "name" to cleanName,
            "iconKey" to iconKey.trim().take(32).ifEmpty { "other" },
            "colorHex" to colorHex,
            "categoryType" to categoryType.wireValue,
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

  suspend fun bulkDeleteCustomCategories(userId: String, categoryIds: List<String>): Result<Unit> {
    val docPath = "users/$userId/categories"
    return try {
      val uid = requireMatchingUserId(userId)
      val cleanIds = categoryIds.filter { it.isNotBlank() }
      for (chunk in cleanIds.chunked(450)) {
        val batch = db.batch()
        for (id in chunk) {
          batch.delete(db.collection("users").document(uid).collection("categories").document(id))
        }
        batch.commit().await()
      }
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
    transfers: List<AccountTransfer> = emptyList(),
    payables: List<Payable> = emptyList(),
    payablePayments: List<PayablePayment> = emptyList(),
    receivables: List<Receivable> = emptyList(),
    receivablePayments: List<ReceivablePayment> = emptyList(),
    customAccounts: List<CustomAccount> = emptyList(),
    existingAccountIds: Set<String> = emptySet(),
  ): Result<Int> {
    return try {
      val uid = requireMatchingUserId(userId)
      var importedCount = 0

      for (acct in customAccounts) {
        val safeId = sanitizeId(acct.id)
        val cleanName = acct.name.trim().take(60)
        if (cleanName.isNotEmpty()) {
          val colRef = db.collection("users").document(uid).collection("accounts")
          val docRef = if (safeId.isNotEmpty()) colRef.document(safeId) else colRef.document()
          val safeIcon = acct.iconKey.trim().take(32).ifEmpty { "wallet" }
          val safeBalance = acct.openingBalance.coerceIn(-1_000_000_000.0, 1_000_000_000.0)
          val safeCurrency = acct.currencyCode.trim().take(8).ifEmpty { CurrencyOption.BDT.code }
          if (safeId.isNotEmpty() && safeId in existingAccountIds) {
            docRef.update(
              mapOf(
                "name" to cleanName,
                "accountType" to PaymentMethod.fromWire(acct.accountType).wireValue,
                "iconKey" to safeIcon,
                "colorHex" to acct.colorHex,
                "openingBalance" to safeBalance,
                "currencyCode" to safeCurrency,
                "custom" to acct.custom,
                "updatedAt" to FieldValue.serverTimestamp(),
              )
            ).await()
          } else {
            docRef.set(
              mapOf(
                "userId" to uid,
                "name" to cleanName,
                "accountType" to PaymentMethod.fromWire(acct.accountType).wireValue,
                "iconKey" to safeIcon,
                "colorHex" to acct.colorHex,
                "openingBalance" to safeBalance,
                "currencyCode" to safeCurrency,
                "custom" to acct.custom,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
              )
            ).await()
          }
        }
      }

      for (cat in customCategories) {
        val safeId = sanitizeId(cat.id)
        if (safeId.isNotEmpty() && cat.name.isNotBlank()) {
          val docRef = db.collection("users").document(uid).collection("categories").document(safeId)
          val catType = CategoryFlowType.fromWire(cat.categoryType).wireValue
          if (safeId in existingCategoryIds) {
            docRef.update(
              mapOf(
                "name" to cat.name.trim().take(40),
                "iconKey" to cat.iconKey.trim().take(32).ifEmpty { "other" },
                "colorHex" to cat.colorHex,
                "categoryType" to catType,
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
                "categoryType" to catType,
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
        val cleanAcc = exp.accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
        val payload = buildMap<String, Any> {
          put("userId", uid)
          put("title", safeTitle)
          put("amount", exp.amount.coerceIn(0.01, 1_000_000_000.0))
          put("categoryId", exp.categoryId.trim().take(64).ifEmpty { "other" })
          put("paymentMethod", PaymentMethod.fromWire(exp.paymentMethod).wireValue)
          if (cleanAcc != null) put("accountId", cleanAcc)
          put("note", exp.note.trim().take(300))
          put("dateMillis", validDate)
          put("monthKey", monthKey)
          put("createdAt", FieldValue.serverTimestamp())
          put("updatedAt", FieldValue.serverTimestamp())
        }
        docRef.set(payload).await()
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
        val cleanCat = inc.categoryId?.trim()?.take(64)?.takeIf { it.isNotEmpty() }
        val cleanAcc = inc.accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
        val payload = buildMap<String, Any> {
          put("userId", uid)
          put("title", safeTitle)
          put("amount", inc.amount.coerceIn(0.01, 1_000_000_000.0))
          if (cleanCat != null) put("categoryId", cleanCat)
          put("paymentMethod", PaymentMethod.fromWire(inc.paymentMethod).wireValue)
          if (cleanAcc != null) put("accountId", cleanAcc)
          put("note", inc.note.trim().take(300))
          put("dateMillis", validDate)
          put("monthKey", monthKey)
          put("createdAt", FieldValue.serverTimestamp())
          put("updatedAt", FieldValue.serverTimestamp())
        }
        docRef.set(payload).await()
        importedCount++
      }

      for (tr in transfers) {
        val fromM = PaymentMethod.fromWire(tr.fromMethod)
        val toM = PaymentMethod.fromWire(tr.toMethod)
        val fromAcc = tr.fromAccountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
        val toAcc = tr.toAccountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
        val distinctAccs = fromAcc != null && toAcc != null && fromAcc != toAcc
        if ((fromM == toM && !distinctAccs) || tr.amount <= 0.0) continue
        val validDate = if (tr.dateMillis > 0L) tr.dateMillis else System.currentTimeMillis()
        val monthKey = DateFormatUtils.monthKeyFromMillis(validDate)
        val safeId = sanitizeId(tr.id)
        val colRef = db.collection("users").document(uid).collection("transfers")
        val docRef = if (safeId.isNotEmpty()) colRef.document(safeId) else colRef.document()
        val payload = buildMap<String, Any> {
          put("userId", uid)
          put("fromMethod", fromM.wireValue)
          put("toMethod", toM.wireValue)
          if (fromAcc != null) put("fromAccountId", fromAcc)
          if (toAcc != null) put("toAccountId", toAcc)
          put("amount", tr.amount.coerceIn(0.01, 1_000_000_000.0))
          put("note", tr.note.trim().take(300))
          put("dateMillis", validDate)
          put("monthKey", monthKey)
          put("createdAt", FieldValue.serverTimestamp())
          put("updatedAt", FieldValue.serverTimestamp())
        }
        docRef.set(payload).await()
        importedCount++
      }

      for (pay in payables) {
        val safeName = pay.personName.trim().take(120)
        if (safeName.isEmpty() || pay.amount <= 0.0) continue
        val validDate = if (pay.dateMillis > 0L) pay.dateMillis else System.currentTimeMillis()
        val monthKey = DateFormatUtils.monthKeyFromMillis(validDate)
        val safeId = sanitizeId(pay.id)
        val colRef = db.collection("users").document(uid).collection("payables")
        val docRef = if (safeId.isNotEmpty()) colRef.document(safeId) else colRef.document()
        val cleanAcc = pay.accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
        val cleanDue = pay.dueDateMillis?.takeIf { it > 0L }
        val payload = buildMap<String, Any> {
          put("userId", uid)
          put("personName", safeName)
          put("amount", pay.amount.coerceIn(0.01, 1_000_000_000.0))
          put("paidAmount", pay.paidAmount.coerceIn(0.0, 1_000_000_000.0))
          put("paymentMethod", PaymentMethod.fromWire(pay.paymentMethod).wireValue)
          if (cleanAcc != null) put("accountId", cleanAcc)
          put("note", pay.note.trim().take(300))
          put("dateMillis", validDate)
          if (cleanDue != null) put("dueDateMillis", cleanDue)
          put("monthKey", monthKey)
          put("settled", pay.settled)
          put("createdAt", FieldValue.serverTimestamp())
          put("updatedAt", FieldValue.serverTimestamp())
        }
        docRef.set(payload).await()
        importedCount++
      }

      for (pmt in payablePayments) {
        val safeName = pmt.personName.trim().take(120)
        val safePayableId = sanitizeId(pmt.payableId)
        if (safeName.isEmpty() || safePayableId.isEmpty() || pmt.amount <= 0.0) continue
        val validDate = if (pmt.dateMillis > 0L) pmt.dateMillis else System.currentTimeMillis()
        val monthKey = DateFormatUtils.monthKeyFromMillis(validDate)
        val safeId = sanitizeId(pmt.id)
        val colRef = db.collection("users").document(uid).collection("payablePayments")
        val docRef = if (safeId.isNotEmpty()) colRef.document(safeId) else colRef.document()
        val cleanAcc = pmt.accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
        val payload = buildMap<String, Any> {
          put("userId", uid)
          put("payableId", safePayableId)
          put("personName", safeName)
          put("amount", pmt.amount.coerceIn(0.01, 1_000_000_000.0))
          put("paymentMethod", PaymentMethod.fromWire(pmt.paymentMethod).wireValue)
          if (cleanAcc != null) put("accountId", cleanAcc)
          put("note", pmt.note.trim().take(300))
          put("dateMillis", validDate)
          put("monthKey", monthKey)
          put("createdAt", FieldValue.serverTimestamp())
          put("updatedAt", FieldValue.serverTimestamp())
        }
        docRef.set(payload).await()
        importedCount++
      }

      for (rec in receivables) {
        val safeName = rec.personName.trim().take(120)
        if (safeName.isEmpty() || rec.amount <= 0.0) continue
        val validDate = if (rec.dateMillis > 0L) rec.dateMillis else System.currentTimeMillis()
        val monthKey = DateFormatUtils.monthKeyFromMillis(validDate)
        val safeId = sanitizeId(rec.id)
        val colRef = db.collection("users").document(uid).collection("receivables")
        val docRef = if (safeId.isNotEmpty()) colRef.document(safeId) else colRef.document()
        val cleanAcc = rec.accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
        val cleanDue = rec.dueDateMillis?.takeIf { it > 0L }
        val payload = buildMap<String, Any> {
          put("userId", uid)
          put("personName", safeName)
          put("amount", rec.amount.coerceIn(0.01, 1_000_000_000.0))
          put("collectedAmount", rec.collectedAmount.coerceIn(0.0, 1_000_000_000.0))
          put("paymentMethod", PaymentMethod.fromWire(rec.paymentMethod).wireValue)
          if (cleanAcc != null) put("accountId", cleanAcc)
          put("note", rec.note.trim().take(300))
          put("dateMillis", validDate)
          if (cleanDue != null) put("dueDateMillis", cleanDue)
          put("monthKey", monthKey)
          put("settled", rec.settled)
          put("createdAt", FieldValue.serverTimestamp())
          put("updatedAt", FieldValue.serverTimestamp())
        }
        docRef.set(payload).await()
        importedCount++
      }

      for (pmt in receivablePayments) {
        val safeName = pmt.personName.trim().take(120)
        val safeReceivableId = sanitizeId(pmt.receivableId)
        if (safeName.isEmpty() || safeReceivableId.isEmpty() || pmt.amount <= 0.0) continue
        val validDate = if (pmt.dateMillis > 0L) pmt.dateMillis else System.currentTimeMillis()
        val monthKey = DateFormatUtils.monthKeyFromMillis(validDate)
        val safeId = sanitizeId(pmt.id)
        val colRef = db.collection("users").document(uid).collection("receivablePayments")
        val docRef = if (safeId.isNotEmpty()) colRef.document(safeId) else colRef.document()
        val cleanAcc = pmt.accountId?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
        val payload = buildMap<String, Any> {
          put("userId", uid)
          put("receivableId", safeReceivableId)
          put("personName", safeName)
          put("amount", pmt.amount.coerceIn(0.01, 1_000_000_000.0))
          put("paymentMethod", PaymentMethod.fromWire(pmt.paymentMethod).wireValue)
          if (cleanAcc != null) put("accountId", cleanAcc)
          put("note", pmt.note.trim().take(300))
          put("dateMillis", validDate)
          put("monthKey", monthKey)
          put("createdAt", FieldValue.serverTimestamp())
          put("updatedAt", FieldValue.serverTimestamp())
        }
        docRef.set(payload).await()
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
