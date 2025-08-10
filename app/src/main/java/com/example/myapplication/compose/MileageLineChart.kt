import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.MileageEntry
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

// Assuming MileageEntry is defined as:
// data class MileageEntry(val id: Int = 0, val miles: Int, val date: Date)

@Composable
fun MileageLineChart(
    entries: List<MileageEntry>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = Color.Gray,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showDataPoints: Boolean = true
) {
  if (entries.isEmpty()) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
      Text("No mileage data to display.", color = textColor)
    }
    return
  }

  // Sort entries by date
  val sortedEntries = entries.sortedBy { it.date }
  val firstEntryDate = sortedEntries.first().date
  val lastEntryDate = sortedEntries.last().date

  val minMiles = sortedEntries.minOfOrNull { it.miles } ?: 0
  val maxMiles = sortedEntries.maxOfOrNull { it.miles } ?: 0
  val milesRange = (maxMiles - minMiles).toFloat()

  // Date formatting for X-axis labels
  val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault()) // e.g., "Jul 4"

  val density = LocalDensity.current
  val textPaint = remember {
    Paint().asFrameworkPaint().apply {
      isAntiAlias = true
      textAlign = android.graphics.Paint.Align.CENTER
      textSize = with(density) { 12.sp.toPx() }
      color = textColor.hashCode() // Need to convert Compose Color to Android Color int
    }
  }
  val textPaintYAxis = remember {
    Paint().asFrameworkPaint().apply {
      isAntiAlias = true
      textAlign = android.graphics.Paint.Align.RIGHT
      textSize = with(density) { 12.sp.toPx() }
      color = textColor.hashCode()
    }
  }

  Canvas(
      modifier =
          modifier
              .fillMaxSize()
              .padding(
                  start = 48.dp, // Space for Y-axis labels
                  bottom = 40.dp, // Space for X-axis labels
                  end = 16.dp, // Padding on the right
                  top = 16.dp // Padding on the top
                  )) {
        val chartWidth = size.width
        val chartHeight = size.height

        // --- Draw Y-axis (Miles) ---
        drawLine(
            color = axisColor,
            start = Offset(0f, 0f),
            end = Offset(0f, chartHeight),
            strokeWidth = 2f)

        // Y-axis labels and grid lines
        val numYLabels = 5 // Adjust as needed
        val yLabelInterval = if (milesRange > 0) milesRange / (numYLabels - 1) else 1f
        for (i in 0 until numYLabels) {
          val yValue = minMiles + (i * yLabelInterval)
          val yPos =
              chartHeight -
                  ((yValue - minMiles) / (if (milesRange > 0) milesRange else 1f)) * chartHeight

          // Grid line
          drawLine(
              color = axisColor.copy(alpha = 0.3f),
              start = Offset(0f, yPos),
              end = Offset(chartWidth, yPos),
              strokeWidth = 1f)
          // Y-axis label
          drawContext.canvas.nativeCanvas.drawText(
              yValue.roundToInt().toString(),
              -8.dp.toPx(), // Offset to the left of the axis
              yPos + textPaintYAxis.textSize / 3, // Center text vertically
              textPaintYAxis)
        }
        // Y-axis Title (Optional)
        // You might need more advanced text rotation for a vertical title
        // drawContext.canvas.nativeCanvas.drawText("Miles", -35.dp.toPx(), chartHeight / 2,
        // textPaintYAxis)

        // --- Draw X-axis (Date) ---
        drawLine(
            color = axisColor,
            start = Offset(0f, chartHeight),
            end = Offset(chartWidth, chartHeight),
            strokeWidth = 2f)

        // X-axis labels and grid lines (Simplified for dates)
        // For dates, precise equidistant points are more complex.
        // We'll place labels for the first, last, and potentially some mid-points if space allows.
        val dateRangeMillis = (lastEntryDate.time - firstEntryDate.time).toFloat()

        val xPositions =
            sortedEntries.map { entry ->
              if (dateRangeMillis > 0) {
                ((entry.date.time - firstEntryDate.time) / dateRangeMillis) * chartWidth
              } else {
                chartWidth / 2f // Center if only one date or all dates are the same
              }
            }

        // Draw labels for some key dates
        val labelIndices = mutableListOf<Int>()
        if (sortedEntries.isNotEmpty()) labelIndices.add(0) // First entry
        if (sortedEntries.size > 2) labelIndices.add(sortedEntries.size / 2) // Middle entry
        if (sortedEntries.size > 1) labelIndices.add(sortedEntries.size - 1) // Last entry

        labelIndices.distinct().forEach { index ->
          val entry = sortedEntries[index]
          val xPos = xPositions[index]
          val dateLabel = dateFormat.format(entry.date)
          drawContext.canvas.nativeCanvas.drawText(
              dateLabel,
              xPos,
              chartHeight + 20.dp.toPx(), // Offset below the axis
              textPaint)
        }
        // X-axis Title (Optional)
        // drawContext.canvas.nativeCanvas.drawText("Date", chartWidth / 2, chartHeight +
        // 35.dp.toPx(), textPaint)

        // --- Draw data line and points ---
        if (sortedEntries.size > 1) {
          val linePath = Path()
          sortedEntries.forEachIndexed { index, entry ->
            val x = xPositions[index]
            val y =
                if (milesRange > 0) {
                  chartHeight - ((entry.miles - minMiles) / milesRange) * chartHeight
                } else {
                  chartHeight / 2f // Center vertically if miles range is zero
                }

            if (index == 0) {
              linePath.moveTo(x, y)
            } else {
              linePath.lineTo(x, y)
            }
          }
          drawPath(path = linePath, color = lineColor, style = Stroke(width = 3.dp.toPx()))
        }

        // Draw data points (optional)
        if (showDataPoints) {
          sortedEntries.forEachIndexed { index, entry ->
            val x = xPositions[index]
            val y =
                if (milesRange > 0) {
                  chartHeight - ((entry.miles - minMiles) / milesRange) * chartHeight
                } else {
                  chartHeight / 2f
                }
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
          }
        }
      }
}
