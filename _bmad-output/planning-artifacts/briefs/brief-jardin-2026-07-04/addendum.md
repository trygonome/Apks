# Addendum au brief — matière pour le PRD

*Contenus volontairement hors du brief (1-2 pages max) mais à ne pas perdre.*

## Idées candidates pour le PRD

- **Compteur « argent repris »** : l'app mesure et affiche ce qu'elle t'a fait
  économiser par rapport à une période de référence (le premier mois d'usage,
  par exemple). Vient de la boutade du propriétaire : « payez-la 1 €, gagnez
  500 € grâce à elle ». La boutade contient la vraie North Star : de l'argent
  économisé, mesurable, attribuable à l'usage.
- **Mode « la pile »** : traitement en lot du stock de tickets accumulés
  (scan en rafale, tri différé). C'est le besoin fondateur du projet — le
  prototype ne savait scanner qu'un ticket à la fois.
- Le jardin comme **pédagogie de l'outil** : chaque mécanisme du jeu doit
  enseigner quelque chose de la gestion réelle (sinon il n'a pas sa place).

## Décisions d'orientation prises pendant la découverte

- Cœur = l'outil (scanner, trier, comprendre) ; jardin = couche didactique.
- Projet de conviction : open source, sans but lucratif, distribution
  F-Droid à évaluer au PRD ; l'angle commercial B2B/TVA de l'audit v3.1
  n'est plus un objectif.
- « Tout désactivable » (loi héritée du prototype) est révisé : produit
  opinioné, bons défauts, réglages minimes.
- Nom de code temporaire : **Budgeskets** — naming définitif au PRD.

## Contraintes techniques pour l'architecture (V1)

- **Thermiques pâlis** : prévoir un pré-traitement de rehaussement de
  contraste avant OCR, et mesurer le taux de réussite sur la pile réelle ;
  le repli en saisie manuelle doit être rapide et sans culpabilisation.
- **Mode rafale** : ~50 tickets à la suite → la caméra doit enchaîner sans
  retour à l'écran d'accueil ; l'analyse peut être différée (file de
  traitement) pour ne pas bloquer la capture.
- Dimensionnement : une pile de 50 tickets = une session d'environ 15-20
  minutes de capture ; c'est le scénario de stress de la V1.

## Contexte hérité (voir docs/HERITAGE.md pour le détail)

Moteurs Kotlin testés réutilisables (OCR géométrique, analyseur à scores,
catégoriseur, statistiques, progression), 50 tests dont un ticket réel,
lois conservées : 100 % local, jamais de FOMO, jamais récompenser la dépense,
la correction humaine prime.
