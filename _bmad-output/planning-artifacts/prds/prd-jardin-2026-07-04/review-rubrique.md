# Revue qualité PRD — BuKet, brique 1 (scanner et stocker ses tickets, en local)

*Document évalué* : `prd.md` (v. 2026-07-04)
*Documents de référence* : brief validé (`brief.md`), recherche état de l'art
(`brique1-etat-de-lart.md`), héritage prototype (`docs/HERITAGE.md`).
*Calibrage appliqué* : projet de conviction solo, PRD volontairement court
(~2 pages), rigueur « hobby sérieux » — la brièveté n'est pas pénalisée,
l'ambiguïté et l'intestabilité le sont.

## Verdict global

Ce PRD est **prêt avec corrections mineures**. Le squelette est solide : un
seul utilisateur réel nommé, des parcours qui pilotent effectivement les
exigences, des FR/NFR presque tous testables avec des seuils chiffrés, un
périmètre hors-scope explicite et des questions ouvertes réellement ouvertes.
Les points faibles sont localisés et peu coûteux à corriger : quelques
adjectifs non opérationnalisés (« digne », « sobrement ») qui fragilisent la
testabilité de 2-3 FR, une ambiguïté sur la correspondance entre le découpage
« brique 1/2/3… » et les phases V1/V2 du brief, et une coquille de nom de
produit. Rien ne bloque un passage à l'architecture, mais un tour de
relecture ciblé est recommandé.

## Décision-readiness (decision-readiness) — adéquat

Les décisions structurantes sont posées comme des décisions et non comme des
« considérations » : licence Apache-2.0, distribution par releases GitHub
(F-Droid préparé mais non soumis), stack 100 % libre, minSdk 26, langue V1
française (§1). Les deux points laissés à trancher (§7, nom de paquet,
son du bip) sont de vraies questions ouvertes, pas des questions rhétoriques
avec la réponse dans la phrase suivante.

Un point est cependant lissé : le choix de la stack 100 % libre
(CameraX + OpenCV + PaddleOCR/ONNX) plutôt que la stack ML Kit du prototype
n'est présenté que comme un principe (« réutiliser l'éprouvé, ne construire
que le manquant », §1) sans nommer le coût accepté. Le document de recherche
en amont est pourtant explicite sur ce coût : « plus d'ingénierie... détection
de bords probablement un cran sous celle de Google au début »
(`brique1-etat-de-lart.md`, Option A). Le PRD absorbe une partie du risque via
le repli « recadrage manuel digne » (FR-1.3), ce qui atténue le problème, mais
le lecteur du PRD seul ne sait pas qu'un compromis a été fait consciemment.

### Findings
- **basse** Le compromis stack libre vs ML Kit (coût d'ingénierie et qualité
  de recadrage moindre au début) n'est pas nommé dans le PRD lui-même (§1) —
  il figure seulement dans l'étude en amont. *Correction proposée* : ajouter
  une phrase du type « ce choix coûte en ingénierie de recadrage propre,
  compensé par le repli manuel (FR-1.3) » pour que le PRD reste
  auto-suffisant.

## Substance vs théâtre (substance over theater) — solide

Aucun théâtre détecté. Les deux parcours (UJ-1, UJ-2) ont un protagoniste
unique et réel (Victor), et chacun pilote directement des FR (UJ-1 → F1/F2/F3,
UJ-2 → N1 et le critère d'acceptation n°3). Les NFR portent des seuils
produit-spécifiques (N1 : <2 s, <1,5 s, <10 s ; N4 : <60 Mo cible <40 Mo) et
non du texte générique « scalable/secure/reliable ». Pas de section
différenciation gratuite : la justification concurrentielle (§1, « le
paysage libre s'arrête au PDF... ») s'appuie directement sur la recherche
état de l'art et n'est pas un exercice de style.

## Cohérence stratégique (strategic coherence) — solide

Thèse claire et unique : occuper le vide entre scanner générique et saisie
manuelle par l'extraction structurée locale (§1). Les 6 groupes de FR
découlent de cette thèse et des deux parcours, pas d'une liste de souhaits.
Les critères d'acceptation (§5) valident la thèse plutôt que de mesurer de
l'activité : « la pile est vaincue », « fiabilité mesurée », « souveraineté
prouvée » sont tous liés au problème énoncé, pas des métriques de vanité
(pas de DAU/MAU hors sujet ici). Aucun contre-métrique explicite n'est
nommé, mais à l'échelle d'un PRD de 2 pages pour un utilisateur unique, ce
n'est pas un manque bloquant.

## Clarté du « done » (done-ness clarity) — adéquat avec réserves

La majorité des FR/NFR sont testables avec un seuil ou un comportement
vérifiable (FR-1.1, FR-1.4, FR-2.2, FR-3.3, FR-5.1/5.2, N1-N4). Quelques
formulations restent adjectivales et donc difficiles à faire échouer un test
dessus :

### Findings
- **moyenne** FR-1.3 : « recadrage manuel digne en repli » — « digne » n'est
  pas opérationnalisé (combien de points de contrôle ? aperçu en direct ?
  annulation possible ?). Le même mot est repris pour la correction de champ
  (§2, principe 1) sans définition commune. *Correction proposée* : remplacer
  par un critère vérifiable, ex. « l'utilisateur peut ajuster les 4 coins du
  cadrage avec aperçu en direct avant validation ».
- **basse** FR-4.2 : « signalés sobrement, sans culpabilisation » — critère
  de ton, non testable en soi ; acceptable pour un projet solo mais à
  garder à l'esprit si une checklist QA est un jour écrite dessus.
  *Correction proposée* : si un jour formalisé, remplacer par une règle
  d'interface concrète (ex. « pas de couleur rouge, pas de compteur
  d'erreurs visible »).
- **basse** N1 : « téléphone milieu de gamme » n'a pas de référence concrète
  (modèle, RAM, année). *Correction proposée* : citer un modèle de
  référence réel (ex. le téléphone de Victor) pour ancrer le test de
  performance.
- **basse** Critère d'acceptation n°2 (§5) : le seuil « ≥ 90 % des tickets en
  bon état » suppose une classification « bon état » vs « délavé/froissé »
  qui n'est définie nulle part dans le PRD. *Correction proposée* : une
  phrase de tri (ex. « bon état = lisible à l'œil nu sans plisser les yeux »)
  suffirait à rendre le seuil vérifiable sans ambiguïté d'interprétation
  a posteriori.

## Honnêteté du périmètre (scope honesty) — adéquat avec une zone grise

La section 6 (Hors périmètre) fait un vrai travail et correspond à la
délégation du brief (« découpage détaillé des fonctionnalités V1 »). Mais
elle mélange deux catégories de nature différente sans les distinguer :

### Findings
- **moyenne** §6 regroupe dans la même liste « Vues budget et
  catégorisation d'affichage » (qui appartient au V1 « outil pur » du brief,
  §« Périmètre du premier livrable ») et « jardin et Carnet (briques
  ultérieures) » (qui est le V2 du brief, une couche pédagogique distincte).
  Le brief lui-même sépare nettement ces deux paliers. Sans distinction
  explicite, un lecteur ne sait pas si la catégorisation/le budget sont une
  « brique 2 » qui arrive bientôt (reste du V1) ou s'ils ont glissé au même
  rang que le jardin ludique (V2, après V1 éprouvée) — ce qui changerait la
  feuille de route perçue. *Correction proposée* : scinder la liste en deux
  lignes, ex. « Reste du V1 (brique 2, à venir) : budget, catégorisation,
  argent repris » vs « V2 (après V1 éprouvée) : jardin, Accapareurs, Carnet ».
- **basse** Aucune mention du format géographique des tickets (Belgique et/ou
  France) alors que l'héritage (`HERITAGE.md`) signale explicitement un
  `AnalyseurTicket` gérant les « taux TVA BE/FR » et un bug de date
  DD.MM.YYYY européen. FR-3.1 mentionne « TVA » sans préciser le ou les pays
  visés, ce qui touche directement la testabilité du critère d'acceptation
  n°2 (sur quelle pile de tickets, de quel pays, le seuil de 90 % est-il
  mesuré ?). *Correction proposée* : préciser en §1 ou §5 le pays / la
  composition de la pile de référence de Victor.

Pas de sur-verrouillage silencieux détecté par ailleurs : la non-soumission
F-Droid, l'absence d'iOS, l'absence d'autres langues sont toutes déclarées
explicitement (§6), en cohérence avec les non-objectifs du brief.

## Utilisabilité en aval (downstream usability) — adéquat pour un PRD de cette taille

IDs contigus et uniques (FR-1.1 → FR-6.1, N1-N6, UJ-1/UJ-2), pas de doublon ni
de trou détecté. Pas de glossaire formel, mais le vocabulaire (ticket,
capture, extraction, confiance, souveraineté) est employé de façon stable
d'une section à l'autre — un glossaire séparé serait probablement une
sur-formalisation pour 2 pages. Le PRD alimente déjà l'architecture (§7,
proposition de nom de paquet « à l'architecture ») : ce lien fonctionne car
les FR sont suffisamment précis pour être repris tels quels dans des tickets
de dev.

## Adéquation de forme (shape fit) — solide

Le PRD est correctement dimensionné pour un « hobby / solo » à utilisateur
unique réel : deux UJ avec protagoniste nommé (Victor), pas de sur-personas,
pas de sur-processus (pas de section stakeholders, pas de RACI). C'est le bon
niveau de formalisme, ni sous- ni sur-formalisé.

## Notes mécaniques

- **Coquille de nom de produit** : §1 ligne « BugKet occupe le vide entre les
  deux » — partout ailleurs le produit est nommé « BuKet ». À corriger avant
  diffusion (cosmétique mais visible dès la première section).
- Pas de dérive de glossaire détectée au-delà de ce point.
- Pas de tag `[ASSUMPTION]` ni `[NOTE FOR PM]` dans le document ; compte tenu
  du calibrage solo/hobby, leur absence n'est pas un manque en soi, mais les
  deux zones grises identifiées ci-dessus (§6 brique 2 vs V2, pays des
  tickets) auraient été de bons candidats à ce marquage explicite s'il avait
  été utilisé.
- Aucune référence croisée cassée détectée ; chaque section reste
  compréhensible isolément.
