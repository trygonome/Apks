# Roadmap — Scan Tickets v5.0 « Progression » 🎮

*Rédigée le 3 juillet 2026, sur la base de la v4.0.*

Transformer le suivi de budget en jeu de progression — objectifs, quêtes,
niveaux, ressources par catégorie — **sans jamais récompenser la dépense**.

## ⚖️ La règle d'or

Une app de budget qui donne des points par euro dépensé pousserait à acheter
pour « farmer » : l'anti-objectif absolu. Les achats par catégorie génèrent
bien les ressources (jetons 🛒 🍔 ⛽ 💊…), mais les gains sont **plafonnés par
ticket et par jour**, et les multiplicateurs viennent du *geste de scanner*,
de la *qualité des données* (tickets vérifiés) et du *respect du budget*.
On farme en trackant, pas en dépensant.

## Économie du jeu

| Action | Récompense | Garde-fou |
|---|---|---|
| Scanner un ticket | +XP, +1 jeton de la catégorie | max 5 tickets/jour comptés |
| Vérifier un ticket douteux | +XP bonus | 1 bonus par ticket |
| Série de jours avec scan 🔥 | multiplicateur d'XP (×1,1 → ×2) | 1 scan/jour suffit |
| Mois fini sous l'objectif | gros bonus + jeton d'or 🏅 | objectif requis |
| Nouvelle enseigne scannée | carte de collection | illimité |

Les jetons se dépensent dans une **boutique 100 % cosmétique** (couleurs
d'accent exclusives, titres). Aucune fonction utile n'est verrouillée par le
jeu.

## Quêtes (3 par semaine, générées localement)

Exemples : scanner 5 tickets · vérifier 3 tickets douteux · rester sous 30 €
en Restaurant · tenir une série de 4 jours · découvrir 2 enseignes.

Succès permanents : premier ticket, 100 tickets, 10 enseignes, 50 TVA
détectées, 3 mois sous budget, les 11 catégories utilisées…

## Architecture

- **L'état du jeu se recalcule depuis les fichiers de scans** (source de
  vérité) : rien à migrer, rien à corrompre ;
- Moteur en pur Kotlin testé (`MoteurProgression`) — chaque règle = un test ;
- **Activable** dans les Réglages : éteint, l'app redevient la v4.0 ;
- 100 % local : pas de classement en ligne, pas de compte.

## Jalons (~1 session chacun)

1. **Moteur de progression** — XP, niveaux, jetons, séries, recalcul depuis
   l'historique. *Fini quand chaque règle a son test, garde-fous compris.*
2. **Quêtes, succès, collection** — génération hebdo déterministe. *Fini
   quand les quêtes se génèrent/valident/renouvellent en tests.*
3. **Onglet Progression** — barre d'XP, quêtes, succès, collection,
   célébrations sobres. *Fini quand scan → récompense est visible de bout en
   bout et que l'interrupteur éteint tout.*
4. **Boutique cosmétique + release v5.0** — équilibrage, polish, APK signé.
   *Fini quand un mois de jeu simulé ne casse rien.*

## Hors périmètre (assumé)

Classements en ligne (contraire au 100 % local), récompenses fonctionnelles,
notifications de rappel (décision au jalon 3).

---

Version détaillée et illustrée : voir le document de roadmap partagé en
session.
