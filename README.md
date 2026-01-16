# Budget Voice 💰🎤

Une application Android de gestion de budget personnalisée qui rend la saisie de dépenses **ultra-rapide** grâce à la reconnaissance vocale et l'OCR.

## 🎯 Concept

Budget Voice résout le problème principal des apps de budget traditionnelles : **la friction de saisie**. Plus besoin de passer du temps à noter chaque dépense manuellement !

## ✨ Fonctionnalités

### 🎤 Saisie Vocale Ultra-Rapide
- Dites simplement "15 euros café" ou "50 euros courses"
- L'app détecte automatiquement le montant et la catégorie
- Enregistrement instantané en quelques secondes

### 📸 Scan de Tickets (OCR)
- Prenez une photo de votre ticket de caisse
- L'OCR détecte automatiquement le montant total
- Validation rapide en un clic

### ⚡ Saisie Manuelle Rapide
- Fallback simple si nécessaire
- Interface minimaliste et intuitive

### 🔔 Notifications Intelligentes
- Rappels personnalisés pour ne rien oublier
- Notifications à 18h (retour du boulot) et 22h (soirée)
- Configuration simple et non-intrusive

### 📊 Dashboard Simple
- Vue claire de vos dépenses (jour/semaine/mois)
- Catégorisation automatique
- Historique détaillé

## 🏷️ Catégories

L'app organise automatiquement vos dépenses en catégories :
- 🍽️ Alimentation
- 🚗 Transport
- 🎮 Loisirs
- 💊 Santé
- 🏠 Logement
- 👕 Vêtements
- 🛒 Courses
- 🍕 Restaurant
- ☕ Café
- 💰 Autre

## 🛠️ Technologies

- **Kotlin** - Langage moderne Android
- **Jetpack Compose** - UI moderne et fluide
- **Room Database** - Stockage local des données
- **ML Kit Text Recognition** - OCR pour les tickets
- **Android Speech Recognition** - Reconnaissance vocale
- **WorkManager** - Notifications programmées
- **MVVM Architecture** - Architecture propre et maintenable

## 📱 Prérequis

- Android 8.0 (API 26) ou supérieur
- Permissions :
  - 🎤 Microphone (pour la saisie vocale)
  - 📷 Caméra (pour le scan de tickets)
  - 🔔 Notifications (pour les rappels)

## 🚀 Installation

1. Cloner le repository
2. Ouvrir le projet dans Android Studio
3. Synchroniser les dépendances Gradle
4. Compiler et installer sur votre appareil

## 📖 Utilisation

1. **Première utilisation** : Accordez les permissions nécessaires
2. **Ajouter une dépense** :
   - 🎤 Appuyez sur le bouton micro et parlez
   - 📸 Utilisez le bouton caméra pour scanner un ticket
   - ✏️ Ou ajoutez manuellement via le bouton "+"
3. **Consulter vos dépenses** : Naviguez entre Aujourd'hui/Semaine/Mois
4. **Gérer** : Appuyez sur une dépense pour la supprimer

## 🎨 Design

Interface minimaliste suivant les guidelines Material Design 3 avec :
- Thème adaptatif (clair/sombre)
- Animations fluides
- Navigation intuitive
- Accessibilité optimisée

## 🔒 Confidentialité

- Toutes les données restent **localement sur votre appareil**
- Aucune connexion internet requise
- Aucune collecte de données
- Base de données chiffrée

## 🎓 Méthodologie

Développé en suivant la **méthode BMAD** (Breakthrough Method for Agile AI-Driven Development) :
- Phase Découverte : Identification du pain point réel
- Phase Brainstorming : Solutions d'automatisation
- Phase Spécification : Définition du MVP
- Phase Développement : Implémentation itérative

## 🤝 Contribution

Cette application a été créée de manière personnalisée pour répondre à des besoins spécifiques. N'hésitez pas à forker et adapter à vos propres besoins !

## 📄 Licence

Ce projet est open source et disponible sous licence MIT.

## 🙏 Remerciements

Développé avec Claude Code en utilisant la méthodologie BMAD pour créer une application de budget qui soit **vraiment utilisable au quotidien**.

---

**Budget Voice** - Gérez votre budget sans friction ! 💰✨
