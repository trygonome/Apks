# Recherche — Brique 1 : scan & stockage de tickets, état de l'art

*4 juillet 2026 · intrant pour le PRD de Budgeskets (nom de code)*

## 1. Ce qui existe (open source, Android)

| Projet | Ce qu'il fait | Ce qu'il ne fait pas | Stack |
|---|---|---|---|
| **MakeACopy** (F-Droid, Apache-2.0) | Scanner de documents hors ligne : détection de bords (modèle ONNX custom + OpenCV 4.13), OCR **PaddleOCR PP-OCRv5 mobile**, export PDF | Aucune extraction structurée (pas de total/date/enseigne/articles), pas de budget | OpenCV + ONNX + PaddleOCR |
| **OSS Document Scanner** (IzzyOnDroid) | Scan rapide → PDF cherchable | Généraliste, pas orienté tickets | Tesseract |
| **FairScan** (F-Droid) | Capture rapide, détection auto, recadrage, OCR → PDF | Généraliste, pas de données structurées | — |
| **Smart Receipts** (open source, historique) | Suivi de reçus pour notes de frais : photos + saisie manuelle, exports CSV/PDF | **Pas d'extraction automatique**, pas de catégorisation, pas de budget ; codebase vieillissante | — |
| **OCR (Tesseract)** (F-Droid) | Brique OCR utilisée par My Expenses | Extraction structurée limitée | Tesseract |

Côté propriétaire (Fetch, Finny, banques…) : OCR cloud, comptes obligatoires,
données monétisées — c'est l'anti-modèle, et c'est notre argument.

## 2. Le vide à occuper

**Personne, en open source Android, ne fait la chaîne complète :**
scan rapide → **extraction structurée** (total, date, enseigne, articles,
TVA) → stockage local exploitable. Les scanners s'arrêtent au PDF ;
Smart Receipts ne lit pas les tickets. La brique 1 vise exactement ce vide :
c'est une opportunité réelle de devenir la référence.

Notre avantage différentiel existe déjà : les moteurs du prototype
(reconstruction géométrique des lignes OCR + analyseur à scores + tests sur
tickets réels) font précisément ce que les scanners généralistes ne font pas.

## 3. Les points durs avec nos moyens

1. **ML Kit est propriétaire.** Le prototype repose sur les briques Google
   (scanner GMS + Text Recognition fermé). Conséquences : **exclusion de
   F-Droid**, échec sur téléphones dégooglisés — incompatible avec
   l'ambition « référence open source ».
2. **Pas de test sur appareil ici.** Le pipeline caméra (rafale, autofocus,
   cadence) s'itérera par cycles de bêta avec l'utilisateur de référence.
3. **Thermiques pâlis** : exigent un pré-traitement (CLAHE/contraste
   adaptatif OpenCV) à valider sur la pile réelle de ~50 tickets.
4. **Taille d'APK** : modèles PaddleOCR mobile ≈ 10-20 Mo — acceptable
   (le prototype ML Kit pesait 45 Mo).

## 4. Solutions élaborées

### Option A — Stack 100 % libre (recommandée)
CameraX (capture rafale maîtrisée) + OpenCV (détection de bords, redressement,
CLAHE) + **PaddleOCR mobile via ONNX Runtime** (texte + boîtes englobantes)
→ nos moteurs existants (reconstruction géométrique, analyseur à scores)
par-dessus, quasi inchangés.
- ✅ Éligible F-Droid, fonctionne dégooglisé, contrôle total du mode rafale ;
- ✅ Faisabilité prouvée par MakeACopy (Apache-2.0 : on peut étudier et
  réutiliser leur approche en toute compatibilité) ;
- ✅ Clin d'œil au plan d'action initial de Victor : PaddleOCR, prévu sur PC,
  arrive dans la poche ;
- ⚠️ Plus d'ingénierie (notre propre UI de capture, notre pipeline de
  recadrage) ; détection de bords probablement un cran sous celle de Google
  au début.

### Option B — Stack Google pragmatique (celle du prototype)
Rapide, recadrage excellent, mais : pas de F-Droid, pas de dégooglisé, pas de
rafale maîtrisée, dépendance propriétaire au cœur d'un projet de conviction.

### Option C — Double saveur (libre + GMS)
Deux variantes de build. Coût de maintenance élevé pour un projet solo ;
à considérer plus tard, pas en brique 1.

### Recommandation
**Option A.** Elle coûte plus cher en ingénierie mais elle est la seule
cohérente avec le brief (« l'app elle-même est irréprochable ») et avec
l'ambition de référence open source. Le risque technique principal
(qualité du recadrage automatique) est borné : MakeACopy le résout déjà en
Apache-2.0, et notre repli est un recadrage manuel digne.

## 5. Sources

- MakeACopy : https://github.com/egdels/makeacopy
- OSS Document Scanner : https://apt.izzysoft.de/fdroid/index/apk/com.akylas.documentscanner
- FairScan : https://f-droid.org/en/packages/org.fairscan.app/
- Smart Receipts : https://github.com/JuliaSoboleva/SmartReceiptsLibrary
- OCR Tesseract (My Expenses) : https://f-droid.org/en/packages/org.totschnig.ocr.tesseract/
- Open Note Scanner : https://f-droid.org/packages/com.todobom.opennotescanner/
