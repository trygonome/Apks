---
title: "PRD — BuKet, brique 1 : scanner et stocker ses tickets, en local"
status: draft
created: 2026-07-04
updated: 2026-07-04
---

# PRD — BuKet · Brique 1

## 1. Contexte et objectif

**BuKet** (budget + ticket) transforme une pile de tickets de caisse en
données locales exploitables — sans cloud, sans compte, sans traqueur.
La brique 1 vise une seule chose, exécutée mieux que quiconque en open
source Android : **scanner vite et stocker proprement**. Le paysage libre
s'arrête aujourd'hui au PDF cherchable (scanners généralistes) ou à la
saisie manuelle (Smart Receipts) ; BugKet occupe le vide entre les deux —
l'extraction structurée locale (total, date, enseigne, articles, TVA).

- **Licence** : Apache-2.0 · **Distribution** : releases GitHub ·
  **Plateforme** : Android (minSdk 26) · **Langue V1** : français.
- **Stack** : 100 % libre (CameraX + OpenCV + PaddleOCR/ONNX) ; principe
  « réutiliser l'éprouvé, ne construire que le manquant ».
- **Utilisateur de référence** : Victor et sa pile réelle (~50 tickets,
  états mixtes). Rien n'est « fini » sans preuve sur cette pile.

## 2. Parcours utilisateur (racontés par Victor, structurés ensemble)

### UJ-1 — La séance « pile » (dimanche après-midi)

Victor s'installe, la pile (~50 tickets, dont des délavés et des froissés)
devant lui. Il positionne le premier ticket, prend une photo **assez large
pour tout couvrir** ; un **bip de caisse très discret** confirme la capture ;
il enchaîne le suivant sans écran intermédiaire, pendant que l'analyse
travaille en file d'attente derrière. **Pour les premiers tickets, il ouvre
le résultat et vérifie la transcription** — il n'y croit pas encore : la
confiance se gagne ticket après ticket, et l'app est conçue pour ça
(vérification facile, correction digne). Ce qui l'énerverait : que de légers
plis rendent la lecture impossible. Le lendemain, il ouvre l'app et tombe
directement sur **ses tickets listés simplement, ligne par ligne, avec le
nombre de tickets en petit en haut à droite**. Rien d'autre.

### UJ-2 — Le réflexe voiture (au quotidien)

Course finie, Victor remonte en voiture, ticket en main. Il ouvre BuKet :
**l'app est prête à capturer en un instant**, un geste, le bip, c'est rangé.
Le critère de réussite est comportemental : *si ça devient un réflexe quasi
moteur, c'est gagné.* Toute lenteur au démarrage ou à la capture tue le
réflexe — c'est la contrainte de performance n° 1 de la brique.

### Principes de conception issus des parcours

1. **La confiance se gagne** : la vérification des premiers tickets est un
   moment de vérité — le résultat montré est toujours le texte *reconstruit*
   (lignes physiques lisibles), jamais la bouillie par blocs de l'OCR.
2. **Le bip suffit** : pas d'écran de confirmation entre deux captures.
3. **La liste nue** : phase 1 = liste chronologique + compteur discret.
   Aucune vue budgétaire — l'outil d'abord, les données ensuite.

*(Sections suivantes en cours de coaching : exigences fonctionnelles, NFR,
critères d'acceptation, hors périmètre.)*
