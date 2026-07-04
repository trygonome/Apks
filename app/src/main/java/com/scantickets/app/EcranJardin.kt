package com.scantickets.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.scantickets.app.data.Categorie
import com.scantickets.app.data.StatistiquesBudget
import com.scantickets.app.progression.CarnetResistance
import com.scantickets.app.progression.EtatJardin
import com.scantickets.app.progression.FicheResistance
import com.scantickets.app.progression.MoteurProgression
import java.time.LocalDate
import kotlin.math.sin

/**
 * Le Jardin : ta souveraineté budgétaire incarnée en scène 2D.
 * Tout est dérivé des scans réels — rien à farmer artificiellement.
 */
@Composable
fun EcranJardin(vm: ScanViewModel) {
    val etat = remember(vm.scans, vm.budgetMensuel) {
        MoteurProgression.calculer(vm.scans, vm.budgetMensuel, LocalDate.now())
    }
    val moisCourant = remember { StatistiquesBudget.moisCourant() }
    val totalMois = remember(vm.scans) {
        StatistiquesBudget.totalDuMois(vm.scans, moisCourant)
    }
    val budget = vm.budgetMensuel
    val depassement = budget != null && totalMois > budget

    var ficheOuverte by remember { mutableStateOf<FicheResistance?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        SceneJardin(
            etat = etat,
            depassement = depassement,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        // ---------- HUD ----------
        Spacer(Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(etat.titre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Niveau ${etat.niveau}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (etat.serieEnCours > 0) {
                        Text(
                            "🔥 ${etat.serieEnCours} j  ×${"%.1f".format(etat.multiplicateurActuel)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                BarreXp(etat)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val grainesTriees = etat.graines.entries.sortedByDescending { it.value }
                    for ((categorie, nombre) in grainesTriees.take(5)) {
                        Text("${categorie.emoji} $nombre", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (etat.grainesOr > 0) {
                        Text("🏅 ${etat.grainesOr}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (etat.herbier.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Herbier : ${etat.herbier.size} enseigne${if (etat.herbier.size > 1) "s" else ""} découverte${if (etat.herbier.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // ---------- Carnet de résistance ----------
        Spacer(Modifier.height(16.dp))
        Text(
            "Carnet de résistance — ${CarnetResistance.nbDebloquees(etat)}/${CarnetResistance.fiches.size}",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            "Des paliers permanents, jamais d'échéance : l'urgence artificielle, " +
                "c'est l'arme de la Sirène, pas la nôtre.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(8.dp))
        for ((fiche, debloquee) in CarnetResistance.etatFiches(etat)) {
            CarteFiche(fiche, debloquee, etat) { if (debloquee) ficheOuverte = fiche }
            Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(24.dp))
    }

    ficheOuverte?.let { fiche ->
        AlertDialog(
            onDismissRequest = { ficheOuverte = null },
            title = { Text("${fiche.accapareur.emoji} ${fiche.titre}") },
            text = {
                Column {
                    Text(fiche.lecon, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Réflexe : ${fiche.reflexe}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { ficheOuverte = null }) { Text("Compris") }
            }
        )
    }
}

@Composable
private fun BarreXp(etat: EtatJardin) {
    val fraction = if (etat.xpPourNiveauSuivant > 0) {
        etat.xpDansNiveau.toFloat() / etat.xpPourNiveauSuivant
    } else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
    Text(
        "${etat.xpDansNiveau} / ${etat.xpPourNiveauSuivant} XP",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun CarteFiche(
    fiche: FicheResistance,
    debloquee: Boolean,
    etat: EtatJardin,
    onClic: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClic)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (debloquee) fiche.accapareur.emoji else "🔒",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (debloquee) fiche.titre else "Fiche verrouillée",
                    style = MaterialTheme.typography.bodyLarge
                )
                val (fait, but) = fiche.progression(etat)
                Text(
                    if (debloquee) "${fiche.accapareur.nom} — lue ou à lire"
                    else "${fiche.objectif}  ($fait/$but)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// ======================= La scène 2D =======================

private val couleursCategories = mapOf(
    Categorie.ALIMENTATION to Color(0xFFE53935),
    Categorie.RESTAURANT to Color(0xFFFB8C00),
    Categorie.TRANSPORT to Color(0xFF546E7A),
    Categorie.SANTE to Color(0xFFEC407A),
    Categorie.HYGIENE to Color(0xFF7E57C2),
    Categorie.MAISON to Color(0xFF8D6E63),
    Categorie.VETEMENTS to Color(0xFF29B6F6),
    Categorie.LOISIRS to Color(0xFFFFEE58),
    Categorie.ELECTRONIQUE to Color(0xFF26A69A),
    Categorie.ANIMAUX to Color(0xFFA1887F),
    Categorie.AUTRE to Color(0xFF9E9E9E)
)

@Composable
private fun SceneJardin(etat: EtatJardin, depassement: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "jardin")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "balancement"
    )

    val joursInactif = etat.joursDepuisDernierScan ?: Long.MAX_VALUE
    val sombre = depassement || joursInactif >= 4

    Canvas(modifier = modifier) {
        dessinerCiel(sombre)
        dessinerCollines(sombre)
        dessinerSol(sombre)
        dessinerPlantes(etat, phase)
        dessinerMascotte(etat, phase)
        if (joursInactif in 4..3650) dessinerOmbresAccapareurs(joursInactif)
    }
}

private fun DrawScope.dessinerCiel(sombre: Boolean) {
    val haut = if (sombre) Color(0xFF546E7A) else Color(0xFF64B5F6)
    val bas = if (sombre) Color(0xFF90A4AE) else Color(0xFFB3E5FC)
    drawRect(brush = Brush.verticalGradient(listOf(haut, bas)))
    // Soleil (pâle si le ciel est sombre)
    drawCircle(
        color = if (sombre) Color(0x66FFF59D) else Color(0xFFFFF176),
        radius = size.minDimension * 0.11f,
        center = Offset(size.width * 0.85f, size.height * 0.18f)
    )
    if (sombre) {
        // Nuages lourds
        for ((dx, r) in listOf(0.18f to 0.10f, 0.30f to 0.13f, 0.44f to 0.09f)) {
            drawCircle(
                color = Color(0xCC78909C),
                radius = size.minDimension * r,
                center = Offset(size.width * dx, size.height * 0.16f)
            )
        }
    }
}

private fun DrawScope.dessinerCollines(sombre: Boolean) {
    val couleur = if (sombre) Color(0xFF4E6B4F) else Color(0xFF66BB6A)
    drawCircle(
        color = couleur,
        radius = size.width * 0.55f,
        center = Offset(size.width * 0.15f, size.height * 1.05f)
    )
    drawCircle(
        color = couleur.copy(alpha = 0.85f),
        radius = size.width * 0.6f,
        center = Offset(size.width * 0.9f, size.height * 1.12f)
    )
}

private fun DrawScope.dessinerSol(sombre: Boolean) {
    val couleur = if (sombre) Color(0xFF5D4A3A) else Color(0xFF6D4C41)
    drawRect(
        color = couleur,
        topLeft = Offset(0f, size.height * 0.82f),
        size = Size(size.width, size.height * 0.18f)
    )
}

/**
 * Une plante par catégorie possédant des graines (max 6, les mieux dotées).
 * Stades : pousse (≥1 graine), plant (≥5), floraison (≥15).
 */
private fun DrawScope.dessinerPlantes(etat: EtatJardin, phase: Float) {
    val plantes = etat.graines.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.value }
        .take(6)
    if (plantes.isEmpty()) return

    val solY = size.height * 0.85f
    plantes.forEachIndexed { index, (categorie, graines) ->
        val x = size.width * (0.12f + 0.13f * index)
        val stade = when {
            graines >= 15 -> 3
            graines >= 5 -> 2
            else -> 1
        }
        val hauteur = size.height * (0.08f + 0.07f * stade)
        val balancement = sin(phase + index) * size.width * 0.006f
        val sommet = Offset(x + balancement, solY - hauteur)
        val couleurTige = Color(0xFF2E7D32)

        drawLine(couleurTige, Offset(x, solY), sommet, strokeWidth = size.width * 0.008f)
        // Feuilles
        drawCircle(couleurTige, size.width * 0.014f, Offset(x - size.width * 0.02f, solY - hauteur * 0.45f))
        drawCircle(couleurTige, size.width * 0.014f, Offset(x + size.width * 0.02f, solY - hauteur * 0.65f))
        // Tête : bouton, fruit ou fleur épanouie selon le stade
        val couleurTete = couleursCategories[categorie] ?: Color.Gray
        when (stade) {
            1 -> drawCircle(couleurTete.copy(alpha = 0.7f), size.width * 0.012f, sommet)
            2 -> drawCircle(couleurTete, size.width * 0.020f, sommet)
            else -> {
                for (i in 0 until 6) {
                    val angle = phase * 0f + i * (Math.PI.toFloat() / 3f)
                    drawCircle(
                        couleurTete.copy(alpha = 0.85f),
                        size.width * 0.013f,
                        sommet + Offset(
                            sin(angle) * size.width * 0.024f,
                            kotlin.math.cos(angle.toDouble()).toFloat() * size.width * 0.024f
                        )
                    )
                }
                drawCircle(Color(0xFFFFF59D), size.width * 0.012f, sommet)
            }
        }
    }
}

/** La tirelire-cochon, gardienne du jardin. Son humeur suit ta série. */
private fun DrawScope.dessinerMascotte(etat: EtatJardin, phase: Float) {
    val rose = Color(0xFFF48FB1)
    val roseFonce = Color(0xFFEC6C9C)
    val cx = size.width * 0.87f
    val respiration = sin(phase * 2) * size.height * 0.004f
    val cy = size.height * 0.83f + respiration
    val r = size.minDimension * 0.075f

    // Pattes, corps, oreille, groin
    drawCircle(roseFonce, r * 0.22f, Offset(cx - r * 0.5f, cy + r * 0.85f))
    drawCircle(roseFonce, r * 0.22f, Offset(cx + r * 0.5f, cy + r * 0.85f))
    drawCircle(rose, r, Offset(cx, cy))
    drawCircle(roseFonce, r * 0.28f, Offset(cx - r * 0.75f, cy - r * 0.75f))
    drawCircle(roseFonce, r * 0.4f, Offset(cx - r * 0.9f, cy + r * 0.1f))
    drawCircle(Color(0xFFAD4F72), r * 0.08f, Offset(cx - r * 1.0f, cy + r * 0.05f))
    drawCircle(Color(0xFFAD4F72), r * 0.08f, Offset(cx - r * 0.8f, cy + r * 0.15f))
    // Fente à pièces sur le dos
    drawLine(
        Color(0xFFAD4F72),
        Offset(cx + r * 0.1f, cy - r * 0.95f),
        Offset(cx + r * 0.5f, cy - r * 0.85f),
        strokeWidth = r * 0.12f
    )
    // Œil : éveillé si la série vit, endormi sinon
    if (etat.serieEnCours > 0) {
        drawCircle(Color(0xFF37282E), r * 0.1f, Offset(cx - r * 0.35f, cy - r * 0.25f))
    } else {
        drawLine(
            Color(0xFF37282E),
            Offset(cx - r * 0.5f, cy - r * 0.25f),
            Offset(cx - r * 0.2f, cy - r * 0.25f),
            strokeWidth = r * 0.08f
        )
    }
}

/** Les ombres des Accapareurs gagnent quand le jardin est laissé sans soin. */
private fun DrawScope.dessinerOmbresAccapareurs(joursInactif: Long) {
    val intensite = ((joursInactif - 3).coerceAtMost(7)) / 7f
    // Pénombre qui gagne depuis la gauche
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(Color(0xAA263238).copy(alpha = 0.55f * intensite), Color.Transparent)
        ),
        size = Size(size.width * 0.5f, size.height)
    )
    // La toile du Traqueur, coin supérieur gauche
    val coin = Offset(0f, 0f)
    val toile = Color(0xCCECEFF1).copy(alpha = 0.6f * intensite)
    for (i in 1..4) {
        val portee = size.minDimension * 0.09f * i
        drawLine(toile, coin, Offset(portee * 1.6f, portee * 0.15f), strokeWidth = 2f)
        drawLine(toile, coin, Offset(portee * 0.15f, portee * 1.6f), strokeWidth = 2f)
        drawLine(
            toile,
            Offset(portee * 1.15f, portee * 0.12f),
            Offset(portee * 0.12f, portee * 1.15f),
            strokeWidth = 2f
        )
    }
}
