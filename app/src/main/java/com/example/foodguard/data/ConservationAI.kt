package com.example.foodguard.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ConservationAI {

    /**
     * Retorna dicas de conservação fixas baseadas no nome ou categoria do alimento.
     * A IA foi totalmente removida para garantir estabilidade e funcionamento offline.
     */
    suspend fun getTips(foodName: String, category: String?): List<String> = withContext(Dispatchers.IO) {
        val name = foodName.lowercase()
        val cat = category?.lowercase() ?: ""

        when {
            name.contains("banana") -> listOf(
                "Mantenha em local fresco e arejado, fora da geladeira.",
                "Envolva o cabo em plástico filme para retardar o amadurecimento."
            )
            name.contains("maçã") || name.contains("maca") -> listOf(
                "Guarde na gaveta da geladeira para manter a crocância por mais tempo.",
                "Mantenha longe de folhas verdes, pois o gás etileno da maçã pode murchá-las."
            )
            name.contains("pão") || name.contains("pao") -> listOf(
                "Mantenha em saco fechado em local seco e escuro.",
                "Se não for consumir logo, fatie e congele para preservar o frescor."
            )
            name.contains("leite") -> listOf(
                "Mantenha sempre refrigerado e consuma em até 3 dias após aberto.",
                "Evite a porta da geladeira; guarde nas prateleiras internas onde a temperatura é estável."
            )
            name.contains("tomate") -> listOf(
                "Se maduros, guarde na geladeira com o caule para baixo.",
                "Se estiverem verdes, deixe amadurecer fora da geladeira em temperatura ambiente."
            )
            name.contains("alface") || name.contains("folha") || name.contains("rúcula") -> listOf(
                "Lave, seque bem e guarde em pote fechado com uma folha de papel toalha.",
                "Mantenha na parte menos fria da geladeira (gaveta inferior)."
            )
            name.contains("ovo") -> listOf(
                "Mantenha na embalagem original dentro da prateleira da geladeira.",
                "Não lave os ovos antes de guardar para não remover a película protetora."
            )
            name.contains("queijo") -> listOf(
                "Envolva em papel manteiga ou plástico filme após aberto.",
                "Mantenha sob refrigeração constante em recipiente fechado."
            )
            name.contains("cenoura") -> listOf(
                "Remova as folhas verdes e guarde na geladeira em saco plástico perfurado.",
                "Pode ser mantida em um pote com água na geladeira para ficar crocante."
            )
            name.contains("batata") -> listOf(
                "Mantenha em local escuro, fresco e seco (como uma despensa).",
                "Evite a geladeira, pois o frio transforma o amido em açúcar, alterando o sabor."
            )
            name.contains("café") || name.contains("cafe") -> listOf(
                "Guarde em pote hermético em local escuro para preservar o aroma.",
                "Evite a geladeira devido à umidade, a menos que o pote seja perfeitamente vedado."
            )
            cat.contains("carne") || cat.contains("frango") || cat.contains("peixe") -> listOf(
                "Mantenha na parte mais fria da geladeira e prepare em até 48 horas.",
                "Para períodos maiores, congele em porções individuais bem vedadas."
            )
            cat.contains("fruta") -> listOf(
                "Retire de sacos plásticos fechados para evitar o acúmulo de umidade.",
                "Lave apenas no momento exato em que for consumir."
            )
            cat.contains("vegetal") || cat.contains("legume") -> listOf(
                "Mantenha na gaveta de legumes da geladeira para controle de umidade.",
                "Remova partes danificadas imediatamente para não contaminar o restante."
            )
            cat.contains("laticínio") || cat.contains("iogurte") -> listOf(
                "Verifique se a embalagem está bem selada após o uso.",
                "Mantenha no fundo da geladeira, onde é mais gelado."
            )
            else -> listOf(
                "Mantenha em local fresco, seco e protegido da luz solar direta.",
                "Certifique-se de que a embalagem esteja bem vedada após o primeiro uso."
            )
        }
    }
}
