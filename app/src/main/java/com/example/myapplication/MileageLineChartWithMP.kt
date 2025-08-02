package com.example.myapplication // Your package name

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Assuming MileageEntry is:
// data class MileageEntry(val id: Int = 0, val miles: Int, val date: Date)

@Composable
fun MileageLineChartWithMP(
    entries: List<MileageEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        // You can keep your existing placeholder Box here if you want
        // Or let the caller handle the empty state
        return
    }

    val sortedEntries = remember(entries) { entries.sortedBy { it.date } }
    val firstDateTimestamp = remember(sortedEntries) {
        sortedEntries.firstOrNull()?.date?.time ?: 0L
    }

    // Convert your MileageEntry data to MPAndroidChart's Entry format
    val chartEntries = remember(sortedEntries, firstDateTimestamp) {
        sortedEntries.mapIndexed { _, mileageEntry ->
            // X value: time in days from the first entry (or any consistent unit)
            // MPAndroidChart works best with float values for x.
            // Using milliseconds directly can make the chart too wide or hard to manage.
            // Converting to days since the first entry can be a good approach.
            val timeDiffMillis = mileageEntry.date.time - firstDateTimestamp
            val daysSinceFirst = TimeUnit.MILLISECONDS.toDays(timeDiffMillis).toFloat()

            Entry(daysSinceFirst, mileageEntry.miles.toFloat())
        }
    }

    // Use AndroidView to embed the MPAndroidChart LineChart
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                // Basic chart setup
                description.isEnabled = false
                setDrawGridBackground(false)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                // X-Axis setup
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f // Show a label for each day (adjust as needed)
                    setDrawGridLines(true)
                    valueFormatter = object : ValueFormatter() {
                        private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            // Convert 'value' (daysSinceFirst) back to a date
                            val millis = firstDateTimestamp + TimeUnit.DAYS.toMillis(value.toLong())
                            return dateFormat.format(Date(millis))
                        }
                    }
                }

                // Y-Axis (Left) setup
                axisLeft.apply {
                    setDrawGridLines(true)
                    // You can set axisMinimum, axisMaximum if needed
                }

                // Y-Axis (Right) setup - disable if not needed
                axisRight.isEnabled = false

                // Legend setup
                legend.isEnabled = true // Or false if you don't need it
            }
        },
        update = { lineChart ->
            // This block is called when 'entries' (and thus 'chartEntries') changes
            val lineDataSet = LineDataSet(chartEntries, "Mileage").apply {
                color = android.graphics.Color.BLUE // Use android.graphics.Color
                valueTextColor = android.graphics.Color.BLACK
                setDrawCircles(true)
                setCircleColor(android.graphics.Color.BLUE)
                circleRadius = 4f
                setDrawValues(false) // Don't draw mileage value on top of each point
            }

            lineChart.data = LineData(lineDataSet)
            lineChart.invalidate() // Refresh the chart
        },
        modifier = modifier
    )
}

