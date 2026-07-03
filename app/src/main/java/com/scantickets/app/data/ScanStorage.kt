package com.scantickets.app.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enregistre chaque scan dans le dossier choisi par l'utilisateur :
 * une photo `ticket_<horodatage>.jpg` + un fichier `ticket_<horodatage>.json`
 * contenant le texte OCR et les champs détectés. C'est ce dossier que
 * Syncthing (ou autre) synchronise vers le PC.
 */
object ScanStorage {

    private val formatHorodatage = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.FRANCE)

    fun enregistrerScan(
        context: Context,
        dossierUri: Uri,
        imageUri: Uri,
        donnees: DonneesTicket
    ): Boolean {
        val dossier = DocumentFile.fromTreeUri(context, dossierUri) ?: return false
        val horodatage = formatHorodatage.format(Date())
        val nomBase = "ticket_$horodatage"
        val resolver = context.contentResolver

        val fichierImage = dossier.createFile("image/jpeg", nomBase) ?: return false
        val copieOk = resolver.openOutputStream(fichierImage.uri)?.use { sortie ->
            resolver.openInputStream(imageUri)?.use { entree ->
                entree.copyTo(sortie)
                true
            } ?: false
        } ?: false
        if (!copieOk) {
            fichierImage.delete()
            return false
        }

        val json = JSONObject().apply {
            put("fichier_image", fichierImage.name ?: "$nomBase.jpg")
            put("scanne_le", horodatage)
            put("total", donnees.total ?: JSONObject.NULL)
            put("date_ticket", donnees.dateTicket ?: JSONObject.NULL)
            put("magasin", donnees.magasin ?: JSONObject.NULL)
            put("texte_ocr", donnees.texteOcr)
        }
        val fichierJson = dossier.createFile("application/json", nomBase) ?: return false
        resolver.openOutputStream(fichierJson.uri)?.use { sortie ->
            sortie.write(json.toString(2).toByteArray(Charsets.UTF_8))
        } ?: return false

        return true
    }

    fun listerScans(context: Context, dossierUri: Uri): List<ScanEnregistre> {
        val dossier = DocumentFile.fromTreeUri(context, dossierUri) ?: return emptyList()
        val fichiers = dossier.listFiles()
        val images = fichiers
            .filter { it.name?.endsWith(".jpg", ignoreCase = true) == true }
            .associateBy { it.name!!.removeSuffix(".jpg") }

        return fichiers
            .filter { it.name?.endsWith(".json", ignoreCase = true) == true }
            .mapNotNull { fichier ->
                runCatching {
                    val texte = context.contentResolver.openInputStream(fichier.uri)!!
                        .use { it.readBytes() }
                        .decodeToString()
                    val json = JSONObject(texte)
                    val nomBase = fichier.name!!.removeSuffix(".json")
                    ScanEnregistre(
                        nomBase = nomBase,
                        imageUri = images[nomBase]?.uri,
                        total = json.champOuNull("total"),
                        dateTicket = json.champOuNull("date_ticket"),
                        magasin = json.champOuNull("magasin"),
                        scanneLe = json.optString("scanne_le", "")
                    )
                }.getOrNull()
            }
            .sortedByDescending { it.scanneLe }
    }

    private fun JSONObject.champOuNull(cle: String): String? =
        if (isNull(cle)) null else optString(cle).takeIf { it.isNotEmpty() }
}
