package com.example.foodguard.data

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ConservationAI {
    private const val TAG = "ConservationAI"
    
    // IMPORTANTE: Se as dicas pararem, troque esta chave em https://aistudio.google.com/app/apikey
    private const val API_KEY = "AIzaSyAAKk5vU82H9eduXTPseAXHHt9__YKCCOw"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            maxOutputTokens = 150
        }
    )

    suspend fun getTips(foodName: String, category: String?): List<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Você é um especialista em conservação de alimentos.
                Dê exatamente 2 dicas curtas, técnicas e muito específicas para conservar: '$foodName'.
                Responda apenas com as dicas, uma em cada linha, sem numeração ou introdução.
                Seja específico para este alimento exato.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val tips = response.text?.lines()
                ?.map { it.trim().removePrefix("- ").removePrefix("* ").trim() }
                ?.filter { it.isNotBlank() && it.length > 5 }

            if (tips.isNullOrEmpty()) {
                Log.w(TAG, "IA retornou vazio para $foodName, usando fallback simples.")
                getSimpleFallback(foodName)
            } else {
                tips.take(2)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro na IA ($foodName): ${e.message}")
            getSimpleFallback(foodName)
        }
    }

    private fun getSimpleFallback(food: String): List<String> {
        return listOf(
            "Mantenha $food em recipiente bem vedado e protegido da luz.",
            "Conserve em local fresco ou refrigerado para aumentar a durabilidade."
        )
    }
}
