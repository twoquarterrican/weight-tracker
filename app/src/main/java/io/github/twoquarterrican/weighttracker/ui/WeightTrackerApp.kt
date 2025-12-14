package io.github.twoquarterrican.weighttracker.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.twoquarterrican.weighttracker.data.WeightEntry
import java.util.Calendar
import java.util.Date

@Composable
fun WeightTrackerApp(viewModel: WeightViewModel) {
    val navController = rememberNavController()
    val weightList by viewModel.allWeights.collectAsState()
    val latestWeight = weightList.firstOrNull()?.weight
    
    // Lift state up to WeightTrackerApp to persist across navigation
    var minimumWeight by remember { mutableStateOf<Float?>(null) }
    var showGraph by remember { mutableStateOf(false) }

    // Calculate default minimum based on data if not manually set
    val dataMin = weightList.minOfOrNull { it.weight } ?: 0f
    val effectiveMinimumWeight = minimumWeight ?: (dataMin * 0.8f).coerceAtLeast(0f)

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                weightList = weightList,
                showGraph = showGraph,
                onToggleGraph = { showGraph = !showGraph },
                minimumWeight = effectiveMinimumWeight,
                onMinimumWeightChange = { minimumWeight = it },
                onAddClick = { navController.navigate("add") }
            )
        }
        composable("add") {
            AddEntryScreen(
                initialWeight = latestWeight,
                onSave = { weight, timestamp ->
                    viewModel.addWeight(weight, timestamp)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeightViewModel,
    weightList: List<WeightEntry>,
    showGraph: Boolean,
    onToggleGraph: () -> Unit,
    minimumWeight: Float,
    onMinimumWeightChange: (Float?) -> Unit,
    onAddClick: () -> Unit
) {
    val currentMinimumWeight = minimumWeight
    var weightToDelete by remember { mutableStateOf<WeightEntry?>(null) }
    
    if (weightToDelete != null) {
        AlertDialog(
            onDismissRequest = { weightToDelete = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this weight entry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        weightToDelete?.let { viewModel.deleteWeight(it) }
                        weightToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { weightToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Tracker") },
                actions = {
                    TextButton(onClick = onToggleGraph) {
                        Text(if (showGraph) "Show Table" else "Show Graph")
                    }
                }
            )
        },
        floatingActionButton = {
            if (showGraph) {
                // Zoom Controls (Only show when graph is visible)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            // Zoom In (Increase minimum towards data)
                            val dataMin = weightList.minOfOrNull { it.weight } ?: 0f
                            val newMin = (currentMinimumWeight + 10f).coerceAtMost(dataMin - 5f)
                            onMinimumWeightChange(newMin.coerceAtLeast(0f))
                        }
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            // Zoom Out (Decrease minimum towards 0)
                            val newMin = (currentMinimumWeight - 10f).coerceAtLeast(0f)
                            onMinimumWeightChange(if (newMin <= 0f) null else newMin)
                        }
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    FloatingActionButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Weight")
                    }
                }
            } else {
                FloatingActionButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Weight")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (showGraph) {
                // Show Graph
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (weightList.isNotEmpty()) {
                        WeightGraph(
                            entries = weightList,
                            minYValue = currentMinimumWeight
                        )
                    } else {
                        Text("No data to graph")
                    }
                }
    } else {
        // Show Table (List)
        WeightList(
            weights = weightList.sortedByDescending { it.date },
            onDelete = { weightToDelete = it }
        )
    }
        }
    }
}

@Composable
fun WeightList(
    weights: List<WeightEntry>,
    onDelete: (WeightEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Table Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Date", style = MaterialTheme.typography.titleMedium)
                Text("Weight (lbs)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(24.dp)) // For delete icon space
            }
            HorizontalDivider()
        }
        
        items(weights) { entry ->
            WeightRow(entry = entry, onDelete = onDelete)
            HorizontalDivider()
        }
    }
}

@Composable
fun WeightRow(
    entry: WeightEntry,
    onDelete: (WeightEntry) -> Unit
) {
    val dateString = DateFormat.format("yyyy-MM-dd h:mm a", Date(entry.date)).toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(dateString)
        Text("${entry.weight}")
        IconButton(onClick = { onDelete(entry) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    initialWeight: Float? = null,
    onSave: (Float, Long) -> Unit,
    onCancel: () -> Unit
) {
    var weightText by remember { mutableStateOf(initialWeight?.toString() ?: "") }
    var isError by remember { mutableStateOf(false) }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    val calendar = remember { Calendar.getInstance() }
    
    // Date & Time Picker Dialogs
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.timeInMillis = selectedTimestamp
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    selectedTimestamp = calendar.timeInMillis
                } else {
                    Toast.makeText(context, "Cannot select future time", Toast.LENGTH_SHORT).show()
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    val datePickerDialog = remember {
        val dpd = DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.timeInMillis = selectedTimestamp
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                
                // Check if the selected date is today
                val now = Calendar.getInstance()
                val isToday = now.get(Calendar.YEAR) == year &&
                              now.get(Calendar.MONTH) == month &&
                              now.get(Calendar.DAY_OF_MONTH) == day

                // When entering a past value, default to 00 for the minute
                if (!isToday) {
                    calendar.set(Calendar.MINUTE, 0)
                }
                
                // If selection is in future (e.g. kept same time but changed date to today where time is future), clamp it.
                selectedTimestamp = if (calendar.timeInMillis > System.currentTimeMillis()) {
                    System.currentTimeMillis()
                } else {
                    calendar.timeInMillis
                }
                
                // Update the TimePicker to match the adjusted time
                // We recreate a calendar from the final selectedTimestamp to ensure we show exactly what was saved
                val displayCalendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                timePickerDialog.updateTime(
                    displayCalendar.get(Calendar.HOUR_OF_DAY),
                    displayCalendar.get(Calendar.MINUTE)
                )
                
                // Show time picker after date is selected
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd
    }

    fun adjustWeight(amount: Float) {
        val currentWeight = weightText.toFloatOrNull() ?: 0f
        val newWeight = (currentWeight + amount).coerceAtLeast(0f)
        // Format to 1 decimal place to avoid floating point artifacts
        weightText = "%.1f".format(newWeight)
        isError = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Weight") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { 
                    weightText = it
                    isError = false
                },
                label = { Text("Weight (lbs)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isError,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val dateString = remember(selectedTimestamp) {
                DateFormat.format("yyyy-MM-dd h:mm a", Date(selectedTimestamp)).toString()
            }
            
            OutlinedTextField(
                value = dateString,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date & Time") },
                trailingIcon = { 
                    Icon(
                        Icons.Default.DateRange, 
                        contentDescription = "Select Date",
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                enabled = false, // Makes it look read-only but clickable via modifier? No, disabled makes text gray.
                // Better approach: readOnly = true, enabled = true.
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
            // Re-override enabled to true so clicks work properly on the field itself if modifiers are tricky
            // Actually simplest is readOnly = true and clickable modifier on the box.
            
            if (weightText.isNotEmpty() || initialWeight != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Quick Adjust", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Row for +/- 0.1
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { adjustWeight(-0.1f) }) {
                        Text("-0.1")
                    }
                    Button(onClick = { adjustWeight(0.1f) }) {
                        Text("+0.1")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Row for +/- 1.0
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { adjustWeight(-1.0f) }) {
                        Text("-1.0")
                    }
                    OutlinedButton(onClick = { adjustWeight(1.0f) }) {
                        Text("+1.0")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val weight = weightText.toFloatOrNull()
                    if (weight != null && weight > 0) {
                        onSave(weight, selectedTimestamp)
                    } else {
                        isError = true
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
}
