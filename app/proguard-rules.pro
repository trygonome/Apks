# R8 : on réduit la taille (shrink + optimize) mais sans obfusquer,
# pour garder des traces d'erreur lisibles et éviter les casses par réflexion.
-dontobfuscate

# ML Kit et Play Services embarquent leurs propres règles consumer ;
# on garde par prudence les points d'entrée du scanner de documents.
-keep class com.google.mlkit.vision.documentscanner.** { *; }
