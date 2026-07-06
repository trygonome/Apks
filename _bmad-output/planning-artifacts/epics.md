---
stepsCompleted: [1, 2, 3]
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

FR1: Epic 2 — capture prête < 2 s (mesurée en contexte pile)
FR2: Epic 2 — captures chaînées + bip désactivable
FR3: Epic 1 — recadrage auto + repli manuel ≤ 3 gestes
FR4: Epic 1 — persistance privée avant tout, refus clair si espace plein
FR5: Epic 1 — pipeline d'analyse en arrière-plan
FR6: Epic 2 — file survivant à la fermeture/redémarrage
FR7: Epic 1 — extraction structurée avec confiance (FR optimisé)
FR8: Epic 1 — texte reconstruit consultable
FR9: Epic 1 — champ vide + confiance basse, jamais d'invention
FR10: Epic 2 — doublon probable signalé, action en un geste
FR11: Epic 2 — correction < 10 s dans la fiche
FR12: Epic 2 — marquage vérifié / douteux sobre
FR13: Epic 1 — fichiers ordinaires dans le dossier choisi
FR14: Epic 1 — schema_version + lecture des versions antérieures
FR15: Epic 3 — ré-indexation d'un dossier existant
FR16: Epic 3 — dossier inaccessible : rien de perdu, rattrapage
FR17: Epic 2 — suppression propre
FR18: Epic 1 — aucune image en galerie publique
FR19: Epic 2 — accueil = liste nue + compteur

## Epic List

### Epic 1: Le premier ticket lu
Victor peut scanner UN ticket et le retrouver, compris et rangé : capture,
recadrage automatique (repli manuel), OCR local, extraction avec confiance,
fichiers image + JSON versionné dans son dossier. Fonde le squelette
:core/:app, fixe les versions, porte les moteurs hérités. Sortie : premier
APK installable sur S24+ — la qualité de lecture se juge sur de vrais
tickets avant de construire la suite.
**FRs covered:** FR3, FR4, FR5, FR7, FR8, FR9, FR13, FR14, FR18

### Epic 2: La pile vaincue
Victor enchaîne ~50 tickets sans friction et leur fait confiance : captures
chaînées + bip, file qui survit à tout, liste nue + compteur, doublons
signalés, correction < 10 s, marquage vérifié, suppression. Sortie : la
bêta de la séance pile.
**FRs covered:** FR1, FR2, FR6, FR10, FR11, FR12, FR17, FR19

### Epic 3: La souveraineté prouvée
Les fichiers de Victor survivent à tout : ré-indexation d'un dossier
existant (migration de téléphone), perte du dossier sans casse, validation
des cibles de performance et de l'accessibilité. Sortie : la version qui
affronte les critères d'acceptation du PRD.
**FRs covered:** FR15, FR16 (+ validation NFR1–NFR6)

## Epic 1: Le premier ticket lu

Victor peut scanner UN ticket et le retrouver, compris et rangé — le
premier APK installable juge la qualité de lecture sur de vrais tickets.

### Story 1.1: Installer une BuKet vide mais vraie

As a Victor,
I want installer sur mon S24+ un APK BuKet signé, construit par la CI,
So that le socle du projet (modules, versions, identité) existe et se
prouve dès le premier jour.

**Acceptance Criteria:**

**Given** le dépôt sur `main`
**When** la CI s'exécute
**Then** elle produit un APK release signé `org.buket.app` (versions de
bibliothèques fixées — clôture du différé « spike S-0 »)
**And** l'app s'installe et s'ouvre sur un écran d'accueil vide en
français, tous textes en `strings.xml` (zéro texte en dur, vérifié par lint)
**And** le module `:core` ne référence aucune API Android (vérifié par la
configuration de build) et `:app` dépend de `:core`, jamais l'inverse.

### Story 1.2: L'OCR local lit un vrai ticket

As a Victor,
I want que le moteur de lecture embarqué transforme l'image d'un ticket en
texte positionné, sans réseau,
So that la brique la plus risquée du projet soit prouvée avant tout le reste.

**Acceptance Criteria:**

**Given** l'image du ticket Quick du corpus (et 2 autres images de test)
**When** `OcrEngine` (PaddleOCR/ONNX embarqué) la traite en test CI
**Then** le résultat contient le texte avec les boîtes englobantes, et on y
retrouve « Quick » et « 13.10 »
**And** rien en aval de l'interface `OcrEngine` ne connaît le moteur (AD-6)
**And** l'APK reste sous 60 Mo modèles inclus (NFR4).

### Story 1.3: Les moteurs hérités comprennent le texte

As a Victor,
I want que le texte OCR soit reconstruit en lignes physiques puis analysé
(total, date, enseigne, articles, TVA, avec confiance),
So that un ticket devient des données structurées honnêtes.

**Acceptance Criteria:**

**Given** le corpus hérité (50 cas du prototype, ticket Quick inclus)
porté dans `:core/src/test`
**When** `LineReassembler` puis `ReceiptAnalyzer` (portage anglais des
moteurs hérités) traitent chaque cas
**Then** tous les tests du corpus passent en CI
**And** chaque champ extrait porte sa confiance, et un cas illisible/devise
non-euro produit un champ vide à confiance basse (FR9) — testé.

### Story 1.4: Le ticket est trouvé dans la photo large

As a Victor,
I want cadrer large et laisser l'app isoler et redresser le ticket
(rehaussement compris pour les délavés),
So that je n'aie jamais à viser précisément.

**Acceptance Criteria:**

**Given** des photos larges de tickets (droits, penchés, délavés — jeu de
test versionné)
**When** l'étape CROPPED du pipeline les traite
**Then** le ticket est détecté, recadré, redressé et rehaussé (CLAHE)
**And** si la détection échoue, la fiche propose un recadrage manuel en
3 gestes maximum (FR3) — jamais de blocage.

### Story 1.5: La photo au coffre, instantanément

As a Victor,
I want que chaque déclenchement mette la photo au coffre privé avant toute
autre chose,
So that aucune capture ne puisse se perdre, quoi qu'il arrive.

**Acceptance Criteria:**

**Given** l'écran de capture (CameraX) ouvert
**When** je déclenche
**Then** la photo est écrite dans le stockage privé avant tout traitement
(FR4), hors galerie publique (FR18)
**And** si l'espace disque est insuffisant, la capture refuse avec un
message clair et actionnable — jamais d'échec silencieux
**And** tuer l'app immédiatement après le déclenchement ne perd pas la photo.

### Story 1.6: Du déclencheur au dossier — le premier ticket complet

As a Victor,
I want scanner un ticket et retrouver sa fiche (champs + confiance + texte
reconstruit) et ses fichiers dans mon dossier,
So that la promesse fondatrice de BuKet soit vraie de bout en bout.

**Acceptance Criteria:**

**Given** un dossier de dépôt choisi au premier lancement
**When** je capture un ticket et que la file (WorkManager, étapes
CAPTURED→…→DEPOSITED) termine
**Then** la fiche montre total/date/enseigne/articles/TVA avec confiance,
et le texte reconstruit est consultable (FR7, FR8)
**And** le dossier contient `<id>.jpg` + `<id>.buket.json` avec
`schema_version` (FR13, FR14), lisibles sur PC
**And** cet APK est livré à Victor : **bêta 1**, verdict qualité de lecture
sur de vrais tickets.

## Epic 2: La pile vaincue

Victor enchaîne ~50 tickets sans friction et leur fait confiance.

### Story 2.1: Enchaîner les captures au bip

As a Victor,
I want déclencher, entendre un bip discret, et enchaîner le ticket suivant
sans écran intermédiaire,
So that la pile de 50 se traite en minutes, pas en heures.

**Acceptance Criteria:**

**Given** l'app ouverte
**When** j'enchaîne plusieurs déclenchements
**Then** chaque capture est confirmée par le bip (désactivable dans un
réglage unique) sans aucun écran de confirmation (FR2)
**And** l'app est prête à capturer < 2 s après ouverture et < 1,5 s entre
deux captures, mesuré sur S24+ (FR1, NFR1)
**And** l'analyse continue en arrière-plan sans jamais bloquer la capture.

### Story 2.2: La file qui ne meurt jamais

As a Victor,
I want que les traitements en attente survivent à la fermeture de l'app et
au redémarrage du téléphone, et que les échecs soient visibles et relançables,
So that je puisse capturer puis oublier, en confiance.

**Acceptance Criteria:**

**Given** 10 captures en file non traitées
**When** l'app est tuée puis le téléphone redémarré
**Then** la file reprend et termine sans perte ni doublon (FR6, étapes
idempotentes AD-4)
**And** un item en échec s'affiche FAILED avec sa raison et se relance en
un geste (AR8).

### Story 2.3: L'accueil — la liste nue

As a Victor,
I want ouvrir l'app et voir mes tickets ligne par ligne, avec le compteur
discret en haut à droite, et rien d'autre,
So that le lendemain de la séance pile, tout soit simplement là.

**Acceptance Criteria:**

**Given** des tickets traités
**When** j'ouvre BuKet
**Then** la liste (triée par date de capture) s'affiche : une ligne par
ticket, compteur en haut à droite, aucun autre élément (FR19)
**And** les tickets douteux portent un signalement sobre, les vérifiés leur
marque (FR12 — affichage)
**And** la liste est intégralement reconstruite depuis les fichiers (AD-3).

### Story 2.4: Corriger en dix secondes

As a Victor,
I want corriger un champ faux directement dans la fiche et marquer le
ticket vérifié,
So that la confiance se gagne ticket après ticket.

**Acceptance Criteria:**

**Given** une fiche ouverte avec un total erroné
**When** je touche le champ, corrige, valide
**Then** la correction est réécrite dans le JSON (< 10 s au total,
chronométré en bêta) et le ticket devient « vérifié » (FR11, FR12)
**And** les champs inconnus du JSON sont préservés à la réécriture (AR10).

### Story 2.5: Doublons signalés, suppression propre

As a Victor,
I want être prévenu si un ticket semble déjà scanné, et pouvoir supprimer
proprement,
So that ma liste reste juste.

**Acceptance Criteria:**

**Given** un ticket dont total+date+enseigne coïncident avec un existant
**When** son analyse se termine
**Then** il est marqué « doublon probable » et un geste unique le conserve
ou le supprime (FR10)
**And** la suppression retire image + JSON du dossier et du coffre (FR17)
**And** l'APK de cette story est livré : **bêta 2 — la séance pile**.

## Epic 3: La souveraineté prouvée

Les fichiers de Victor survivent à tout, et le PRD rend son verdict.

### Story 3.1: Un dossier qui renaît

As a Victor,
I want pointer BuKet (réinstallée ou sur un autre téléphone) vers mon
dossier existant et retrouver toute ma liste,
So that mes fichiers soient la seule vérité, pour toujours.

**Acceptance Criteria:**

**Given** un dossier contenant des paires image+JSON BuKet (y compris d'une
version de schéma antérieure)
**When** je le choisis au premier lancement
**Then** la liste renaît intégralement, versions antérieures lues (FR15,
FR14), champs inconnus préservés
**And** un fichier corrompu ou étranger est signalé, jamais bloquant.

### Story 3.2: La perte du dossier n'est pas une perte

As a Victor,
I want que BuKet détecte un dossier devenu inaccessible, m'en fasse choisir
un autre et rattrape le dépôt,
So that une carte SD retirée ne coûte jamais un ticket.

**Acceptance Criteria:**

**Given** des tickets capturés et un dossier de dépôt devenu inaccessible
**When** la file tente le dépôt
**Then** rien n'est perdu (coffre privé), l'app signale la situation et
propose de choisir un nouveau dossier (FR16)
**And** après le choix, le dépôt rattrape tout l'arriéré, sans doublon.

### Story 3.3: Le verdict du PRD

As a Victor,
I want dérouler le protocole d'acceptation complet sur ma vraie pile et mon
S24+,
So that la brique 1 soit déclarée finie sur des preuves, pas des promesses.

**Acceptance Criteria:**

**Given** la pile réelle triée « bon état / dégradé » avant la séance
**When** la séance pile complète est menée avec la version candidate
**Then** les critères 1 à 4 du PRD sont mesurés et publiés (≤ 20 min,
≥ 90 % sur bons états, réflexe < 5 s, fichiers lisibles sur PC +
renaissance sur second choix de dossier)
**And** les cibles NFR1 sont confirmées ou révisées en transparence,
l'accessibilité de base (TalkBack, contrastes) est vérifiée (NFR6)
**And** les deux semaines d'usage réel démarrent (critère 5) — la brique
n'est close qu'à leur verdict.
