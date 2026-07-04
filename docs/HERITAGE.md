# Héritage du prototype (v1 → v5.0-beta1)

*Intrants pour les phases Analyst/PM/Architect. Le code vit sur la branche
`archive/scan-tickets-v5beta` — rien n'est perdu, tout est réutilisable sur
décision de l'Architect.*

## Actifs techniques réutilisables (Kotlin pur, testés, sans dépendance UI)

| Actif | Chemin (archive) | Rôle | Tests |
|---|---|---|---|
| `ReconstructeurLignes` | `app/src/main/java/com/scantickets/app/data/` | Reconstruit la mise en page physique du ticket depuis les coordonnées OCR (répare les colonnes mélangées de ML Kit) | 4 |
| `AnalyseurTicket` | idem | Extraction à scores : total, date (num. + toutes lettres), enseigne, articles, TVA (taux BE/FR), confiance | 16 |
| `Categoriseur` | idem | Enseigne → catégorie ; mots-clés d'articles (matching début de mot) → ventilation du caddie | 4 |
| `StatistiquesBudget` | idem | Agrégations mensuelles, top magasins, projection, historiques de prix unitaires | 7 |
| `MoteurProgression` | `…/progression/` | XP/niveaux/titres, graines plafonnées/jour, séries ×1,1→×2, graines d'or, herbier — état 100 % recalculé depuis les scans | 13 |
| `CarnetResistance` | idem | 12 fiches pédagogiques à paliers permanents (4 Accapareurs) | 6 |

**Total : 50 tests verts**, dont un ticket réel (Quick Jenappes, bloc
publicitaire OCR déformé compris) qui sert de corpus de non-régression.

## Leçons de produit (validées par l'usage réel)

1. **Le 100 % local est l'identité du produit**, pas une contrainte — c'est
   l'argument (RGPD, anticapitalisme cohérent, données financières).
2. **Toute fonction doit être désactivable** — l'utilisateur compose son app
   (validé par la demande explicite ayant mené à la v4.0).
3. **Jamais de mécanique à échéance/FOMO** — les quêtes hebdomadaires ont été
   construites puis rejetées : l'urgence artificielle contredit le propos.
4. **Ne jamais récompenser la dépense** — règle d'or du jeu : gains plafonnés,
   multiplicateurs liés au comportement (scanner, vérifier, tenir le budget).
5. **La correction humaine prime** — champ `corrige_manuellement`, bonus de
   vérification : la donnée validée vaut plus que la donnée brute.
6. **L'utilisateur veut un livrable installable fréquemment** — les phases
   documentaires doivent annoncer clairement quand il n'y a pas d'APK.
7. Les monstres sont des **mécanismes, pas des marques** (pas de contentieux).

## Leçons techniques / d'environnement

- OCR : ne jamais analyser le texte brut de ML Kit — toujours reconstruire les
  rangées depuis les boîtes englobantes.
- Dates : « 25.06.2026 » produit un faux montant de 25,06 si on ne retire pas
  les dates avant la détection des montants.
- L'environnement de build : SDK dans `/opt/android-sdk`, Gradle via le miroir
  Tencent (services.gradle.org redirige vers GitHub, bloqué par le proxy) ;
  la clé de signature ne doit **jamais** être versionnée (refus mérité du
  garde-fou) — nouvelle identité ⇒ nouvelle clé, hors git.
- Format d'échange éprouvé : JPEG + JSON par ticket dans un dossier choisi par
  l'utilisateur (SAF), synchronisable (Syncthing) vers un éventuel pipeline PC
  (plan d'action « Scanner de tickets » : PaddleOCR + Qwen + SQLite).

## Dettes à ne pas reproduire (audit v3.1)

- Textes UI en dur (pas de `strings.xml`) → i18n impossible a posteriori ;
- Monolithe à module unique → viser une séparation moteurs/données/features ;
- Aucune validation terrain multi-enseignes → prévoir une campagne de tickets ;
- Pas de pipeline de vente/paiement ni statut légal si commercialisation.
