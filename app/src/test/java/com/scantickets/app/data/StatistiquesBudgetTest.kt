package com.scantickets.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatistiquesBudgetTest {

    private fun scan(
        nomBase: String,
        total: String?,
        dateTicket: String?,
        magasin: String?,
        scanneLe: String,
        articles: List<ArticleTicket> = emptyList(),
        coherenceOk: Boolean? = null
    ) = ScanEnregistre(
        nomBase = nomBase,
        imageUri = null,
        total = total,
        dateTicket = dateTicket,
        magasin = magasin,
        articles = articles,
        coherenceOk = coherenceOk,
        confiance = Confiance.HAUTE,
        scanneLe = scanneLe,
        texteOcr = ""
    )

    private val scans = listOf(
        // Caddie Carrefour cohérent : ventilé par article (3.55 = 1.15 + 2.40).
        scan(
            "t1", "3.55", "03/07/2026", "CARREFOUR MARKET", "2026-07-03_100000",
            articles = listOf(
                ArticleTicket("LAIT DEMI-ECREME", "1.15"),
                ArticleTicket("LESSIVE ARIEL", "2.40")
            ),
            coherenceOk = true
        ),
        // Quick incohérent : tout le total part en Restaurant.
        scan(
            "t2", "13.10", "25/06/2026", "Quick B782 Jenappes", "2026-07-03_110000",
            articles = listOf(ArticleTicket("Eden CHICKEN ML", "12.10")),
            coherenceOk = false
        ),
        // Second passage Carrefour en juillet : le lait a augmenté.
        scan(
            "t3", "1.29", "15/07/2026", "CARREFOUR MARKET", "2026-07-15_100000",
            articles = listOf(ArticleTicket("LAIT DEMI-ECREME", "1.29")),
            coherenceOk = true
        ),
        // Sans date de ticket : rattaché au mois du scan.
        scan("t4", "5.00", null, null, "2026-07-20_100000")
    )

    @Test
    fun `mois d'un scan - date du ticket prioritaire, sinon date de scan`() {
        assertEquals("2026-06", StatistiquesBudget.moisDe(scans[1])) // ticket de juin scanné en juillet
        assertEquals("2026-07", StatistiquesBudget.moisDe(scans[3]))
    }

    @Test
    fun `total du mois`() {
        assertEquals(3.55 + 1.29 + 5.00, StatistiquesBudget.totalDuMois(scans, "2026-07"), 0.001)
        assertEquals(13.10, StatistiquesBudget.totalDuMois(scans, "2026-06"), 0.001)
        assertEquals(0.0, StatistiquesBudget.totalDuMois(scans, "2026-05"), 0.001)
    }

    @Test
    fun `repartition par categorie - caddie ventile, ticket incoherent entier`() {
        val juillet = StatistiquesBudget.parCategorie(scans, "2026-07").toMap()
        assertEquals(1.15 + 1.29, juillet[Categorie.ALIMENTATION]!!, 0.001)
        assertEquals(2.40, juillet[Categorie.MAISON]!!, 0.001)
        assertEquals(5.00, juillet[Categorie.AUTRE]!!, 0.001)

        val juin = StatistiquesBudget.parCategorie(scans, "2026-06").toMap()
        assertEquals(13.10, juin[Categorie.RESTAURANT]!!, 0.001)
    }

    @Test
    fun `top magasins du mois`() {
        val top = StatistiquesBudget.topMagasins(scans, "2026-07")
        assertEquals("Magasin inconnu", top[0].first)
        assertEquals(5.00, top[0].second, 0.001)
        assertEquals("CARREFOUR MARKET", top[1].first)
        assertEquals(3.55 + 1.29, top[1].second, 0.001)
    }

    @Test
    fun `historique de prix - le lait suivi sur deux achats`() {
        val historiques = StatistiquesBudget.historiquesPrix(scans)
        assertEquals(1, historiques.size)
        val lait = historiques[0]
        assertEquals("LAIT DEMI-ECREME", lait.libelle)
        assertEquals(2, lait.points.size)
        assertEquals(1.15, lait.premierPrix, 0.001)
        assertEquals(1.29, lait.dernierPrix, 0.001)
        assertTrue("le prix a augmenté", lait.variation > 0)
    }

    @Test
    fun `navigation entre mois`() {
        assertEquals("2026-06", StatistiquesBudget.moisPrecedent("2026-07"))
        assertEquals("2025-12", StatistiquesBudget.moisPrecedent("2026-01"))
        assertEquals("2026-01", StatistiquesBudget.moisSuivant("2025-12"))
        assertEquals("juillet 2026", StatistiquesBudget.libelleMois("2026-07"))
    }

    @Test
    fun `projection nulle pour un mois passe`() {
        assertEquals(null, StatistiquesBudget.projectionFinDeMois(scans, "2020-01"))
    }
}
