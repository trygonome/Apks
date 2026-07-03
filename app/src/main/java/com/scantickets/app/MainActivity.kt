package com.scantickets.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.scantickets.app.data.ScanEnregistre
import com.scantickets.app.data.StatistiquesBudget
import com.scantickets.app.ui.theme.ScanTicketsTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScanTicketsTheme {
                EcranPrincipal(activity = this)
            }
        }
    }
}

private fun iconeOnglet(onglet: Onglet): ImageVector = when (onglet) {
    Onglet.TICKETS -> Icons.AutoMirrored.Filled.ReceiptLong
    Onglet.BUDGET -> Icons.Filled.PieChart
    Onglet.PRIX -> Icons.AutoMirrored.Filled.TrendingUp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranPrincipal(activity: ComponentActivity, vm: ScanViewModel = viewModel()) {
    val snackbar = remember { SnackbarHostState() }

    val choixDossier = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let(vm::definirDossier)
    }

    val lanceurScan = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { resultat ->
        if (resultat.resultCode == Activity.RESULT_OK) {
            GmsDocumentScanningResult.fromActivityResultIntent(resultat.data)
                ?.pages
                ?.firstOrNull()
                ?.imageUri
                ?.let(vm::traiterScan)
        }
    }

    fun lancerScan() {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options)
            .getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                lanceurScan.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { erreur ->
                vm.message = "Scanner indisponible : ${erreur.message}"
            }
    }

    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.effacerMessage()
        }
    }

    val detail = vm.scanSelectionne

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            detail != null -> "Détail du ticket"
                            vm.ongletActif == Onglet.TICKETS -> "Scan Tickets"
                            else -> vm.ongletActif.libelle
                        }
                    )
                },
                navigationIcon = {
                    if (detail != null) {
                        IconButton(onClick = { vm.selectionner(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (detail == null) {
                NavigationBar {
                    for (onglet in Onglet.entries) {
                        NavigationBarItem(
                            selected = vm.ongletActif == onglet,
                            onClick = { vm.ongletActif = onglet },
                            icon = { Icon(iconeOnglet(onglet), contentDescription = null) },
                            label = { Text(onglet.libelle) }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when {
                detail != null -> EcranDetail(detail, vm)
                vm.dossierUri == null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    CarteChoixDossier { choixDossier.launch(null) }
                }
                vm.ongletActif == Onglet.BUDGET -> EcranBudget(vm)
                vm.ongletActif == Onglet.PRIX -> EcranPrix(vm)
                else -> EcranTickets(
                    vm = vm,
                    onScanner = ::lancerScan,
                    onChangerDossier = { choixDossier.launch(null) }
                )
            }
        }
    }
}

@Composable
private fun EcranTickets(
    vm: ScanViewModel,
    onScanner: () -> Unit,
    onChangerDossier: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onScanner,
            enabled = !vm.enTraitement,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            if (vm.enTraitement) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(12.dp))
                Text("Traitement en cours…")
            } else {
                Icon(Icons.Filled.DocumentScanner, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Scanner un ticket", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(12.dp))
        CarteStatsMois(vm.scans)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = vm::exporterCsv,
                enabled = vm.scans.isNotEmpty()
            ) {
                Icon(Icons.Filled.TableChart, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Exporter CSV")
            }
            TextButton(onClick = onChangerDossier) {
                Icon(Icons.Filled.Folder, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Dossier")
            }
        }
        ListeScans(vm.scans, onClic = vm::selectionner)
    }
}

@Composable
private fun CarteStatsMois(scans: List<ScanEnregistre>) {
    val moisCourant = remember { StatistiquesBudget.moisCourant() }
    val scansDuMois = StatistiquesBudget.scansDuMois(scans, moisCourant)
    val totalMois = StatistiquesBudget.totalDuMois(scans, moisCourant)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ce mois-ci", style = MaterialTheme.typography.labelMedium)
                Text(
                    String.format(Locale.FRANCE, "%.2f €", totalMois),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "${scansDuMois.size} ticket${if (scansDuMois.size > 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun CarteChoixDossier(onChoisir: () -> Unit) {
    Spacer(Modifier.height(24.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Choisis le dossier où enregistrer les tickets scannés. " +
                    "C'est ce dossier que ton PC pourra synchroniser (Syncthing, câble…).",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onChoisir) {
                Text("Choisir le dossier de sortie")
            }
        }
    }
}

@Composable
private fun ListeScans(
    scans: List<ScanEnregistre>,
    onClic: (ScanEnregistre) -> Unit
) {
    if (scans.isEmpty()) {
        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Aucun ticket pour l'instant. Scanne ton premier ticket !",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(scans, key = { it.nomBase }) { scan ->
            CarteScan(scan, onClic = { onClic(scan) })
        }
    }
}

@Composable
private fun CarteScan(scan: ScanEnregistre, onClic: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClic)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = scan.imageUri,
                contentDescription = "Photo du ticket",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = scan.total?.let { "$it €" } ?: "Total non détecté",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = com.scantickets.app.data.Categoriseur
                            .categoriserTicket(scan.magasin).emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (scan.aVerifier) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "à vérifier",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(
                    text = listOfNotNull(scan.magasin, scan.dateTicket)
                        .joinToString(" · ")
                        .ifEmpty { scan.scanneLe },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
