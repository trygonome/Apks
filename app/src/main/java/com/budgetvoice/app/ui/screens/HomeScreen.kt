package com.budgetvoice.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetvoice.app.data.model.Expense
import com.budgetvoice.app.data.model.ExpenseCategory
import com.budgetvoice.app.ui.viewmodel.ExpenseViewModel
import com.budgetvoice.app.ui.viewmodel.Period
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onNavigateToVoice: () -> Unit,
    onNavigateToCamera: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Voice", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Voice input button
                FloatingActionButton(
                    onClick = onNavigateToVoice,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Saisie vocale",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Camera button
                FloatingActionButton(
                    onClick = onNavigateToCamera,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Scanner ticket",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Manual input button
                SmallFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajout manuel")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Period selector
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { viewModel.updatePeriod(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total card
            TotalCard(
                total = when (uiState.selectedPeriod) {
                    Period.TODAY -> uiState.todayTotal
                    Period.WEEK -> uiState.weekTotal
                    Period.MONTH -> uiState.monthTotal
                },
                period = uiState.selectedPeriod
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Expenses list
            Text(
                text = "Dépenses récentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune dépense enregistrée",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.expenses) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ManualAddDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, category, description ->
                viewModel.addExpense(amount, category, description, isManual = true)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Period.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        when (period) {
                            Period.TODAY -> "Aujourd'hui"
                            Period.WEEK -> "Cette semaine"
                            Period.MONTH -> "Ce mois"
                        }
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TotalCard(total: Double, period: Period) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (period) {
                    Period.TODAY -> "Dépensé aujourd'hui"
                    Period.WEEK -> "Dépensé cette semaine"
                    Period.MONTH -> "Dépensé ce mois"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "%.2f €".format(total),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.category.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .wrapContentSize(Alignment.Center)
                )

                Column {
                    Text(
                        text = expense.category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (expense.description.isNotEmpty()) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = expense.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "%.2f €".format(expense.amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la dépense ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun ManualAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, ExpenseCategory, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.AUTRE) }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une dépense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Montant (€)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.emoji} ${selectedCategory.displayName}",
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
                        ExpenseCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.emoji} ${category.displayName}") },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        onConfirm(amountValue, selectedCategory, description)
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
