package com.scantickets.app.data

import android.net.Uri

/** Données extraites d'un ticket par l'OCR + le parseur. */
data class DonneesTicket(
    val total: String?,
    val dateTicket: String?,
    val magasin: String?,
    val texteOcr: String
)

/** Un scan enregistré dans le dossier de sortie (photo + fichier JSON). */
data class ScanEnregistre(
    val nomBase: String,
    val imageUri: Uri?,
    val total: String?,
    val dateTicket: String?,
    val magasin: String?,
    val scanneLe: String,
    val texteOcr: String
)
