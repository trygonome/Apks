package com.scantickets.app.progression

import com.scantickets.app.data.ArticleTicket
import com.scantickets.app.data.Categorie
import com.scantickets.app.data.Confiance
import com.scantickets.app.data.ScanEnregistre
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoteurProgressionTest {

    private fun scan(
        nomBase: String,
        scanneLe: String,
        magasin: String? = "CARREFOUR MARKET",
        total: String? = "10.00",
        dateTicket: String? = null,
        corrige: Boolean = false
    ) = ScanEnregistre(
        nomBase = nomBase,
        imageUri = null,
        total = total,
        dateTicket = dateTicket,
        magasin = magasin,
        articles = emptyList<ArticleTicket>(),
        tva = emptyList(),
        coherenceOk = null,
        confiance = Confiance.HAUTE,
        scanneLe = scanneLe,
        texteOcr = "",
        corrige = corrige
    )

    private val aujourdHui = LocalDate.of(2026, 7, 4)

    @Test
    fun `jardin vide - etat initial`() {
        val etat = MoteurProgression.calculer(emptyList(), null, aujourdHui)
        assertEquals(0, etat.xpTotal)
        assertEquals(1, etat.niveau)
        assertEquals("Pousse", etat.titre)
        assertEquals(0, etat.serieEnCours)
        assertEquals(0, etat.grainesOr)
        assertTrue(etat.herbier.isEmpty())
        assertEquals(null, etat.joursDepuisDernierScan)
    }

    @Test
    fun `plafond journalier - 7 scans le meme jour, 5 comptes`() {
        val scans = (1..7).map { i ->
            scan("t$i", "2026-07-04_10000$i")
        }
        val etat = MoteurProgression.calculer(scans, null, aujourdHui)
        assertEquals(50, etat.xpTotal) // 5 × 10 XP, multiplicateur ×1,0
        assertEquals(5, etat.graines[Categorie.ALIMENTATION])
    }

    @Test
    fun `bonus de verification`() {
        val scans = listOf(scan("t1", "2026-07-04_100000", corrige = true))
        val etat = MoteurProgression.calculer(scans, null, aujourdHui)
        assertEquals(15, etat.xpTotal) // 10 + 5
    }

    @Test
    fun `serie de 3 jours - multiplicateur croissant`() {
        val scans = listOf(
            scan("t1", "2026-07-02_100000"),
            scan("t2", "2026-07-03_100000"),
            scan("t3", "2026-07-04_100000")
        )
        val etat = MoteurProgression.calculer(scans, null, aujourdHui)
        // 10×1,0 + 10×1,1 + 10×1,2 = 33
        assertEquals(33, etat.xpTotal)
        assertEquals(3, etat.serieEnCours)
        assertEquals(3, etat.meilleureSerie)
        assertEquals(1.2, etat.multiplicateurActuel, 0.001)
    }

    @Test
    fun `serie cassee par un jour sans scan`() {
        val scans = listOf(
            scan("t1", "2026-07-01_100000"),
            scan("t2", "2026-07-03_100000")
        )
        val etat = MoteurProgression.calculer(scans, null, LocalDate.of(2026, 7, 3))
        assertEquals(20, etat.xpTotal) // deux jours à ×1,0
        assertEquals(1, etat.serieEnCours)
    }

    @Test
    fun `serie morte si dernier scan trop ancien`() {
        val scans = listOf(scan("t1", "2026-06-20_100000"))
        val etat = MoteurProgression.calculer(scans, null, aujourdHui)
        assertEquals(0, etat.serieEnCours)
        assertEquals(1.0, etat.multiplicateurActuel, 0.001)
        assertEquals(14L, etat.joursDepuisDernierScan)
    }

    @Test
    fun `multiplicateur plafonne a x2`() {
        assertEquals(1.0, MoteurProgression.multiplicateurPour(1), 0.001)
        assertEquals(1.9, MoteurProgression.multiplicateurPour(10), 0.001)
        assertEquals(2.0, MoteurProgression.multiplicateurPour(11), 0.001)
        assertEquals(2.0, MoteurProgression.multiplicateurPour(50), 0.001)
    }

    @Test
    fun `graines de la categorie du ticket`() {
        val scans = listOf(
            scan("t1", "2026-07-04_100000", magasin = "Quick B782 Jenappes"),
            scan("t2", "2026-07-04_110000", magasin = "Pharmacie Dupont")
        )
        val etat = MoteurProgression.calculer(scans, null, aujourdHui)
        assertEquals(1, etat.graines[Categorie.RESTAURANT])
        assertEquals(1, etat.graines[Categorie.SANTE])
    }

    @Test
    fun `graine d'or pour un mois revolu sous le budget`() {
        val scans = listOf(
            scan("t1", "2026-06-10_100000", total = "120.00", dateTicket = "10/06/2026"),
            scan("t2", "2026-06-20_100000", total = "80.00", dateTicket = "20/06/2026")
        )
        val etat = MoteurProgression.calculer(scans, budgetMensuel = 300.0, aujourdHui = aujourdHui)
        assertEquals(1, etat.grainesOr)
        // 2 scans × 10 (jours isolés, ×1,0) + 100 de bonus
        assertEquals(120, etat.xpTotal)
    }

    @Test
    fun `pas de graine d'or pour le mois en cours ni au-dessus du budget`() {
        val scans = listOf(
            scan("t1", "2026-07-01_100000", total = "50.00", dateTicket = "01/07/2026"),
            scan("t2", "2026-06-15_100000", total = "500.00", dateTicket = "15/06/2026")
        )
        val etat = MoteurProgression.calculer(scans, budgetMensuel = 300.0, aujourdHui = aujourdHui)
        assertEquals(0, etat.grainesOr) // juillet en cours, juin au-dessus du budget
    }

    @Test
    fun `herbier - enseignes distinctes en ordre de decouverte`() {
        val scans = listOf(
            scan("t1", "2026-07-01_100000", magasin = "Quick B782 Jenappes"),
            scan("t2", "2026-07-02_100000", magasin = "CARREFOUR MARKET"),
            scan("t3", "2026-07-03_100000", magasin = "quick b782 jenappes"),
            scan("t4", "2026-07-04_100000", magasin = null)
        )
        val etat = MoteurProgression.calculer(scans, null, aujourdHui)
        assertEquals(listOf("Quick B782 Jenappes", "CARREFOUR MARKET"), etat.herbier)
    }

    @Test
    fun `niveaux - 100 XP par palier progressif`() {
        assertEquals(Triple(1, 99, 100), MoteurProgression.decomposerXp(99))
        assertEquals(Triple(2, 0, 200), MoteurProgression.decomposerXp(100))
        assertEquals(Triple(2, 150, 200), MoteurProgression.decomposerXp(250))
        assertEquals(Triple(3, 0, 300), MoteurProgression.decomposerXp(300))
    }

    @Test
    fun `titre du niveau, plafonne au dernier`() {
        val etat = MoteurProgression.calculer(emptyList(), null, aujourdHui)
        assertEquals("Pousse", etat.titre)
        assertEquals(
            "Légende du Jardin",
            MoteurProgression.TITRES[MoteurProgression.TITRES.size - 1]
        )
    }
}
