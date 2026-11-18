package com.example.data.repository

import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.MarketList
import com.example.data.model.MarketListItem
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

class MarketListRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {

  private fun requireSignedInUid(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Market Lists.")
  }

  private fun normalizeEmail(raw: String?): String =
    raw.orEmpty().trim().lowercase().take(160)

  fun observeMarketLists(userId: String, userEmail: String): Flow<List<MarketList>> {
    val cleanEmail = normalizeEmail(userEmail)
    val path = "marketLists"
    val query = if (cleanEmail.isNotEmpty()) {
      db.collection("marketLists").whereArrayContains("memberEmails", cleanEmail)
    } else {
      db.collection("marketLists").whereEqualTo("ownerUid", userId)
    }
    return query.snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        snapshot.toObjects(
          MarketList::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedWith(
          compareBy<MarketList> { it.completed }
            .thenByDescending { it.dateMillis }
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

  fun observeMarketItems(listId: String, userId: String, userEmail: String): Flow<List<MarketListItem>> {
    val cleanEmail = normalizeEmail(userEmail)
    val path = "marketLists/$listId/items"
    val colRef = db.collection("marketLists").document(listId).collection("items")
    val query = if (cleanEmail.isNotEmpty()) {
      colRef.whereArrayContains("memberEmails", cleanEmail)
    } else {
      colRef.whereEqualTo("ownerUid", userId)
    }
    return query.snapshots(MetadataChanges.INCLUDE)
      .map { snapshot ->
        snapshot.toObjects(
          MarketListItem::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedWith(
          compareBy<MarketListItem> { it.bought }
            .thenBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            .thenBy { it.name.lowercase() }
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

  suspend fun createMarketList(
    ownerUid: String,
    ownerEmail: String,
    ownerName: String,
    title: String,
    sharedWithEmail: String,
    note: String,
  ): Result<String> {
    var docPath = "marketLists"
    return try {
      val uid = requireSignedInUid()
      check(uid == ownerUid) { "Signed-in user mismatch" }
      val cleanOwnerEmail = normalizeEmail(ownerEmail.ifBlank { auth.currentUser?.email })
      require(cleanOwnerEmail.isNotEmpty()) { "Verified email is required to create a market list" }
      val cleanShared = normalizeEmail(sharedWithEmail).let { if (it == cleanOwnerEmail) "" else it }
      val cleanTitle = title.trim().take(120)
      val cleanOwnerName = ownerName.trim().take(80)
      val cleanNote = note.trim().take(300)
      require(cleanTitle.isNotEmpty()) { "Title cannot be empty" }

      val members = listOfNotNull(cleanOwnerEmail, cleanShared.takeIf { it.isNotEmpty() })
        .distinct()
        .take(10)

      val docRef = db.collection("marketLists").document()
      docPath = docRef.path
      val payload = mapOf(
        "ownerUid" to uid,
        "ownerEmail" to cleanOwnerEmail,
        "ownerName" to cleanOwnerName,
        "title" to cleanTitle,
        "sharedWithEmail" to cleanShared,
        "memberEmails" to members,
        "note" to cleanNote,
        "completed" to false,
        "dateMillis" to System.currentTimeMillis(),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateMarketListAndShare(
    list: MarketList,
    title: String,
    sharedWithEmail: String,
    note: String,
  ): Result<Unit> {
    val docPath = "marketLists/${list.id}"
    return try {
      val uid = requireSignedInUid()
      check(uid == list.ownerUid) { "Only the list creator can update sharing settings" }
      require(list.id.isNotBlank()) { "List ID required" }

      val cleanTitle = title.trim().take(120)
      require(cleanTitle.isNotEmpty()) { "Title cannot be empty" }
      val cleanOwnerEmail = normalizeEmail(list.ownerEmail)
      val cleanShared = normalizeEmail(sharedWithEmail).let { if (it == cleanOwnerEmail) "" else it }
      val members = listOfNotNull(cleanOwnerEmail, cleanShared.takeIf { it.isNotEmpty() })
        .distinct()
        .take(10)

      val itemsSnap = db.collection("marketLists").document(list.id).collection("items")
        .whereEqualTo("ownerUid", uid)
        .get()
        .await()

      val batch = db.batch()
      val listRef = db.collection("marketLists").document(list.id)
      batch.update(
        listRef,
        mapOf(
          "title" to cleanTitle,
          "sharedWithEmail" to cleanShared,
          "memberEmails" to members,
          "note" to note.trim().take(300),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      for (itemDoc in itemsSnap.documents) {
        batch.update(
          itemDoc.reference,
          mapOf(
            "memberEmails" to members,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun toggleMarketListCompleted(
    listId: String,
    completed: Boolean,
  ): Result<Unit> {
    val docPath = "marketLists/$listId"
    return try {
      requireSignedInUid()
      require(listId.isNotBlank()) { "List ID required" }
      db.collection("marketLists").document(listId)
        .update(
          mapOf(
            "completed" to completed,
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

  suspend fun deleteMarketList(ownerUid: String, listId: String): Result<Unit> {
    val docPath = "marketLists/$listId"
    return try {
      val uid = requireSignedInUid()
      check(uid == ownerUid) { "Only creator can delete list" }
      require(listId.isNotBlank()) { "List ID required" }

      val itemsSnap = db.collection("marketLists").document(listId).collection("items")
        .whereEqualTo("ownerUid", uid)
        .get()
        .await()

      val batch = db.batch()
      for (itemDoc in itemsSnap.documents) {
        batch.delete(itemDoc.reference)
      }
      batch.delete(db.collection("marketLists").document(listId))
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }

  suspend fun addMarketItem(
    list: MarketList,
    addedByEmail: String,
    name: String,
    quantity: String,
    estimatedPrice: Double,
    note: String,
  ): Result<Unit> {
    val docPath = "marketLists/${list.id}/items"
    return try {
      requireSignedInUid()
      require(list.id.isNotBlank()) { "List ID required" }
      val cleanName = name.trim().take(120)
      require(cleanName.isNotEmpty()) { "Item name cannot be empty" }
      val safeMembers = list.memberEmails.take(10).ifEmpty { listOf(normalizeEmail(list.ownerEmail)) }
      val safePrice = estimatedPrice.coerceIn(0.0, 1_000_000_000.0)

      val itemRef = db.collection("marketLists").document(list.id).collection("items").document()
      val payload = mapOf(
        "listId" to list.id,
        "ownerUid" to list.ownerUid,
        "memberEmails" to safeMembers,
        "addedByEmail" to normalizeEmail(addedByEmail),
        "name" to cleanName,
        "quantity" to quantity.trim().take(40),
        "estimatedPrice" to safePrice,
        "bought" to false,
        "note" to note.trim().take(200),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      itemRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docPath)
      Result.failure(e)
    }
  }

  suspend fun updateMarketItem(
    listId: String,
    itemId: String,
    name: String,
    quantity: String,
    estimatedPrice: Double,
    bought: Boolean,
    note: String,
  ): Result<Unit> {
    val docPath = "marketLists/$listId/items/$itemId"
    return try {
      requireSignedInUid()
      require(listId.isNotBlank() && itemId.isNotBlank()) { "IDs required" }
      val cleanName = name.trim().take(120)
      require(cleanName.isNotEmpty()) { "Item name cannot be empty" }
      val safePrice = estimatedPrice.coerceIn(0.0, 1_000_000_000.0)

      db.collection("marketLists").document(listId).collection("items").document(itemId)
        .update(
          mapOf(
            "name" to cleanName,
            "quantity" to quantity.trim().take(40),
            "estimatedPrice" to safePrice,
            "bought" to bought,
            "note" to note.trim().take(200),
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

  suspend fun deleteMarketItem(listId: String, itemId: String): Result<Unit> {
    val docPath = "marketLists/$listId/items/$itemId"
    return try {
      requireSignedInUid()
      require(listId.isNotBlank() && itemId.isNotBlank()) { "IDs required" }
      db.collection("marketLists").document(listId).collection("items").document(itemId)
        .delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docPath)
      Result.failure(e)
    }
  }
}
