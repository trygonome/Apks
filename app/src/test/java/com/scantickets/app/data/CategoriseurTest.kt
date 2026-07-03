package com.scantickets.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoriseurTest {

    @Test
    fun `enseignes connues vers la bonne categorie`() {
        assertEquals(Categorie.RESTAURANT, Categoriseur.categoriserTicket("Quick B782 Jenappes"))
        assertEquals(Categorie.ALIMENTATION, Categoriseur.categoriserTicket("CARREFOUR MARKET"))
        assertEquals(Categorie.ALIMENTATION, Categoriseur.categoriserTicket("Colruyt Mons"))
        assertEquals(Categorie.SANTE, Categoriseur.categoriserTicket("Pharmacie Dupont"))
        assertEquals(Categorie.MAISON, Categoriseur.categoriserTicket("BRICO Jemappes"))
        assertEquals(Categorie.TRANSPORT, Categoriseur.categoriserTicket("TotalEnergies Wilson"))
        assertEquals(Categorie.ELECTRONIQUE, Categoriseur.categoriserTicket("MediaMarkt Mons"))
    }

    @Test
    fun `accents et casse ignores`() {
        assertEquals(Categorie.ALIMENTATION, Categoriseur.categoriserTicket("ÉPICERIE du coin"))
    }

    @Test
    fun `enseigne inconnue ou absente - autre`() {
        assertEquals(Categorie.AUTRE, CategoriserTicketInconnu())
        assertEquals(Categorie.AUTRE, Categoriseur.categoriserTicket(null))
    }

    private fun CategoriserTicketInconnu() =
        Categoriseur.categoriserTicket("Chez Gérard SPRL")

    @Test
    fun `article affine par mots-cles, sinon categorie du ticket`() {
        // Un caddie Carrefour se ventile.
        assertEquals(
            Categorie.MAISON,
            Categoriseur.categoriserArticle("LESSIVE ARIEL 2L", Categorie.ALIMENTATION)
        )
        assertEquals(
            Categorie.HYGIENE,
            Categoriseur.categoriserArticle("Shampooing doux", Categorie.ALIMENTATION)
        )
        assertEquals(
            Categorie.ALIMENTATION,
            Categoriseur.categoriserArticle("LAIT DEMI-ECREME", Categorie.ALIMENTATION)
        )
        // Article inconnu : il suit le ticket.
        assertEquals(
            Categorie.RESTAURANT,
            Categoriseur.categoriserArticle("Eden CHICKEN ML", Categorie.RESTAURANT)
        )
    }
}
