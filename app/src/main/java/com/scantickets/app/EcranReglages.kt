package com.scantickets.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.scantickets.app.ui.theme.accentsDisponibles

/** Réglages : l'utilisateur compose son application à la carte. */
@Composable
fun EcranReglages(vm: ScanViewModel, onChangerDossier: () -> Unit) {
    val contexte = LocalContext.current

    BackHandler { vm.reglagesOuverts = false }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // ---------- Apparence ----------
        TitreSection("Apparence")

        Text("Thème", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((mode, libelle) in listOf(
                "systeme" to "Système", "clair" to "Clair", "sombre" to "Sombre"
            )) {
                FilterChip(
                    selected = vm.themeMode == mode,
                    onClick = { vm.definirThemeMode(mode) },
                    label = { Text(libelle) }
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LigneInterrupteur(
                titre = "Couleurs dynamiques",
                sousTitre = "Suivre les couleurs de ton fond d'écran (Android 12+)",
                coche = vm.couleurDynamique,
                onCoche = vm::definirCouleurDynamique
            )
        }

        if (!vm.couleurDynamique) {
            Spacer(Modifier.height(8.dp))
            Text("Couleur d'accent", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (accent in accentsDisponibles) {
                    val choisi = vm.accent == accent.nom
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accent.clair)
                            .then(
                                if (choisi) Modifier.border(
                                    3.dp, MaterialTheme.colorScheme.onSurface, CircleShape
                                ) else Modifier
                            )
                            .clickable { vm.definirAccent(accent.nom) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (choisi) Text("✓", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }

        // ---------- Fonctions ----------
        TitreSection("Fonctions")
        val options = vm.options
        LigneInterrupteur("Onglet Budget", "Tableau de bord mensuel et objectif",
            options.ongletBudget) { vm.modifierOptions(options.copy(ongletBudget = it)) }
        LigneInterrupteur("Onglet Prix", "Évolution du prix de tes articles récurrents",
            options.ongletPrix) { vm.modifierOptions(options.copy(ongletPrix = it)) }
        LigneInterrupteur("TVA détectée", "Afficher les taux et montants de TVA sur le détail",
            options.montrerTva) { vm.modifierOptions(options.copy(montrerTva = it)) }
        LigneInterrupteur("Articles détectés", "Lister les lignes d'achat sur le détail",
            options.montrerArticles) { vm.modifierOptions(options.copy(montrerArticles = it)) }
        LigneInterrupteur("Badge « à vérifier »", "Signaler les tickets à la lecture douteuse",
            options.montrerBadge) { vm.modifierOptions(options.copy(montrerBadge = it)) }
        LigneInterrupteur("Emoji de catégorie", "🍔 🛒 ⛽ sur les cartes de tickets",
            options.montrerEmoji) { vm.modifierOptions(options.copy(montrerEmoji = it)) }

        // ---------- Scan ----------
        TitreSection("Scan")
        LigneInterrupteur("Import depuis la galerie",
            "Autoriser le choix d'une photo existante en plus de la caméra",
            options.importGalerie) { vm.modifierOptions(options.copy(importGalerie = it)) }

        // ---------- Dossier ----------
        TitreSection("Dossier de sortie")
        Text(
            vm.dossierUri?.lastPathSegment?.substringAfterLast(':')?.ifEmpty { "(racine)" }
                ?: "Aucun dossier choisi",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onChangerDossier) { Text("Changer le dossier") }

        // ---------- À propos ----------
        TitreSection("À propos")
        Text(
            "Scan Tickets ${BuildConfig.VERSION_NAME} — 100 % hors ligne, " +
                "aucune donnée ne quitte ton téléphone.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Row {
            TextButton(onClick = {
                contexte.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/trygonome/Apks"))
                )
            }) { Text("Code source") }
            TextButton(onClick = {
                contexte.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/trygonome/Apks/blob/main/PRIVACY.md")
                    )
                )
            }) { Text("Confidentialité") }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TitreSection(titre: String) {
    Spacer(Modifier.height(20.dp))
    Text(
        titre,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun LigneInterrupteur(
    titre: String,
    sousTitre: String,
    coche: Boolean,
    onCoche: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titre, style = MaterialTheme.typography.bodyLarge)
            Text(
                sousTitre,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = coche, onCheckedChange = onCoche)
    }
}
