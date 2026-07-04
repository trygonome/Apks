package com.scantickets.app.progression

/** Les quatre Accapareurs : des mécanismes réels de captation, pas des marques. */
enum class Accapareur(val nom: String, val emoji: String, val devise: String) {
    TRAQUEUR("Le Traqueur", "🕷️", "Se nourrit de tout ce que tu laisses voir de toi."),
    SIRENE("La Sirène des Promos", "🧜", "Chante « dernière chance » pour te faire acheter."),
    ABONNITE("L'Abonnite", "🐙", "Une tentacule par abonnement oublié."),
    NUAGE("Le Grand Nuage", "☁️", "Veut aspirer ton jardin dans son silo.")
}

/**
 * Une fiche du Carnet de résistance : un palier permanent (jamais d'échéance,
 * jamais de reset — l'anti-FOMO par principe) qui débloque une leçon concrète
 * d'éducation à la gestion de l'argent.
 */
data class FicheResistance(
    val id: String,
    val accapareur: Accapareur,
    val titre: String,
    val objectif: String,
    val condition: (EtatJardin) -> Boolean,
    val progression: (EtatJardin) -> Pair<Int, Int>,
    val lecon: String,
    val reflexe: String
)

/**
 * Le Carnet de résistance : le système de progression pédagogique du Jardin.
 * Pas de quêtes hebdomadaires, pas de compte à rebours : ce sont des paliers
 * accumulés à ton rythme — l'urgence artificielle est l'arme de la Sirène,
 * pas la nôtre.
 */
object CarnetResistance {

    val fiches: List<FicheResistance> = listOf(

        // ---------- 🕷️ Le Traqueur — la lucidité (scanner) ----------
        FicheResistance(
            id = "traqueur_0", accapareur = Accapareur.TRAQUEUR,
            titre = "Rencontre avec le Traqueur",
            objectif = "Ouvrir le Carnet",
            condition = { true }, progression = { 1 to 1 },
            lecon = "Le pistage publicitaire suit tes recherches, ta position et tes achats " +
                "pour construire ton profil. « Gratuit » signifie le plus souvent que le " +
                "produit vendu, c'est ton attention — et tes données.",
            reflexe = "Cette app est 100 % locale : tes tickets ne quittent jamais ton téléphone. " +
                "Exige la même chose des autres."
        ),
        FicheResistance(
            id = "traqueur_1", accapareur = Accapareur.TRAQUEUR,
            titre = "Premier fil coupé",
            objectif = "Scanner 15 tickets",
            condition = { it.ticketsScannes >= 15 },
            progression = { it.ticketsScannes.coerceAtMost(15) to 15 },
            lecon = "Savoir précisément où va ton argent est la contre-mesure de base : le " +
                "marketing prospère sur le flou. Un ticket scanné est une dépense devenue " +
                "consciente — et une dépense consciente se discute.",
            reflexe = "Scanne le jour même de l'achat : la mémoire du contexte (pourquoi j'ai " +
                "acheté ça ?) disparaît en 48 h."
        ),
        FicheResistance(
            id = "traqueur_2", accapareur = Accapareur.TRAQUEUR,
            titre = "La toile se déchire",
            objectif = "Scanner 50 tickets",
            condition = { it.ticketsScannes >= 50 },
            progression = { it.ticketsScannes.coerceAtMost(50) to 50 },
            lecon = "Avec 50 tickets, tes propres données te renseignent mieux que n'importe " +
                "quel algorithme de recommandation : tu vois tes vrais prix, tes vraies " +
                "habitudes, tes vraies dérives — sans intermédiaire intéressé.",
            reflexe = "Consulte l'onglet Prix : ton inflation personnelle, mesurée par toi, chez toi."
        ),

        // ---------- 🐙 L'Abonnite — la vigilance (vérifier) ----------
        FicheResistance(
            id = "abonnite_0", accapareur = Accapareur.ABONNITE,
            titre = "Rencontre avec l'Abonnite",
            objectif = "Ouvrir le Carnet",
            condition = { true }, progression = { 1 to 1 },
            lecon = "9,99 €/mois semble indolore : c'est calculé pour. Sur un an, c'est " +
                "120 € — et l'économie de l'abonnement repose sur l'oubli : une part " +
                "importante des abonnés payent des services qu'ils n'utilisent plus.",
            reflexe = "Liste tes abonnements une fois par trimestre. Chaque tentacule " +
                "identifiée se rétracte."
        ),
        FicheResistance(
            id = "abonnite_1", accapareur = Accapareur.ABONNITE,
            titre = "Première tentacule rétractée",
            objectif = "Vérifier 5 tickets",
            condition = { it.ticketsVerifies >= 5 },
            progression = { it.ticketsVerifies.coerceAtMost(5) to 5 },
            lecon = "Vérifier ses tickets, c'est attraper les erreurs de caisse, les articles " +
                "comptés deux fois, les promotions non appliquées. Ces micro-pertes sont " +
                "invisibles une à une, significatives cumulées.",
            reflexe = "Un ticket marqué « à vérifier » = 30 secondes de contrôle. " +
                "C'est le meilleur taux horaire de ta journée."
        ),
        FicheResistance(
            id = "abonnite_2", accapareur = Accapareur.ABONNITE,
            titre = "Le poulpe recule",
            objectif = "Vérifier 15 tickets",
            condition = { it.ticketsVerifies >= 15 },
            progression = { it.ticketsVerifies.coerceAtMost(15) to 15 },
            lecon = "La donnée vérifiée vaut plus que la donnée brute : c'est vrai pour ton " +
                "budget comme pour toute décision. Les systèmes qui te veulent passif " +
                "préfèrent que tu ne regardes pas les détails.",
            reflexe = "Repère tes dépenses récurrentes dans l'historique : tout ce qui revient " +
                "chaque mois mérite la question « encore utile ? »."
        ),

        // ---------- 🧜 La Sirène des Promos — la maîtrise (budget) ----------
        FicheResistance(
            id = "sirene_0", accapareur = Accapareur.SIRENE,
            titre = "Rencontre avec la Sirène",
            objectif = "Ouvrir le Carnet",
            condition = { true }, progression = { 1 to 1 },
            lecon = "« Plus que 2 en stock », « offre limitée », « -70 % » : l'urgence " +
                "artificielle et les faux rabais sont des techniques documentées (dark " +
                "patterns) conçues pour court-circuiter ta réflexion. Le prix barré est " +
                "parfois gonflé juste avant la promo.",
            reflexe = "Devant une « affaire », attends 24 h. Si tu n'y penses plus le " +
                "lendemain, ce n'était pas un besoin."
        ),
        FicheResistance(
            id = "sirene_1", accapareur = Accapareur.SIRENE,
            titre = "Boucles d'oreilles de cire",
            objectif = "Finir 1 mois sous ton objectif de budget",
            condition = { it.grainesOr >= 1 },
            progression = { it.grainesOr.coerceAtMost(1) to 1 },
            lecon = "Un budget n'est pas une privation : c'est une décision prise à froid, " +
                "quand la Sirène ne chante pas. Chaque mois tenu prouve que tes choix " +
                "t'appartiennent encore.",
            reflexe = "Fixe l'objectif au calme en début de mois — jamais en magasin."
        ),
        FicheResistance(
            id = "sirene_2", accapareur = Accapareur.SIRENE,
            titre = "La Sirène se tait",
            objectif = "Finir 3 mois sous ton objectif",
            condition = { it.grainesOr >= 3 },
            progression = { it.grainesOr.coerceAtMost(3) to 3 },
            lecon = "Trois mois tenus, c'est une habitude installée — et les habitudes sont " +
                "précisément le champ de bataille : toute l'industrie de la persuasion " +
                "travaille à installer les siennes chez toi.",
            reflexe = "Réévalue ton objectif : trop facile ? Baisse-le. Intenable ? Il doit " +
                "rester crédible pour rester utile."
        ),

        // ---------- ☁️ Le Grand Nuage — la souveraineté (régularité) ----------
        FicheResistance(
            id = "nuage_0", accapareur = Accapareur.NUAGE,
            titre = "Rencontre avec le Grand Nuage",
            objectif = "Ouvrir le Carnet",
            condition = { true }, progression = { 1 to 1 },
            lecon = "L'« enclosure numérique » : des services pratiques qui, une fois tes " +
                "données et tes habitudes captées, rendent la sortie coûteuse. Ce qui entre " +
                "dans le silo n'en ressort pas facilement — c'est le modèle d'affaires.",
            reflexe = "Tes tickets sont des fichiers ordinaires dans TON dossier : lisibles " +
                "par n'importe quel outil, pour toujours. C'est ça, la portabilité."
        ),
        FicheResistance(
            id = "nuage_1", accapareur = Accapareur.NUAGE,
            titre = "Éclaircie",
            objectif = "Tenir une série de 5 jours",
            condition = { it.meilleureSerie >= 5 },
            progression = { it.meilleureSerie.coerceAtMost(5) to 5 },
            lecon = "La souveraineté n'est pas un statut, c'est une pratique : cinq jours " +
                "d'attention valent mieux qu'une grande résolution de janvier. Les systèmes " +
                "de captation parient sur ton relâchement.",
            reflexe = "Associe le scan à un geste existant : en vidant tes poches, en rentrant. " +
                "L'habitude porte l'effort à ta place."
        ),
        FicheResistance(
            id = "nuage_2", accapareur = Accapareur.NUAGE,
            titre = "Grand ciel bleu",
            objectif = "Tenir une série de 14 jours",
            condition = { it.meilleureSerie >= 14 },
            progression = { it.meilleureSerie.coerceAtMost(14) to 14 },
            lecon = "Deux semaines de pratique et le jardin est à toi : tu sais ce que tu " +
                "dépenses, où, et pourquoi. Aucune plateforme ne peut te vendre cette " +
                "lucidité — elle ne s'achète pas, elle se cultive.",
            reflexe = "Partage la méthode, pas les données : c'est l'outil qu'on transmet, " +
                "le jardin reste privé."
        )
    )

    /** Les fiches avec leur état de déblocage, dans l'ordre du Carnet. */
    fun etatFiches(etat: EtatJardin): List<Pair<FicheResistance, Boolean>> =
        fiches.map { it to it.condition(etat) }

    fun nbDebloquees(etat: EtatJardin): Int = fiches.count { it.condition(etat) }
}
