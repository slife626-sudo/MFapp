package com.example.data.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DriveBackupFile(
  val id: String,
  val name: String,
  val modifiedTime: String,
  val sizeBytes: Long,
)

sealed interface DriveAuthOutcome {
  data class Authorized(val accessToken: String) : DriveAuthOutcome
  data class NeedsResolution(val result: AuthorizationResult) : DriveAuthOutcome
}

object GoogleDriveBackupManager {

  private const val DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file"

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  suspend fun requestDriveAuthorization(activity: Activity): Result<DriveAuthOutcome> {
    return try {
      val request = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(DRIVE_FILE_SCOPE)))
        .build()
      val result = Identity.getAuthorizationClient(activity).authorize(request).await()
      if (result.hasResolution()) {
        Result.success(DriveAuthOutcome.NeedsResolution(result))
      } else {
        val token = result.accessToken
        if (token.isNullOrBlank()) {
          Result.failure(IllegalStateException("Google Drive authorization returned an empty token."))
        } else {
          Result.success(DriveAuthOutcome.Authorized(token))
        }
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun extractTokenFromIntent(context: Context, data: Intent?): Result<String> {
    return try {
      val authResult = Identity.getAuthorizationClient(context)
        .getAuthorizationResultFromIntent(data)
      val token = authResult.accessToken
      if (token.isNullOrBlank()) {
        Result.failure(IllegalStateException("No access token received from Google Drive consent."))
      } else {
        Result.success(token)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun uploadBackup(
    accessToken: String,
    jsonContent: String,
  ): Result<DriveBackupFile> = withContext(Dispatchers.IO) {
    try {
      val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
      val fileName = "hisab_backup_$dateStamp.json"

      // Check if today's backup already exists so we update cleanly instead of spamming duplicates
      val existing = listBackups(accessToken).getOrNull()?.firstOrNull { it.name == fileName }

      val metadataJson = JSONObject().apply {
        put("name", fileName)
        put("mimeType", "application/json")
      }.toString()

      val multipartBody = MultipartBody.Builder()
        .setType("multipart/related".toMediaType())
        .addPart(
          okhttp3.Headers.headersOf("Content-Type", "application/json; charset=UTF-8"),
          metadataJson.toRequestBody("application/json; charset=UTF-8".toMediaType())
        )
        .addPart(
          okhttp3.Headers.headersOf("Content-Type", "application/json; charset=UTF-8"),
          jsonContent.toRequestBody("application/json; charset=UTF-8".toMediaType())
        )
        .build()

      val url = if (existing != null) {
        "https://www.googleapis.com/upload/drive/v3/files/${existing.id}?uploadType=multipart&fields=id,name,modifiedTime,size"
      } else {
        "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,modifiedTime,size"
      }

      val reqBuilder = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")

      val request = if (existing != null) {
        reqBuilder.patch(multipartBody).build()
      } else {
        reqBuilder.post(multipartBody).build()
      }

      httpClient.newCall(request).execute().use { response ->
        val bodyStr = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
          return@withContext Result.failure(
            IllegalStateException("Drive upload failed (${response.code}): $bodyStr")
          )
        }
        val json = JSONObject(bodyStr)
        Result.success(
          DriveBackupFile(
            id = json.optString("id"),
            name = json.optString("name", fileName),
            modifiedTime = json.optString("modifiedTime", ""),
            sizeBytes = json.optString("size", "0").toLongOrNull() ?: 0L,
          )
        )
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun listBackups(accessToken: String): Result<List<DriveBackupFile>> =
    withContext(Dispatchers.IO) {
      try {
        val query = URLEncoder.encode(
          "name contains 'hisab_backup' and trashed = false",
          "UTF-8"
        )
        val url =
          "https://www.googleapis.com/drive/v3/files?q=$query&orderBy=modifiedTime+desc&pageSize=15&fields=files(id,name,modifiedTime,size)"
        val request = Request.Builder()
          .url(url)
          .addHeader("Authorization", "Bearer $accessToken")
          .get()
          .build()

        httpClient.newCall(request).execute().use { response ->
          val bodyStr = response.body?.string().orEmpty()
          if (!response.isSuccessful) {
            return@withContext Result.failure(
              IllegalStateException("Drive list failed (${response.code}): $bodyStr")
            )
          }
          val root = JSONObject(bodyStr)
          val arr = root.optJSONArray("files")
          val files = buildList {
            if (arr != null) {
              for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                add(
                  DriveBackupFile(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    modifiedTime = item.optString("modifiedTime"),
                    sizeBytes = item.optString("size", "0").toLongOrNull() ?: 0L,
                  )
                )
              }
            }
          }
          Result.success(files)
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun downloadBackup(accessToken: String, fileId: String): Result<String> =
    withContext(Dispatchers.IO) {
      try {
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        val request = Request.Builder()
          .url(url)
          .addHeader("Authorization", "Bearer $accessToken")
          .get()
          .build()

        httpClient.newCall(request).execute().use { response ->
          val bodyStr = response.body?.string().orEmpty()
          if (!response.isSuccessful) {
            return@withContext Result.failure(
              IllegalStateException("Drive download failed (${response.code}): $bodyStr")
            )
          }
          Result.success(bodyStr)
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
}
