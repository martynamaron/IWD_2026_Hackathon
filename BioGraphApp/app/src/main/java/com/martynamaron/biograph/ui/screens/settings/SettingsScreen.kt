package com.martynamaron.biograph.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.martynamaron.biograph.BioGraphApplication
import com.martynamaron.biograph.util.MockDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as BioGraphApplication
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val dataTypes by app.dataTypeRepository.getAllFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    var showConfirmDialog by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Demo Data",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Generate 2 months of randomised daily entries for testing and demo purposes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (dataTypes.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar("Create some data types first") }
                    } else {
                        showConfirmDialog = true
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isGenerating) "Generating…" else "Generate Mock Data")
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Generate Mock Data?") },
            text = {
                Text("This will generate random daily entries for the past 2 months. Existing entries for those dates will be overwritten.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    isGenerating = true
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val types = app.dataTypeRepository.getAllFlow().first()
                            MockDataGenerator(app.dataTypeRepository, app.dailyEntryRepository, app.multipleChoiceRepository)
                                .generate(types)
                        }
                        isGenerating = false
                        snackbarHostState.showSnackbar("Mock data generated successfully")
                    }
                }) {
                    Text("Generate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
