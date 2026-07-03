package com.scantickets.app.data

import java.text.Normalizer
import java.util.Locale

/** Catégories de dépenses. */
enum class Categorie(val cle: String, val libelle: String, val emoji: String) {
    ALIMENTATION("alimentation", "Alimentation", "🛒"),
    RESTAURANT("restaurant", "Restaurant & fast-food", "🍔"),
    TRANSPORT("transport", "Transport & carburant", "⛽"),
    SANTE("sante", "Santé & pharmacie", "💊"),
    HYGIENE("hygiene", "Hygiène & beauté", "🧴"),
    MAISON("maison", "Maison & entretien", "🏠"),
    VETEMENTS("vetements", "Vêtements", "👕"),
    LOISIRS("loisirs", "Loisirs & sport", "🎮"),
    ELECTRONIQUE("electronique", "High-tech", "📱"),
    ANIMAUX("animaux", "Animaux", "🐾"),
    AUTRE("autre", "Autre", "📦");

    companion object {
        fun depuisCle(cle: String?): Categorie =
            entries.firstOrNull { it.cle == cle } ?: AUTRE
    }
}

/**
 * Catégorisation automatique, déterministe et hors ligne :
 * - le ticket reçoit une catégorie selon l'enseigne (Quick → Restaurant…) ;
 * - chaque article peut être affiné par mots-clés (« LESSIVE » chez Carrefour
 *   → Maison), ce qui permet de ventiler un caddie de supermarché.
 *
 * Choix assumé : pas de LLM embarqué — pour cette tâche, un dictionnaire
 * est plus précis, instantané, testable et gratuit en batterie.
 */
object Categoriseur {

    // ---------- Enseignes → catégorie de ticket ----------

    private val enseignesParCategorie: List<Pair<Categorie, List<String>>> = listOf(
        Categorie.RESTAURANT to listOf(
            "quick", "mcdonald", "mc do", "burger king", "kfc", "subway",
            "domino", "pizza", "o'tacos", "otacos", "exki", "panos",
            "friterie", "frituur", "snack", "brasserie", "restaurant",
            "tea-room", "traiteur", "sushi", "kebab"
        ),
        Categorie.TRANSPORT to listOf(
            "total energies", "totalenergies", "esso", "shell", "q8", "lukoil",
            "texaco", "dats 24", "dats24", "sncb", "stib", "de lijn",
            "uber", "taxi", "parking", "carwash", "car wash", "station"
        ),
        Categorie.SANTE to listOf(
            "pharmacie", "apotheek", "medi-market", "medimarket", "dentiste",
            "clinique", "hopital", "optique", "optic", "audition"
        ),
        Categorie.HYGIENE to listOf(
            "kruidvat", "yves rocher", "ici paris", "sephora", "rituals",
            "planet parfum", "nocibe", "coiffure", "coiffeur", "barbier"
        ),
        Categorie.MAISON to listOf(
            "ikea", "brico", "gamma", "hubo", "mr bricolage", "casa", "action",
            "hema", "zeeman", "blokker", "leroy merlin", "castorama", "maisons du monde"
        ),
        Categorie.VETEMENTS to listOf(
            "primark", "h&m", "h & m", "zara", "c&a", "c & a", "jbc", "kiabi",
            "celio", "bershka", "pull&bear", "jack & jones", "vinted", "chaussea"
        ),
        Categorie.LOISIRS to listOf(
            "decathlon", "kinepolis", "ugc", "pathe", "cinema", "librairie",
            "standaard boekhandel", "club", "intersport", "jd sports", "game mania"
        ),
        Categorie.ELECTRONIQUE to listOf(
            "fnac", "mediamarkt", "media markt", "krefel", "vanden borre",
            "coolblue", "apple store", "samsung store"
        ),
        Categorie.ANIMAUX to listOf(
            "tom&co", "tom & co", "maxi zoo", "maxizoo", "veterinaire", "animalerie"
        ),
        // En dernier : les enseignes alimentaires génériques.
        Categorie.ALIMENTATION to listOf(
            "carrefour", "lidl", "aldi", "delhaize", "colruyt", "intermarche",
            "leclerc", "auchan", "casino", "monoprix", "franprix", "super u",
            "hyper u", "cora", "match", "spar", "okay", "proxy", "grand frais",
            "picard", "biocoop", "epicerie", "boucherie", "boulangerie",
            "patisserie", "poissonnerie", "marche", "bio"
        )
    )

    // ---------- Mots-clés d'articles → catégorie fine ----------

    private val articlesParCategorie: List<Pair<Categorie, List<String>>> = listOf(
        Categorie.MAISON to listOf(
            "lessive", "adoucissant", "javel", "nettoyant", "eponge",
            "sac poubelle", "ampoule", "pile", "essuie-tout", "essuie tout",
            "liquide vaisselle", "papier alu", "film etirable", "desodorisant"
        ),
        Categorie.HYGIENE to listOf(
            "shampo", "gel douche", "savon", "dentifrice", "brosse a dent",
            "deo", "rasoir", "mousse a raser", "coton", "maquillage",
            "papier toilette", "papier wc", "mouchoir", "couche", "serviette hyg"
        ),
        Categorie.SANTE to listOf(
            "paracetamol", "dafalgan", "ibuprofen", "nurofen", "vitamine",
            "sirop", "pansement", "medicament", "spray nasal", "antidouleur"
        ),
        Categorie.ANIMAUX to listOf(
            "croquette", "litiere", "patee", "friandise chat", "friandise chien"
        ),
        Categorie.ALIMENTATION to listOf(
            "lait", "pain", "oeuf", "fromage", "yaourt", "yoghourt", "beurre",
            "jambon", "poulet", "boeuf", "steak", "viande", "poisson", "saumon",
            "riz", "pates", "pizza", "legume", "tomate", "pomme", "banane",
            "salade", "cafe", "sucre", "farine", "huile", "eau", "jus",
            "soda", "coca", "biere", "vin", "chocolat", "biscuit", "chips",
            "cereale", "soupe", "frites", "surgele", "glace", "fruit"
        )
    )

    /** Catégorie d'un ticket d'après son enseigne. */
    fun categoriserTicket(magasin: String?): Categorie {
        val normalise = normaliser(magasin ?: return Categorie.AUTRE)
        for ((categorie, motifs) in enseignesParCategorie) {
            if (motifs.any { normalise.contains(it) }) return categorie
        }
        return Categorie.AUTRE
    }

    /**
     * Catégorie d'un article : mots-clés d'abord, sinon la catégorie du ticket.
     * (Un article « LESSIVE » chez Carrefour part en Maison, le reste du
     * caddie reste en Alimentation.)
     */
    fun categoriserArticle(libelle: String, categorieTicket: Categorie): Categorie {
        val normalise = normaliser(libelle)
        // Matching par début de mot : « lait » reconnaît « LAITS », mais
        // « creme » ne se déclenche pas au milieu de « DEMI-ECREME ».
        val mots = normalise.split(Regex("""[^a-z0-9]+""")).filter { it.isNotEmpty() }
        fun correspond(motCle: String): Boolean =
            if (motCle.contains(' ')) normalise.contains(motCle)
            else mots.any { it.startsWith(motCle) }

        for ((categorie, motsCles) in articlesParCategorie) {
            if (motsCles.any { correspond(it) }) return categorie
        }
        return categorieTicket
    }

    private fun normaliser(texte: String): String =
        Normalizer.normalize(texte.lowercase(Locale.FRANCE), Normalizer.Form.NFD)
            .replace(Regex("""\p{Mn}+"""), "")
}
