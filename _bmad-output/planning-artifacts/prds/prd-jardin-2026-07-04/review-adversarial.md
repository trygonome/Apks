---
title: "Revue adversariale — PRD BuKet, brique 1"
status: draft
created: 2026-07-06
reviewer: "Revue cynique BMAD (adversarial-general)"
target: "_bmad-output/planning-artifacts/prds/prd-jardin-2026-07-04/prd.md"
---

# Revue adversariale — PRD « BuKet, brique 1 »

## Verdict global

Ce PRD est bien écrit, cohérent dans le ton et fidèle à l'esprit du brief —
mais il vend des chiffres de performance et un chiffre de fiabilité comme des
faits acquis alors que la recherche technique qui l'accompagne dit noir sur
blanc qu'**aucun test sur appareil n'a été fait**. La brique 1 change
intégralement le moteur OCR hérité (ML Kit → PaddleOCR/ONNX) sans reconnaître
que la réutilisation « quasi inchangée » des moteurs de reconstruction est une
hypothèse, pas un acquis. Plusieurs angles morts Android classiques (SAF
révoqué, Doze/kill de service, fuite via la galerie publique) menacent
directement les promesses les plus dures du produit (« aucune capture n'est
perdue », « zéro connexion réseau », « souveraineté »). Le projet reste
parfaitement raisonnable pour un solo de conviction — mais tel quel, ce PRD
serait démoli à la première bêta sur au moins trois de ses propres critères
d'acceptation.

## Synthèse des sévérités

| Sévérité | Nombre |
|---|---|
| Critique | 3 |
| Haute | 5 |
| Moyenne | 6 |
| Basse | 3 |

---

## Section 1 — Contexte et objectif

### C1.1 [Basse] Coquille sur le nom du produit
Le texte introduit « **BuKet** » puis écrit une phrase plus loin « **BugKet**
occupe le vide entre les deux » (ligne 17). Sur un document qui prétend viser
la « référence open source », une coquille sur le nom du produit dès le
paragraphe d'ouverture est un signal de relecture insuffisante.
**Correction** : corriger la coquille ; passer une relecture dédiée au nommage
avant publication (le nom n'est même pas encore définitif — cf. section 7 —
ce qui aggrave le risque de contamination du mauvais nom dans le code, les
noms de fichiers, les captures d'écran de bêta).

### C1.2 [Moyenne] « Référence open source » sans les conditions de la référence
Le PRD revendique l'ambition de devenir *la* référence open source Android
sur ce créneau (cohérent avec la recherche, section 2 : « une opportunité
réelle de devenir la référence »). Mais rien dans le PRD ne prévoit :
- un fichier `CONTRIBUTING.md` ou une politique de contribution ;
- une CI publique (build + tests automatiques à chaque push/PR) — alors que
  l'héritage (`HERITAGE.md`) mentionne 50 tests verts existants qui ne sont
  mentionnés nulle part comme exigence à maintenir ou étendre ;
  un build reproductible (exigence de fait pour l'inclusion F-Droid) ;
- une politique de licence/provenance des **poids du modèle** PaddleOCR
  embarqués (un modèle ONNX préentraîné est un binaire ; F-Droid et la
  communauté open source posent régulièrement la question de la provenance
  et de la reproductibilité de tels artefacts — voir C4.1 plus bas).
Pour un projet solo, il n'est pas question d'exiger la bureaucratie d'une
fondation — mais la brique 1 est explicitement le socle sur lequel repose la
prétention à la référence : si aucune de ces conditions minimales n'est ne
serait-ce que *planifiée* (pas nécessairement livrée en brique 1), le PRD
laisse un angle mort sur sa propre ambition centrale.
**Correction** : ajouter une exigence non fonctionnelle « N7 — préparation à
la contribution externe » qui liste a minima : CI GitHub Actions
(build + les 50 tests hérités portés), `CONTRIBUTING.md` minimal, note sur la
provenance/licence des poids PaddleOCR utilisés — même si le *contenu* est
réalisé en brique 2 ou avant la publication F-Droid, la brique 1 doit statuer
sur qui en est responsable et quand.

### C1.3 [Moyenne] Réutilisation des moteurs hérités présentée comme acquise
Le PRD (comme la recherche en amont) affirme que les moteurs hérités
(`ReconstructeurLignes`, `AnalyseurTicket`) seront repris « quasi inchangés »
par-dessus PaddleOCR. Or `HERITAGE.md` précise que `ReconstructeurLignes`
« répare les colonnes mélangées **de ML Kit** » — c'est-à-dire qu'il a été
conçu et testé contre le format de sortie (boîtes englobantes, découpage en
blocs) d'un moteur OCR précis, qui n'est plus celui utilisé en brique 1.
PaddleOCR via ONNX ne produit pas nécessairement les mêmes boîtes, le même
découpage en lignes/blocs, ni les mêmes artefacts de bruit. Le PRD ne
mentionne nulle part le risque que ces moteurs doivent être ré-adaptés (voire
partiellement réécrits) avant de tenir leurs promesses de qualité
(FR-3.1, FR-3.2, critère d'acceptation 2).
**Correction** : ajouter une note de risque explicite en section 1 ou 4
reconnaissant que la portabilité des moteurs hérités vers la sortie
PaddleOCR/ONNX est *à valider*, pas acquise ; prévoir un jalon de bêta interne
tôt dédié uniquement à vérifier que `ReconstructeurLignes`/`AnalyseurTicket`
fonctionnent sur la nouvelle sortie OCR avant d'investir dans le reste du
pipeline de capture.

---

## Section 3 — Exigences fonctionnelles

### C3.1 [Critique] Fuite de la promesse « zéro connexion réseau / souveraineté » via la galerie publique
FR-1.4 exige que « la photo est persistée avant toute analyse » et N2 promet
« permissions limitées à la caméra et au dossier choisi ». Rien dans le PRD
n'interdit explicitement que la capture CameraX écrive (comme c'est le
comportement par défaut de nombreuses implémentations) une copie dans le
stockage public/MediaStore (dossier `Pictures` ou galerie). Si une copie
publique existe, même temporairement, elle est indexée par la galerie
système et peut être aspirée par n'importe quelle appli tierce ayant la
permission lecture-médias (y compris, ironie totale pour un produit
anticapitaliste, un service de sauvegarde photo grand public). C'est une
violation concrète et silencieuse de la promesse de souveraineté/vie privée
qui est l'identité même du produit (Loi n°1 du brief : « 100 % local — c'est
l'identité »).
**Correction** : ajouter une exigence explicite (FR-1.4 bis ou N2 bis) : « la
photo brute n'est jamais écrite dans un emplacement visible par MediaStore ou
la galerie système ; elle est écrite uniquement dans l'espace applicatif
privé puis, une fois validée, dans le dossier SAF choisi par l'utilisateur ».
Ajouter un test d'acceptation dédié : vérifier via une appli de galerie tierce
qu'aucune image de ticket n'apparaît jamais en dehors du dossier choisi.

### C3.2 [Critique] SAF : la permission de dossier peut être révoquée sans que rien ne le détecte
FR-5.1 fait reposer *tout* le stockage sur un dossier choisi par l'utilisateur
via SAF (Storage Access Framework, cf. `HERITAGE.md`). C'est un choix
technique connu pour ses pièges : les permissions persistées peuvent être
invalidées par un changement de volume (carte SD retirée/remontée), par
certaines mises à jour d'OS constructeur, par un « effacer le stockage » de
l'appli, ou lors du remplacement du téléphone (cf. angle mort explicitement
demandé). Le PRD ne prévoit :
- aucune détection de permission perdue ;
- aucun message d'erreur ni reprompt utilisateur en cas d'échec d'écriture ;
- aucun comportement de repli si le dossier devient inaccessible en pleine
  séance « pile » (le pire moment possible).
Cela contredit directement FR-1.4 (« aucune capture n'est perdue ») : une
capture qui échoue silencieusement à s'écrire *est* une capture perdue, et le
PRD n'a aucune exigence pour l'empêcher ou au moins l'exposer à l'utilisateur.
**Correction** : ajouter FR-5.3 « Toute perte d'accès au dossier choisi (SAF
invalidé, volume absent, écriture échouée) est détectée avant capture ou au
moment de l'écriture, et bloque proprement la séance avec un message clair
plutôt qu'une perte silencieuse » ; ajouter un test d'acceptation : révoquer
manuellement la permission du dossier en cours de séance et vérifier qu'aucun
ticket n'est perdu.

### C3.3 [Haute] Le service en arrière-plan peut être tué par le gestionnaire d'énergie du téléphone (Doze / OEM battery killers)
FR-2.1/FR-2.2 promettent que le traitement (redressement, OCR) tourne en
arrière-plan et que « la file survit à la fermeture de l'app ». Sur Android
réel — et particulièrement sur les « téléphones milieu de gamme » visés par
N1, souvent des Xiaomi/Samsung/Oppo notoirement agressifs sur le kill de
process en arrière-plan — un traitement CPU-intensif (OCR + OpenCV) qui n'est
pas explicitement un `ForegroundService` avec notification persistante risque
d'être suspendu ou tué dès que l'utilisateur quitte l'app ou reçoit un appel
pendant la séance « pile ». Le PRD ne mentionne ni foreground service, ni
wake lock, ni stratégie de reprise après kill — seulement que la file
« reprend où elle en était », ce qui suppose que l'état est bien persisté
mais ne garantit pas que le traitement progresse réellement pendant que
l'app est en arrière-plan.
**Correction** : préciser dans F2 le mécanisme (ex. `ForegroundService` avec
notification discrète tant que la file n'est pas vide) et ajouter un critère
d'acceptation : « la file continue de progresser (ou reprend intégralement)
même si l'app est mise en arrière-plan ou si le téléphone reçoit un appel
pendant le traitement ».

### C3.4 [Moyenne] Aucun fallback de confirmation si le bip est désactivé
FR-1.2 rend le bip désactivable (« un des rares réglages ») mais le principe
de conception n°2 interdit tout écran de confirmation entre deux captures.
Si l'utilisateur désactive le bip (par exemple dans un lieu public, ou pour
un usage accessible — cf. N6), *aucune* modalité de confirmation de capture
ne subsiste : pas de son, pas d'écran, et rien n'indique une alternative
haptique. Un utilisateur malvoyant/malentendant utilisant TalkBack (N6) n'a
par ailleurs aucune garantie qu'un « bip » brut (souvent généré hors du
pipeline d'accessibilité, via `ToneGenerator` par exemple) soit perçu ou
annoncé par les services d'accessibilité.
**Correction** : ajouter à FR-1.2 une confirmation haptique (vibration
courte) comme modalité par défaut ou de repli, indépendante du son, et
vérifier explicitement la perception de la confirmation de capture via
TalkBack dans les critères N6.

### C3.5 [Moyenne] Bip « très discret » : un seul niveau pour deux contextes opposés
UJ-1 (dimanche, salon) justifie un bip « très discret ». UJ-2 (réflexe
voiture, sortie de supermarché) se déroule dans un environnement bruyant
(moteur, parking, autres personnes) où un bip « très discret » risque de ne
pas être perçu du tout, cassant justement le réflexe que UJ-2 cherche à
garantir (« toute lenteur... tue le réflexe » — un doute sur la capture
provoque une vérification qui casse le geste tout autant qu'une lenteur).
Le PRD ne résout pas cette tension entre les deux parcours qu'il a lui-même
posés comme fondateurs.
**Correction** : soit ajuster le volume du bip au contexte (détection basique
du niveau sonore ambiant, ou réglage rapide accessible), soit assumer
explicitement un compromis et le justifier ; a minima, documenter que le
retour haptique (cf. C3.4) sert aussi de garde-fou dans les environnements
bruyants.

---

## Section 4 — Exigences non fonctionnelles

### C4.1 [Critique] Chiffres de performance présentés comme acquis alors que la recherche dit l'inverse
N1 fixe trois chiffres durs : ouverture→prêt <2 s, capture→capture <1,5 s,
analyse <10 s sur « téléphone milieu de gamme ». Or le document de recherche
qui sert d'intrant direct à ce PRD est explicite : *« Pas de test sur
appareil ici. Le pipeline caméra (rafale, autofocus, cadence) s'itérera par
cycles de bêta »* (`brique1-etat-de-lart.md`, section 3.2). Le PRD ne reprend
nulle part cette réserve : les chiffres apparaissent en section 4 avec la
même autorité que des faits mesurés, et resurgissent en section 5 comme
**critères d'acceptation** de la brique (« ouverture → capture confirmée en
< 5 s »). Un critère d'acceptation formulé à partir d'une hypothèse non
testée n'est pas un critère, c'est un vœu — et le premier qui en fera les
frais est le calendrier de la bêta 1, quand ces chiffres exploseront sur le
premier « téléphone milieu de gamme » réellement disponible.
Deux causes concrètes, documentées ailleurs dans le PRD lui-même, rendent le
doute légitime :
- « Téléphone milieu de gamme » n'est défini par aucun modèle/chipset/RAM de
  référence : l'exigence est donc invérifiable telle quelle (personne ne
  peut dire objectivement si un test « passe » ou « échoue »).
- Le chargement d'un runtime ONNX + modèles PaddleOCR + OpenCV au démarrage
  d'une app, sur un SoC d'entrée/milieu de gamme, a un coût d'initialisation
  qui n'est cité nulle part (contrairement à la taille APK, chiffrée en
  section 3 de la recherche) — le PRD ne distingue même pas si « prêt à
  capturer » exige un préchargement du modèle OCR ou seulement l'aperçu
  caméra (ce qui change tout le calcul de faisabilité).
**Correction** : (1) reformuler N1 en objectifs de bêta à valider plutôt
qu'en critères d'acceptation fermes tant qu'aucune mesure sur appareil réel
n'existe ; (2) nommer au moins un ou deux appareils de référence concrets
pour « milieu de gamme » (marque/chipset/RAM/année) ; (3) préciser
explicitement que « prêt à capturer » ne requiert que l'aperçu caméra, le
modèle OCR étant chargé de façon paresseuse/asynchrone — sinon assumer le
risque et prévoir un plan B (ex. écran de démarrage minimal pendant le
chargement).

### C4.2 [Haute] Contention CPU non anticipée entre capture et traitement en arrière-plan
FR-2.1 fait tourner OpenCV (redressement, CLAHE) et l'inférence PaddleOCR/ONNX
« en arrière-plan » pendant que l'utilisateur continue de capturer à moins de
1,5 s d'intervalle (N1). Sur un SoC milieu de gamme à peu de cœurs
performants, le pipeline caméra (aperçu, autofocus, capture) et l'inférence
OCR sont tous deux gourmands en CPU/mémoire et se disputeront les mêmes
ressources ; un traitement soutenu de dizaines de tickets d'affilée (comme le
scénario UJ-1, 50 tickets) peut aussi provoquer un throttling thermique qui
dégrade les deux chiffres de N1 simultanément, précisément pendant le
scénario que le critère d'acceptation 1 (pile vaincue en ≤ 20 min) est censé
valider. Rien dans le PRD ne prévoit de tester ce cas de charge soutenue, ni
de prioriser le thread de capture sur le thread d'analyse.
**Correction** : ajouter un critère de charge soutenue explicite (ex. « la
cadence de capture ne se dégrade pas de plus de X % après 30 captures
consécutives ») et exiger une priorité claire du pipeline de capture sur la
file d'analyse dans l'architecture.

### C4.3 [Moyenne] Taille APK (< 60 Mo / cible 40 Mo) : le calcul omet OpenCV et le runtime ONNX
N4 chiffre la cible en tenant compte des « modèles OCR inclus », en écho à la
recherche qui ne chiffre que les poids PaddleOCR mobile (~10-20 Mo). Mais la
distribution prévue est « releases GitHub » — pas de Play Store avec App
Bundle, donc probablement un APK universel embarquant les bibliothèques
natives OpenCV (`.so`) pour plusieurs ABI (`armeabi-v7a`, `arm64-v8a`,
potentiellement `x86_64`) plus le runtime ONNX natif, tous deux notoirement
lourds en version précompilée complète. Le budget de 60 Mo (cible 40 Mo) n'a
donc pas de garantie réaliste tant que ces deux composants ne sont pas
chiffrés, et le prototype de référence cité (ML Kit, 45 Mo) n'est pas
comparable car il n'embarquait pas OpenCV natif dans l'app elle-même.
**Correction** : chiffrer précisément OpenCV (version minimale nécessaire,
ABI filtering) + runtime ONNX avant de figer N4 ; envisager des APK
« splits » par ABI dès la brique 1 si le budget est dépassé, plutôt que de le
découvrir après coup.

### C4.4 [Basse] N6 (accessibilité) n'a pas de définition de « fait »
« TalkBack et contrastes corrects sur les écrans principaux » ne définit ni
le niveau visé (WCAG AA ?), ni la liste des « écrans principaux », ni de test
d'acceptation vérifiable. Combiné à C3.4 (pas de confirmation non-sonore de
capture), l'accessibilité reste une intention plutôt qu'une exigence
opérationnelle.
**Correction** : lister explicitement les écrans couverts (liste, fiche
ticket, correction de champ) et un niveau de contraste cible (WCAG AA,
ratio 4.5:1) vérifiable par un contrôle manuel simple avant la fin de la
brique 1.

---

## Section 5 — Critères d'acceptation de la brique

### C5.1 [Critique] Le critère de fiabilité (≥ 90 %) n'est pas mesurable tel quel
« ≥ 90 % des tickets en bon état... corrects sans correction » pose trois
problèmes cumulés qui, ensemble, rendent le critère quasi incontestable
(dans le mauvais sens du terme — impossible à faire échouer objectivement) :
1. **« Bon état » n'est pas défini avant le test.** Rien n'indique si le tri
   bon-état / délavé / froissé se fait *avant* de voir le résultat de l'OCR
   (protocole aveugle) ou après. Sans tri pré-enregistré, rien n'empêche —
   même inconsciemment — de reclasser a posteriori un ticket raté comme
   « pas vraiment en bon état », ce qui rend le seuil de 90 % increvable par
   construction.
2. **Le juge est l'auteur du produit.** Victor évalue son propre travail sur
   sa propre pile, sans double contrôle, sans protocole écrit, sans horaire
   fixé (« mesurée sur cette pile » — quand ? une fois ? après combien
   d'itérations de correction du modèle avant de « repasser » le test ?).
   Acceptable pour un projet solo qui n'a personne d'autre à qui déléguer le
   jugement — mais alors le PRD devrait le dire explicitement plutôt que de
   présenter le chiffre comme une mesure neutre.
3. **n non précisé et probablement trop petit pour un chiffre à 90 %.** Sur
   ~50 tickets, si seulement une fraction est « en bon état » (le reste étant
   délavé/froissé et donc évalué par un critère différent, plus mou :
   « lisible... ou honnêtement signalé douteux »), l'échantillon réel testé
   pourrait être de l'ordre de 20-30 tickets. À ce niveau, 90 % correspond à
   tolérer 2-3 échecs sur un tirage unique et non représentatif (une seule
   pile, un seul utilisateur, une seule zone géographique/linguistique) : ce
   n'est pas un taux de fiabilité généralisable, c'est une anecdote chiffrée.
   Cela n'invalide pas son usage comme jalon personnel de Victor, mais le
   PRD ne peut pas s'en servir plus tard comme preuve de qualité pour un
   public plus large sans le répéter sur un corpus indépendant.
**Correction** : (1) figer la classification bon-état/délavé/froissé *avant*
de lancer le test (photo + étiquette écrites à l'avance) ; (2) préciser le
nombre exact de tickets dans chaque catégorie et donc le n réel du calcul de
90 % ; (3) documenter explicitement que ce test est un jalon personnel à
validité anecdotique, et prévoir — même hors brique 1 — un second corpus
(quelques dizaines de tickets d'un tiers volontaire) avant toute
communication publique sur la fiabilité du produit.

### C5.2 [Haute] Critère 1 (« pile vaincue en ≤ 20 min ») : seuil sans référence ni marge
Le chiffre de 20 minutes pour ~50 tickets (soit 24 s/ticket en moyenne, temps
de manipulation *et* de capture confondus) n'a aucune baseline citée (combien
de temps prend la saisie manuelle actuelle de Victor ? combien de temps a
duré un essai informel, s'il y en a eu un ?). C'est un chiffre rond qui sonne
bien mais qui n'est adossé à aucune mesure préalable, alors que d'autres
sections du PRD (N1) fixent des chiffres à la demi-seconde près. L'écart de
rigueur entre les deux est révélateur : le PRD est précis là où c'est facile
à écrire, flou là où la difficulté (charge réelle, tickets froissés
nécessitant plusieurs tentatives, repli en recadrage manuel de FR-1.3) se
cache.
**Correction** : documenter, même sommairement, l'estimation qui a mené à 20
minutes (un essai chronométré avec le prototype existant suffirait), ou
reformuler le critère avec une marge explicite (« entre 20 et 30 min » par
exemple) plutôt qu'un couperet unique.

### C5.3 [Haute] Critère 4 (« souveraineté prouvée ») ne teste pas le cas qui menace réellement la souveraineté : le changement de téléphone
Le critère se limite à « les fichiers s'ouvrent et se lisent sur un PC, sans
l'app » — ce qui prouve le format, pas la portabilité en usage réel. L'angle
mort explicitement identifié dans le brief de cette revue — *changement de
téléphone* — n'est testé nulle part : rien ne vérifie que copier le dossier
choisi vers un nouveau téléphone puis rouvrir l'app fait réapparaître
l'historique. C'est pourtant le scénario où la promesse de souveraineté est
mise à l'épreuve pour de vrai (un utilisateur qui change de téléphone tous
les 3-4 ans, ce qui arrivera nécessairement sur la durée de vie du produit).
Le brief mentionnait par ailleurs des fichiers « synchronisables », et
`HERITAGE.md` documente une intention de synchronisation via Syncthing — ce
mot n'apparaît nulle part dans le PRD, ni dans les exigences, ni dans le
hors-périmètre (voir C7.1 ci-dessous).
**Correction** : ajouter au critère 4 un test explicite de portabilité inter-
appareil (copier le dossier vers un second téléphone, rouvrir l'app,
vérifier que la liste et les images réapparaissent à l'identique) ; a minima
documenter la procédure manuelle attendue puisqu'aucun outil de migration
n'est prévu en brique 1.

### C5.4 [Basse] Critère 5 : jugement subjectif sans protocole, mais assumé comme tel
« Après 2 semaines d'usage réel, Victor n'a pas envie de revenir au papier »
est un critère honnête pour un projet de conviction solo — il n'est pas
reproché en soi. Mais aucun mécanisme n'est prévu pour ce qui se passe si la
réponse est non (la brique est-elle rouverte ? sur quels signaux précis
Victor doit-il s'appuyer pour trancher — nombre de tickets papier oubliés,
irritants relevés ?). Sans grille même minimale, le jugement risque d'être
un ressenti global difficile à transformer en actions correctives ciblées.
**Correction** : ajouter une liste courte de 3-4 signaux concrets à noter
pendant les 2 semaines (ex. : nombre de fois où le geste a été sauté par
flemme, nombre de corrections manuelles nécessaires, nombre de tickets
papier encore traînant) pour que le jugement final soit motivé, pas
seulement une impression.

---

## Section 6 — Hors périmètre & cohérence avec le brief

### C6.1 [Haute] Le brief promet « exportables, synchronisables » ; le PRD abandonne silencieusement l'export et la synchronisation
Le brief (« Souveraineté des données ») liste explicitement : *« fichiers
ordinaires dans un dossier choisi par l'utilisateur, exportables,
synchronisables »*. Le PRD (FR-5.1) ne reprend que la première moitié :
« fichiers ordinaires... lisibles sans l'app, pour toujours » — l'export et
la synchronisation ont disparu, et contrairement aux autres renoncements du
PRD (budget, jardin, F-Droid, autres langues, iOS — tous listés
explicitement en section 6 « Hors périmètre »), cette perte n'est **pas**
déclarée comme un renoncement volontaire. Un lecteur du PRD seul (sans
relire le brief) ne peut pas savoir que cette promesse existait et a été
tacitement abandonnée plutôt que traitée. C'est exactement le genre d'écart
brief→PRD que cette revue doit détecter.
**Correction** : soit ajouter « export / synchronisation » à la liste du
hors-périmètre (section 6) avec une justification explicite (par exemple :
« le format fichier ouvert suffit à rendre l'export/la synchro possibles via
des outils tiers — Syncthing, exploration de fichiers — sans fonctionnalité
dédiée en brique 1 »), soit réintégrer une exigence minimale si l'intention
est de la traiter en brique 1.

### C6.2 [Moyenne] Le « Comprendre » du brief (V1) glisse silencieusement derrière la brique 1, sans feuille de route visible
Le brief scope son « V1 — l'outil pur » avec un volet explicite
« Comprendre : tri automatique par catégories, vue mensuelle, objectif de
budget, argent repris » et délègue au PRD le « découpage détaillé des
fonctionnalités V1 ». Le PRD ne fait pas ce découpage explicitement : il
définit une « brique 1 » qui exclut entièrement les « vues budget et
catégorisation d'affichage », en les regroupant dans le hors-périmètre aux
côtés du « jardin et Carnet (briques ultérieures) » — alors que le brief
distinguait clairement l'outil (V1, dont *fait partie* le tri/budget) de la
couche pédagogique/ludique (V2, le jardin). En fusionnant ces deux
renoncements dans une seule liste « hors périmètre », le PRD laisse penser
que le tri par catégorie et les vues budget sont reportés aussi loin que le
jardin, ce que le brief ne prévoyait pas. Or `HERITAGE.md` montre que
`Categoriseur` et `StatistiquesBudget` existent déjà et sont testés — leur
report n'est donc pas un problème de faisabilité technique, mais une
décision de séquençage qui mériterait d'être justifiée et rattachée à une
« brique 2 » nommément prévue, plutôt que noyée dans le même hors-périmètre
que le jardin.
**Correction** : scinder la liste de la section 6 en deux : (a) hors-
périmètre du produit *V1* dans son ensemble (jardin/Carnet, iOS, autres
langues — cohérent avec le brief), et (b) reporté à une « brique 2 » déjà
identifiée du même V1 (budget/catégorisation), avec une phrase reliant
explicitement cette brique 2 au reste du « Comprendre » promis par le brief,
pour que le lecteur comprenne que rien n'est perdu, seulement séquencé.

---

## Section 7 — Questions ouvertes

### C7.1 [Basse] Le son du bip est repoussé à « la première bêta », mais c'est un élément central de deux des trois parcours utilisateurs
Reporter la validation du son exact du bip à la bêta est raisonnable en soi.
Mais vu le poids que le PRD lui-même donne au bip dans ses deux parcours
fondateurs (UJ-1 : « un bip de caisse très discret confirme la capture » ;
UJ-2 : « un geste, le bip, c'est rangé » — littéralement le résumé du
réflexe recherché), le laisser en question ouverte sans même un brief de
critères (durée, ton, contexte d'usage — cf. C3.5) revient à laisser flou un
élément que le PRD présente ailleurs comme central à l'expérience.
**Correction** : transformer la question ouverte en un mini-cahier des
charges (durée cible < X ms, fréquence, distinct d'une notification système)
à trancher en bêta, plutôt qu'une case vide.

---

## Récapitulatif actionnable (par priorité)

1. **Critique** — Vérifier/interdire toute écriture de la photo dans un
   emplacement visible par la galerie/MediaStore (C3.1).
2. **Critique** — Gérer la perte de permission SAF sans perte silencieuse de
   capture (C3.2).
3. **Critique** — Requalifier les chiffres de performance (N1) en objectifs
   de bêta à valider, nommer un appareil de référence concret (C4.1).
4. **Critique** — Fixer un protocole de mesure de la fiabilité (classification
   pré-enregistrée, n réel documenté) avant d'annoncer 90 % (C5.1).
5. **Haute** — Foreground service / résilience Doze pour le traitement en
   arrière-plan (C3.3).
6. **Haute** — Anticiper la contention CPU capture/OCR en charge soutenue
   (C4.2).
7. **Haute** — Documenter la baseline du critère « 20 minutes » (C5.2).
8. **Haute** — Tester la portabilité inter-téléphone, pas seulement
   inter-PC, pour la souveraineté (C5.3).
9. **Haute** — Réintégrer ou déclarer explicitement l'abandon de
   l'export/synchronisation promis par le brief (C6.1).
10. Le reste (moyennes/basses) peut attendre une itération, mais devrait être
    au moins tracé (issue ou note) pour ne pas se reperdre.
