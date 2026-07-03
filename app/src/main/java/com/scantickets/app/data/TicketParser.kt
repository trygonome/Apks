package com.scantickets.app.data

import java.util.Locale

/**
 * Extraction heuristique du total, de la date et du magasin depuis le texte OCR.
 * Le but n'est pas d'être parfait : la structuration fine (articles, catégories)
 * est faite sur le PC par le LLM. Ici on veut juste un aperçu immédiat pour
 * vérifier que le scan est exploitable — et l'écran détail permet de corriger.
 */
object TicketParser {

    private val montantRegex = Regex("""(\d{1,5})\s*[.,]\s*(\d{2})(?!\d)""")
    private val dateRegex = Regex("""\b([0-3]?\d)[/.\-]([01]?\d)[/.\-]((?:20)?\d{2})\b""")

    // Lignes susceptibles de porter le total : mots-clés + devises.
    private val ligneTotalRegex = Regex(
        """(?i)(\btotal\b|a payer|à payer|\bmontant\b|net a payer|net à payer|\bcb\b|\bcarte\b|espèces|especes|\beur\b|€)"""
    )

    // Libellés parasites fréquents en tête de ticket : jamais le nom du magasin.
    private val ligneParasiteRegex = Regex(
        """(?i)(ticket|caisse|tva|facture|reçu|recu\b|bienvenue|merci|www\.|http|@|code|terminal|order|host|drive|caissier|client|vendeur|\btel\b|tél|siret|siren|\bdate\b|heure|scanne|avis|gratuit)"""
    )

    fun analyser(texte: String): DonneesTicket {
        val lignes = texte.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val date = dateRegex.find(texte)?.value

        // Montants d'une ligne, en ignorant ce qui appartient à une date
        // (sinon « 25.06.2026 » produit un faux montant de 25.06).
        fun montantsDe(ligne: String): List<Double> {
            val sansDates = ligne.replace(dateRegex, " ")
            return montantRegex.findAll(sansDates)
                .mapNotNull { "${it.groupValues[1]}.${it.groupValues[2]}".toDoubleOrNull() }
                .toList()
        }

        // 1. Le plus grand montant d'une ligne à mot-clé (TOTAL, À PAYER, EUR, CB…).
        var total = lignes
            .filter { ligneTotalRegex.containsMatchIn(it) }
            .flatMap { montantsDe(it) }
            .maxOrNull()

        // 2. Sinon : un montant répété est probablement le total (Total / CB / rendu) ;
        //    à défaut, le plus grand montant du ticket.
        if (total == null) {
            val tous = lignes.flatMap { montantsDe(it) }
            val repete = tous.groupingBy { it }.eachCount()
                .filterValues { it >= 2 }
                .keys.maxOrNull()
            total = repete ?: tous.maxOrNull()
        }

        // Magasin : première ligne du haut du ticket qui ressemble à une enseigne —
        // au moins 3 lettres, pas un libellé parasite, pas de montant ni de date.
        val magasin = lignes.take(8).firstOrNull { ligne ->
            ligne.count { it.isLetter() } >= 3 &&
                !ligneParasiteRegex.containsMatchIn(ligne) &&
                !dateRegex.containsMatchIn(ligne) &&
                montantsDe(ligne).isEmpty()
        }

        return DonneesTicket(
            total = total?.let { String.format(Locale.US, "%.2f", it) },
            dateTicket = date,
            magasin = magasin,
            texteOcr = texte
        )
    }
}
