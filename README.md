# Scan Tickets 🧾

[![Build APK](https://github.com/trygonome/Apks/actions/workflows/build-apk.yml/badge.svg)](https://github.com/trygonome/Apks/actions/workflows/build-apk.yml)

Application Android de numérisation de tickets de caisse, 100 % hors ligne.
Porte d'entrée du système local d'analyse de dépenses (pipeline PC avec OCR,
LLM local et SQLite décrit dans le plan d'action « Scanner de tickets de caisse »),
et utilisable seule au quotidien.

**📥 Télécharger la dernière version :**
https://github.com/trygonome/Apks/raw/main/apk/scan-tickets.apk

## Fonctionnalités

- **Scan intelligent** — détection du ticket, recadrage et redressement de
  perspective automatiques (ML Kit Document Scanner, hors ligne).
- **OCR embarqué** — texte extrait sur le téléphone (ML Kit Text Recognition,
  modèle inclus dans l'APK, aucune connexion requise).
- **Reconstruction géométrique** — les lignes OCR sont réassemblées d'après
  leurs coordonnées pour restituer la mise en page physique du ticket
  (libellé + prix sur la même ligne), au lieu du texte par blocs de ML Kit
  qui mélange les colonnes.
- **Analyse à scores** — total, date (numérique ou en toutes lettres), enseigne
  (dictionnaire d'enseignes FR/BE + ancrage sur l'adresse) et **articles ligne
  par ligne**, avec contrôle de cohérence (somme des articles vs total) et
  niveau de confiance affiché.
- **Correction manuelle** — chaque ticket s'ouvre en détail : champs éditables,
  articles détectés, texte OCR complet, suppression. Les tickets douteux sont
  marqués « à vérifier ».
- **Stats du mois** — total dépensé et nombre de tickets en cours de mois.
- **Export CSV** — `tickets.csv` (séparateur `;`, compatible Excel/LibreOffice FR).

## Chaîne complète

```
📱 Scan (recadrage auto) → OCR local → analyse → dossier de sortie
                                                    ├── ticket_<date>.jpg
                                                    ├── ticket_<date>.json
                                                    └── tickets.csv
💻 PC (optionnel) : Syncthing → PaddleOCR/Qwen → SQLite → tableaux de bord
```

Le dossier de sortie est choisi par l'utilisateur (Storage Access Framework —
aucune permission caméra ni stockage). Pointez Syncthing dessus pour alimenter
le pipeline PC ; les corrections manuelles y sont signalées
(`corrige_manuellement: true`) et priment sur la détection automatique.

## Format du fichier JSON

```json
{
  "fichier_image": "ticket_2026-07-03_141530.jpg",
  "scanne_le": "2026-07-03_141530",
  "total": "13.10",
  "date_ticket": "25/06/2026",
  "magasin": "Quick B782 Jenappes",
  "confiance_total": "haute",
  "somme_articles_ok": false,
  "corrige_manuellement": false,
  "articles": [
    { "libelle": "Eden CHICKEN ML", "prix": "12.10", "quantite": 1 },
    { "libelle": "Rabais", "prix": "-2.35", "quantite": 1 }
  ],
  "texte_ocr": "… texte brut reconstruit du ticket …"
}
```

## Technique

- Kotlin 2.0 · Jetpack Compose · Material 3 · minSdk 26 (Android 8.0), targetSdk 35
- ML Kit Document Scanner + Text Recognition (embarqué)
- Analyse déterministe et testée : 17 tests unitaires, dont des tickets réels
- Release signée et minifiée (R8, sans obfuscation)

## Compilation

```bash
./gradlew testDebugUnitTest   # tests du moteur d'analyse
./gradlew assembleRelease     # APK signé : app/build/outputs/apk/release/app-release.apk
```

La CI GitHub Actions exécute les tests et produit l'APK signé à chaque push
(artefact `scan-tickets-release`).

> 🔐 **Signature** : la clé (`signing/scantickets.jks`) n'est pas versionnée
> (dépôt public). En son absence — sur la CI notamment — la release est signée
> avec la clé de debug : l'APK reste installable. L'APK publié dans `apk/` est
> signé avec la clé de release.

## Historique

L'ancienne application « Budget Voice » est archivée sur la branche
[`archive/budget-voice-v1`](https://github.com/trygonome/Apks/tree/archive/budget-voice-v1).

## Licence

[MIT](LICENSE)
