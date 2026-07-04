package com.scantickets.app.progression

import com.scantickets.app.data.Categorie
import com.scantickets.app.data.Categoriseur
import com.scantickets.app.data.ScanEnregistre
import com.scantickets.app.data.StatistiquesBudget
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min
import kotlin.math.roundToInt

/** L'état complet du Jardin, entièrement dérivé de l'historique des scans. */
data class EtatJardin(
    val xpTotal: Int,
    val niveau: Int,
    val titre: String,
    val xpDansNiveau: Int,
    val xpPourNiveauSuivant: Int,
    val graines: Map<Categorie, Int>,
    val grainesOr: Int,
    val serieEnCours: Int,
    val meilleureSerie: Int,
    val multiplicateurActuel: Double,
    val herbier: List<String>,
    val moisSousBudget: Int,
    val joursDepuisDernierScan: Long?
)

/**
 * Moteur de progression du Jardin — pur Kotlin, déterministe, testé.
 *
 * Principe fondateur : l'état du jeu se **recalcule intégralement** depuis les
 * scans (source de vérité). Rien n'est sauvegardé à part ; supprimer un ticket
 * recalcule honnêtement le jardin.
 *
 * Règle d'or : on ne récompense jamais la dépense. Les gains viennent du geste
 * de scanner (plafonné), de la qualité des données (tickets vérifiés), de la
 * régularité (série) et du respect du budget (graines d'or).
 */
object MoteurProgression {

    const val XP_PAR_TICKET = 10
    const val XP_BONUS_VERIFICATION = 5
    const val XP_BONUS_MOIS_SOUS_BUDGET = 100
    const val TICKETS_COMPTES_PAR_JOUR = 5
    const val MULTIPLICATEUR_MAX = 2.0
    const val MULTIPLICATEUR_PAS = 0.1

    /** Titres de jardinier-résistant, un par niveau (le dernier est conservé au-delà). */
    val TITRES = listOf(
        "Pousse",
        "Semeur",
        "Jardinier",
        "Cueilleur",
        "Maraîcher",
        "Herboriste",
        "Apiculteur",
        "Gardien du Jardin",
        "Éclaireur de la Résistance",
        "Légende du Jardin"
    )

    fun calculer(
        scans: List<ScanEnregistre>,
        budgetMensuel: Double?,
        aujourdHui: LocalDate
    ): EtatJardin {
        val ordonnes = scans.sortedBy { it.scanneLe }

        var xp = 0.0
        val graines = mutableMapOf<Categorie, Int>()
        val herbier = mutableListOf<String>()

        // Parcours chronologique jour par jour : plafond journalier + série.
        var serie = 0
        var meilleureSerie = 0
        var jourPrecedent: LocalDate? = null
        var dernierJour: LocalDate? = null

        val parJour = ordonnes.groupBy { it.scanneLe.take(10) }.toSortedMap()
        for ((jourTexte, scansDuJour) in parJour) {
            val jour = jourLocal(jourTexte) ?: continue
            serie = if (jourPrecedent != null && jourPrecedent!!.plusDays(1) == jour) serie + 1 else 1
            meilleureSerie = maxOf(meilleureSerie, serie)
            jourPrecedent = jour
            dernierJour = jour

            val multiplicateur = multiplicateurPour(serie)
            for (scan in scansDuJour.take(TICKETS_COMPTES_PAR_JOUR)) {
                val base = XP_PAR_TICKET + if (scan.corrige) XP_BONUS_VERIFICATION else 0
                xp += base * multiplicateur
                val categorie = Categoriseur.categoriserTicket(scan.magasin)
                graines.merge(categorie, 1, Int::plus)
            }
        }

        // Herbier : chaque enseigne à sa première apparition (tous scans confondus).
        for (scan in ordonnes) {
            val magasin = scan.magasin?.trim()?.takeIf { it.isNotEmpty() } ?: continue
            if (herbier.none { it.equals(magasin, ignoreCase = true) }) herbier.add(magasin)
        }

        // Graines d'or : chaque mois révolu terminé sous l'objectif de budget.
        val moisCourant = String.format("%04d-%02d", aujourdHui.year, aujourdHui.monthValue)
        var grainesOr = 0
        if (budgetMensuel != null && budgetMensuel > 0) {
            val moisAvecScans = ordonnes.map { StatistiquesBudget.moisDe(it) }.distinct()
            for (mois in moisAvecScans) {
                if (mois >= moisCourant) continue
                val total = StatistiquesBudget.totalDuMois(ordonnes, mois)
                if (total > 0 && total <= budgetMensuel) {
                    grainesOr++
                    xp += XP_BONUS_MOIS_SOUS_BUDGET
                }
            }
        }

        // La série n'est « en cours » que si le dernier scan date d'aujourd'hui ou d'hier.
        val joursDepuis = dernierJour?.let { ChronoUnit.DAYS.between(it, aujourdHui) }
        val serieEnCours = when {
            joursDepuis == null -> 0
            joursDepuis <= 1 -> serie
            else -> 0
        }

        val xpTotal = xp.roundToInt()
        val (niveau, dansNiveau, pourSuivant) = decomposerXp(xpTotal)

        return EtatJardin(
            xpTotal = xpTotal,
            niveau = niveau,
            titre = TITRES[min(niveau - 1, TITRES.size - 1)],
            xpDansNiveau = dansNiveau,
            xpPourNiveauSuivant = pourSuivant,
            graines = graines,
            grainesOr = grainesOr,
            serieEnCours = serieEnCours,
            meilleureSerie = meilleureSerie,
            multiplicateurActuel = if (serieEnCours > 0) multiplicateurPour(serieEnCours) else 1.0,
            herbier = herbier,
            moisSousBudget = grainesOr,
            joursDepuisDernierScan = joursDepuis
        )
    }

    /** ×1,0 au premier jour, +0,1 par jour de série, plafonné à ×2,0. */
    fun multiplicateurPour(serie: Int): Double =
        min(MULTIPLICATEUR_MAX, 1.0 + MULTIPLICATEUR_PAS * (serie - 1).coerceAtLeast(0))

    /**
     * Le niveau n coûte 100×n XP à franchir.
     * Retourne (niveau, XP acquise dans le niveau, XP nécessaire pour le suivant).
     */
    fun decomposerXp(xpTotal: Int): Triple<Int, Int, Int> {
        var restant = xpTotal
        var niveau = 1
        while (restant >= 100 * niveau) {
            restant -= 100 * niveau
            niveau++
        }
        return Triple(niveau, restant, 100 * niveau)
    }

    private fun jourLocal(jourTexte: String): LocalDate? =
        runCatching { LocalDate.parse(jourTexte) }.getOrNull()
}
