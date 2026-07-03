package com.scantickets.app

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.scantickets.app.data.AnalyseurTicket
import com.scantickets.app.data.ReconstructeurLignes
import com.scantickets.app.data.ScanEnregistre
import com.scantickets.app.data.ScanStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** Onglets de l'application. */
enum class Onglet(val libelle: String) {
    TICKETS("Tickets"),
    BUDGET("Budget"),
    PRIX("Prix")
}

/** Fonctions activables de l'application — l'utilisateur compose son app. */
data class OptionsApp(
    val ongletBudget: Boolean = true,
    val ongletPrix: Boolean = true,
    val montrerTva: Boolean = true,
    val montrerArticles: Boolean = true,
    val montrerBadge: Boolean = true,
    val montrerEmoji: Boolean = true,
    val importGalerie: Boolean = true
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("scantickets", Context.MODE_PRIVATE)

    var dossierUri by mutableStateOf(chargerDossier())
        private set
    var ongletActif by mutableStateOf(Onglet.TICKETS)
    var reglagesOuverts by mutableStateOf(false)
    var budgetMensuel by mutableStateOf(
        prefs.getFloat("budget_mensuel", 0f).toDouble().takeIf { it > 0 }
    )
        private set

    // ---- Réglages ----

    var options by mutableStateOf(chargerOptions())
        private set
    var themeMode by mutableStateOf(prefs.getString("theme_mode", "systeme") ?: "systeme")
        private set
    var accent by mutableStateOf(prefs.getString("accent", "vert") ?: "vert")
        private set
    var couleurDynamique by mutableStateOf(prefs.getBoolean("couleur_dynamique", false))
        private set

    private fun chargerOptions() = OptionsApp(
        ongletBudget = prefs.getBoolean("opt_budget", true),
        ongletPrix = prefs.getBoolean("opt_prix", true),
        montrerTva = prefs.getBoolean("opt_tva", true),
        montrerArticles = prefs.getBoolean("opt_articles", true),
        montrerBadge = prefs.getBoolean("opt_badge", true),
        montrerEmoji = prefs.getBoolean("opt_emoji", true),
        importGalerie = prefs.getBoolean("opt_galerie", true)
    )

    fun modifierOptions(nouvelles: OptionsApp) {
        options = nouvelles
        prefs.edit()
            .putBoolean("opt_budget", nouvelles.ongletBudget)
            .putBoolean("opt_prix", nouvelles.ongletPrix)
            .putBoolean("opt_tva", nouvelles.montrerTva)
            .putBoolean("opt_articles", nouvelles.montrerArticles)
            .putBoolean("opt_badge", nouvelles.montrerBadge)
            .putBoolean("opt_emoji", nouvelles.montrerEmoji)
            .putBoolean("opt_galerie", nouvelles.importGalerie)
            .apply()
    }

    fun definirThemeMode(mode: String) {
        themeMode = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun definirAccent(nom: String) {
        accent = nom
        prefs.edit().putString("accent", nom).apply()
    }

    fun definirCouleurDynamique(active: Boolean) {
        couleurDynamique = active
        prefs.edit().putBoolean("couleur_dynamique", active).apply()
    }
    var scans by mutableStateOf<List<ScanEnregistre>>(emptyList())
        private set
    var enTraitement by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
    var scanSelectionne by mutableStateOf<ScanEnregistre?>(null)
        private set

    init {
        rafraichir()
    }

    private fun chargerDossier(): Uri? = prefs.getString("dossier", null)?.toUri()

    fun definirDossier(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            getApplication<Application>().contentResolver.takePersistableUriPermission(uri, flags)
        }
        prefs.edit().putString("dossier", uri.toString()).apply()
        dossierUri = uri
        rafraichir()
    }

    fun selectionner(scan: ScanEnregistre?) {
        scanSelectionne = scan
    }

    fun definirBudget(montant: Double?) {
        budgetMensuel = montant?.takeIf { it > 0 }
        prefs.edit().putFloat("budget_mensuel", (budgetMensuel ?: 0.0).toFloat()).apply()
    }

    fun traiterScan(imageUri: Uri) {
        val dossier = dossierUri ?: run {
            message = "Choisis d'abord le dossier de sortie."
            return
        }
        viewModelScope.launch {
            enTraitement = true
            try {
                val texte = withContext(Dispatchers.IO) { lireTexte(imageUri) }
                val donnees = AnalyseurTicket.analyser(texte)
                val ok = withContext(Dispatchers.IO) {
                    ScanStorage.enregistrerScan(getApplication(), dossier, imageUri, donnees)
                }
                message = if (ok) {
                    val total = donnees.total?.let { "$it €" } ?: "total non détecté"
                    val date = donnees.dateTicket ?: "date non détectée"
                    "Ticket enregistré — $total, $date (confiance ${donnees.confiance.libelle})"
                } else {
                    "Impossible d'écrire dans le dossier de sortie."
                }
                rafraichir()
            } catch (e: Exception) {
                message = "Erreur pendant le traitement : ${e.message}"
            } finally {
                enTraitement = false
            }
        }
    }

    fun enregistrerCorrection(
        scan: ScanEnregistre,
        total: String?,
        dateTicket: String?,
        magasin: String?
    ) {
        val dossier = dossierUri ?: return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                ScanStorage.mettreAJourScan(
                    getApplication(), dossier, scan,
                    total?.takeIf { it.isNotBlank() },
                    dateTicket?.takeIf { it.isNotBlank() },
                    magasin?.takeIf { it.isNotBlank() }
                )
            }
            message = if (ok) "Correction enregistrée." else "Impossible d'enregistrer la correction."
            scanSelectionne = null
            rafraichir()
        }
    }

    fun supprimerScan(scan: ScanEnregistre) {
        val dossier = dossierUri ?: return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                ScanStorage.supprimerScan(getApplication(), dossier, scan)
            }
            message = if (ok) "Ticket supprimé." else "Impossible de supprimer ce ticket."
            scanSelectionne = null
            rafraichir()
        }
    }

    fun exporterCsv() {
        val dossier = dossierUri ?: return
        viewModelScope.launch {
            val nom = withContext(Dispatchers.IO) {
                ScanStorage.exporterCsv(getApplication(), dossier, scans)
            }
            message = if (nom != null) {
                "Export $nom créé dans le dossier de sortie."
            } else {
                "Échec de l'export CSV."
            }
        }
    }

    /**
     * OCR puis reconstruction de la mise en page physique du ticket à partir
     * des coordonnées des lignes : ML Kit renvoie le texte par blocs, ce qui
     * mélange les colonnes libellés/prix ; on les recolle par rangée.
     */
    private suspend fun lireTexte(imageUri: Uri): String {
        val image = InputImage.fromFilePath(getApplication(), imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val resultat = try {
            recognizer.process(image).await()
        } finally {
            recognizer.close()
        }
        val fragments = resultat.textBlocks
            .flatMap { it.lines }
            .mapNotNull { ligne ->
                val boite = ligne.boundingBox ?: return@mapNotNull null
                ReconstructeurLignes.FragmentOcr(
                    texte = ligne.text,
                    gauche = boite.left,
                    haut = boite.top,
                    bas = boite.bottom
                )
            }
        return if (fragments.isNotEmpty()) {
            ReconstructeurLignes.reconstruire(fragments)
        } else {
            resultat.text
        }
    }

    fun rafraichir() {
        val dossier = dossierUri ?: return
        viewModelScope.launch {
            scans = withContext(Dispatchers.IO) {
                ScanStorage.listerScans(getApplication(), dossier)
            }
        }
    }

    fun effacerMessage() {
        message = null
    }
}
