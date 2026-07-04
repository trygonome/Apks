---
title: "PRD — BugKet, brique 1 : scanner et stocker ses tickets, en local"
status: draft
created: 2026-07-04
updated: 2026-07-04
---

# PRD — BugKet · Brique 1

## 1. Contexte et objectif

**BugKet** (budget + ticket) transforme une pile de tickets de caisse en
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

*(Sections suivantes en cours de coaching : parcours, exigences, NFR,
critères d'acceptation, hors périmètre.)*
