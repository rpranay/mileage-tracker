package com.example.myapplication

import MileageLineChart
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        val viewModel = MileageListViewModel(this)

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    MileageTrackerApp(viewModel)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MileageTrackerApp(viewModel: MileageListViewModel) {
//    var mileageEntries by remember { mutableStateOf(listOf<MileageEntry>()) }
    var mileageEntries = viewModel.allMileageEntries.collectAsStateWithLifecycle()
    var milesText by remember { mutableStateOf("") }
    var dateText by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    val maxEntries = 1000
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Car Mileage Tracker",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Input Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header with entry count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Entry",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Entries: ${mileageEntries.value.size}/$maxEntries",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Miles input
                OutlinedTextField(
                    value = milesText,
                    onValueChange = { milesText = it },
                    label = { Text("Miles") },
                    placeholder = { Text("Enter miles (e.g., 25000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )

                // Date input
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true
                )

                // Add button
                Button(
                    onClick = {
                        addMileageEntry(
                            milesText = milesText,
                            dateText = dateText,
                            currentEntries = mileageEntries.value,
                            maxEntries = maxEntries,
                            onSuccess = { newEntries, message ->
                                milesText = ""
                                viewModel.addMileageEntry(newEntries)
                                // Show success message (you can implement SnackBar here)
                            },
                            onError = { errorMessage ->
                                // Show error message (you can implement SnackBar here)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Add Entry", color = Color.White)
                }
            }
        }

        // Chart Section
       /* Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            if (mileageEntries.value.isNotEmpty()) {
                MileageChart(
                    entries = mileageEntries.value,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Add entries to see chart",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }*/
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // Increased height for better readability
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            if (mileageEntries.value.isNotEmpty()) {
                MileageLineChartWithMP( // Use the new chart
                    entries = mileageEntries.value,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Add entries to see chart",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Entries List Header
        Text(
            text = "Recent Entries",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Entries List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(mileageEntries.value.sortedByDescending { it.date }) { entry ->
                MileageEntryItem(
                    entry = entry,
                    onDelete = {
                        viewModel.deleteMileageEntry(entry)
                    }
                )
            }
        }
    }
}

@Composable
fun MileageEntryItem(
    entry: MileageEntry,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${entry.miles} miles",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(entry.date),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete entry",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

@Composable
fun MileageChart(
    entries: List<MileageEntry>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (entries.isEmpty()) return@Canvas

        val sortedEntries = entries.sortedBy { it.date }
        val minMiles = sortedEntries.minOf { it.miles }
        val maxMiles = sortedEntries.maxOf { it.miles }
        val mileRange = maxMiles - minMiles

        if (mileRange == 0) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 40f

        // Draw axes
        drawLine(
            color = Color.Gray,
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Gray,
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2f
        )

        // Draw data points and line
        val path = Path()
        var isFirst = true

        sortedEntries.forEachIndexed { index, entry ->
            val x = padding + (index.toFloat() / (sortedEntries.size - 1)) * (width - 2 * padding)
            val y = height - padding - ((entry.miles - minMiles).toFloat() / mileRange) * (height - 2 * padding)

            if (isFirst) {
                path.moveTo(x, y)
                isFirst = false
            } else {
                path.lineTo(x, y)
            }

            // Draw data point
            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        // Draw line
        drawPath(
            path = path,
            color = primaryColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }
}


private fun addMileageEntry(
    milesText: String,
    dateText: String,
    currentEntries: List<MileageEntry>,
    maxEntries: Int,
    onSuccess: (MileageEntry, String) -> Unit,
    onError: (String) -> Unit
) {
    if (milesText.isEmpty() || dateText.isEmpty()) {
        onError("Please enter both miles and date")
        return
    }

    if (currentEntries.size >= maxEntries) {
        onError("Maximum $maxEntries entries allowed")
        return
    }

    try {
        val miles = milesText.toInt()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateText) ?: Date()

        // Check for duplicate dates
        if (currentEntries.any {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date) == dateText
            }) {
            onError("Entry for this date already exists")
            return
        }

        val entry = MileageEntry(0 ,miles, date)
//        val newEntries = (currentEntries + entry).sortedBy { it.date }

        onSuccess(entry, "Entry added successfully")

    } catch (e: Exception) {
        onError("Invalid input format")
    }
}
// Chart Formatter
//class DateValueFormatter : ValueFormatter() {
//    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
//
//    override fun getFormattedValue(value: Float): String {
//        return dateFormat.format(Date(value.toLong()))
//    }
//}
