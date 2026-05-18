package com.example.foodguard.data

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ConservationAI {
    private const val TAG = "ConservationAI"
    // IMPORTANTE: Gere sua API Key em https://aistudio.google.com/ e substitua abaixo
    private const val API_KEY = "AIzaSyAAKk5vU82H9eduXTPseAXHHt9__YKCCOw"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 150
        }
    )

    suspend fun getTips(foodName: String, category: String?): List<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Você é um especialista em conservação de alimentos do app FoodGuard.
                Dê exatamente 2 dicas curtíssimas, práticas e específicas de como conservar o alimento: '$foodName' (Categoria: ${category ?: "Geral"}).
                Responda APENAS com as dicas, uma em cada linha, sem numeração, sem asteriscos, sem introdução e sem explicações longas.
                Seja específico para este alimento.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text
            
            if (responseText.isNullOrBlank()) {
                Log.w(TAG, "IA retornou texto vazio para: $foodName")
                return@withContext getLocalFallbackTips(foodName, category)
            }

            // Filtra e limpa as linhas retornadas pela IA
            val tips = responseText.split("\n")
                .map { it.trim().removePrefix("- ").removePrefix("* ").trim() }
                .filter { it.isNotBlank() && it.length > 5 && !it.contains("Aqui estão", ignoreCase = true) }

            if (tips.isEmpty()) {
                Log.w(TAG, "Processamento das dicas falhou para: $foodName. Resposta bruta: $responseText")
                getLocalFallbackTips(foodName, category)
            } else {
                tips.take(2)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro na IA ($foodName): ${e.message}", e)
            // Se falhar (sem internet, erro de chave, etc), usa as dicas locais mais variadas
            getLocalFallbackTips(foodName, category)
        }
    }

    private fun getLocalFallbackTips(foodName: String, category: String?): List<String> {
        val name = foodName.lowercase()
        val cat = category?.lowercase() ?: ""
        
        val specificTips = when {
            name.contains("banana") -> listOf(
                "Mantenha fora da geladeira em local fresco e arejado.",
                "Envolva o cabo em plástico filme para retardar o amadurecimento."
            )
            name.contains("maçã") || name.contains("maca") -> listOf(
                "Guarde na gaveta da geladeira para manter a crocância.",
                "Mantenha longe de vegetais folhosos, pois solta gás etileno."
            )
            name.contains("pão") || name.contains("pao") -> listOf(
                "Mantenha em saco fechado em local seco.",
                "Para conservar por mais tempo, fatie e congele."
            )
            name.contains("leite") -> listOf(
                "Mantenha refrigerado e consuma em até 3 dias após aberto.",
                "Evite guardar na porta da geladeira devido à variação de temperatura."
            )
            name.contains("tomate") -> listOf(
                "Se maduros, guarde na geladeira com o caule para baixo.",
                "Evite empilhar para não machucar a polpa."
            )
            name.contains("alface") || name.contains("folha") || name.contains("rúcula") -> listOf(
                "Lave, seque bem e guarde em pote fechado com papel toalha.",
                "Mantenha na parte menos fria da geladeira."
            )
            name.contains("ovo") -> listOf(
                "Mantenha na embalagem original dentro da geladeira.",
                "Não lave os ovos antes de guardar para não remover a película protetora."
            )
            name.contains("queijo") -> listOf(
                "Envolva em papel manteiga ou plástico filme após aberto.",
                "Mantenha sempre sob refrigeração em recipiente fechado."
            )
            name.contains("cenoura") -> listOf(
                "Corte as folhas e guarde na geladeira em saco perfurado.",
                "Pode ser mantida em um pote com água na geladeira para ficar crocante."
            )
            name.contains("batata") -> listOf(
                "Mantenha em local escuro, fresco e seco.",
                "Nunca guarde na geladeira, pois o amido vira açúcar."
            )
            cat.contains("carne") || cat.contains("frango") || cat.contains("peixe") -> listOf(
                "Mantenha na prateleira mais fria e consuma em 48h.",
                "Se não for consumir logo, congele em porções individuais."
            )
            cat.contains("fruta") -> listOf(
                "Retire de sacos plásticos fechados para evitar umidade excessiva.",
                "Lave as frutas apenas no momento de consumir."
            )
            cat.contains("vegetal") || cat.contains("legume") -> listOf(
                "Mantenha na gaveta de legumes para umidade controlada.",
                "Remova partes danificadas para não contaminar o restante."
            )
            cat.contains("laticínio") || cat.contains("iogurte") -> listOf(
                "Mantenha sempre no fundo da geladeira onde é mais frio.",
                "Verifique sempre o lacre após o primeiro uso."
            )
            else -> listOf(
                "Mantenha em local fresco, seco e protegido da luz direta.",
                "Certifique-se de que a embalagem esteja bem vedada após o uso."
            )
        }
        return specificTips.take(2)
    }
}
