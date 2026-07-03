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
import com.scantickets.app.data.ScanEnregistre
import com.scantickets.app.data.ScanStorage
import com.scantickets.app.data.TicketParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("scantickets", Context.MODE_PRIVATE)

    var dossierUri by mutableStateOf(chargerDossier())
        private set
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

    fun traiterScan(imageUri: Uri) {
        val dossier = dossierUri ?: run {
            message = "Choisis d'abord le dossier de sortie."
            return
        }
        viewModelScope.launch {
            enTraitement = true
            try {
                val texte = withContext(Dispatchers.IO) { lireTexte(imageUri) }
                val donnees = TicketParser.analyser(texte)
                val ok = withContext(Dispatchers.IO) {
                    ScanStorage.enregistrerScan(getApplication(), dossier, imageUri, donnees)
                }
                message = if (ok) {
                    val total = donnees.total?.let { "$it €" } ?: "total non détecté"
                    val date = donnees.dateTicket ?: "date non détectée"
                    "Ticket enregistré — $total, $date"
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

    private suspend fun lireTexte(imageUri: Uri): String {
        val image = InputImage.fromFilePath(getApplication(), imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            recognizer.process(image).await().text
        } finally {
            recognizer.close()
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
