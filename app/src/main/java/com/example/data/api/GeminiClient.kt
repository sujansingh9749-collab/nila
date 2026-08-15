package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askAssistant(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        personaPrompt: String = "You are a smart, helpful, witty voice assistant that understands both Bengali (বাংলা) and English. Keep responses concise, spoken-friendly, and actionable."
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured yet. Using instant offline intelligence!"))
        }

        try {
            val contentList = mutableListOf<GeminiContent>()

            // Add previous conversational context (last 4 turns)
            for ((userMsg, assistantMsg) in conversationHistory.takeLast(4)) {
                contentList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMsg))))
                contentList.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = assistantMsg))))
            }

            // Current user prompt
            contentList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))

            val request = GeminiRequest(
                contents = contentList,
                systemInstruction = GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            text = """
                                $personaPrompt
                                
                                You are running inside an Android Assistant Application that can execute phone actions.
                                When responding to queries:
                                1. If the user asks for a conversational answer, general knowledge, explanation, or translation, give a direct, natural answer in the language requested (English or Bengali).
                                2. If the user commands an action (e.g. "turn on flashlight", "set timer for 10 minutes", "send whatsapp message to John", "call mom", "add expense 500 taka on books", "write note"), explain politely that you are executing it, and summarize clearly.
                                3. Keep responses punchy and ideal for Text-To-Speech audio playback (1-3 sentences maximum).
                            """.trimIndent()
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.6f,
                    maxOutputTokens = 350
                )
            )

            val response = service.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I heard you, but I couldn't generate a response."

            Result.success(reply.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
