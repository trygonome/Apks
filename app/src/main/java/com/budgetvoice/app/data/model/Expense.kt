package com.budgetvoice.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val description: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isManual: Boolean = false // true si saisi manuellement, false si via voix/OCR
)

enum class ExpenseCategory(val displayName: String, val emoji: String) {
    ALIMENTATION("Alimentation", "🍽️"),
    TRANSPORT("Transport", "🚗"),
    LOISIRS("Loisirs", "🎮"),
    SANTE("Santé", "💊"),
    LOGEMENT("Logement", "🏠"),
    VETEMENTS("Vêtements", "👕"),
    COURSES("Courses", "🛒"),
    RESTAURANT("Restaurant", "🍕"),
    CAFE("Café", "☕"),
    AUTRE("Autre", "💰");

    companion object {
        fun fromString(text: String): ExpenseCategory {
            val normalized = text.lowercase().trim()
            return when {
                normalized.contains("alim") || normalized.contains("food") || normalized.contains("nourriture") -> ALIMENTATION
                normalized.contains("transport") || normalized.contains("essence") || normalized.contains("bus") -> TRANSPORT
                normalized.contains("loisir") || normalized.contains("sortie") || normalized.contains("jeu") -> LOISIRS
                normalized.contains("santé") || normalized.contains("sante") || normalized.contains("médecin") || normalized.contains("pharmacie") -> SANTE
                normalized.contains("loyer") || normalized.contains("logement") || normalized.contains("maison") -> LOGEMENT
                normalized.contains("vêtement") || normalized.contains("vetement") || normalized.contains("habit") -> VETEMENTS
                normalized.contains("course") || normalized.contains("supermarché") || normalized.contains("marché") -> COURSES
                normalized.contains("restaurant") || normalized.contains("resto") || normalized.contains("manger") -> RESTAURANT
                normalized.contains("café") || normalized.contains("cafe") || normalized.contains("bar") -> CAFE
                else -> AUTRE
            }
        }
    }
}
