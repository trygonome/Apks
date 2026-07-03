package com.scantickets.app.data

import java.text.Normalizer
import java.util.Calendar
import java.util.Locale

/** Un relevé de prix d'un article à une date donnée. */
data class PointPrix(
    val date: String,      // clé triable : "yyyy-MM-dd" (ou horodatage de scan)
    val prix: Double,
    val magasin: String?
)

/** Historique de prix d'un article acheté plusieurs fois. */
data class HistoriquePrix(
    val libelle: String,
    val points: List<PointPrix>
) {
    val premierPrix: Double get() = points.first().prix
    val dernierPrix: Double get() = points.last().prix
    val variation: Double get() = dernierPrix - premierPrix
}

/**
 * Agrégations budgétaires sur les scans, en pur Kotlin testable.
 *
 * Règle de ventilation : si les articles d'un ticket sont cohérents avec son
 * total, chaque article part dans sa propre catégorie (le caddie est ventilé) ;
 * sinon le total entier part dans la catégorie de l'enseigne.
 */
object StatistiquesBudget {

    // ---------- Clés de mois ----------

    /** Mois d'un scan au format "yyyy-MM" (date du ticket, sinon date de scan). */
    fun moisDe(scan: ScanEnregistre): String {
        val morceaux = scan.dateTicket?.split("/")
        if (morceaux != null && morceaux.size == 3) {
            val annee = morceaux[2].toIntOrNull()
            val mois = morceaux[1].toIntOrNull()
            if (annee != null && mois in 1..12) {
                return String.format(Locale.US, "%04d-%02d", annee, mois)
            }
        }
        return scan.scanneLe.take(7)
    }

    fun moisCourant(): String {
        val cal = Calendar.getInstance()
        return String.format(
            Locale.US, "%04d-%02d",
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1
        )
    }

    fun moisPrecedent(mois: String): String = decalerMois(mois, -1)

    fun moisSuivant(mois: String): String = decalerMois(mois, +1)

    private fun decalerMois(mois: String, delta: Int): String {
        val annee = mois.take(4).toIntOrNull() ?: return mois
        val numero = mois.drop(5).toIntOrNull() ?: return mois
        val total = annee * 12 + (numero - 1) + delta
        return String.format(Locale.US, "%04d-%02d", total / 12, total % 12 + 1)
    }

    /** "2026-07" → "juillet 2026" */
    fun libelleMois(mois: String): String {
        val noms = listOf(
            "janvier", "février", "mars", "avril", "mai", "juin", "juillet",
            "août", "septembre", "octobre", "novembre", "décembre"
        )
        val numero = mois.drop(5).toIntOrNull() ?: return mois
        val nom = noms.getOrNull(numero - 1) ?: return mois
        return "$nom ${mois.take(4)}"
    }

    // ---------- Agrégations d'un mois ----------

    fun scansDuMois(scans: List<ScanEnregistre>, mois: String): List<ScanEnregistre> =
        scans.filter { moisDe(it) == mois }

    fun totalDuMois(scans: List<ScanEnregistre>, mois: String): Double =
        scansDuMois(scans, mois).sumOf { it.total?.toDoubleOrNull() ?: 0.0 }

    /** Répartition par catégorie, triée par montant décroissant. */
    fun parCategorie(scans: List<ScanEnregistre>, mois: String): List<Pair<Categorie, Double>> {
        val montants = mutableMapOf<Categorie, Double>()
        for (scan in scansDuMois(scans, mois)) {
            val total = scan.total?.toDoubleOrNull() ?: continue
            val categorieTicket = Categoriseur.categoriserTicket(scan.magasin)
            if (scan.coherenceOk == true && scan.articles.isNotEmpty()) {
                for (article in scan.articles) {
                    val prix = article.prix.toDoubleOrNull() ?: continue
                    val categorie = Categoriseur.categoriserArticle(article.libelle, categorieTicket)
                    montants.merge(categorie, prix, Double::plus)
                }
            } else {
                montants.merge(categorieTicket, total, Double::plus)
            }
        }
        return montants.entries
            .filter { it.value > 0.005 }
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }

    /** Top des magasins du mois par montant dépensé. */
    fun topMagasins(
        scans: List<ScanEnregistre>,
        mois: String,
        limite: Int = 5
    ): List<Pair<String, Double>> =
        scansDuMois(scans, mois)
            .mapNotNull { scan ->
                val total = scan.total?.toDoubleOrNull() ?: return@mapNotNull null
                (scan.magasin ?: "Magasin inconnu") to total
            }
            .groupBy({ it.first }, { it.second })
            .map { (magasin, totaux) -> magasin to totaux.sum() }
            .sortedByDescending { it.second }
            .take(limite)

    /**
     * Projection de fin de mois au rythme actuel (uniquement pour le mois en
     * cours) : dépense journalière moyenne × nombre de jours du mois.
     */
    fun projectionFinDeMois(scans: List<ScanEnregistre>, mois: String): Double? {
        if (mois != moisCourant()) return null
        val cal = Calendar.getInstance()
        val jourActuel = cal.get(Calendar.DAY_OF_MONTH)
        val joursDansMois = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (jourActuel <= 0) return null
        val total = totalDuMois(scans, mois)
        return total / jourActuel * joursDansMois
    }

    // ---------- Historique des prix ----------

    /**
     * Articles achetés au moins `minimumAchats` fois (libellés normalisés),
     * avec leur historique de prix trié par date. Les remises (prix négatifs
     * ou nuls) sont ignorées.
     */
    fun historiquesPrix(
        scans: List<ScanEnregistre>,
        minimumAchats: Int = 2
    ): List<HistoriquePrix> {
        data class Releve(val cle: String, val libelle: String, val point: PointPrix)

        val releves = mutableListOf<Releve>()
        for (scan in scans) {
            val date = dateTriable(scan)
            for (article in scan.articles) {
                val prixUnitaire = prixUnitaire(article) ?: continue
                if (prixUnitaire <= 0) continue
                releves.add(
                    Releve(
                        cle = normaliser(article.libelle),
                        libelle = article.libelle,
                        point = PointPrix(date, prixUnitaire, scan.magasin)
                    )
                )
            }
        }
        return releves
            .groupBy { it.cle }
            .values
            .filter { groupe -> groupe.size >= minimumAchats }
            .map { groupe ->
                HistoriquePrix(
                    libelle = groupe.first().libelle,
                    points = groupe.map { it.point }.sortedBy { it.date }
                )
            }
            .sortedByDescending { it.points.size }
    }

    private fun prixUnitaire(article: ArticleTicket): Double? {
        val prix = article.prix.toDoubleOrNull() ?: return null
        val quantite = article.quantite.coerceAtLeast(1)
        return prix / quantite
    }

    /** Date triable "yyyy-MM-dd" à partir de la date du ticket, sinon du scan. */
    private fun dateTriable(scan: ScanEnregistre): String {
        val morceaux = scan.dateTicket?.split("/")
        if (morceaux != null && morceaux.size == 3) {
            val jour = morceaux[0].toIntOrNull()
            val mois = morceaux[1].toIntOrNull()
            val annee = morceaux[2].toIntOrNull()
            if (jour != null && mois != null && annee != null) {
                return String.format(Locale.US, "%04d-%02d-%02d", annee, mois, jour)
            }
        }
        return scan.scanneLe.take(10)
    }

    private fun normaliser(texte: String): String =
        Normalizer.normalize(texte.lowercase(Locale.FRANCE), Normalizer.Form.NFD)
            .replace(Regex("""\p{Mn}+"""), "")
            .replace(Regex("""[^a-z0-9 ]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
}
