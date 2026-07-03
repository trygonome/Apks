package com.scantickets.app.data

/**
 * Reconstruit la mise en page physique d'un ticket à partir des lignes OCR
 * et de leurs coordonnées.
 *
 * ML Kit renvoie le texte par *blocs* : sur un ticket à deux colonnes
 * (libellés à gauche, prix à droite), on obtient tous les libellés puis tous
 * les prix, dans le désordre. Ici on regroupe les fragments par recouvrement
 * vertical (même rangée physique) puis on les trie de gauche à droite,
 * ce qui restitue des lignes « LIBELLÉ …… PRIX » exploitables par l'analyseur.
 */
object ReconstructeurLignes {

    /** Un fragment de texte OCR avec son empan vertical et sa position gauche. */
    data class FragmentOcr(
        val texte: String,
        val gauche: Int,
        val haut: Int,
        val bas: Int
    ) {
        val centreVertical: Float get() = (haut + bas) / 2f
    }

    fun reconstruire(fragments: List<FragmentOcr>): String {
        if (fragments.isEmpty()) return ""

        val tries = fragments.sortedBy { it.centreVertical }
        val rangees = mutableListOf<MutableList<FragmentOcr>>()

        for (fragment in tries) {
            val rangee = rangees.lastOrNull()
            if (rangee != null && appartientALaRangee(rangee, fragment)) {
                rangee.add(fragment)
            } else {
                rangees.add(mutableListOf(fragment))
            }
        }

        return rangees.joinToString("\n") { rangee ->
            rangee.sortedBy { it.gauche }.joinToString("  ") { it.texte }
        }
    }

    /**
     * Un fragment appartient à la rangée si son centre vertical tombe dans
     * l'empan vertical d'un des fragments déjà placés (tolérance naturelle :
     * les fragments d'une même ligne physique se chevauchent verticalement).
     */
    private fun appartientALaRangee(
        rangee: List<FragmentOcr>,
        fragment: FragmentOcr
    ): Boolean = rangee.any { existant ->
        fragment.centreVertical >= existant.haut && fragment.centreVertical <= existant.bas
    }
}
