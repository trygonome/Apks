package com.scantickets.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TicketParserTest {

    // Cas réel : ticket Quick (drive) — la date 25.06.2026 ne doit pas
    // être prise pour un montant, et le total est porté par les lignes EUR.
    private val ticketQuick = """
        TICKET DE
        CAISSE TVA
        Quick B782 Jenappes
        Avenue Wilson 510
        7012 Jenappes
        0032 65 82 30 62
        BE0443.037.206
        Unique Ticket Code:
        Terminal name: BE-RO782-POS22
        Order No:
        25.06.2026
        DT-148
        19:04
        #210141
        Host: Drive 1 Drive Thru
        Order Type: Drive Thru
        1 x Eden CHICKEN ML
        12.10
        1 x Fries MAXI
        3.10 C
        1 x Soft
        3.30 C
        1 x Ketchup
        1.00 C
        1 x Soft
        2.50 A
        -1 x Soft (void)
        -2.50 A
        1 x Rabais
        -2.35 C
        POS Card
        EUR 13.10
        Visa
        EUR 13.10
        Total
        EUR 13.10
    """.trimIndent()

    @Test
    fun `ticket quick - total depuis les lignes EUR, pas depuis la date`() {
        val resultat = TicketParser.analyser(ticketQuick)
        assertEquals("13.10", resultat.total)
    }

    @Test
    fun `ticket quick - la date est detectee`() {
        val resultat = TicketParser.analyser(ticketQuick)
        assertEquals("25.06.2026", resultat.dateTicket)
    }

    @Test
    fun `ticket quick - le magasin n'est pas un libelle parasite`() {
        val resultat = TicketParser.analyser(ticketQuick)
        assertEquals("Quick B782 Jenappes", resultat.magasin)
    }

    @Test
    fun `total sur ligne a mot-cle classique`() {
        val texte = """
            CARREFOUR MARKET
            03/07/2026 14:32
            LAIT 1.15
            PAIN 2.40
            TOTAL A PAYER 3.55
            CB 3.55
        """.trimIndent()
        val resultat = TicketParser.analyser(texte)
        assertEquals("3.55", resultat.total)
        assertEquals("03/07/2026", resultat.dateTicket)
        assertEquals("CARREFOUR MARKET", resultat.magasin)
    }

    @Test
    fun `sans mot-cle - montant repete prefere au maximum`() {
        val texte = """
            BOULANGERIE DUPONT
            CROISSANT 1.20
            BAGUETTE 1.30
            2.50
            2.50
        """.trimIndent()
        val resultat = TicketParser.analyser(texte)
        assertEquals("2.50", resultat.total)
    }

    @Test
    fun `texte vide - aucun champ detecte`() {
        val resultat = TicketParser.analyser("")
        assertEquals(null, resultat.total)
        assertEquals(null, resultat.dateTicket)
        assertEquals(null, resultat.magasin)
    }
}
