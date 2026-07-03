package com.scantickets.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.scantickets.app.data.StatistiquesBudget
import java.util.Locale

/** Tableau de bord budgétaire : mois navigable, catégories, objectif, top magasins. */
@Composable
fun EcranBudget(vm: ScanViewModel) {
    var mois by remember { mutableStateOf(StatistiquesBudget.moisCourant()) }
    var editerBudget by remember { mutableStateOf(false) }

    val scans = vm.scans
    val total = StatistiquesBudget.totalDuMois(scans, mois)
    val moisPrecedent = StatistiquesBudget.moisPrecedent(mois)
    val totalPrecedent = StatistiquesBudget.totalDuMois(scans, moisPrecedent)
    val categories = StatistiquesBudget.parCategorie(scans, mois)
    val topMagasins = StatistiquesBudget.topMagasins(scans, mois)
    val projection = StatistiquesBudget.projectionFinDeMois(scans, mois)
    val estMoisCourant = mois == StatistiquesBudget.moisCourant()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // ---- Sélecteur de mois ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { mois = StatistiquesBudget.moisPrecedent(mois) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mois précédent")
            }
            Text(
                text = StatistiquesBudget.libelleMois(mois)
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = { mois = StatistiquesBudget.moisSuivant(mois) },
                enabled = !estMoisCourant
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mois suivant")
            }
        }

        // ---- Total du mois ----
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dépensé ce mois", style = MaterialTheme.typography.labelMedium)
                Text(
                    euros(total),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (totalPrecedent > 0) {
                    val delta = total - totalPrecedent
                    val signe = if (delta >= 0) "+" else ""
                    Text(
                        "$signe${euros(delta)} par rapport à " +
                            StatistiquesBudget.libelleMois(moisPrecedent),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (projection != null && total > 0) {
                    Text(
                        "Projection fin de mois au rythme actuel : ${euros(projection)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // ---- Objectif de budget ----
                val budget = vm.budgetMensuel
                Spacer(Modifier.height(8.dp))
                if (budget != null) {
                    val fraction = (total / budget).coerceIn(0.0, 1.0)
                    val depasse = total > budget
                    BarreProgression(
                        fraction = fraction.toFloat(),
                        couleur = if (depasse) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (depasse) "Objectif ${euros(budget)} dépassé !"
                            else "Objectif : ${euros(budget)} — reste ${euros(budget - total)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (depasse) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.outline
                        )
                        TextButton(onClick = { editerBudget = true }) { Text("Modifier") }
                    }
                } else {
                    TextButton(onClick = { editerBudget = true }) {
                        Text("Définir un objectif de budget mensuel")
                    }
                }
            }
        }

        // ---- Par catégorie ----
        if (categories.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Par catégorie", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            val maximum = categories.first().second
            for ((categorie, montant) in categories) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${categorie.emoji} ${categorie.libelle}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(euros(montant), style = MaterialTheme.typography.bodyMedium)
                    }
                    BarreProgression(
                        fraction = (montant / maximum).toFloat(),
                        couleur = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Spacer(Modifier.height(24.dp))
            Text(
                "Aucune dépense ce mois-ci. Scanne des tickets pour voir la répartition !",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // ---- Top magasins ----
        if (topMagasins.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Top magasins", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            for ((magasin, montant) in topMagasins) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        magasin,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(euros(montant), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (editerBudget) {
        DialogueBudget(
            budgetActuel = vm.budgetMensuel,
            onValider = { montant ->
                vm.definirBudget(montant)
                editerBudget = false
            },
            onFermer = { editerBudget = false }
        )
    }
}

@Composable
private fun BarreProgression(fraction: Float, couleur: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(couleur)
        )
    }
}

@Composable
private fun DialogueBudget(
    budgetActuel: Double?,
    onValider: (Double?) -> Unit,
    onFermer: () -> Unit
) {
    var saisie by remember {
        mutableStateOf(budgetActuel?.let { String.format(Locale.US, "%.0f", it) } ?: "")
    }
    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Objectif de budget mensuel") },
        text = {
            OutlinedTextField(
                value = saisie,
                onValueChange = { saisie = it },
                label = { Text("Montant (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onValider(saisie.replace(",", ".").toDoubleOrNull())
            }) { Text("Enregistrer") }
        },
        dismissButton = {
            Row {
                if (budgetActuel != null) {
                    TextButton(onClick = { onValider(null) }) { Text("Supprimer") }
                    Spacer(Modifier.width(4.dp))
                }
                TextButton(onClick = onFermer) { Text("Annuler") }
            }
        }
    )
}

internal fun euros(montant: Double): String =
    String.format(Locale.FRANCE, "%.2f €", montant)
