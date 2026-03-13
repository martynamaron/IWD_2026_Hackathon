package com.martynamaron.biograph.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.BioGraphApplication
import com.martynamaron.biograph.R
import com.martynamaron.biograph.ui.theme.GreenLightest
import com.martynamaron.biograph.util.MockDataGenerator
import kotlinx.coroutines.Dispatchers
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

    var showTimeRangeDialog by remember { mutableStateOf(false) }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Faint green logo tilted diagonally in the bottom-right corner
            Image(
                painter = painterResource(R.drawable.black_logo),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(350.dp)
                    .align(Alignment.TopEnd)
                    .rotate(-35f)
                    .alpha(0.08f),
                colorFilter = ColorFilter.tint(GreenLightest),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
            Text(
                text = "Demo Data",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Generate sample health data with realistic patterns and correlations for testing and demo purposes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showTimeRangeDialog = true },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isGenerating) "Generating…" else "Generate Mock Data")
            }
        }
        }
    }

    if (showTimeRangeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeRangeDialog = false },
            title = { Text("Generate Sample Data") },
            text = {
                Text("Choose how much sample data to generate. Any existing data will be replaced.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showTimeRangeDialog = false
                    isGenerating = true
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            MockDataGenerator(
                                app.dataTypeRepository,
                                app.dailyEntryRepository,
                                app.multipleChoiceRepository,
                                app.insightRepository
                            ).generate(6)
                        }
                        isGenerating = false
                        snackbarHostState.showSnackbar("6 months of sample data generated")
                    }
                }) {
                    Text("6 months")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTimeRangeDialog = false
                    isGenerating = true
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            MockDataGenerator(
                                app.dataTypeRepository,
                                app.dailyEntryRepository,
                                app.multipleChoiceRepository,
                                app.insightRepository
                            ).generate(3)
                        }
                        isGenerating = false
                        snackbarHostState.showSnackbar("3 months of sample data generated")
                    }
                }) {
                    Text("3 months")
                }
            }
        )
    }
}
