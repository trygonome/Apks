package com.scantickets.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyseurTicketTest {

    // Cas réel : ticket Quick (drive, Belgique), avec le bloc publicitaire
    // déformé par l'OCR au-dessus de l'enseigne. Lignes reconstruites
    // par ReconstructeurLignes (libellé et prix sur la même rangée).
    private val ticketQuick = """
        TICKET DE
        CAISSE TVA
        Ton avis = 1 Classic
        Burger Gratuit'
        Remplis le questionnaire et reçois un mail
        contenant un coupon. Utilise le lors
        1 de ta prochaine tomimanle:. Ben: appetit!
        www.quickandyou.be
        À l'achat d'1 Maxi Menu
        Quick B782 Jenappes
        Avenue Wilson 510
        7012 Jenappes
        0032 65 82 30 62
        BE0443.037.206
        Unique Ticket Code:
        Terminal name: BE-RO782-POS22
        Order No:  25.06.2026
        DT-148  19:04
        Host: Drive 1 Drive Thru  #210141
        Order Type: Drive Thru
        1 x Eden CHICKEN ML  12.10
        1 x Fries MAXI  3.10 C
        1 x Soft  3.30 C
        1 x Ketchup  1.00 C
        1 x Soft  2.50 A
        -1 x Soft (void)  -2.50 A
        1 x Rabais  -2.35 C
        POS Card  EUR 13.10
        Visa  EUR 13.10
        Total  EUR 13.10
    """.trimIndent()

    @Test
    fun `quick - total 13,10 et pas la date`() {
        val r = AnalyseurTicket.analyser(ticketQuick)
        assertEquals("13.10", r.total)
    }

    @Test
    fun `quick - confiance haute sur le total`() {
        // « Total » + « EUR » + valeur répétée 3× + bas de ticket
        val r = AnalyseurTicket.analyser(ticketQuick)
        assertEquals(Confiance.HAUTE, r.confiance)
    }

    @Test
    fun `quick - date normalisee`() {
        val r = AnalyseurTicket.analyser(ticketQuick)
        assertEquals("25/06/2026", r.dateTicket)
    }

    @Test
    fun `quick - enseigne trouvee malgre le bloc publicitaire deforme`() {
        val r = AnalyseurTicket.analyser(ticketQuick)
        assertEquals("Quick B782 Jenappes", r.magasin)
    }

    @Test
    fun `quick - articles extraits avec quantites et negatifs`() {
        val r = AnalyseurTicket.analyser(ticketQuick)
        val libelles = r.articles.map { it.libelle }
        assertTrue("Eden CHICKEN ML attendu", libelles.contains("Eden CHICKEN ML"))
        assertTrue("Rabais attendu", libelles.contains("Rabais"))
        assertEquals("-2.35", r.articles.first { it.libelle == "Rabais" }.prix)
    }

    @Test
    fun `carrefour - cas francais classique`() {
        val texte = """
            CARREFOUR MARKET
            12 rue de la Paix
            75002 Paris
            03/07/2026 14:32
            LAIT DEMI-ECREME  1.15
            PAIN COMPLET  2.40
            TOTAL A PAYER  3.55
            CB  3.55
        """.trimIndent()
        val r = AnalyseurTicket.analyser(texte)
        assertEquals("3.55", r.total)
        assertEquals(Confiance.HAUTE, r.confiance)
        assertEquals("03/07/2026", r.dateTicket)
        assertEquals("CARREFOUR MARKET", r.magasin)
        assertEquals(true, r.coherenceOk)
        assertEquals(2, r.articles.size)
    }

    @Test
    fun `coherence detecte un total qui ne colle pas aux articles`() {
        val texte = """
            EPICERIE DU COIN
            CAFE  4.50
            SUCRE  1.20
            TOTAL  9.99
        """.trimIndent()
        val r = AnalyseurTicket.analyser(texte)
        assertEquals("9.99", r.total)
        assertEquals(false, r.coherenceOk)
    }

    @Test
    fun `date en toutes lettres`() {
        val texte = """
            BOULANGERIE MARTIN
            Le 3 juillet 2026
            CROISSANT  1.20
            TOTAL  1.20
        """.trimIndent()
        val r = AnalyseurTicket.analyser(texte)
        assertEquals("03/07/2026", r.dateTicket)
    }

    @Test
    fun `sous-total et rendu ne sont pas pris pour le total`() {
        val texte = """
            SUPERETTE
            ARTICLE A  5.00
            ARTICLE B  3.00
            SOUS-TOTAL  8.00
            TOTAL  8.00
            ESPECES  10.00
            RENDU  2.00
        """.trimIndent()
        val r = AnalyseurTicket.analyser(texte)
        assertEquals("8.00", r.total)
        assertEquals(true, r.coherenceOk)
    }

    @Test
    fun `annee sur deux chiffres normalisee`() {
        val r = AnalyseurTicket.analyser("MAGASIN TEST\n03/07/26\nTOTAL 5.00")
        assertEquals("03/07/2026", r.dateTicket)
    }

    @Test
    fun `date invalide ignoree`() {
        // 45/13/26 n'est pas une date : jour et mois hors bornes.
        val r = AnalyseurTicket.analyser("MAGASIN TEST\n45/13/26\nTOTAL 5.00")
        assertEquals(null, r.dateTicket)
    }

    @Test
    fun `texte vide - rien de detecte, confiance basse`() {
        val r = AnalyseurTicket.analyser("")
        assertEquals(null, r.total)
        assertEquals(null, r.dateTicket)
        assertEquals(null, r.magasin)
        assertEquals(Confiance.BASSE, r.confiance)
        assertEquals(null, r.coherenceOk)
        assertTrue(r.articles.isEmpty())
    }

    @Test
    fun `tva belge multi-taux extraite`() {
        val texte = """
            COLRUYT JEMAPPES
            PAIN  2.10
            AMPOULE LED  8.90
            TOTAL  11.00
            TVA 6%  1.98  0.12
            TVA 21%  7.36  1.54
        """.trimIndent()
        val r = AnalyseurTicket.analyser(texte)
        assertEquals("11.00", r.total)
        assertEquals(2, r.tva.size)
        assertEquals("6", r.tva[0].taux)
        assertEquals("0.12", r.tva[0].montant)
        assertEquals("21", r.tva[1].taux)
        assertEquals("1.54", r.tva[1].montant)
    }

    @Test
    fun `tva francaise avec virgule et sans le mot tva`() {
        val texte = """
            CARREFOUR CITY
            SANDWICH  4.50
            TOTAL  4.50
            5,5%  4,27  0,23
        """.trimIndent()
        val r = AnalyseurTicket.analyser(texte)
        assertEquals(1, r.tva.size)
        assertEquals("5.5", r.tva[0].taux)
        assertEquals("0.23", r.tva[0].montant)
    }

    @Test
    fun `pourcentage de remise sans taux valide ignore`() {
        // « -30% » n'est pas un taux de TVA : rien ne doit être extrait.
        val texte = """
            MAGASIN PROMO
            PULL -30%  14.00
            TOTAL  14.00
        """.trimIndent()
        val r = AnalyseurTicket.analyser(texte)
        assertEquals(0, r.tva.size)
    }

    @Test
    fun `montant repete sans mot-cle prefere au maximum brut`() {
        val texte = """
            BAR DES SPORTS
            CAFE  1.50
            DEMI  2.80
            4.30
            4.30
        """.trimIndent()
        val r = AnalyseurTicket.analyser(texte)
        assertEquals("4.30", r.total)
    }
}
