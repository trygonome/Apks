package com.scantickets.app.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.json.JSONArray
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

        val json = jsonTicket(
            nomImage = fichierImage.name ?: "$nomBase.jpg",
            scanneLe = horodatage,
            total = donnees.total,
            dateTicket = donnees.dateTicket,
            magasin = donnees.magasin,
            articles = donnees.articles,
            coherenceOk = donnees.coherenceOk,
            confiance = donnees.confiance,
            texteOcr = donnees.texteOcr,
            corrige = false
        )
        val fichierJson = dossier.createFile("application/json", nomBase) ?: return false
        resolver.openOutputStream(fichierJson.uri)?.use { sortie ->
            sortie.write(json.toString(2).toByteArray(Charsets.UTF_8))
        } ?: return false

        return true
    }

    /** Réécrit le JSON d'un scan après correction manuelle dans l'app. */
    fun mettreAJourScan(
        context: Context,
        dossierUri: Uri,
        scan: ScanEnregistre,
        total: String?,
        dateTicket: String?,
        magasin: String?
    ): Boolean {
        val dossier = DocumentFile.fromTreeUri(context, dossierUri) ?: return false
        val fichierJson = dossier.listFiles()
            .firstOrNull { it.name == "${scan.nomBase}.json" } ?: return false
        val json = jsonTicket(
            nomImage = "${scan.nomBase}.jpg",
            scanneLe = scan.scanneLe,
            total = total,
            dateTicket = dateTicket,
            magasin = magasin,
            articles = scan.articles,
            coherenceOk = scan.coherenceOk,
            confiance = Confiance.HAUTE, // validé par un humain
            texteOcr = scan.texteOcr,
            corrige = true
        )
        return context.contentResolver.openOutputStream(fichierJson.uri, "wt")?.use { sortie ->
            sortie.write(json.toString(2).toByteArray(Charsets.UTF_8))
            true
        } ?: false
    }

    /** Supprime la photo et le JSON d'un scan. */
    fun supprimerScan(context: Context, dossierUri: Uri, scan: ScanEnregistre): Boolean {
        val dossier = DocumentFile.fromTreeUri(context, dossierUri) ?: return false
        var supprime = false
        for (fichier in dossier.listFiles()) {
            val nom = fichier.name ?: continue
            if (nom == "${scan.nomBase}.jpg" || nom == "${scan.nomBase}.json") {
                supprime = fichier.delete() || supprime
            }
        }
        return supprime
    }

    /**
     * Écrit `tickets.csv` (séparateur « ; », compatible Excel FR) dans le dossier
     * de sortie. Retourne le nom du fichier créé, ou null en cas d'échec.
     */
    fun exporterCsv(context: Context, dossierUri: Uri, scans: List<ScanEnregistre>): String? {
        val dossier = DocumentFile.fromTreeUri(context, dossierUri) ?: return null
        dossier.listFiles().firstOrNull { it.name == "tickets.csv" }?.delete()
        val fichier = dossier.createFile("text/csv", "tickets") ?: return null

        val contenu = buildString {
            append("fichier;scanne_le;date_ticket;magasin;categorie;total;confiance;nb_articles\n")
            for (scan in scans.sortedBy { it.scanneLe }) {
                append("${scan.nomBase}.jpg;")
                append("${scan.scanneLe};")
                append("${champCsv(scan.dateTicket)};")
                append("${champCsv(scan.magasin)};")
                append("${Categoriseur.categoriserTicket(scan.magasin).cle};")
                append("${scan.total ?: ""};")
                append("${scan.confiance.libelle};")
                append("${scan.articles.size}\n")
            }
        }
        val ecrit = context.contentResolver.openOutputStream(fichier.uri, "wt")?.use { sortie ->
            sortie.write(contenu.toByteArray(Charsets.UTF_8))
            true
        } ?: false
        return if (ecrit) fichier.name else null
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
                        articles = lireArticles(json),
                        coherenceOk = if (json.isNull("somme_articles_ok")) null
                        else json.optBoolean("somme_articles_ok"),
                        confiance = Confiance.depuisLibelle(json.champOuNull("confiance_total")),
                        scanneLe = json.optString("scanne_le", ""),
                        texteOcr = json.optString("texte_ocr", "")
                    )
                }.getOrNull()
            }
            .sortedByDescending { it.scanneLe }
    }

    private fun lireArticles(json: JSONObject): List<ArticleTicket> {
        val tableau = json.optJSONArray("articles") ?: return emptyList()
        return (0 until tableau.length()).mapNotNull { i ->
            val article = tableau.optJSONObject(i) ?: return@mapNotNull null
            ArticleTicket(
                libelle = article.optString("libelle"),
                prix = article.optString("prix"),
                quantite = article.optInt("quantite", 1)
            )
        }
    }

    private fun jsonTicket(
        nomImage: String,
        scanneLe: String,
        total: String?,
        dateTicket: String?,
        magasin: String?,
        articles: List<ArticleTicket>,
        coherenceOk: Boolean?,
        confiance: Confiance,
        texteOcr: String,
        corrige: Boolean
    ): JSONObject = JSONObject().apply {
        put("fichier_image", nomImage)
        put("scanne_le", scanneLe)
        put("total", total ?: JSONObject.NULL)
        put("date_ticket", dateTicket ?: JSONObject.NULL)
        put("magasin", magasin ?: JSONObject.NULL)
        put("categorie", Categoriseur.categoriserTicket(magasin).cle)
        put("confiance_total", confiance.libelle)
        put("somme_articles_ok", coherenceOk ?: JSONObject.NULL)
        put("corrige_manuellement", corrige)
        put("articles", JSONArray().apply {
            for (article in articles) {
                put(JSONObject().apply {
                    put("libelle", article.libelle)
                    put("prix", article.prix)
                    put("quantite", article.quantite)
                })
            }
        })
        put("texte_ocr", texteOcr)
    }

    private fun champCsv(valeur: String?): String =
        (valeur ?: "").replace(";", ",").replace("\n", " ")

    private fun JSONObject.champOuNull(cle: String): String? =
        if (isNull(cle)) null else optString(cle).takeIf { it.isNotEmpty() }
}
