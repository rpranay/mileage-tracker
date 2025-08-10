package com.example.myapplication.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.MileageEntry
import com.example.myapplication.viewmodel.MileageListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddMileageEntryCard(
    mileageEntries: State<List<MileageEntry>>,
    maxEntries: Int,
    viewModel: MileageListViewModel
) {
  var milesText by remember { mutableStateOf("") }
  var dateText by remember {
    mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
  }
  Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header with entry count
      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Add New Entry", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Entries: ${mileageEntries.value.size}/$maxEntries", fontSize = 14.sp)
          }

      // Miles input
      OutlinedTextField(
          value = milesText,
          onValueChange = { milesText = it },
          label = { Text("Miles") },
          placeholder = { Text("Enter miles (e.g., 25000)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
          singleLine = true)

      // Date input
      OutlinedTextField(
          value = dateText,
          onValueChange = { dateText = it },
          label = { Text("Date") },
          placeholder = { Text("YYYY-MM-DD") },
          modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
          singleLine = true)

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
                })
          },
          modifier = Modifier.fillMaxWidth(),
      ) {
        Text("Add Entry")
      }
    }
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

    val entry = MileageEntry(0, miles, date)
    //        val newEntries = (currentEntries + entry).sortedBy { it.date }

    onSuccess(entry, "Entry added successfully")
  } catch (e: Exception) {
    onError("Invalid input format")
  }
}
