package com.example.hitproduct.common.util

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Utility object for managing FCM topic subscriptions.
 */
object TopicManager {
    private const val TAG = "TopicManager"

    /**
     * Subscribe to a given topic to receive notifications.
     * @param topic name of the topic
     * @param onComplete callback with result
     */
    private fun subscribeToTopic(
        topic: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                val success = task.isSuccessful
                if (success) Log.d("TopicManager", "Subscribed to topic: $topic")
                else Log.w("TopicManager", "Failed to subscribe to topic: $topic", task.exception)
                onComplete?.invoke(success)
            }
    }

    /**
     * Unsubscribe from a given topic.
     */
    fun unsubscribeFromTopic(
        topic: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        FirebaseMessaging.getInstance()
            .unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                val success = task.isSuccessful
                if (success) Log.d(TAG, "Unsubscribed from topic: $topic")
                else Log.w(TAG, "Failed to unsubscribe from topic: $topic", task.exception)
                onComplete?.invoke(success)
            }
    }

    /**
     * Subscribe to this user's own topic (userId).
     * Call after login when userId is stored in SharedPreferences.
     */
    fun subscribeToOwnTopic(context: Context) {
        getUserId(context)?.let { subscribeToTopic(it) }
            ?: Log.w(TAG, "subscribeToOwnTopic: userId not available")
    }

    /**
     * Unsubscribe from this user's own topic.
     * Call on logout or unpair.
     */
    fun unsubscribeFromOwnTopic(context: Context) {
        getUserId(context)?.let { unsubscribeFromTopic(it) }
            ?: Log.w(TAG, "unsubscribeFromOwnTopic: userId not available")
    }

    /**
     * Internal helper: read userId from SharedPreferences
     */
    private fun getUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(
            AuthPrefersConstants.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return prefs.getString(AuthPrefersConstants.MY_USER_ID, null)
    }
}

/**
 * HTTP v1 FCM client to send messages to topics directly from client-side.
 * WARNING: service account keys in client can be exposed.
 */
object FcmClient {
    private const val TAG = "FcmClient"
    private const val FCM_URL =
        "https://fcm.googleapis.com/v1/projects/love-story-app-4c8d7/messages:send"

    private val httpClient = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Models for the JSON payload
    private data class AndroidNotification(val channel_id: String)
    private data class AndroidConfig(
        val notification: AndroidNotification,
        val priority: String = "HIGH"
    )
    private data class Notification(
        val title: String,
        val body: String
    )
    private data class Message(
        val topic: String,
        val notification: Notification,
        val android: AndroidConfig,
        val data: Map<String, String>? = null
    )
    private data class SendRequest(val message: Message)

    /**
     * Send a push to the given topic (usually the other user's userId).
     *
     * @param receiverUserId Topic name (userId) to send to.
     * @param title           Notification title.
     * @param body            Notification body.
     * @param data            Optional custom data payload.
     */
    fun sendToTopic(
        receiverUserId: String,
        title: String,
        body: String,
        data: Map<String, String>? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = AccessToken.getAccessToken()
                if (token.isNullOrBlank()) {
                    Log.e(TAG, "Access token missing, abort send")
                    return@launch
                }
                // Build message with Android config for system delivery
                val msg = Message(
                    topic = receiverUserId,
                    notification = Notification(title, body),
                    android = AndroidConfig(
                        notification = AndroidNotification("default_channel")
                    ),
                    data = data
                )
                // Serialize to JSON
                val payload = moshi.adapter(SendRequest::class.java)
                    .toJson(SendRequest(msg))
                    .toRequestBody(JSON)

                // Build HTTP request
                val request = Request.Builder()
                    .url(FCM_URL)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json; UTF-8")
                    .post(payload)
                    .build()

                // Execute and log result
                httpClient.newCall(request).execute().use { resp ->
                    val responseBody = resp.body?.string()
                    if (!resp.isSuccessful) {
                        Log.e("TopicManager", "FCM send failed ${resp.code}: $responseBody")
                    } else {
                        Log.d("TopicManager", "FCM send success: $responseBody")
                    }
                }
            } catch (e: Exception) {
                Log.e("TopicManager", "Error sending FCM message", e)
            }
        }
    }
}