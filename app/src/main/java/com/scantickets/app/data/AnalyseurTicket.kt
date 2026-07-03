package com.scantickets.app.data

import java.text.Normalizer
import java.util.Locale

/**
 * Moteur d'analyse d'un ticket de caisse à partir du texte OCR reconstruit.
 *
 * Stratégie : plutôt que des règles rigides, chaque montant candidat au titre
 * de total reçoit un score selon son contexte (mots-clés, devise, position,
 * répétition). Le magasin est cherché d'abord dans un dictionnaire d'enseignes
 * connues, puis par ancrage sur l'adresse, puis par élimination. Les articles
 * sont extraits ligne par ligne et confrontés au total (contrôle de cohérence).
 *
 * L'analyseur annonce sa confiance : l'app signale les tickets « à vérifier »
 * plutôt que de faire semblant d'être sûr.
 */
object AnalyseurTicket {

    // ---------- Expressions régulières ----------

    private val montantRegex = Regex("""(-?)\s*(\d{1,5})\s*[.,]\s*(\d{2})(?!\d)""")

    private val dateNumeriqueRegex =
        Regex("""\b([0-3]?\d)[/.\-]([01]?\d)[/.\-]((?:20)?\d{2})\b""")
    private val dateTextuelleRegex = Regex(
        """(?i)\b([0-3]?\d)(?:er)?\s+(janv|f[ée]vr|mars|avr|mai|juin|juil|ao[uû]t|sept|oct|nov|d[ée]c)[a-zûé]*\.?\s+((?:20)?\d{2})\b"""
    )
    private val moisTextuels = listOf(
        "janv", "fevr", "mars", "avr", "mai", "juin",
        "juil", "aout", "sept", "oct", "nov", "dec"
    )

    private val motTotalRegex =
        Regex("""(?i)(\btotal\b|a payer|à payer|net a payer|net à payer|\bmontant\b|\bsomme\b)""")
    private val motPaiementRegex =
        Regex("""(?i)(\bcb\b|\bcarte\b|\bvisa\b|mastercard|maestro|bancontact|payconiq|esp[eè]ces|pos card|\beur\b|€)""")
    private val motExclusionRegex =
        Regex("""(?i)(sous[- ]total|\brendu\b|monnaie|\btva\b|\bhtva\b|\bht\b|cashback|\bremise carte\b)""")

    private val ligneVoieRegex = Regex(
        """(?i)\b(rue|avenue|av\.|boulevard|bd\b|chauss[ée]e|place|route|chemin|all[ée]e|impasse|quai|cours|zac?\b|centre commercial)\b"""
    )
    private val ligneCodePostalRegex = Regex("""^\d{4,5}\s+\p{L}""")

    private val ligneParasiteRegex = Regex(
        """(?i)(ticket|caisse|tva|facture|reçu|recu\b|bienvenue|merci|www\.|http|@|code|terminal|order|host|drive|caissier|client|vendeur|\btel\b|tél|siret|siren|\bdate\b|heure|scanne|avis|gratuit|coupon|questionnaire|prochaine|app[ée]tit|achat)"""
    )

    // Enseignes courantes (France/Belgique), comparées sans accents ni casse.
    private val enseignesConnues = listOf(
        "carrefour", "lidl", "aldi", "delhaize", "colruyt", "intermarche",
        "leclerc", "auchan", "casino", "monoprix", "franprix", "super u",
        "hyper u", "cora", "match", "spar", "okay", "proxy delhaize",
        "grand frais", "picard", "biocoop", "quick", "mcdonald", "burger king",
        "kfc", "subway", "domino", "pizza hut", "action", "hema", "kruidvat",
        "zeeman", "primark", "decathlon", "ikea", "brico", "gamma", "hubo",
        "fnac", "mediamarkt", "krefel", "vanden borre", "pharmacie", "boulangerie"
    )

    // Un article : [quantité x] libellé …… prix [code TVA]
    private val articleRegex =
        Regex("""^(?:(\d{1,2})\s*[xX]\s+)?(.{2,60}?)\s{1,}(-?\d{1,4}[.,]\d{2})\s*([A-Z]{1,2})?$""")

    // ---------- API ----------

    fun analyser(texte: String): DonneesTicket {
        val lignes = texte.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val total = detecterTotal(lignes)
        val articles = detecterArticles(lignes)
        val coherence = verifierCoherence(articles, total.valeur)

        return DonneesTicket(
            total = total.valeur?.let { String.format(Locale.US, "%.2f", it) },
            dateTicket = detecterDate(texte),
            magasin = detecterMagasin(lignes),
            articles = articles,
            coherenceOk = coherence,
            confiance = total.confiance,
            texteOcr = texte
        )
    }

    // ---------- Total : scoring de candidats ----------

    private data class TotalDetecte(val valeur: Double?, val confiance: Confiance)

    private data class Candidat(val valeur: Double, val indexLigne: Int, val ligne: String)

    private fun detecterTotal(lignes: List<String>): TotalDetecte {
        val candidats = mutableListOf<Candidat>()
        lignes.forEachIndexed { index, ligne ->
            for (montant in montantsDe(ligne)) {
                if (montant >= 0) candidats.add(Candidat(montant, index, ligne))
            }
        }
        if (candidats.isEmpty()) return TotalDetecte(null, Confiance.BASSE)

        val occurrences = candidats.groupingBy { it.valeur }.eachCount()
        val seuilBas = lignes.size * 2 / 3

        fun score(c: Candidat): Int {
            var s = 0
            if (motTotalRegex.containsMatchIn(c.ligne)) s += 4
            if (motPaiementRegex.containsMatchIn(c.ligne)) s += 2
            if (motExclusionRegex.containsMatchIn(c.ligne)) s -= 4
            if ((occurrences[c.valeur] ?: 0) >= 2) s += 2
            if (c.indexLigne >= seuilBas) s += 1
            return s
        }

        val meilleur = candidats.maxWithOrNull(
            compareBy({ score(it) }, { it.valeur })
        ) ?: return TotalDetecte(null, Confiance.BASSE)

        val meilleurScore = score(meilleur)
        val confiance = when {
            meilleurScore >= 6 -> Confiance.HAUTE
            meilleurScore >= 3 -> Confiance.MOYENNE
            else -> Confiance.BASSE
        }
        return TotalDetecte(meilleur.valeur, confiance)
    }

    /** Montants d'une ligne, hors fragments de dates (« 25.06.2026 » ≠ 25.06 €). */
    private fun montantsDe(ligne: String): List<Double> {
        val sansDates = ligne.replace(dateNumeriqueRegex, " ")
        return montantRegex.findAll(sansDates).mapNotNull { m ->
            val signe = if (m.groupValues[1] == "-") -1 else 1
            "${m.groupValues[2]}.${m.groupValues[3]}".toDoubleOrNull()?.times(signe)
        }.toList()
    }

    // ---------- Date ----------

    private fun detecterDate(texte: String): String? {
        dateNumeriqueRegex.findAll(texte).forEach { m ->
            val jour = m.groupValues[1].toIntOrNull() ?: return@forEach
            val mois = m.groupValues[2].toIntOrNull() ?: return@forEach
            val annee = normaliserAnnee(m.groupValues[3]) ?: return@forEach
            if (jour in 1..31 && mois in 1..12) {
                return String.format(Locale.US, "%02d/%02d/%04d", jour, mois, annee)
            }
        }
        dateTextuelleRegex.findAll(texte).forEach { m ->
            val jour = m.groupValues[1].toIntOrNull() ?: return@forEach
            val moisTexte = sansAccents(m.groupValues[2].lowercase(Locale.FRANCE))
            val mois = moisTextuels.indexOfFirst { moisTexte.startsWith(it) } + 1
            val annee = normaliserAnnee(m.groupValues[3]) ?: return@forEach
            if (jour in 1..31 && mois in 1..12) {
                return String.format(Locale.US, "%02d/%02d/%04d", jour, mois, annee)
            }
        }
        return null
    }

    private fun normaliserAnnee(brut: String): Int? {
        val annee = brut.toIntOrNull() ?: return null
        return if (annee < 100) 2000 + annee else annee
    }

    // ---------- Magasin ----------

    private fun detecterMagasin(lignes: List<String>): String? {
        val haut = lignes.take(15)

        // 1. Enseigne connue dans le haut du ticket (hors adresses web :
        //    « www.quickandyou.be » contient « quick » mais n'est pas l'enseigne).
        val ligneWebRegex = Regex("""(?i)(www\.|http|@|\.be\b|\.fr\b|\.com\b)""")
        for (ligne in haut.take(12)) {
            if (ligneWebRegex.containsMatchIn(ligne)) continue
            val normalisee = sansAccents(ligne.lowercase(Locale.FRANCE))
            if (enseignesConnues.any { normalisee.contains(it) }) return ligne
        }

        // 2. Ancrage sur l'adresse : l'enseigne est la ligne exploitable la plus
        //    proche au-dessus de la première ligne d'adresse.
        val indexAdresse = haut.indexOfFirst {
            ligneVoieRegex.containsMatchIn(it) || ligneCodePostalRegex.containsMatchIn(it)
        }
        if (indexAdresse > 0) {
            for (i in indexAdresse - 1 downTo 0) {
                if (estCandidatEnseigne(haut[i])) return haut[i]
            }
        }

        // 3. Première ligne exploitable du haut du ticket.
        return lignes.take(8).firstOrNull(::estCandidatEnseigne)
    }

    private fun estCandidatEnseigne(ligne: String): Boolean =
        ligne.count { it.isLetter() } >= 3 &&
            !ligneParasiteRegex.containsMatchIn(ligne) &&
            !dateNumeriqueRegex.containsMatchIn(ligne) &&
            montantsDe(ligne).isEmpty()

    private fun sansAccents(texte: String): String =
        Normalizer.normalize(texte, Normalizer.Form.NFD)
            .replace(Regex("""\p{Mn}+"""), "")

    // ---------- Articles ----------

    private fun detecterArticles(lignes: List<String>): List<ArticleTicket> {
        val articles = mutableListOf<ArticleTicket>()
        for (ligne in lignes) {
            if (motTotalRegex.containsMatchIn(ligne)) continue
            if (motPaiementRegex.containsMatchIn(ligne)) continue
            if (motExclusionRegex.containsMatchIn(ligne)) continue
            if (dateNumeriqueRegex.containsMatchIn(ligne)) continue

            val m = articleRegex.find(ligne) ?: continue
            val quantite = m.groupValues[1].toIntOrNull() ?: 1
            val libelle = m.groupValues[2].trim().trimEnd('.', '…', ':')
            val prix = m.groupValues[3].replace(",", ".").toDoubleOrNull() ?: continue
            if (libelle.count { it.isLetter() } < 2) continue

            articles.add(
                ArticleTicket(
                    libelle = libelle,
                    prix = String.format(Locale.US, "%.2f", prix),
                    quantite = quantite
                )
            )
        }
        return articles
    }

    private fun verifierCoherence(articles: List<ArticleTicket>, total: Double?): Boolean? {
        if (articles.isEmpty() || total == null) return null
        val somme = articles.sumOf { it.prix.toDoubleOrNull() ?: 0.0 }
        return kotlin.math.abs(somme - total) <= 0.05
    }
}
