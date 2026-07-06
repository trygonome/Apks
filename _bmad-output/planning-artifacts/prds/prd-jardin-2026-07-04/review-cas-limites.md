---
title: "Revue cas limites — PRD BuKet, brique 1"
status: draft
created: 2026-07-06
reviewer: "Chasseur de cas limites (BMAD)"
target: "prd.md (2026-07-04)"
---

# Revue des cas limites — PRD BuKet, brique 1

Méthode : parcours exhaustif de chaque exigence et parcours utilisateur ;
seuls les cas **réellement absents** du texte du PRD sont listés. Les cas déjà
couverts (ex. FR-1.3 recadrage manuel en repli, FR-2.2 reprise de file après
fermeture d'app, FR-3.3 zone illisible → champ vide) sont omis.

## Légende sévérité

- **Critique** : perte ou corruption de données, ou blocage complet d'un
  parcours central.
- **Haute** : dégrade fortement la confiance/fiabilité ou casse un principe
  de conception explicite du PRD.
- **Moyenne** : dégrade l'expérience ou la fiabilité dans un cas minoritaire.
- **Basse** : cas marginal, impact limité.

---

## UJ-1 — La séance « pile »

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| UJ-1 / F3 / F6 | Le même ticket est photographié deux fois (doublon accidentel dans la pile) | Deux entrées identiques polluent la liste et faussent toute future lucidité financière ; aucune règle de dédoublonnage n'est mentionnée | Critique | Détecter la quasi-similarité (montant+date+enseigne) et signaler un doublon probable à la vérification |
| F1 / F3 | Photo totalement ratée (flou, bougé, sur-/sous-exposée) rendant l'image inexploitable, distinct du simple pli léger déjà traité par FR-3.3 | Aucun mécanisme de reprise/re-capture décrit ; le ticket peut finir en fiche vide silencieuse sans que Victor sache qu'il doit rescanner | Haute | Détecter la netteté/exposition à la capture ou en sortie d'OCR et proposer explicitly une reprise de capture |
| UJ-1 / F1 | Ticket thermique long (liste de courses) nécessitant deux photos bout à bout (plié en deux) | Pas de notion de "ticket multi-photos" ; risque de deux fiches séparées ou de troncature du total/des articles | Critique | Prévoir un mode "suite du ticket" qui associe plusieurs captures à une seule fiche avant analyse |
| F1 / F3 | Photo qui n'est pas un ticket (doigt, table, mauvais objet capturé par erreur pendant l'enchaînement rapide) | FR-1.3 ne détecte que le cadrage/recadrage d'un ticket, pas l'absence de ticket ; risque de fiche vide ou aberrante insérée dans la liste | Haute | Ajouter un seuil de confiance "contient un ticket" en sortie de détection ; en repli, proposer suppression immédiate |
| F1 / F5 | Deux tickets présents dans le même cadre photographié (empilés ou côte à côte) | FR-1.3 ne mentionne que la détection/recadrage d'un ticket unique ; risque de fusion des deux tickets en une seule extraction erronée | Moyenne | Détecter plusieurs contours de ticket dans une même image et proposer un recadrage/scission manuel |
| F2 / N1 | Stockage plein (carte SD/mémoire interne) survenant en pleine séance de ~50 tickets | FR-1.4 garantit que la photo est persistée avant analyse, mais aucun comportement n'est défini si l'écriture échoue faute d'espace | Critique | Détecter l'espace disponible avant capture, avertir Victor et bloquer proprement la capture plutôt que de perdre silencieusement la photo |
| F5 | Dossier SAF révoqué, supprimé, ou carte SD retirée pendant la séance (analyse en file ou nouvelle capture) | FR-2.1/FR-5.1 supposent un accès permanent au dossier choisi ; aucune gestion de la perte d'accès n'est prévue | Critique | Détecter la perte d'accès SAF, mettre la file en pause, conserver les captures en attente et notifier clairement l'utilisateur |
| UJ-1 (ticket "délavé/froissé", contexte Belgique) | Ticket rédigé en néerlandais ou en allemand (zone frontalière/Belgique bilingue) | AnalyseurTicket (héritage) gère les taux de TVA BE/FR mais rien n'indique une gestion des mots-clés ("totaal", "BTW", "Summe", "MwSt.") hors français | Haute | Étendre la détection de mots-clés d'extraction à NL/DE ou signaler explicitement une confiance basse quand la langue du ticket diffère du français |
| F3 | Montant exprimé en devise étrangère (achat transfrontalier) | Aucun champ ni règle de gestion de devise n'est prévu ; un montant en devise étrangère risque d'être interprété comme un montant en euros | Critique | Détecter le symbole/code devise et soit convertir explicitement, soit marquer le champ à confiance basse sans l'agréger comme des euros |

## UJ-2 — Le réflexe voiture

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| F1 / N2 | Permission caméra révoquée par l'utilisateur (Android) au moment précis où il veut capturer | FR-1.1/FR-1.2 supposent la caméra disponible instantanément ; aucun repli n'est décrit si la permission manque | Haute | Détecter l'absence de permission au démarrage et proposer un chemin de re-demande clair, sans casser le délai de 2 s pour le cas nominal |
| F1 / F2 | Interruption pendant la capture même (appel entrant, notification, mise en veille) avant que la photo ne soit prise | FR-1.4 protège la photo une fois prise, mais rien n'est dit sur l'état intermédiaire (visée caméra en cours) en cas d'interruption système | Moyenne | Garantir qu'une interruption avant le déclenchement ne laisse aucun état incohérent ; réouverture = retour à l'état "prêt à capturer" |

## F1 — Capture

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| FR-1.1 | Téléphone bas/milieu de gamme réel ne tenant pas la cible de 2 s | Le PRD fixe une cible de performance mais ne définit aucun comportement dégradé si elle n'est pas atteinte sur le terrain | Moyenne | Définir un mode dégradé explicite (ex. barre de progression discrète) plutôt que de laisser un dépassement silencieux |
| FR-1.4 | Échec d'écriture du fichier photo lui-même (pas seulement disque plein : SAF instable, IO error) | La garantie "aucune capture n'est perdue" repose sur une écriture qui pourrait échouer sans qu'aucun repli ne soit défini | Haute | Prévoir une zone tampon locale de secours et une notification explicite en cas d'échec d'écriture |

## F2 — Traitement en file d'attente

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| FR-2.1 | L'analyse d'un ticket particulier échoue/plante (image corrompue, format inattendu) | Rien n'indique si une erreur sur un ticket bloque le reste de la file ou est isolée | Haute | Isoler chaque analyse (une erreur ticket ne bloque pas les suivants) et marquer le ticket en échec, visible et rejouable |
| FR-2.2 | Ticket resté en file au-delà d'un temps très long (app tuée à répétition, batterie économe empêchant le travail de fond sur plusieurs jours) | Aucune limite/retry ni notification n'est prévue si la file "ne finit jamais" de traiter certains tickets | Moyenne | Ajouter un état visible "en attente depuis X" et un déclenchement explicite (ouverture app, notification) pour reprendre |

## F3 — Extraction structurée

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| FR-3.1 / FR-3.3 | OCR échoue totalement sur un ticket (thermique trop pâli, tous les champs illisibles) | Rien n'indique si le ticket est quand même créé (fiche entièrement vide) ou écarté, ni comment il apparaît dans la liste (F6) | Haute | Définir explicitement l'état "ticket illisible" : fiche créée, signalée en tête de liste comme prioritaire à vérifier, jamais silencieusement absente |

## F4 — Vérification & correction

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| FR-4.2 | Un ticket déjà marqué "vérifié" est ensuite recorrigé (Victor change d'avis après coup) | Le statut du marqueur "vérifié" après une nouvelle correction n'est pas défini (reste vérifié ? redevient douteux ?) | Moyenne | Préciser que toute nouvelle correction manuelle maintient/renforce le statut "vérifié" (cohérent avec la loi "la correction humaine prime") |

## F5 — Stockage souverain

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| FR-5.1 | Fichiers modifiés, déplacés ou supprimés manuellement en dehors de l'app (permis explicitement par "lisibles sans l'app") | Aucune règle de resynchronisation : la liste (F6) peut référencer des fichiers absents, ou ignorer des fichiers ajoutés/modifiés hors app | Haute | Définir un mécanisme de rafraîchissement (scan du dossier à l'ouverture) qui réconcilie la liste avec l'état réel du dossier |
| FR-5.1 | Changement de téléphone (migration) : nouvelle installation pointée vers un dossier existant plein de tickets déjà scannés | Le PRD garantit la lisibilité des fichiers "pour toujours" mais ne précise pas comment l'app reconstitue sa liste/état interne (vérifié, doublons) depuis un dossier existant | Haute | Spécifier que l'ouverture d'un dossier existant reconstruit intégralement la liste et les statuts depuis les fichiers, sans perte ni duplication |
| FR-5.1 | Collision de noms de fichiers dans le dossier choisi (fichiers déjà présents, ex. copie manuelle antérieure ou export d'une autre install) | Aucune règle de nommage/anti-écrasement n'est donnée ; risque d'écraser silencieusement un fichier existant | Moyenne | Garantir un schéma de nommage non collisionnable (horodatage + identifiant unique) |
| FR-5.2 | Suppression partiellement échouée (image supprimée mais JSON non supprimé, ou l'inverse, en cas de coupure/carte retirée en cours de suppression) | Aucune atomicité n'est spécifiée ; risque de fichiers orphelins incohérents avec la liste affichée | Moyenne | Rendre la suppression idempotente/rejouable au prochain lancement (nettoyage des orphelins détectés) |
| FR-5.1 (schéma de données) | Évolution du format JSON structuré entre deux versions de l'app (nouveau champ, renommage) | Aucune stratégie de version/migration de schéma n'est mentionnée alors que F5 promet une lisibilité "pour toujours" | Critique | Versionner explicitement le format de données et prévoir une migration/rétro-compatibilité de lecture |

## F6 — L'accueil

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| FR-6.1 | Critère de tri de la liste "chronologique" ambigu : date de capture vs date imprimée sur le ticket (extraite, potentiellement incorrecte ou absente) | Aucune règle explicite ; en cas de date système erronée (voir N1/critères) ou de date de ticket illisible, l'ordre affiché devient imprévisible | Haute | Préciser explicitement le champ de tri (ex. date de capture stable, avec date ticket en complément d'affichage) |
| FR-6.1 | Premier lancement, aucun ticket encore scanné (liste vide) | Rien n'est prévu pour l'état vide de l'écran d'accueil "rien d'autre" | Basse | Définir un état vide minimal cohérent avec le principe de sobriété |

## N1 — Performance

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| N1 | Horloge/fuseau du téléphone incorrect au moment de la séance ou de l'usage quotidien | Affecte à la fois l'ordre chronologique de la liste (F6) et l'horodatage des fichiers stockés (F5), sans qu'aucune détection/garde-fou ne soit prévue | Haute | Ne pas dépendre uniquement de l'horloge système pour l'ordre interne, ou détecter une incohérence flagrante (date future/antérieure à l'installation) |
| N1 | Rafale de ~50 analyses en file sur téléphone bas de gamme : cumul de charge CPU/chauffe/consommation batterie | Le PRD fixe un temps par ticket (<10 s) mais ne borne pas l'effet cumulé d'un traitement de masse en conditions réelles | Moyenne | Définir un comportement de limitation (throttling) explicite pour les traitements en lot afin d'éviter surchauffe/épuisement batterie |

## N2 — Vie privée / permissions

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| N2 | Permission d'accès au dossier SAF révoquée après coup (mise à jour Android, changement de volume) en dehors du cas déjà listé "pile" | Aucune détection persistante de la validité de l'autorisation au démarrage n'est prévue | Haute | Vérifier la validité du droit d'accès SAF à chaque ouverture d'app et guider une re-sélection si besoin |

## N6 — Accessibilité

| Exigence concernée | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| N6 / FR-1.2 | Utilisation de TalkBack pendant l'enchaînement rapide de captures avec bip sonore | Le bip et l'enchaînement sans écran intermédiaire (principe "le bip suffit") ne précisent pas la restitution vocale pour un utilisateur non-voyant ; conflit potentiel bip/lecture d'écran | Moyenne | Spécifier le comportement TalkBack pendant la capture en rafale (annonce vocale courte compatible avec le rythme du bip) |

## Critères d'acceptation de la brique

| Critère concerné | Cas limite | Conséquence si ignoré | Sévérité | Traitement proposé |
|---|---|---|---|---|
| Critère 1 (pile vaincue, ≤ 20 min) | Le chrono de 20 minutes ne précise pas s'il inclut les reprises dues à des captures ratées/doublons/tickets non reconnus | Mesure de succès non reproductible ; désaccord possible sur ce qui compte dans les 20 minutes | Moyenne | Définir explicitement le périmètre du chronométrage (captures brutes vs tickets finalement validés) |
| Critère 2 (fiabilité ≥ 90 %) | Ticket dans un état "mixte" ambigu (à moitié délavé, à moitié net) ne rentrant clairement ni dans "bon état" ni dans "délavé/froissé" | Le seuil de 90 % dépend d'une classification d'état qui n'est pas définie précisément, rendant la mesure sujette à interprétation | Basse | Donner un critère opérationnel simple de classement d'état pour fiabiliser la mesure |
| Critère 4 (souveraineté prouvée) | Lecture des fichiers sur PC après une évolution du format de données entre deux versions de l'app (cf. gap F5 schéma) | La preuve de souveraineté pourrait ne plus tenir si le format a changé sans compatibilité descendante | Critique | Rattacher ce critère à l'exigence de versionnage du format (cf. gap F5) |

---

## Résumé des cas trouvés

- Critique : 6
- Haute : 12
- Moyenne : 9
- Basse : 2

**Total : 29 cas limites non traités identifiés.**
