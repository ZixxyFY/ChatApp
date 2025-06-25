package com.example.chatapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object GeminiApiService {
    private const val API_KEY = "AIzaSyDBUA51VgAy2J5hdYxNIvvn8ppS3SLMiM0"
    private const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$API_KEY"
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class GeminiRequest(
        val contents: List<Content>
    ) {
        @Serializable
        data class Content(
            val parts: List<Part>
        ) {
            @Serializable
            data class Part(
                val text: String
            )
        }
    }

    @Serializable
    data class GeminiResponse(
        val candidates: List<Candidate>? = null
    ) {
        @Serializable
        data class Candidate(
            val content: Content? = null
        ) {
            @Serializable
            data class Content(
                val parts: List<Part>? = null
            ) {
                @Serializable
                data class Part(
                    val text: String? = null
                )
            }
        }
    }

    suspend fun getGeminiReply(prompt: String): String {
        return try {
            val requestBody = GeminiRequest(
                contents = listOf(
                    GeminiRequest.Content(
                        parts = listOf(GeminiRequest.Content.Part(text = prompt))
                    )
                )
            )
            val bodyString = json.encodeToString(GeminiRequest.serializer(), requestBody)
            val request = Request.Builder()
                .url(ENDPOINT)
                .post(bodyString.toRequestBody("application/json".toMediaType()))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return "[No response]"

            if (!response.isSuccessful) {
                return "[Gemini HTTP error: ${response.code} - $responseBody]"
            }

            val geminiResponse = json.decodeFromString(GeminiResponse.serializer(), responseBody)
            geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "[No reply]"
        } catch (e: Exception) {
            "[Gemini fatal error: ${e.message ?: "unknown"} | ${e::class.qualifiedName} | ${e.stackTraceToString().take(200)}]"
        }
    }
} 