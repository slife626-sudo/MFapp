package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrencyFormat
import com.example.data.model.DateFormatUtils
import com.example.ui.theme.HisabMonoFontFamily
import com.example.viewmodel.CategoryBreakdown
import kotlin.math.atan2
import kotlin.math.hypot

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
  breakdowns: List<CategoryBreakdown>,
  totalSpent: Double,
  currencySymbol: String,
  totalLabel: String,
  modifier: Modifier = Modifier,
) {
  var selectedIndex by remember(breakdowns) { mutableIntStateOf(-1) }
  var played by remember { mutableStateOf(false) }
  val progress by animateFloatAsState(
    targetValue = if (played) 1f else 0f,
    animationSpec = tween(durationMillis = 650),
    label = "donutAnim",
  )
  LaunchedEffect(breakdowns) { played = true }

  val selectedItem = breakdowns.getOrNull(selectedIndex)
  val surfaceBg = MaterialTheme.colorScheme.surfaceContainerLow

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier.size(210.dp),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(
        modifier = Modifier
          .size(200.dp)
          .pointerInput(breakdowns) {
            detectTapGestures { offset ->
              val cx = size.width / 2f
              val cy = size.height / 2f
              val dx = offset.x - cx
              val dy = offset.y - cy
              val dist = hypot(dx, dy)
              val outer = minOf(size.width, size.height) / 2f
              if (dist in (outer * 0.42f)..outer) {
                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                if (angle < 0f) angle += 360f
                var accum = 0f
                breakdowns.forEachIndexed { idx, item ->
                  val sweep = item.percentage * 360f
                  if (angle in accum..(accum + sweep)) {
                    selectedIndex = if (selectedIndex == idx) -1 else idx
                    return@detectTapGestures
                  }
                  accum += sweep
                }
              }
            }
          },
      ) {
        val strokeWidth = 34.dp.toPx()
        val pad = strokeWidth / 2f + 6.dp.toPx()
        val arcSize = Size(size.width - pad * 2f, size.height - pad * 2f)
        val topLeft = Offset(pad, pad)

        if (breakdowns.isEmpty() || totalSpent <= 0.0) {
          drawArc(
            color = surfaceBg,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
          )
        } else {
          var startAngle = -90f
          breakdowns.forEachIndexed { idx, item ->
            val sweep = (item.percentage * 360f) * progress
            val isHighlighted = selectedIndex == idx
            val extraStroke = if (isHighlighted) 8.dp.toPx() else 0f
            drawArc(
              color = Color(item.category.colorHex),
              startAngle = startAngle,
              sweepAngle = (sweep - 2f).coerceAtLeast(1f),
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeWidth + extraStroke, cap = StrokeCap.Butt),
            )
            startAngle += sweep
          }
        }
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp),
      ) {
        Text(
          text = selectedItem?.category?.name ?: totalLabel,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
          text = CurrencyFormat.format(selectedItem?.totalAmount ?: totalSpent, currencySymbol),
          fontFamily = HisabMonoFontFamily,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        if (selectedItem != null) {
          Text(
            text = "${(selectedItem.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = Color(selectedItem.category.colorHex),
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }

    Spacer(Modifier.height(14.dp))

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      breakdowns.forEachIndexed { idx, item ->
        val selected = idx == selectedIndex
        Surface(
          shape = RoundedCornerShape(50),
          color = if (selected) {
            Color(item.category.colorHex).copy(alpha = 0.18f)
          } else {
            MaterialTheme.colorScheme.surfaceContainerLow
          },
          modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { selectedIndex = if (selected) -1 else idx },
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(item.category.colorHex))
            )
            Spacer(Modifier.width(6.dp))
            Text(
              text = "${item.category.name} ${(item.percentage * 100).toInt()}%",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
    }
  }
}

@Composable
fun DailySpendingBarChart(
  monthKey: String,
  dailyTotals: Map<Int, Double>,
  budgetLimit: Double,
  currencySymbol: String,
  tapHint: String,
  modifier: Modifier = Modifier,
) {
  val daysInMonth = DateFormatUtils.daysInMonth(monthKey)
  var selectedDay by remember(monthKey) { mutableIntStateOf(-1) }
  val maxDaily = remember(dailyTotals, budgetLimit, daysInMonth) {
    val maxExpense = dailyTotals.values.maxOrNull() ?: 0.0
    val dailyPace = if (budgetLimit > 0) budgetLimit / daysInMonth else 0.0
    maxOf(maxExpense, dailyPace * 1.2, 1.0)
  }
  val barColor = MaterialTheme.colorScheme.primary
  val activeBarColor = MaterialTheme.colorScheme.tertiary
  val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
  val guideColor = MaterialTheme.colorScheme.error.copy(alpha = 0.65f)

  Column(modifier = modifier.fillMaxWidth()) {
    if (selectedDay in 1..daysInMonth) {
      val amt = dailyTotals[selectedDay] ?: 0.0
      Text(
        text = "${monthKey}-${selectedDay.toString().padStart(2, '0')}: ${CurrencyFormat.format(amt, currencySymbol)}",
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
      )
    } else {
      Text(
        text = tapHint,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
      )
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(170.dp)
        .pointerInput(monthKey, daysInMonth) {
          detectTapGestures { offset ->
            val slotW = size.width / daysInMonth.toFloat()
            val tapped = (offset.x / slotW).toInt() + 1
            if (tapped in 1..daysInMonth) {
              selectedDay = if (selectedDay == tapped) -1 else tapped
            }
          }
        },
    ) {
      val slotW = size.width / daysInMonth.toFloat()
      val barW = (slotW * 0.62f).coerceAtLeast(3.dp.toPx())
      val chartH = size.height - 20.dp.toPx()

      for (day in 1..daysInMonth) {
        val x = (day - 1) * slotW + (slotW - barW) / 2f
        // Background track
        drawRoundRect(
          color = trackColor,
          topLeft = Offset(x, 0f),
          size = Size(barW, chartH),
          cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        )
        val value = dailyTotals[day] ?: 0.0
        if (value > 0.0) {
          val ratio = (value / maxDaily).toFloat().coerceIn(0.05f, 1f)
          val barH = chartH * ratio
          drawRoundRect(
            color = if (day == selectedDay) activeBarColor else barColor,
            topLeft = Offset(x, chartH - barH),
            size = Size(barW, barH),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
          )
        }
      }

      // Daily budget pace dashed line
      if (budgetLimit > 0.0) {
        val pace = budgetLimit / daysInMonth
        val y = chartH - (chartH * (pace / maxDaily).toFloat().coerceIn(0f, 1f))
        drawLine(
          color = guideColor,
          start = Offset(0f, y),
          end = Offset(size.width, y),
          strokeWidth = 1.5.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      listOf(1, 8, 15, 22, daysInMonth).forEach { d ->
        Text(
          text = d.toString(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun SixMonthComparisonChart(
  sixMonthTotals: List<Pair<String, Double>>,
  currencySymbol: String,
  modifier: Modifier = Modifier,
) {
  var selectedIdx by remember(sixMonthTotals) { mutableIntStateOf(sixMonthTotals.lastIndex) }
  val maxVal = remember(sixMonthTotals) {
    (sixMonthTotals.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
  }
  val primary = MaterialTheme.colorScheme.primary
  val secondary = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
  val track = MaterialTheme.colorScheme.surfaceContainerHigh

  Column(modifier = modifier.fillMaxWidth()) {
    val active = sixMonthTotals.getOrNull(selectedIdx)
    if (active != null) {
      Text(
        text = "${active.first}: ${CurrencyFormat.format(active.second, currencySymbol)}",
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 10.dp),
      )
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .pointerInput(sixMonthTotals) {
          detectTapGestures { offset ->
            if (sixMonthTotals.isEmpty()) return@detectTapGestures
            val slotW = size.width / sixMonthTotals.size.toFloat()
            val idx = (offset.x / slotW).toInt().coerceIn(0, sixMonthTotals.lastIndex)
            selectedIdx = idx
          }
        },
    ) {
      if (sixMonthTotals.isEmpty()) return@Canvas
      val slotW = size.width / sixMonthTotals.size.toFloat()
      val barW = (slotW * 0.48f).coerceAtMost(38.dp.toPx())
      val chartH = size.height

      sixMonthTotals.forEachIndexed { idx, (_, amount) ->
        val x = idx * slotW + (slotW - barW) / 2f
        drawRoundRect(
          color = track,
          topLeft = Offset(x, 0f),
          size = Size(barW, chartH),
          cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        )
        if (amount > 0.0) {
          val h = chartH * (amount / maxVal).toFloat().coerceIn(0.08f, 1f)
          drawRoundRect(
            color = if (idx == selectedIdx) primary else secondary,
            topLeft = Offset(x, chartH - h),
            size = Size(barW, h),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
          )
        }
      }
    }

    Spacer(Modifier.height(6.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
      sixMonthTotals.forEachIndexed { idx, (label, _) ->
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
            color = if (idx == selectedIdx) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          )
        }
      }
    }
  }
}
