package com.budgetvoice.app.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

object OcrProcessor {

    fun processImage(context: Context, imageUri: Uri, onResult: (Double?) -> Unit) {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val detectedAmount = extractAmount(visionText.text)
                    onResult(detectedAmount)
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } catch (e: IOException) {
            onResult(null)
        }
    }

    private fun extractAmount(text: String): Double? {
        // Look for patterns like "TOTAL", "Total", "total" followed by an amount
        val lines = text.lines()

        // Common patterns for total amounts on receipts
        val totalPatterns = listOf(
            """(?i)total\s*:?\s*(\d+[,.]?\d{0,2})""".toRegex(),
            """(?i)montant\s*:?\s*(\d+[,.]?\d{0,2})""".toRegex(),
            """(?i)somme\s*:?\s*(\d+[,.]?\d{0,2})""".toRegex(),
            """(\d+[,.]\d{2})\s*€""".toRegex(),
            """€\s*(\d+[,.]\d{2})""".toRegex()
        )

        // Try to find total amount
        for (line in lines.reversed()) { // Start from bottom as total is usually at the end
            for (pattern in totalPatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    val amountStr = match.groupValues[1].replace(",", ".")
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0 && amount < 10000) { // Reasonable amount range
                        return amount
                    }
                }
            }
        }

        // Fallback: find any amount that looks like a total (largest amount found)
        val amounts = mutableListOf<Double>()
        val amountPattern = """(\d+[,.]\d{2})""".toRegex()

        for (line in lines) {
            amountPattern.findAll(line).forEach { match ->
                val amountStr = match.groupValues[1].replace(",", ".")
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0 && amount < 10000) {
                    amounts.add(amount)
                }
            }
        }

        return amounts.maxOrNull()
    }
}
