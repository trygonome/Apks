package com.scantickets.app.data

import android.net.Uri

/** Un article détecté sur le ticket. */
data class ArticleTicket(
    val libelle: String,
    val prix: String,
    val quantite: Int = 1
)

/** Une ligne de TVA du ticket (taux en %, montant de taxe). */
data class LigneTva(
    val taux: String,
    val montant: String
)

/** Niveau de confiance de la détection automatique du total. */
enum class Confiance(val libelle: String) {
    HAUTE("haute"),
    MOYENNE("moyenne"),
    BASSE("basse");

    companion object {
        fun depuisLibelle(libelle: String?): Confiance =
            entries.firstOrNull { it.libelle == libelle } ?: BASSE
    }
}

/** Données extraites d'un ticket par l'OCR + l'analyseur. */
data class DonneesTicket(
    val total: String?,
    val dateTicket: String?,
    val magasin: String?,
    val articles: List<ArticleTicket>,
    val tva: List<LigneTva>,
    val coherenceOk: Boolean?,
    val confiance: Confiance,
    val texteOcr: String
)

/** Un scan enregistré dans le dossier de sortie (photo + fichier JSON). */
data class ScanEnregistre(
    val nomBase: String,
    val imageUri: Uri?,
    val total: String?,
    val dateTicket: String?,
    val magasin: String?,
    val articles: List<ArticleTicket>,
    val tva: List<LigneTva>,
    val coherenceOk: Boolean?,
    val confiance: Confiance,
    val scanneLe: String,
    val texteOcr: String
) {
    /** Le scan mérite une vérification humaine. */
    val aVerifier: Boolean
        get() = total == null || confiance == Confiance.BASSE || coherenceOk == false
}
