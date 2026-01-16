package com.budgetvoice.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.budgetvoice.app.data.model.ExpenseCategory
import com.budgetvoice.app.ui.viewmodel.ExpenseViewModel
import com.budgetvoice.app.utils.OcrProcessor
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScanScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var detectedAmount by remember { mutableStateOf<Double?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.AUTRE) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            isProcessing = true
            OcrProcessor.processImage(context, imageUri!!) { amount ->
                isProcessing = false
                if (amount != null) {
                    detectedAmount = amount
                    showConfirmDialog = true
                } else {
                    Toast.makeText(context, "Montant non détecté, réessayez", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera(context) { uri ->
                imageUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Permission caméra requise", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner un ticket") },
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
                text = "Scannez votre ticket",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Prenez une photo claire de votre ticket de caisse.\nL'app détectera automatiquement le montant total.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (imageUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Ticket scanné",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            launchCamera(context) { uri ->
                                imageUri = uri
                                cameraLauncher.launch(uri)
                            }
                        }
                        else -> {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = !isProcessing
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (imageUri == null) "Prendre une photo" else "Reprendre",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (isProcessing) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Analyse du ticket...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showConfirmDialog && detectedAmount != null) {
        ConfirmExpenseDialog(
            amount = detectedAmount!!,
            category = selectedCategory,
            onCategoryChange = { selectedCategory = it },
            onConfirm = {
                viewModel.addExpense(detectedAmount!!, selectedCategory, "Ticket scanné", isManual = false)
                Toast.makeText(context, "Dépense ajoutée: ${detectedAmount}€", Toast.LENGTH_SHORT).show()
                onBack()
            },
            onDismiss = {
                showConfirmDialog = false
                detectedAmount = null
            }
        )
    }
}

@Composable
fun ConfirmExpenseDialog(
    amount: Double,
    category: ExpenseCategory,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmer la dépense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Montant détecté: %.2f €".format(amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = "${category.emoji} ${category.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ExpenseCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.emoji} ${cat.displayName}") },
                                onClick = {
                                    onCategoryChange(cat)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

private fun launchCamera(context: android.content.Context, onUriCreated: (Uri) -> Unit) {
    val photoFile = File(context.cacheDir, "ticket_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
    onUriCreated(uri)
}
