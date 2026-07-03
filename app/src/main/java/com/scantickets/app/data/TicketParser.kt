package com.scantickets.app.data

import java.util.Locale

/**
 * Extraction heuristique du total, de la date et du magasin depuis le texte OCR.
 * Le but n'est pas d'être parfait : la structuration fine (articles, catégories)
 * est faite sur le PC par le LLM. Ici on veut juste un aperçu immédiat pour
 * vérifier que le scan est exploitable.
 */
object TicketParser {

    private val montantRegex = Regex("""(\d{1,5})\s*[.,]\s*(\d{2})(?!\d)""")
    private val ligneTotalRegex = Regex(
        """(?i)\b(total|a payer|à payer|montant|net a payer|net à payer|cb|carte bancaire|espèces|especes)\b"""
    )
    private val dateRegex = Regex("""\b([0-3]?\d)[/.\-]([01]?\d)[/.\-]((?:20)?\d{2})\b""")

    fun analyser(texte: String): DonneesTicket {
        val lignes = texte.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // Total : le plus grand montant trouvé sur une ligne contenant un mot-clé
        // (TOTAL, À PAYER, CB…), sinon le plus grand montant du ticket.
        var total: Double? = null
        for (ligne in lignes) {
            if (!ligneTotalRegex.containsMatchIn(ligne)) continue
            for (m in montantRegex.findAll(ligne)) {
                val valeur = "${m.groupValues[1]}.${m.groupValues[2]}".toDoubleOrNull() ?: continue
                val actuel = total
                if (actuel == null || valeur > actuel) total = valeur
            }
        }
        if (total == null) {
            for (ligne in lignes) {
                for (m in montantRegex.findAll(ligne)) {
                    val valeur = "${m.groupValues[1]}.${m.groupValues[2]}".toDoubleOrNull() ?: continue
                    val actuel = total
                    if (actuel == null || valeur > actuel) total = valeur
                }
            }
        }

        val date = dateRegex.find(texte)?.value

        // Magasin : première ligne "texte" sans chiffres (souvent l'enseigne en haut du ticket).
        val magasin = lignes.firstOrNull { it.length >= 3 && it.none(Char::isDigit) }

        return DonneesTicket(
            total = total?.let { String.format(Locale.US, "%.2f", it) },
            dateTicket = date,
            magasin = magasin,
            texteOcr = texte
        )
    }
}
