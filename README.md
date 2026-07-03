# Scan Tickets 🧾📱

Application Android qui sert de **porte d'entrée** au système local d'analyse de tickets de caisse
(décrit dans le plan d'action « Scanner de tickets de caisse » — pipeline PC avec OCR, LLM local et SQLite).

## Rôle de l'app

Le téléphone fait **une seule chose, bien** : capturer des tickets exploitables et les mettre
à disposition du PC.

1. **Scan** — recadrage, redressement de perspective et nettoyage automatiques
   (API ML Kit Document Scanner, hors ligne).
2. **OCR local** — le texte brut est extrait sur le téléphone (ML Kit Text Recognition,
   modèle embarqué, 100 % hors ligne). L'app affiche immédiatement le total et la date
   détectés : si rien n'est lu, on rescanne sur place.
3. **Dépôt** — chaque scan est enregistré dans un dossier choisi par l'utilisateur :
   - `ticket_<horodatage>.jpg` — la photo recadrée ;
   - `ticket_<horodatage>.json` — texte OCR + total/date/magasin détectés.

En plus de la capture, l'app est **utilisable seule au quotidien** :

- **Correction manuelle** — appuyer sur un ticket ouvre le détail : photo, champs
  total/date/magasin éditables (réécrits dans le JSON avec `corrige_manuellement: true`),
  texte OCR complet, suppression.
- **Stats du mois** — total dépensé et nombre de tickets du mois en cours.
- **Export CSV** — génère `tickets.csv` (séparateur `;`, compatible Excel/LibreOffice FR)
  dans le dossier de sortie.

Ce dossier est ensuite synchronisé vers le PC (Syncthing recommandé, ou transfert par câble).
La **structuration fine** (articles, prix, catégories) reste au PC : c'est le rôle du LLM local
(Qwen via llama.cpp) du plan d'action.

## Format du fichier JSON

```json
{
  "fichier_image": "ticket_2026-07-03_141530.jpg",
  "scanne_le": "2026-07-03_141530",
  "total": "23.47",
  "date_ticket": "03/07/2026",
  "magasin": "CARREFOUR",
  "corrige_manuellement": false,
  "texte_ocr": "… texte brut complet du ticket …"
}
```

Les champs `total`, `date_ticket` et `magasin` sont des détections heuristiques (contrôle qualité
au moment du scan) — le pipeline PC fait foi pour la structuration définitive.

## Technique

- Kotlin 2.0 · Jetpack Compose · Material 3
- ML Kit Document Scanner (recadrage) + ML Kit Text Recognition (OCR, embarqué)
- Storage Access Framework : l'utilisateur choisit le dossier de sortie, aucune permission
  caméra/stockage requise
- Aucune connexion réseau : tout reste sur l'appareil
- minSdk 26 (Android 8.0), targetSdk 35

## Compilation

**Téléchargement direct de la dernière version** :
https://github.com/trygonome/Apks/raw/main/apk/scan-tickets.apk

L'APK de debug est aussi construit automatiquement par GitHub Actions à chaque push
(artefact `scan-tickets-debug`). En local :

```bash
./gradlew assembleDebug
# APK : app/build/outputs/apk/debug/app-debug.apk
```

## Historique

L'ancienne application « Budget Voice » est archivée sur la branche `archive/budget-voice-v1`.
