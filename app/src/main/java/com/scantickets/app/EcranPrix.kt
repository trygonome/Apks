package com.scantickets.app

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.scantickets.app.data.HistoriquePrix
import com.scantickets.app.data.StatistiquesBudget
import kotlin.math.abs

/**
 * Suivi de l'évolution des prix : chaque article acheté au moins deux fois
 * (d'après les tickets scannés) apparaît avec sa tendance et son historique.
 */
@Composable
fun EcranPrix(vm: ScanViewModel) {
    val historiques = remember(vm.scans) {
        StatistiquesBudget.historiquesPrix(vm.scans)
    }

    if (historiques.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Le suivi des prix apparaît dès qu'un même article " +
                    "(même libellé sur le ticket) a été acheté au moins deux fois. " +
                    "Continue à scanner tes courses !",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(historiques, key = { it.libelle }) { historique ->
            CartePrix(historique)
        }
    }
}

@Composable
private fun CartePrix(historique: HistoriquePrix) {
    var deplie by remember { mutableStateOf(false) }
    val variation = historique.variation
    val couleurTendance = when {
        variation > 0.005 -> MaterialTheme.colorScheme.error
        variation < -0.005 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    val iconeTendance = when {
        variation > 0.005 -> Icons.AutoMirrored.Filled.TrendingUp
        variation < -0.005 -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { deplie = !deplie }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(historique.libelle, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${historique.points.size} achats — " +
                            "${euros(historique.premierPrix)} → ${euros(historique.dernierPrix)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Sparkline(
                    valeurs = historique.points.map { it.prix },
                    couleur = couleurTendance,
                    modifier = Modifier
                        .width(72.dp)
                        .height(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    iconeTendance,
                    contentDescription = null,
                    tint = couleurTendance
                )
            }
            if (abs(variation) > 0.005) {
                val signe = if (variation > 0) "+" else ""
                Text(
                    "$signe${euros(variation)} depuis le premier achat",
                    style = MaterialTheme.typography.bodySmall,
                    color = couleurTendance
                )
            }
            if (deplie) {
                Spacer(Modifier.height(8.dp))
                for (point in historique.points) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            point.date + (point.magasin?.let { "  ·  $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.weight(1f)
                        )
                        Text(euros(point.prix), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/** Mini-courbe d'évolution, sans bibliothèque : une simple polyligne. */
@Composable
private fun Sparkline(valeurs: List<Double>, couleur: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (valeurs.size < 2) return@Canvas
        val minimum = valeurs.min()
        val maximum = valeurs.max()
        val plage = (maximum - minimum).takeIf { it > 0 } ?: 1.0
        val pas = size.width / (valeurs.size - 1)
        val marge = size.height * 0.15f

        var precedent: Offset? = null
        valeurs.forEachIndexed { index, valeur ->
            val x = index * pas
            val y = marge +
                (size.height - 2 * marge) * (1f - ((valeur - minimum) / plage).toFloat())
            val point = Offset(x, y)
            precedent?.let { avant ->
                drawLine(
                    color = couleur,
                    start = avant,
                    end = point,
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
            precedent = point
        }
    }
}
