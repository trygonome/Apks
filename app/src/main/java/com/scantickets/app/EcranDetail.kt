package com.scantickets.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.scantickets.app.data.ScanEnregistre

/**
 * Détail d'un ticket : photo, correction manuelle des champs détectés,
 * texte OCR complet, suppression. Les corrections sont réécrites dans le
 * fichier JSON du dossier de sortie (avec `corrige_manuellement: true`),
 * donc le pipeline PC reçoit les données corrigées.
 */
@Composable
fun EcranDetail(scan: ScanEnregistre, vm: ScanViewModel) {
    var total by remember(scan.nomBase) { mutableStateOf(scan.total ?: "") }
    var dateTicket by remember(scan.nomBase) { mutableStateOf(scan.dateTicket ?: "") }
    var magasin by remember(scan.nomBase) { mutableStateOf(scan.magasin ?: "") }
    var voirOcr by remember(scan.nomBase) { mutableStateOf(false) }
    var confirmerSuppression by remember { mutableStateOf(false) }

    BackHandler { vm.selectionner(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AsyncImage(
            model = scan.imageUri,
            contentDescription = "Photo du ticket",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = total,
            onValueChange = { total = it },
            label = { Text("Total (€)") },
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = dateTicket,
            onValueChange = { dateTicket = it },
            label = { Text("Date du ticket") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = magasin,
            onValueChange = { magasin = it },
            label = { Text("Magasin") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { confirmerSuppression = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Supprimer")
            }
            Button(
                onClick = { vm.enregistrerCorrection(scan, total, dateTicket, magasin) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Enregistrer")
            }
        }

        if (scan.articles.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Articles détectés (${scan.articles.size})",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(4.dp))
            for (article in scan.articles) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (article.quantite > 1) {
                            "${article.quantite} × ${article.libelle}"
                        } else {
                            article.libelle
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${article.prix} €",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (scan.coherenceOk == false) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "⚠ La somme des articles ne correspond pas au total détecté — " +
                        "vérifie le total ci-dessus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { voirOcr = !voirOcr }) {
            Text(if (voirOcr) "Masquer le texte OCR" else "Voir le texte OCR complet")
        }
        if (voirOcr) {
            Text(
                text = scan.texteOcr.ifBlank { "(aucun texte détecté)" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmerSuppression) {
        AlertDialog(
            onDismissRequest = { confirmerSuppression = false },
            title = { Text("Supprimer ce ticket ?") },
            text = { Text("La photo et les données seront supprimées du dossier de sortie.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmerSuppression = false
                    vm.supprimerScan(scan)
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmerSuppression = false }) { Text("Annuler") }
            }
        )
    }
}
