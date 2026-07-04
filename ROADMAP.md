# Roadmap — Scan Tickets v5 « Le Jardin » 🌱

*Mise à jour : 4 juillet 2026. Base : v4.0.*

Un **jeu de résistance budgétaire** et un **outil d'éducation à la gestion de
l'argent** : ton jardin, c'est ta souveraineté — ton argent et tes données,
cultivés chez toi. Il pousse quand tu es actif ; les Accapareurs avancent
quand tu restes passif. Comme dans la vraie vie.

L'app incarne son propre message : 100 % locale, zéro pub, zéro traqueur,
zéro compte.

## ⚖️ La règle d'or

**On ne récompense jamais le fait de dépenser plus.** Les achats génèrent les
graines (plafonnées/jour), mais le jardin ne prospère que par le geste de
scanner, la qualité des données et le respect du budget. On cultive en
trackant, pas en consommant.

## Les Accapareurs (bestiaire pédagogique)

Des *mécanismes*, pas des marques. Chaque victoire débloque une fiche du
**Carnet de résistance** (le mécanisme réel + un réflexe concret) :

| Monstre | Se nourrit de | Repoussé par | Fiche pédagogique |
|---|---|---|---|
| 🕷️ Le Traqueur | tes données | scanner (lucidité) | pub ciblée & pistage |
| 🧜 La Sirène des Promos | l'achat impulsif | finir sous budget | dark patterns & fausse urgence |
| 🐙 L'Abonnite | les abonnements oubliés | vérifier ses tickets | coût cumulé des abonnements |
| ☁️ Le Grand Nuage | la dépendance aux silos | tenir sa série 🔥 | enclosure numérique |

La passivité réelle a un coût visible (ombres, mauvaises herbes) — jamais
punitif : un scan et le jardin respire.

## Économie du jardin

| Action réelle | Effet | Garde-fou |
|---|---|---|
| Scanner un ticket | +10 XP, +1 graine de la catégorie | max 5/jour comptés |
| Vérifier un ticket douteux | +5 XP bonus | 1 par ticket |
| Série de jours 🔥 | XP ×1,1 → ×2,0 | 1 scan/jour suffit |
| Mois fini sous l'objectif | graine d'or 🏅 + 100 XP | objectif requis |
| Nouvelle enseigne | spécimen d'herbier | illimité |

Les graines plantent des décors 100 % cosmétiques. Aucune fonction utile
verrouillée par le jeu.

## Jalons

1. ✅ **Moteur de progression** (`progression/MoteurProgression.kt`) — XP,
   niveaux et titres de jardinier, graines plafonnées, séries et
   multiplicateur, graines d'or, herbier. Recalculé depuis l'historique.
   13 tests.
2. ⬜ **Missions, Carnet de résistance, herbier** — 3 missions hebdo générées
   localement, ~20 fiches pédagogiques débloquées par les victoires.
3. ⬜ **La scène du Jardin (2D)** — Canvas Compose 60 fps, art vectoriel
   procédural : ciel selon l'état du mois, plantes, mascotte, pluie de pièces
   au scan. (1-2 sessions, itération de game feel avec l'utilisateur.)
4. ⬜ **Les Accapareurs à l'écran** — ombres selon la passivité, animations de
   recul, liaison monstres ↔ fiches.
5. ⬜ **Plantations, équilibrage, release v5.0** — catalogue de décors,
   équilibrage, APK signé.

## Hors périmètre (assumé)

Marques réelles nommées, classements en ligne, comptes, récompenses
fonctionnelles. Interrupteur « Le Jardin » dans les Réglages : éteint, l'app
redevient l'outil pur.
