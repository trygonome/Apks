package com.scantickets.app.progression

import com.scantickets.app.data.Categorie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarnetResistanceTest {

    private fun etat(
        ticketsScannes: Int = 0,
        ticketsVerifies: Int = 0,
        meilleureSerie: Int = 0,
        grainesOr: Int = 0
    ) = EtatJardin(
        xpTotal = 0, niveau = 1, titre = "Pousse",
        xpDansNiveau = 0, xpPourNiveauSuivant = 100,
        graines = emptyMap<Categorie, Int>(), grainesOr = grainesOr,
        serieEnCours = 0, meilleureSerie = meilleureSerie,
        multiplicateurActuel = 1.0, herbier = emptyList(),
        moisSousBudget = grainesOr, joursDepuisDernierScan = null,
        ticketsScannes = ticketsScannes, ticketsVerifies = ticketsVerifies
    )

    @Test
    fun `12 fiches, 3 par accapareur`() {
        assertEquals(12, CarnetResistance.fiches.size)
        for (accapareur in Accapareur.entries) {
            assertEquals(
                "3 fiches pour ${accapareur.nom}",
                3,
                CarnetResistance.fiches.count { it.accapareur == accapareur }
            )
        }
    }

    @Test
    fun `au depart - seules les 4 fiches de rencontre sont debloquees`() {
        assertEquals(4, CarnetResistance.nbDebloquees(etat()))
    }

    @Test
    fun `paliers du Traqueur - le nombre de scans debloque les fiches`() {
        assertEquals(4, CarnetResistance.nbDebloquees(etat(ticketsScannes = 14)))
        assertEquals(5, CarnetResistance.nbDebloquees(etat(ticketsScannes = 15)))
        assertEquals(6, CarnetResistance.nbDebloquees(etat(ticketsScannes = 50)))
    }

    @Test
    fun `paliers croises - chaque metrique debloque sa piste`() {
        val complet = etat(
            ticketsScannes = 50,
            ticketsVerifies = 15,
            meilleureSerie = 14,
            grainesOr = 3
        )
        assertEquals(CarnetResistance.fiches.size, CarnetResistance.nbDebloquees(complet))
    }

    @Test
    fun `progression d'une fiche - bornee a son objectif`() {
        val fiche = CarnetResistance.fiches.first { it.id == "traqueur_1" }
        assertEquals(7 to 15, fiche.progression(etat(ticketsScannes = 7)))
        assertEquals(15 to 15, fiche.progression(etat(ticketsScannes = 99)))
    }

    @Test
    fun `toutes les fiches ont une lecon et un reflexe`() {
        for (fiche in CarnetResistance.fiches) {
            assertTrue(fiche.lecon.length > 50)
            assertTrue(fiche.reflexe.length > 20)
        }
    }
}
