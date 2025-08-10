package com.example.myapplication.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.compose.AddMileageEntryCard
import com.example.myapplication.compose.AddMileageEntryDialog
import com.example.myapplication.compose.MileageLineChartWithMP
import com.example.myapplication.data.MileageEntry
import com.example.myapplication.imageParser.ImageParser
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.viewmodel.MileageListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewModel = MileageListViewModel(this)

    setContent {
      AppTheme {
        enableEdgeToEdge()
        // State to hold the selected image URI
        val context = LocalContext.current

        // State for dialog visibility
        var showMileageDialog by remember { mutableStateOf(false) }
        // State to hold the extracted mileage entry data for the dialog
        var extractedEntryForDialog by remember { mutableStateOf<MileageEntry?>(null) }
        // ActivityResultLauncher for picking a single image
        val singlePhotoPickerLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = { uri ->
                  uri?.let {
                    ImageParser(uri, context).apply {
                      initBitmap()
                      processImageForMileageEntry(
                          onSuccess = {
                            extractedEntryForDialog = it
                            showMileageDialog = true // Show the dialog
                          },
                          onError = {
                            Toast.makeText(this@MainActivity, "Error $it", Toast.LENGTH_LONG).show()
                          })
                    }
                  }
                })

        if (showMileageDialog) {
          extractedEntryForDialog?.also {
            AddMileageEntryDialog(
                entry = it,
                onConfirm = {
                  viewModel.addMileageEntry(it)
                  showMileageDialog = false
                },
                onDismiss = { showMileageDialog = false })
          }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
            floatingActionButton = {
              FloatingActionButton(
                  onClick = {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                  }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Entry")
                  }
            }) { innerPadding ->
              MileageTrackerApp(viewModel, Modifier.padding(innerPadding))
            }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MileageTrackerApp(viewModel: MileageListViewModel, modifier: Modifier = Modifier) {
  val mileageEntries = viewModel.allMileageEntries.collectAsStateWithLifecycle()

  Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      item("addMileageEntry") { AddMileageEntryCard(mileageEntries, 1000, viewModel) }
      item("chart") {
        Card(
            modifier =
                Modifier.fillMaxWidth()
                    .height(250.dp) // Increased height for better readability
                    .padding(bottom = 16.dp)) {
              if (mileageEntries.value.isNotEmpty()) {
                MileageLineChartWithMP( // Use the new chart
                    entries = mileageEntries.value, modifier = Modifier.fillMaxSize())
              } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  Text("Add entries to see chart", fontSize = 16.sp)
                }
              }
            }
      }
      item("entriesHeader") {
        Text(
            text = "Recent Entries",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp))
      }
      items(mileageEntries.value.sortedByDescending { it.date }) { entry ->
        MileageEntryItem(entry = entry, onDelete = { viewModel.deleteMileageEntry(entry) })
      }
    }
  }
}

@Composable
fun MileageEntryItem(entry: MileageEntry, onDelete: () -> Unit) {
  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

  Card(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "${entry.miles} miles", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = dateFormat.format(entry.date), fontSize = 14.sp)
          }

          IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete entry",
                tint = MaterialTheme.colorScheme.error)
          }
        }
  }
}
