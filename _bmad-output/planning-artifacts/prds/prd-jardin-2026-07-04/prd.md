---
title: "PRD — BuKet, brique 1 : scanner et stocker ses tickets, en local"
status: final
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
saisie manuelle (Smart Receipts) ; BuKet occupe le vide entre les deux —
l'extraction structurée locale (total, date, enseigne, articles, TVA).

- **Licence** : Apache-2.0 · **Distribution** : releases GitHub ·
  **Plateforme** : Android (minSdk 26) · **Langue V1** : français.
- **Stack** : 100 % libre (CameraX + OpenCV + PaddleOCR/ONNX) ; principe
  « réutiliser l'éprouvé, ne construire que le manquant ». Compromis
  assumé : le recadrage automatique sera d'abord un cran sous les solutions
  propriétaires — le repli manuel digne (FR-1.3) le couvre.
- **Utilisateur de référence** : Victor et sa pile réelle (~50 tickets,
  états mixtes). **Appareil de référence** des mesures : Samsung Galaxy
  S24+. Rien n'est « fini » sans preuve sur cette pile et cet appareil.

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
4. **Jamais d'échec silencieux** : quand l'app ne sait pas (zone illisible,
   devise inconnue, espace insuffisant), elle le dit — elle n'invente pas.

## 3. Exigences fonctionnelles

### F1 — Capture (le geste)
- **FR-1.1** L'app est prête à capturer en moins de 2 s après ouverture.
- **FR-1.2** Les captures s'enchaînent sans écran intermédiaire ; un bip de
  caisse très discret confirme chaque prise (désactivable — un des rares
  réglages).
- **FR-1.3** Une photo cadrée large est acceptée : détection du ticket et
  recadrage automatiques ; en repli, recadrage manuel en 3 gestes maximum.
- **FR-1.4** Aucune capture n'est perdue : la photo est persistée dans le
  stockage privé de l'app **avant** toute autre opération, puis déposée
  dans le dossier choisi. Si l'espace manque, la capture refuse avec un
  message clair — jamais d'échec silencieux.

### F2 — Traitement en file d'attente
- **FR-2.1** Redressement, rehaussement de contraste (délavés, plis légers)
  et OCR s'exécutent en arrière-plan ; la capture n'attend jamais l'analyse.
- **FR-2.2** La file survit à la fermeture de l'app (et au redémarrage du
  téléphone) et reprend où elle en était.

### F3 — Extraction structurée
- **FR-3.1** Champs extraits : total, date, enseigne, articles, TVA (taux
  belges 6/12/21 % et français 5,5/10/20 %) — chacun avec un niveau de
  confiance annoncé. Extraction optimisée pour le français (décision
  propriétaire : le néerlandais attendra une brique ultérieure).
- **FR-3.2** Le texte intégral consultable est toujours le texte
  **reconstruit** (rangées physiques lisibles), jamais la sortie par blocs.
- **FR-3.3** Une zone illisible, une langue non couverte ou une devise
  autre que l'euro produisent des champs vides à confiance basse — jamais
  une invention silencieuse.
- **FR-3.4** Un doublon probable (même total, même date, même enseigne)
  est signalé au moment où il apparaît ; le conserver ou le supprimer est
  un geste unique.

### F4 — Vérification & correction
- **FR-4.1** Corriger un champ prend moins de 10 s, sans quitter la fiche.
- **FR-4.2** Un ticket vérifié est marqué comme tel ; les douteux sont
  signalés sobrement, sans culpabilisation.

### F5 — Stockage souverain
- **FR-5.1** Chaque ticket = fichiers ordinaires (image + données
  structurées) dans un dossier choisi par l'utilisateur, lisibles sans
  l'app. Le dossier est synchronisable par tout outil tiers (Syncthing,
  câble…) — la synchronisation elle-même reste hors de l'app.
- **FR-5.2** Chaque fichier de données porte la version de son schéma ;
  l'app s'engage à toujours savoir lire les versions antérieures.
- **FR-5.3** Au premier lancement (ou après changement de téléphone),
  choisir un dossier contenant déjà des tickets BuKet reconstruit la liste
  intégralement — les fichiers sont la seule source de vérité.
- **FR-5.4** Si le dossier choisi devient inaccessible (permission
  révoquée, carte retirée), rien n'est perdu (FR-1.4) : l'app le signale
  et propose d'en choisir un autre, puis rattrape le dépôt.
- **FR-5.5** Suppression propre (image + données) depuis l'app.
- **FR-5.6** Aucune image ne transite par la galerie publique du téléphone
  (MediaStore) : stockage privé et dossier choisi, rien d'autre.

### F6 — L'accueil
- **FR-6.1** L'écran d'accueil est la liste des tickets triée par date de
  capture (la date du ticket est une donnée, pas la clé de tri), ligne par
  ligne, avec le compteur discret en haut à droite. Rien d'autre — pas de
  bilan de séance, pas de tableau de bord.

## 4. Exigences non fonctionnelles

- **N1 Performance** — *cibles mesurées sur l'appareil de référence
  (Samsung Galaxy S24+), validées ou révisées en transparence à la première
  bêta* : ouverture → prêt à capturer < 2 s ; capture → capture suivante
  < 1,5 s ; analyse d'un ticket < 10 s en arrière-plan.
- **N2 Vie privée** : zéro connexion réseau ; permissions limitées à la
  caméra et au dossier choisi ; aucune trace hors stockage privé et dossier
  choisi (FR-5.6).
- **N3 Hors ligne & sans Google** : fonctionne intégralement sur un
  téléphone dégooglisé (stack 100 % libre).
- **N4 Taille** : APK < 60 Mo modèles OCR inclus (cible < 40).
- **N5 i18n prête dès le premier écran** (strings.xml) même si la V1 est
  francophone — dette du prototype à ne pas reproduire.
- **N6 Accessibilité de base** : TalkBack et contrastes corrects sur les
  écrans principaux.

## 5. Critères d'acceptation de la brique

1. **La pile est vaincue** : les ~50 tickets réels de Victor capturés en
   ≤ 20 minutes de manipulation.
2. **Fiabilité mesurée, protocole écrit** : *avant* la séance, Victor trie
   sa pile en « bon état » / « dégradé » (tri documenté) ; sur les tickets
   « bon état », ≥ 90 % ont total **et** date corrects sans correction ;
   les « dégradés » sont soit lisibles après rehaussement, soit honnêtement
   signalés douteux. n = la pile réelle.
3. **Le réflexe voiture tient** : ouverture → capture confirmée en < 5 s,
   mesuré sur l'appareil de référence.
4. **Souveraineté prouvée** : les fichiers s'ouvrent et se lisent sur un PC
   sans l'app, et un dossier recopié sur un second appareil reconstruit la
   liste (FR-5.3).
5. **Le juge final** : après 2 semaines d'usage réel, Victor n'a pas envie
   de revenir au papier.

## 6. Hors périmètre (brique 1)

- **Reporté aux briques suivantes** : vues budget et catégorisation
  d'affichage (brique 2 pressentie) ; jardin, Accapareurs et Carnet de
  résistance (brique jeu) ; export comptable.
- **Exclusions assumées de la V1** : tickets en néerlandais ou allemand
  (sortiront en confiance basse — décision propriétaire) ; devises autres
  que l'euro (idem) ; tickets trop longs nécessitant plusieurs photos ;
  soumission F-Droid (la stack la rend possible, elle n'est pas engagée) ;
  autres langues d'interface que le français (préparées par N5, non
  livrées) ; iOS.

## 7. Questions ouvertes

- Nom de paquet Android définitif (proposition à l'architecture :
  `org.buket.app`).
- Choix du son exact du bip — tranché à la première bêta.
- Tickets longs multi-photos — besoin réel à confirmer sur la pile, tranché
  après la bêta.
