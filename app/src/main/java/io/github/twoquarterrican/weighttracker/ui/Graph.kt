package io.github.twoquarterrican.weighttracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.twoquarterrican.weighttracker.data.WeightEntry

import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import android.text.format.DateFormat
import java.util.Date

@Composable
fun WeightGraph(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    minYValue: Float = 0f
) {
    if (entries.isEmpty()) return

    // Sort by date just in case
    val sortedEntries = remember(entries) { entries.sortedBy { it.date } }
    
    val weights = sortedEntries.map { it.weight }
    // Ensure 0 is the baseline
    val minWeight = minYValue
    val maxDataWeight = weights.maxOrNull() ?: 100f
    val maxWeight = if (maxDataWeight > 0) maxDataWeight + 1f else 100f
    
    val yRange = maxWeight - minWeight
    
    // Time Axis Calculations
    val firstDate = sortedEntries.first().date
    val lastDate = sortedEntries.last().date
    val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L
    
    // Ensure at least 1 week is shown on X-axis
    val maxDate = if ((lastDate - firstDate) < oneWeekMillis) {
        firstDate + oneWeekMillis
    } else {
        lastDate
    }
    
    val timeRange = (maxDate - firstDate).coerceAtLeast(1)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        
        // Draw Axes
        // Y-Axis (Left)
        drawLine(
            color = textColor,
            start = Offset(0f, 0f),
            end = Offset(0f, height),
            strokeWidth = 2.dp.toPx()
        )
        // X-Axis (Bottom)
        drawLine(
            color = textColor,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 2.dp.toPx()
        )

        // Draw Labels using native canvas
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = textColor.toArgb()
                textSize = 32f
                textAlign = Paint.Align.LEFT
            }
            // Y-Axis Labels
            // Label for 0 (or Min)
            canvas.nativeCanvas.drawText("${minWeight.toInt()}", 10f, height - 10f, paint)
            // Label for Max
            canvas.nativeCanvas.drawText("${maxWeight.toInt()}", 10f, 40f, paint)
            
            // X-Axis Labels (Dates)
            val datePaint = Paint().apply {
                color = textColor.toArgb()
                textSize = 32f
                textAlign = Paint.Align.CENTER
            }
            
            // Start Date
            val startDateStr = DateFormat.format("M/d", Date(firstDate)).toString()
            canvas.nativeCanvas.drawText(startDateStr, 30f, height + 40f, datePaint)
            
            // End Date
            val endDateStr = DateFormat.format("M/d", Date(maxDate)).toString()
            canvas.nativeCanvas.drawText(endDateStr, width - 30f, height + 40f, datePaint)
            
            // Optional: Middle Date (only if wide enough)
            if (width > 600) {
                 val midDate = firstDate + (timeRange / 2)
                 val midDateStr = DateFormat.format("M/d", Date(midDate)).toString()
                 canvas.nativeCanvas.drawText(midDateStr, width / 2, height + 40f, datePaint)
            }
        }
        
        val points = sortedEntries.map { entry ->
            // Scale X based on Time
            val x = ((entry.date - firstDate).toFloat() / timeRange) * width
            
            // Invert Y because canvas 0 is at top
            // Scale: (weight - 0) / range * height
            val y = height - ((entry.weight - minWeight) / yRange * height)
            Offset(x, y)
        }

        if (points.isNotEmpty()) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    center = point,
                    radius = 6.dp.toPx()
                )
            }
        }
    }
}
