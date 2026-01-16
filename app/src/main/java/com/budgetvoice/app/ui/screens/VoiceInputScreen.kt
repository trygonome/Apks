package com.budgetvoice.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budgetvoice.app.data.model.ExpenseCategory
import com.budgetvoice.app.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var parsedAmount by remember { mutableStateOf<Double?>(null) }
    var parsedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull() ?: ""
            recognizedText = text

            if (text.isNotEmpty()) {
                val (amount, category) = viewModel.parseVoiceInput(text)
                parsedAmount = amount
                parsedCategory = category

                if (amount != null) {
                    viewModel.addExpense(amount, category, text, isManual = false)
                    Toast.makeText(context, "Dépense ajoutée: ${amount}€", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    Toast.makeText(context, "Montant non reconnu, réessayez", Toast.LENGTH_SHORT).show()
                }
            }
        }
        isListening = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saisie vocale") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Prononcez votre dépense",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Exemples:\n\"15 euros café\"\n\"50 euros courses\"\n\"30 euros restaurant\"",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Animated microphone button
            MicrophoneButton(
                isListening = isListening,
                onClick = {
                    isListening = true
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites le montant et la catégorie")
                    }
                    speechRecognizerLauncher.launch(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (recognizedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Reconnu:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recognizedText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        if (parsedAmount != null && parsedCategory != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${parsedCategory!!.emoji} ${parsedAmount}€ - ${parsedCategory!!.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (isListening) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Écoute en cours...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MicrophoneButton(
    isListening: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = "Microphone",
            modifier = Modifier.size(64.dp),
            tint = Color.White
        )
    }
}
