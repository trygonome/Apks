---
stepsCompleted: [1]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-jardin-2026-07-04/prd.md
  - _bmad-output/planning-artifacts/architecture/spine-buket-brique1-2026-07-04/ARCHITECTURE-SPINE.md
  - docs/HERITAGE.md
---

# BuKet · Brique 1 - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for BuKet
brique 1, decomposing the requirements from the PRD and Architecture spine
into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1 (=FR-1.1) : L'app est prête à capturer en moins de 2 s après ouverture.
FR2 (=FR-1.2) : Captures enchaînées sans écran intermédiaire ; bip de caisse discret confirmant chaque prise, désactivable.
FR3 (=FR-1.3) : Photo cadrée large acceptée ; détection du ticket et recadrage automatiques ; repli manuel en ≤ 3 gestes.
FR4 (=FR-1.4) : Photo persistée dans le stockage privé avant toute autre opération ; refus clair si espace insuffisant — jamais d'échec silencieux.
FR5 (=FR-2.1) : Redressement, rehaussement de contraste et OCR en arrière-plan ; la capture n'attend jamais l'analyse.
FR6 (=FR-2.2) : La file de traitement survit à la fermeture de l'app et au redémarrage du téléphone.
FR7 (=FR-3.1) : Extraction : total, date, enseigne, articles, TVA (BE 6/12/21, FR 5,5/10/20), chacun avec confiance annoncée ; optimisée français.
FR8 (=FR-3.2) : Le texte intégral consultable est le texte reconstruit (rangées physiques), jamais la sortie par blocs.
FR9 (=FR-3.3) : Zone illisible / langue non couverte / devise non-euro ⇒ champ vide à confiance basse, jamais d'invention.
FR10 (=FR-3.4) : Doublon probable (total+date+enseigne identiques) signalé ; conserver ou supprimer en un geste.
FR11 (=FR-4.1) : Corriger un champ en < 10 s sans quitter la fiche.
FR12 (=FR-4.2) : Ticket vérifié marqué ; douteux signalés sobrement.
FR13 (=FR-5.1) : Ticket = fichiers ordinaires (image + JSON) dans le dossier choisi, lisibles sans l'app, synchronisables par outil tiers.
FR14 (=FR-5.2) : schema_version dans chaque JSON ; lecture garantie des versions antérieures.
FR15 (=FR-5.3) : Choisir un dossier contenant déjà des tickets BuKet reconstruit la liste intégralement.
FR16 (=FR-5.4) : Dossier inaccessible ⇒ rien de perdu, signalement, choix d'un nouveau dossier, rattrapage du dépôt.
FR17 (=FR-5.5) : Suppression propre (image + données) depuis l'app.
FR18 (=FR-5.6) : Aucune image dans la galerie publique (MediaStore).
FR19 (=FR-6.1) : Accueil = liste triée par date de capture, compteur discret en haut à droite, rien d'autre.

### NonFunctional Requirements

NFR1 (=N1) : Cibles sur Samsung Galaxy S24+ (validées/révisées à la 1re bêta) : ouverture→prêt < 2 s ; capture→capture < 1,5 s ; analyse < 10 s en arrière-plan.
NFR2 (=N2) : Zéro connexion réseau ; permissions caméra + dossier choisi uniquement ; aucune trace hors stockage privé et dossier.
NFR3 (=N3) : Fonctionne intégralement sur téléphone dégooglisé (stack 100 % libre).
NFR4 (=N4) : APK < 60 Mo modèles inclus (cible < 40).
NFR5 (=N5) : i18n dès le premier écran (strings.xml), V1 francophone.
NFR6 (=N6) : Accessibilité de base (TalkBack, contrastes) sur les écrans principaux.

### Additional Requirements

- AR1 (AD-1) : Pas de starter template — la story fondatrice (spike S-0) crée le squelette 2 modules `:core` (Kotlin pur) / `:app` (Android), fixe les versions de bibliothèques et prouve la chaîne PaddleOCR/ONNX sur un ticket réel.
- AR2 (AD-2) : Code/API/commits en anglais ; textes UI en français via resources.
- AR3 (AD-3/AD-4) : Pipeline CAPTURED→CROPPED→OCRED→EXTRACTED→DEPOSITED, étapes idempotentes orchestrées par WorkManager, communication par fichiers uniquement.
- AR4 (AD-5) : Privé d'abord, dépôt SAF par copie, purge après confirmation.
- AR5 (AD-6) : OCR derrière interface OcrEngine (texte + boîtes).
- AR6 (AD-7) : Portage en anglais des moteurs hérités (LineReassembler, ReceiptAnalyzer) depuis archive/scan-tickets-v5beta.
- AR7 (AD-8) : Mono-activité Compose, un ViewModel par écran, 3 écrans (Capture, Liste, Fiche), pas de lib de navigation.
- AR8 (AD-9) : Tout item de file expose son état, dont FAILED(raison)+relance.
- AR9 (AD-10) : Tests JUnit purs obligatoires pour toute story touchant :core ; corpus de tickets réels versionné (ticket Quick inclus d'office).
- AR10 (AD-11) : Lecteurs tolérants (champs inconnus ignorés en lecture, préservés en écriture).
- AR11 (AD-12) : applicationId org.buket.app, semver, clé de signature hors git.
- AR12 : CI GitHub Actions : tests + APK release à chaque push (pratique héritée, leçon environnement : Gradle via miroir).

### UX Design Requirements

Pas de contrat UX séparé — les principes de conception du PRD tiennent lieu
de contrat : la confiance se gagne (vérification facile), le bip suffit
(pas d'écran entre captures), la liste nue, jamais d'échec silencieux.

### FR Coverage Map

{{requirements_coverage_map}}

## Epic List

{{epics_list}}
