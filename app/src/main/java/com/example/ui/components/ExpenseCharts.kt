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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrencyFormat
import com.example.data.model.DateFormatUtils
import com.example.ui.theme.HisabMonoFontFamily
import com.example.ui.theme.SemanticDangerLight
import com.example.ui.theme.SemanticSafeLight
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
fun NetCashFlowAreaChart(
  points: List<Pair<String, Double>>,
  currencySymbol: String,
  modifier: Modifier = Modifier,
) {
  val lineColor = MaterialTheme.colorScheme.primary
  val zeroLineColor = MaterialTheme.colorScheme.outlineVariant
  val minVal = remember(points) { minOf(0.0, points.minOfOrNull { it.second } ?: 0.0) }
  val maxVal = remember(points) { maxOf(0.0, points.maxOfOrNull { it.second } ?: 0.0) }
  val span = (maxVal - minVal).coerceAtLeast(1.0)
  var selectedIdx by remember(points) { mutableIntStateOf(-1) }

  Column(modifier = modifier.fillMaxWidth()) {
    val active = points.getOrNull(selectedIdx)
    if (active != null) {
      Text(
        text = "${active.first}: ${CurrencyFormat.format(active.second, currencySymbol)}",
        fontFamily = HisabMonoFontFamily,
        style = MaterialTheme.typography.labelMedium,
        color = if (active.second >= 0.0) SemanticSafeLight else SemanticDangerLight,
        modifier = Modifier.padding(bottom = 6.dp),
      )
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .pointerInput(points) {
          detectTapGestures { offset ->
            if (points.isEmpty()) return@detectTapGestures
            val slotW = size.width / points.size.toFloat()
            val idx = (offset.x / slotW).toInt().coerceIn(0, points.lastIndex)
            selectedIdx = if (selectedIdx == idx) -1 else idx
          }
        },
    ) {
      if (points.isEmpty()) return@Canvas
      val padTop = 10.dp.toPx()
      val padBottom = 10.dp.toPx()
      val chartH = (size.height - padTop - padBottom).coerceAtLeast(10f)
      val zeroY = padTop + chartH * (maxVal / span).toFloat()

      drawLine(
        color = zeroLineColor,
        start = Offset(0f, zeroY),
        end = Offset(size.width, zeroY),
        strokeWidth = 1.dp.toPx(),
      )

      val stepX = if (points.size <= 1) size.width else size.width / (points.size - 1).toFloat()
      val coords = points.mapIndexed { idx, (_, value) ->
        val x = idx * stepX
        val y = padTop + chartH * ((maxVal - value) / span).toFloat()
        Offset(x, y)
      }

      val linePath = Path().apply {
        coords.forEachIndexed { idx, pt ->
          if (idx == 0) moveTo(pt.x, pt.y)
          else {
            val prev = coords[idx - 1]
            val cx = (prev.x + pt.x) / 2f
            cubicTo(cx, prev.y, cx, pt.y, pt.x, pt.y)
          }
        }
      }

      val areaPath = Path().apply {
        addPath(linePath)
        if (coords.isNotEmpty()) {
          lineTo(coords.last().x, size.height)
          lineTo(coords.first().x, size.height)
          close()
        }
      }

      drawPath(
        path = areaPath,
        brush = Brush.verticalGradient(
          colors = listOf(
            lineColor.copy(alpha = 0.25f),
            lineColor.copy(alpha = 0.02f),
          ),
        ),
      )

      drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
      )

      if (selectedIdx in coords.indices) {
        val pt = coords[selectedIdx]
        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = pt)
        drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)
      }
    }
  }
}

@Composable
fun IncomeVsExpenseChart(
  sixMonthData: List<Triple<String, Double, Double>>,
  currencySymbol: String,
  showLineChart: Boolean = false,
  modifier: Modifier = Modifier,
) {
  var selectedIdx by remember(sixMonthData) { mutableIntStateOf(sixMonthData.lastIndex) }
  val maxVal = remember(sixMonthData) {
    (sixMonthData.maxOfOrNull { maxOf(it.second, it.third) } ?: 0.0).coerceAtLeast(1.0)
  }
  val incomeColor = SemanticSafeLight
  val expenseColor = SemanticDangerLight
  val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

  Column(modifier = modifier.fillMaxWidth()) {
    val active = sixMonthData.getOrNull(selectedIdx)
    if (active != null) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = active.first,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "+${CurrencyFormat.format(active.second, currencySymbol)}",
          fontFamily = HisabMonoFontFamily,
          style = MaterialTheme.typography.labelMedium,
          color = incomeColor,
        )
        Text(
          text = "-${CurrencyFormat.format(active.third, currencySymbol)}",
          fontFamily = HisabMonoFontFamily,
          style = MaterialTheme.typography.labelMedium,
          color = expenseColor,
        )
      }
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(155.dp)
        .pointerInput(sixMonthData) {
          detectTapGestures { offset ->
            if (sixMonthData.isEmpty()) return@detectTapGestures
            val slotW = size.width / sixMonthData.size.toFloat()
            selectedIdx = (offset.x / slotW).toInt().coerceIn(0, sixMonthData.lastIndex)
          }
        },
    ) {
      if (sixMonthData.isEmpty()) return@Canvas
      val chartH = size.height - 8.dp.toPx()
      for (i in 0..3) {
        val y = chartH * (i / 3f)
        drawLine(
          color = gridColor,
          start = Offset(0f, y),
          end = Offset(size.width, y),
          strokeWidth = 1.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
        )
      }

      val slotW = size.width / sixMonthData.size.toFloat()
      if (!showLineChart) {
        val barW = (slotW * 0.24f).coerceAtMost(16.dp.toPx())
        val gap = 4.dp.toPx()
        sixMonthData.forEachIndexed { idx, (_, inc, exp) ->
          val centerX = idx * slotW + slotW / 2f
          if (inc > 0.0) {
            val h = chartH * (inc / maxVal).toFloat().coerceIn(0.04f, 1f)
            drawRoundRect(
              color = incomeColor,
              topLeft = Offset(centerX - barW - gap / 2f, chartH - h),
              size = Size(barW, h),
              cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            )
          }
          if (exp > 0.0) {
            val h = chartH * (exp / maxVal).toFloat().coerceIn(0.04f, 1f)
            drawRoundRect(
              color = expenseColor,
              topLeft = Offset(centerX + gap / 2f, chartH - h),
              size = Size(barW, h),
              cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            )
          }
        }
      } else {
        val incPts = sixMonthData.mapIndexed { idx, (_, inc, _) ->
          Offset(idx * slotW + slotW / 2f, chartH - chartH * (inc / maxVal).toFloat().coerceIn(0f, 1f))
        }
        val expPts = sixMonthData.mapIndexed { idx, (_, _, exp) ->
          Offset(idx * slotW + slotW / 2f, chartH - chartH * (exp / maxVal).toFloat().coerceIn(0f, 1f))
        }
        fun drawLineSeries(pts: List<Offset>, col: Color) {
          val p = Path().apply {
            pts.forEachIndexed { i, pt ->
              if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
          }
          drawPath(p, color = col, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
          pts.forEachIndexed { idx, pt ->
            drawCircle(color = col, radius = if (idx == selectedIdx) 5.dp.toPx() else 3.dp.toPx(), center = pt)
          }
        }
        drawLineSeries(incPts, incomeColor)
        drawLineSeries(expPts, expenseColor)
      }
    }

    Spacer(Modifier.height(6.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
      sixMonthData.forEachIndexed { idx, (label, _, _) ->
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
            color = if (idx == selectedIdx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
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

@Composable
fun NetCashFlowAreaChart(
  series: List<Pair<String, Double>>,
  currencySymbol: String,
  surplusLabel: String,
  deficitLabel: String,
  blurBalances: Boolean = false,
  modifier: Modifier = Modifier,
) {
  var selectedIdx by remember(series) { mutableIntStateOf(series.lastIndex.coerceAtLeast(0)) }
  val active = series.getOrNull(selectedIdx)
  val maxAbs = remember(series) {
    (series.maxOfOrNull { kotlin.math.abs(it.second) } ?: 0.0).coerceAtLeast(1.0)
  }
  val hasPositive = series.any { it.second > 0.0 }
  val hasNegative = series.any { it.second < 0.0 }

  val safeColor = com.example.ui.theme.SemanticSafeLight
  val dangerColor = com.example.ui.theme.SemanticDangerLight
  val lineStrokeColor = if ((active?.second ?: 0.0) >= 0.0) safeColor else dangerColor
  val guideColor = MaterialTheme.colorScheme.outlineVariant

  Column(modifier = modifier.fillMaxWidth()) {
    if (active != null) {
      val isSurplus = active.second >= 0.0
      Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SensitiveAmountText(
          text = "${active.first}: ${CurrencyFormat.formatSigned(active.second, currencySymbol)}",
          blurBalances = blurBalances,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = if (isSurplus) safeColor else dangerColor,
        )
        Surface(
          color = (if (isSurplus) safeColor else dangerColor).copy(alpha = 0.14f),
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = if (isSurplus) surplusLabel else deficitLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isSurplus) safeColor else dangerColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(145.dp)
        .pointerInput(series) {
          detectTapGestures { offset ->
            if (series.isEmpty()) return@detectTapGestures
            val slotW = size.width / series.size.toFloat()
            selectedIdx = (offset.x / slotW).toInt().coerceIn(0, series.lastIndex)
          }
        },
    ) {
      if (series.isEmpty()) return@Canvas
      val padY = 12.dp.toPx()
      val usableH = (size.height - padY * 2f).coerceAtLeast(10f)
      val zeroY = when {
        hasPositive && hasNegative -> padY + usableH / 2f
        hasNegative -> padY
        else -> padY + usableH
      }
      val positiveScale = if (hasPositive && hasNegative) usableH / 2f else usableH
      val negativeScale = if (hasPositive && hasNegative) usableH / 2f else usableH

      drawLine(
        color = guideColor,
        start = Offset(0f, zeroY),
        end = Offset(size.width, zeroY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
      )

      val stepX = if (series.size > 1) size.width / (series.size - 1).toFloat() else size.width / 2f
      val points = series.mapIndexed { idx, (_, value) ->
        val x = if (series.size > 1) idx * stepX else size.width / 2f
        val y = if (value >= 0.0) {
          zeroY - ((value / maxAbs).toFloat() * positiveScale)
        } else {
          zeroY + ((-value / maxAbs).toFloat() * negativeScale)
        }
        Offset(x, y)
      }

      if (points.size >= 2) {
        val fillPath = androidx.compose.ui.graphics.Path().apply {
          moveTo(points.first().x, zeroY)
          points.forEach { pt -> lineTo(pt.x, pt.y) }
          lineTo(points.last().x, zeroY)
          close()
        }
        drawPath(
          path = fillPath,
          brush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
              lineStrokeColor.copy(alpha = 0.28f),
              lineStrokeColor.copy(alpha = 0.03f),
            ),
          ),
        )
        val strokePath = androidx.compose.ui.graphics.Path().apply {
          moveTo(points.first().x, points.first().y)
          for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
          }
        }
        drawPath(
          path = strokePath,
          color = lineStrokeColor,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
      }

      points.forEachIndexed { idx, pt ->
        val isSel = idx == selectedIdx
        val dotColor = if (series[idx].second >= 0.0) safeColor else dangerColor
        drawCircle(
          color = dotColor,
          radius = if (isSel) 6.dp.toPx() else 4.dp.toPx(),
          center = pt,
        )
        if (isSel) {
          drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = pt,
          )
        }
      }
    }

    Spacer(Modifier.height(6.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
      series.forEachIndexed { idx, (label, _) ->
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
            color = if (idx == selectedIdx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun IncomeVsExpenseChart(
  series: List<Triple<String, Double, Double>>,
  currencySymbol: String,
  incomeLabel: String,
  expenseLabel: String,
  barLabel: String,
  lineLabel: String,
  blurBalances: Boolean = false,
  modifier: Modifier = Modifier,
) {
  var useLineMode by remember { mutableStateOf(false) }
  var selectedIdx by remember(series) { mutableIntStateOf(series.lastIndex.coerceAtLeast(0)) }
  val maxVal = remember(series) {
    (series.maxOfOrNull { maxOf(it.second, it.third) } ?: 0.0).coerceAtLeast(1.0)
  }
  val incomeColor = com.example.ui.theme.SemanticSafeLight
  val expenseColor = com.example.ui.theme.SemanticDangerLight
  val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(Modifier.size(8.dp).clip(CircleShape).background(incomeColor))
          Text(incomeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(Modifier.size(8.dp).clip(CircleShape).background(expenseColor))
          Text(expenseLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (!useLineMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
          modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { useLineMode = false },
        ) {
          Text(
            text = barLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (!useLineMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (useLineMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
          modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { useLineMode = true },
        ) {
          Text(
            text = lineLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (useLineMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }
    }

    series.getOrNull(selectedIdx)?.let { active ->
      Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = active.first,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
        )
        SensitiveAmountText(
          text = "+${CurrencyFormat.format(active.second, currencySymbol)}",
          blurBalances = blurBalances,
          style = MaterialTheme.typography.labelMedium,
          color = incomeColor,
        )
        SensitiveAmountText(
          text = "-${CurrencyFormat.format(active.third, currencySymbol)}",
          blurBalances = blurBalances,
          style = MaterialTheme.typography.labelMedium,
          color = expenseColor,
        )
      }
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(145.dp)
        .pointerInput(series) {
          detectTapGestures { offset ->
            if (series.isEmpty()) return@detectTapGestures
            val slotW = size.width / series.size.toFloat()
            selectedIdx = (offset.x / slotW).toInt().coerceIn(0, series.lastIndex)
          }
        },
    ) {
      if (series.isEmpty()) return@Canvas
      val chartH = size.height
      if (!useLineMode) {
        val slotW = size.width / series.size.toFloat()
        val barW = (slotW * 0.28f).coerceAtMost(18.dp.toPx())
        val gap = 3.dp.toPx()
        series.forEachIndexed { idx, (_, inc, exp) ->
          val cx = idx * slotW + slotW / 2f
          val incX = cx - barW - gap / 2f
          val expX = cx + gap / 2f
          drawRoundRect(
            color = trackColor,
            topLeft = Offset(incX, 0f),
            size = Size(barW, chartH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
          )
          drawRoundRect(
            color = trackColor,
            topLeft = Offset(expX, 0f),
            size = Size(barW, chartH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
          )
          if (inc > 0.0) {
            val h = chartH * (inc / maxVal).toFloat().coerceIn(0.06f, 1f)
            drawRoundRect(
              color = if (idx == selectedIdx) incomeColor else incomeColor.copy(alpha = 0.65f),
              topLeft = Offset(incX, chartH - h),
              size = Size(barW, h),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
          }
          if (exp > 0.0) {
            val h = chartH * (exp / maxVal).toFloat().coerceIn(0.06f, 1f)
            drawRoundRect(
              color = if (idx == selectedIdx) expenseColor else expenseColor.copy(alpha = 0.65f),
              topLeft = Offset(expX, chartH - h),
              size = Size(barW, h),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
          }
        }
      } else {
        val padY = 10.dp.toPx()
        val usableH = (chartH - padY * 2f).coerceAtLeast(10f)
        val stepX = if (series.size > 1) size.width / (series.size - 1).toFloat() else size.width / 2f
        val incPts = series.mapIndexed { idx, (_, inc, _) ->
          val x = if (series.size > 1) idx * stepX else size.width / 2f
          val y = padY + usableH * (1f - (inc / maxVal).toFloat().coerceIn(0f, 1f))
          Offset(x, y)
        }
        val expPts = series.mapIndexed { idx, (_, _, exp) ->
          val x = if (series.size > 1) idx * stepX else size.width / 2f
          val y = padY + usableH * (1f - (exp / maxVal).toFloat().coerceIn(0f, 1f))
          Offset(x, y)
        }
        if (series.size >= 2) {
          val incPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(incPts.first().x, incPts.first().y)
            for (i in 1 until incPts.size) lineTo(incPts[i].x, incPts[i].y)
          }
          val expPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(expPts.first().x, expPts.first().y)
            for (i in 1 until expPts.size) lineTo(expPts[i].x, expPts[i].y)
          }
          drawPath(incPath, color = incomeColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
          drawPath(expPath, color = expenseColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        incPts.forEachIndexed { idx, pt ->
          drawCircle(incomeColor, radius = if (idx == selectedIdx) 5.dp.toPx() else 3.dp.toPx(), center = pt)
        }
        expPts.forEachIndexed { idx, pt ->
          drawCircle(expenseColor, radius = if (idx == selectedIdx) 5.dp.toPx() else 3.dp.toPx(), center = pt)
        }
      }
    }

    Spacer(Modifier.height(6.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
      series.forEachIndexed { idx, (label, _, _) ->
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
            color = if (idx == selectedIdx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
