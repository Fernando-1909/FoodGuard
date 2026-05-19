package com.example.foodguard.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ConservationAI {

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
            name.contains("alho") || name.contains("cebola") -> listOf(
                "Mantenha em local seco, escuro e com boa ventilação.",
                "Evite a geladeira, pois a umidade faz com que brotem ou fiquem mofados."
            )
            name.contains("arroz") -> listOf(
                "Guarde o arroz cru em recipiente hermético em local seco e fresco.",
                "Se cozido, mantenha na geladeira em pote fechado por até 4 dias."
            )
            name.contains("feijão") || name.contains("feijao") -> listOf(
                "Mantenha os grãos crus em local seco, fresco e arejado.",
                "Se cozido, guarde na geladeira por até 5 dias ou congele por até 3 meses."
            )
            name.contains("macarrão") || name.contains("macarrao") || name.contains("massa") -> listOf(
                "Mantenha a massa seca em pote vedado em local seco.",
                "Após cozido, guarde na geladeira e consuma em até 3 dias (adicione um fio de azeite)."
            )
            name.contains("café") || name.contains("cafe") -> listOf(
                "Guarde em pote hermético em local escuro para preservar o aroma.",
                "Evite a geladeira devido à umidade, a menos que o pote seja perfeitamente vedado."
            )
            name.contains("chocolate") -> listOf(
                "Mantenha em local fresco e seco, longe de odores fortes.",
                "Evite a geladeira para não alterar a textura; se estiver muito calor, use um pote vedado."
            )
            name.contains("salgadinho") || name.contains("biscoito") || name.contains("bolacha") || name.contains("snack") -> listOf(
                "Mantenha a embalagem bem fechada com grampos ou transfira para potes herméticos.",
                "Consuma logo após aberto para manter a crocância."
            )
            name.contains("melancia") -> listOf(
                "Inteira, pode ser mantida em local fresco por até 2 semanas.",
                "Depois de cortada, cubra com filme plástico e mantenha na geladeira."
            )
            name.contains("morango") -> listOf(
                "Não lave antes de guardar; a umidade acelera o apodrecimento.",
                "Mantenha na geladeira em recipiente forrado com papel toalha."
            )
            name.contains("mel") -> listOf(
                "Mantenha sempre em temperatura ambiente e bem vedado.",
                "O mel não estraga, mas pode cristalizar na geladeira; se ocorrer, aqueça levemente."
            )
            name.contains("molho") -> listOf(
                "Após aberto, transfira para um recipiente de vidro ou plástico e refrigere.",
                "Consuma em até 3 a 5 dias após a abertura da embalagem original."
            )
            name.contains("azeite") || name.contains("óleo") -> listOf(
                "Mantenha longe do calor e da luz para evitar a oxidação.",
                "Certifique-se de que a tampa esteja sempre bem apertada."
            )
            cat.contains("carne") || cat.contains("frango") || cat.contains("peixe") -> listOf(
                "Mantenha na parte mais fria da geladeira e prepare em até 48 horas.",
                "Para períodos maiores, congele em porções individuais bem vedadas."
            )
            cat.contains("fruta") -> listOf(
                "Retire de sacos plásticos fechados para evitar o acúmulo de umidade.",
                "Lave apenas no momento exato em que for consumir."
            )
            cat.contains("vegetal") || cat.contains("legume") || cat.contains("verdura") -> listOf(
                "Mantenha na gaveta de legumes da geladeira para controle de umidade.",
                "Remova partes danificadas imediatamente para não contaminar o restante."
            )
            cat.contains("laticínio") || cat.contains("iogurte") -> listOf(
                "Verifique se a embalagem está bem selada após o uso.",
                "Mantenha no fundo da geladeira, onde a temperatura é mais estável."
            )
            cat.contains("sobremesa") || cat.contains("doce") -> listOf(
                "Mantenha sob refrigeração se contiver leite, ovos ou frutas frescas.",
                "Doces secos podem ser mantidos em recipientes vedados fora da geladeira."
            )
            else -> listOf(
                "Mantenha em local fresco, seco e protegido da luz solar direta.",
                "Certifique-se de que a embalagem esteja bem vedada após o primeiro uso."
            )
        }
    }
}
