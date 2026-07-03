package com.scantickets.app.data

import com.scantickets.app.data.ReconstructeurLignes.FragmentOcr
import org.junit.Assert.assertEquals
import org.junit.Test

class ReconstructeurLignesTest {

    @Test
    fun `deux colonnes melangees par blocs sont recollees en rangees`() {
        // ML Kit renvoie typiquement : bloc gauche (libellés) puis bloc droit (prix).
        val fragments = listOf(
            FragmentOcr("1 x Eden CHICKEN ML", gauche = 10, haut = 100, bas = 120),
            FragmentOcr("1 x Fries MAXI", gauche = 10, haut = 130, bas = 150),
            FragmentOcr("1 x Soft", gauche = 10, haut = 160, bas = 180),
            FragmentOcr("12.10", gauche = 400, haut = 102, bas = 118),
            FragmentOcr("3.10 C", gauche = 400, haut = 132, bas = 148),
            FragmentOcr("3.30 C", gauche = 400, haut = 162, bas = 178)
        )
        val attendu = """
            1 x Eden CHICKEN ML  12.10
            1 x Fries MAXI  3.10 C
            1 x Soft  3.30 C
        """.trimIndent()
        assertEquals(attendu, ReconstructeurLignes.reconstruire(fragments))
    }

    @Test
    fun `l'ordre gauche-droite est retabli dans une rangee`() {
        val fragments = listOf(
            FragmentOcr("13.10", gauche = 400, haut = 500, bas = 520),
            FragmentOcr("Total", gauche = 10, haut = 502, bas = 518),
            FragmentOcr("EUR", gauche = 300, haut = 501, bas = 519)
        )
        assertEquals("Total  EUR  13.10", ReconstructeurLignes.reconstruire(fragments))
    }

    @Test
    fun `des lignes bien separees restent separees`() {
        val fragments = listOf(
            FragmentOcr("Quick B782 Jenappes", gauche = 50, haut = 10, bas = 30),
            FragmentOcr("Avenue Wilson 510", gauche = 50, haut = 40, bas = 60)
        )
        assertEquals(
            "Quick B782 Jenappes\nAvenue Wilson 510",
            ReconstructeurLignes.reconstruire(fragments)
        )
    }

    @Test
    fun `liste vide - chaine vide`() {
        assertEquals("", ReconstructeurLignes.reconstruire(emptyList()))
    }
}
