package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.AccountTransfer
import com.example.data.model.CurrencyOption
import com.example.data.model.CustomAccount
import com.example.data.model.DateFormatUtils
import com.example.data.model.Expense
import com.example.data.model.Income
import com.example.data.model.Payable
import com.example.data.model.PaymentMethod
import com.example.data.model.Receivable
import com.example.data.model.TrashItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
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

class AccountAndTrashRepository(
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

  fun observeAccounts(userId: String): Flow<SyncSnapshot<CustomAccount>> {
    val path = "users/$userId/accounts"
    return db.collection("users").document(userId).collection("accounts")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        val list = snapshot.toObjects(
          CustomAccount::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedBy { it.name.lowercase() }
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

  fun observeTrash(userId: String): Flow<List<TrashItem>> {
    val path = "users/$userId/trash"
    return db.collection("users").document(userId).collection("trash")
      .snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        snapshot.toObjects(
          TrashItem::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedByDescending { it.deletedAtMillis }
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

  suspend fun addAccount(
    userId: String,
    name: String,
    accountType: PaymentMethod,
    iconKey: String,
    colorHex: Long,
    openingBalance: Double,
    currencyCode: String,
  ): Result<String> {
    var docPath = "users/$userId/accounts"
    return try {
      val uid = requireMatchingUserId(userId)
      val cleanName = name.trim().take(60)
      require(cleanName.isNotEmpty()) { "Account name cannot be empty" }
      val safeBalance = openingBalance.coerceIn(-1_000_000_000.0, 1_000_000_000.0)
      val safeIcon = iconKey.trim().take(32).ifEmpty { "wallet" }
      val safeCurrency = currencyCode.trim().take(8).ifEmpty { CurrencyOption.BDT.code }

      val docRef = db.collection("users").document(uid).collection("accounts").document()
      docPath = docRef.path
      docRef.set(
        mapOf(
          "userId" to uid,
          "name" to cleanName,
          "accountType" to accountType.wireValue,
          "iconKey" to safeIcon,
          "colorHex" to colorHex,
          "openingBalance" to safeBalance,
          "currencyCode" to safeCurrency,
          "custom" to true,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateAccount(
    userId: String,
    accountId: String,
    name: String,
    accountType: PaymentMethod,
    iconKey: String,
    colorHex: Long,
    openingBalance: Double,
    currencyCode: String,
  ): Result<Unit> {
    val docPath = "users/$userId/accounts/$accountId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(accountId.isNotBlank()) { "Account ID required" }
      val cleanName = name.trim().take(60)
      require(cleanName.isNotEmpty()) { "Account name cannot be empty" }
      val safeBalance = openingBalance.coerceIn(-1_000_000_000.0, 1_000_000_000.0)
      val safeIcon = iconKey.trim().take(32).ifEmpty { "wallet" }
      val safeCurrency = currencyCode.trim().take(8).ifEmpty { CurrencyOption.BDT.code }

      db.collection("users").document(uid).collection("accounts").document(accountId)
        .update(
          mapOf(
            "name" to cleanName,
            "accountType" to accountType.wireValue,
            "iconKey" to safeIcon,
            "colorHex" to colorHex,
            "openingBalance" to safeBalance,
            "currencyCode" to safeCurrency,
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

  suspend fun deleteAccount(userId: String, accountId: String): Result<Unit> {
    val docPath = "users/$userId/accounts/$accountId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(accountId.isNotBlank()) { "Account ID required" }
      db.collection("users").document(uid).collection("accounts").document(accountId)
        .delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun moveExpenseToTrash(userId: String, expense: Expense): Result<Unit> {
    val docPath = "users/$userId/trash"
    return try {
      val uid = requireMatchingUserId(userId)
      require(expense.id.isNotBlank()) { "Expense ID required" }
      val now = System.currentTimeMillis()
      val payload = buildMap<String, Any> {
        put("title", expense.title.trim().take(120).ifEmpty { "Expense" })
        put("amount", expense.amount.coerceIn(0.01, 1_000_000_000.0))
        put("categoryId", expense.categoryId.trim().take(64).ifEmpty { "other" })
        put("paymentMethod", PaymentMethod.fromWire(expense.paymentMethod).wireValue)
        if (!expense.accountId.isNullOrBlank()) put("accountId", expense.accountId.trim().take(128))
        put("note", expense.note.trim().take(300))
        put("dateMillis", if (expense.dateMillis > 0L) expense.dateMillis else now)
        put("monthKey", expense.monthKey.ifBlank { DateFormatUtils.monthKeyFromMillis(now) })
      }
      val batch = db.batch()
      val trashRef = db.collection("users").document(uid).collection("trash").document()
      batch.set(
        trashRef,
        mapOf(
          "userId" to uid,
          "entityType" to "EXPENSE",
          "sourceId" to expense.id,
          "title" to expense.title.trim().take(120).ifEmpty { "Expense" },
          "amount" to expense.amount.coerceIn(0.0, 1_000_000_000.0),
          "deletedAtMillis" to now,
          "payload" to payload,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.delete(db.collection("users").document(uid).collection("expenses").document(expense.id))
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  suspend fun moveIncomeToTrash(userId: String, income: Income): Result<Unit> {
    val docPath = "users/$userId/trash"
    return try {
      val uid = requireMatchingUserId(userId)
      require(income.id.isNotBlank()) { "Income ID required" }
      val now = System.currentTimeMillis()
      val payload = buildMap<String, Any> {
        put("title", income.title.trim().take(120).ifEmpty { "Balance" })
        put("amount", income.amount.coerceIn(0.01, 1_000_000_000.0))
        if (!income.categoryId.isNullOrBlank()) put("categoryId", income.categoryId.trim().take(64))
        put("paymentMethod", PaymentMethod.fromWire(income.paymentMethod).wireValue)
        if (!income.accountId.isNullOrBlank()) put("accountId", income.accountId.trim().take(128))
        put("note", income.note.trim().take(300))
        put("dateMillis", if (income.dateMillis > 0L) income.dateMillis else now)
        put("monthKey", income.monthKey.ifBlank { DateFormatUtils.monthKeyFromMillis(now) })
      }
      val batch = db.batch()
      val trashRef = db.collection("users").document(uid).collection("trash").document()
      batch.set(
        trashRef,
        mapOf(
          "userId" to uid,
          "entityType" to "INCOME",
          "sourceId" to income.id,
          "title" to income.title.trim().take(120).ifEmpty { "Balance" },
          "amount" to income.amount.coerceIn(0.0, 1_000_000_000.0),
          "deletedAtMillis" to now,
          "payload" to payload,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.delete(db.collection("users").document(uid).collection("incomes").document(income.id))
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  suspend fun moveTransferToTrash(userId: String, transfer: AccountTransfer): Result<Unit> {
    val docPath = "users/$userId/trash"
    return try {
      val uid = requireMatchingUserId(userId)
      require(transfer.id.isNotBlank()) { "Transfer ID required" }
      val now = System.currentTimeMillis()
      val payload = buildMap<String, Any> {
        put("fromMethod", PaymentMethod.fromWire(transfer.fromMethod).wireValue)
        put("toMethod", PaymentMethod.fromWire(transfer.toMethod).wireValue)
        if (!transfer.fromAccountId.isNullOrBlank()) put("fromAccountId", transfer.fromAccountId.trim().take(128))
        if (!transfer.toAccountId.isNullOrBlank()) put("toAccountId", transfer.toAccountId.trim().take(128))
        put("amount", transfer.amount.coerceIn(0.01, 1_000_000_000.0))
        put("note", transfer.note.trim().take(300))
        put("dateMillis", if (transfer.dateMillis > 0L) transfer.dateMillis else now)
        put("monthKey", transfer.monthKey.ifBlank { DateFormatUtils.monthKeyFromMillis(now) })
      }
      val batch = db.batch()
      val trashRef = db.collection("users").document(uid).collection("trash").document()
      val title = "${transfer.fromMethod} -> ${transfer.toMethod}".take(120)
      batch.set(
        trashRef,
        mapOf(
          "userId" to uid,
          "entityType" to "TRANSFER",
          "sourceId" to transfer.id,
          "title" to title,
          "amount" to transfer.amount.coerceIn(0.0, 1_000_000_000.0),
          "deletedAtMillis" to now,
          "payload" to payload,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.delete(db.collection("users").document(uid).collection("transfers").document(transfer.id))
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  suspend fun movePayableToTrash(
    userId: String,
    payable: Payable,
    relatedPaymentIds: List<String>,
  ): Result<Unit> {
    val docPath = "users/$userId/trash"
    return try {
      val uid = requireMatchingUserId(userId)
      require(payable.id.isNotBlank()) { "Payable ID required" }
      val now = System.currentTimeMillis()
      val payload = buildMap<String, Any> {
        put("personName", payable.personName.trim().take(120).ifEmpty { "Lender" })
        put("amount", payable.amount.coerceIn(0.01, 1_000_000_000.0))
        put("paidAmount", 0.0)
        put("paymentMethod", PaymentMethod.fromWire(payable.paymentMethod).wireValue)
        if (!payable.accountId.isNullOrBlank()) put("accountId", payable.accountId.trim().take(128))
        put("note", payable.note.trim().take(300))
        put("dateMillis", if (payable.dateMillis > 0L) payable.dateMillis else now)
        if (payable.dueDateMillis != null && payable.dueDateMillis > 0L) put("dueDateMillis", payable.dueDateMillis)
        put("monthKey", payable.monthKey.ifBlank { DateFormatUtils.monthKeyFromMillis(now) })
        put("settled", false)
      }
      val batch = db.batch()
      val trashRef = db.collection("users").document(uid).collection("trash").document()
      batch.set(
        trashRef,
        mapOf(
          "userId" to uid,
          "entityType" to "PAYABLE",
          "sourceId" to payable.id,
          "title" to payable.personName.trim().take(120).ifEmpty { "Lender" },
          "amount" to payable.amount.coerceIn(0.0, 1_000_000_000.0),
          "deletedAtMillis" to now,
          "payload" to payload,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.delete(db.collection("users").document(uid).collection("payables").document(payable.id))
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
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  suspend fun moveReceivableToTrash(
    userId: String,
    receivable: Receivable,
    relatedPaymentIds: List<String>,
  ): Result<Unit> {
    val docPath = "users/$userId/trash"
    return try {
      val uid = requireMatchingUserId(userId)
      require(receivable.id.isNotBlank()) { "Receivable ID required" }
      val now = System.currentTimeMillis()
      val payload = buildMap<String, Any> {
        put("personName", receivable.personName.trim().take(120).ifEmpty { "Borrower" })
        put("amount", receivable.amount.coerceIn(0.01, 1_000_000_000.0))
        put("collectedAmount", 0.0)
        put("paymentMethod", PaymentMethod.fromWire(receivable.paymentMethod).wireValue)
        if (!receivable.accountId.isNullOrBlank()) put("accountId", receivable.accountId.trim().take(128))
        put("note", receivable.note.trim().take(300))
        put("dateMillis", if (receivable.dateMillis > 0L) receivable.dateMillis else now)
        if (receivable.dueDateMillis != null && receivable.dueDateMillis > 0L) put("dueDateMillis", receivable.dueDateMillis)
        put("monthKey", receivable.monthKey.ifBlank { DateFormatUtils.monthKeyFromMillis(now) })
        put("settled", false)
      }
      val batch = db.batch()
      val trashRef = db.collection("users").document(uid).collection("trash").document()
      batch.set(
        trashRef,
        mapOf(
          "userId" to uid,
          "entityType" to "RECEIVABLE",
          "sourceId" to receivable.id,
          "title" to receivable.personName.trim().take(120).ifEmpty { "Borrower" },
          "amount" to receivable.amount.coerceIn(0.0, 1_000_000_000.0),
          "deletedAtMillis" to now,
          "payload" to payload,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.delete(db.collection("users").document(uid).collection("receivables").document(receivable.id))
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
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  suspend fun restoreFromTrash(userId: String, item: TrashItem): Result<Unit> {
    val docPath = "users/$userId/trash/${item.id}"
    return try {
      val uid = requireMatchingUserId(userId)
      require(item.id.isNotBlank()) { "Trash ID required" }
      val p = item.payload
      val now = System.currentTimeMillis()
      val dateMillis = ((p["dateMillis"] as? Number)?.toLong() ?: now).let { if (it > 0L) it else now }
      val monthKey = (p["monthKey"] as? String).orEmpty().ifBlank { DateFormatUtils.monthKeyFromMillis(dateMillis) }
      val batch = db.batch()
      val userDoc = db.collection("users").document(uid)
      val safeSourceId = item.sourceId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)

      when (item.entityType) {
        "EXPENSE" -> {
          val targetRef = if (safeSourceId.isNotEmpty()) userDoc.collection("expenses").document(safeSourceId)
          else userDoc.collection("expenses").document()
          val data = buildMap<String, Any> {
            put("userId", uid)
            put("title", ((p["title"] as? String)?.trim()?.take(120)).orEmpty().ifEmpty { item.title.ifEmpty { "Expense" } })
            put("amount", ((p["amount"] as? Number)?.toDouble() ?: item.amount).coerceIn(0.01, 1_000_000_000.0))
            put("categoryId", ((p["categoryId"] as? String)?.trim()?.take(64)).orEmpty().ifEmpty { "other" })
            put("paymentMethod", PaymentMethod.fromWire(p["paymentMethod"] as? String).wireValue)
            (p["accountId"] as? String)?.trim()?.take(128)?.takeIf { it.isNotEmpty() }?.let { put("accountId", it) }
            put("note", ((p["note"] as? String)?.trim()?.take(300)).orEmpty())
            put("dateMillis", dateMillis)
            put("monthKey", monthKey)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
          }
          batch.set(targetRef, data)
        }
        "INCOME" -> {
          val targetRef = if (safeSourceId.isNotEmpty()) userDoc.collection("incomes").document(safeSourceId)
          else userDoc.collection("incomes").document()
          val data = buildMap<String, Any> {
            put("userId", uid)
            put("title", ((p["title"] as? String)?.trim()?.take(120)).orEmpty().ifEmpty { item.title.ifEmpty { "Balance" } })
            put("amount", ((p["amount"] as? Number)?.toDouble() ?: item.amount).coerceIn(0.01, 1_000_000_000.0))
            (p["categoryId"] as? String)?.trim()?.take(64)?.takeIf { it.isNotEmpty() }?.let { put("categoryId", it) }
            put("paymentMethod", PaymentMethod.fromWire(p["paymentMethod"] as? String).wireValue)
            (p["accountId"] as? String)?.trim()?.take(128)?.takeIf { it.isNotEmpty() }?.let { put("accountId", it) }
            put("note", ((p["note"] as? String)?.trim()?.take(300)).orEmpty())
            put("dateMillis", dateMillis)
            put("monthKey", monthKey)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
          }
          batch.set(targetRef, data)
        }
        "TRANSFER" -> {
          val targetRef = if (safeSourceId.isNotEmpty()) userDoc.collection("transfers").document(safeSourceId)
          else userDoc.collection("transfers").document()
          val fromMethod = PaymentMethod.fromWire(p["fromMethod"] as? String).wireValue
          val toMethod = PaymentMethod.fromWire(p["toMethod"] as? String).wireValue
          val fromAcc = (p["fromAccountId"] as? String)?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
          val toAcc = (p["toAccountId"] as? String)?.trim()?.take(128)?.takeIf { it.isNotEmpty() }
          val data = buildMap<String, Any> {
            put("userId", uid)
            put("fromMethod", fromMethod)
            put("toMethod", toMethod)
            if (fromAcc != null) put("fromAccountId", fromAcc)
            if (toAcc != null) put("toAccountId", toAcc)
            put("amount", ((p["amount"] as? Number)?.toDouble() ?: item.amount).coerceIn(0.01, 1_000_000_000.0))
            put("note", ((p["note"] as? String)?.trim()?.take(300)).orEmpty())
            put("dateMillis", dateMillis)
            put("monthKey", monthKey)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
          }
          batch.set(targetRef, data)
        }
        "PAYABLE" -> {
          val targetRef = if (safeSourceId.isNotEmpty()) userDoc.collection("payables").document(safeSourceId)
          else userDoc.collection("payables").document()
          val data = buildMap<String, Any> {
            put("userId", uid)
            put("personName", ((p["personName"] as? String)?.trim()?.take(120)).orEmpty().ifEmpty { item.title.ifEmpty { "Lender" } })
            put("amount", ((p["amount"] as? Number)?.toDouble() ?: item.amount).coerceIn(0.01, 1_000_000_000.0))
            put("paidAmount", ((p["paidAmount"] as? Number)?.toDouble() ?: 0.0).coerceIn(0.0, 1_000_000_000.0))
            put("paymentMethod", PaymentMethod.fromWire(p["paymentMethod"] as? String).wireValue)
            (p["accountId"] as? String)?.trim()?.take(128)?.takeIf { it.isNotEmpty() }?.let { put("accountId", it) }
            put("note", ((p["note"] as? String)?.trim()?.take(300)).orEmpty())
            put("dateMillis", dateMillis)
            (p["dueDateMillis"] as? Number)?.toLong()?.takeIf { it > 0L }?.let { put("dueDateMillis", it) }
            put("monthKey", monthKey)
            put("settled", (p["settled"] as? Boolean) ?: false)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
          }
          batch.set(targetRef, data)
        }
        "RECEIVABLE" -> {
          val targetRef = if (safeSourceId.isNotEmpty()) userDoc.collection("receivables").document(safeSourceId)
          else userDoc.collection("receivables").document()
          val data = buildMap<String, Any> {
            put("userId", uid)
            put("personName", ((p["personName"] as? String)?.trim()?.take(120)).orEmpty().ifEmpty { item.title.ifEmpty { "Borrower" } })
            put("amount", ((p["amount"] as? Number)?.toDouble() ?: item.amount).coerceIn(0.01, 1_000_000_000.0))
            put("collectedAmount", ((p["collectedAmount"] as? Number)?.toDouble() ?: 0.0).coerceIn(0.0, 1_000_000_000.0))
            put("paymentMethod", PaymentMethod.fromWire(p["paymentMethod"] as? String).wireValue)
            (p["accountId"] as? String)?.trim()?.take(128)?.takeIf { it.isNotEmpty() }?.let { put("accountId", it) }
            put("note", ((p["note"] as? String)?.trim()?.take(300)).orEmpty())
            put("dateMillis", dateMillis)
            (p["dueDateMillis"] as? Number)?.toLong()?.takeIf { it > 0L }?.let { put("dueDateMillis", it) }
            put("monthKey", monthKey)
            put("settled", (p["settled"] as? Boolean) ?: false)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
          }
          batch.set(targetRef, data)
        }
      }
      batch.delete(userDoc.collection("trash").document(item.id))
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deleteTrashItem(userId: String, trashId: String): Result<Unit> {
    val docPath = "users/$userId/trash/$trashId"
    return try {
      val uid = requireMatchingUserId(userId)
      require(trashId.isNotBlank()) { "Trash ID required" }
      db.collection("users").document(uid).collection("trash").document(trashId)
        .delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun deleteTrashItemsBatch(userId: String, trashIds: List<String>): Result<Unit> {
    val docPath = "users/$userId/trash"
    return try {
      val uid = requireMatchingUserId(userId)
      val refs = trashIds.filter { it.isNotBlank() }
        .map { db.collection("users").document(uid).collection("trash").document(it) }
      commitBatchDeletes(refs)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun clearAllUserData(userId: String): Result<Unit> {
    val docPath = "users/$userId"
    return try {
      val uid = requireMatchingUserId(userId)
      val userDoc = db.collection("users").document(uid)
      val subcollections = listOf(
        "expenses",
        "incomes",
        "transfers",
        "payables",
        "payablePayments",
        "receivables",
        "receivablePayments",
        "categories",
        "budgets",
        "accounts",
        "trash",
      )
      val refs = mutableListOf<DocumentReference>()
      for (col in subcollections) {
        val snap = userDoc.collection(col).get().await()
        for (doc in snap.documents) {
          refs.add(doc.reference)
        }
      }
      commitBatchDeletes(refs)
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  private suspend fun commitBatchDeletes(refs: List<DocumentReference>) {
    for (chunk in refs.chunked(450)) {
      val batch = db.batch()
      for (ref in chunk) {
        batch.delete(ref)
      }
      batch.commit().await()
    }
  }
}
