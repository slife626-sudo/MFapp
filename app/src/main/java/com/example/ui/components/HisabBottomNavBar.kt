package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RibbonNavItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

@Composable
fun HisabRibbonBottomBar(
  items: List<RibbonNavItem>,
  currentRoute: String,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val barSurfaceColor = MaterialTheme.colorScheme.surface
  val primaryColor = MaterialTheme.colorScheme.primary
  val ribbonTopColor = lerp(primaryColor, Color.White, 0.16f)
  val ribbonBottomColor = lerp(primaryColor, Color.Black, 0.14f)
  val ribbonFoldColor = lerp(primaryColor, Color.Black, 0.38f)
  val onRibbonColor = Color.White
  val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Alignment.BottomCenter,
  ) {
    // Background bar surface starts 10.dp below the top so active ribbon visibly rises above the bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .matchParentSize(),
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        color = barSurfaceColor,
        tonalElevation = 3.dp,
        shadowElevation = 10.dp,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
      ) {}
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(NavigationBarDefaults.windowInsets)
        .height(74.dp)
        .padding(horizontal = 4.dp),
      verticalAlignment = Alignment.Bottom,
    ) {
      items.forEach { item ->
        val isSelected = currentRoute == item.route
        val activeProgress by animateFloatAsState(
          targetValue = if (isSelected) 1f else 0f,
          animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
          label = "ribbon_tab_${item.route}",
        )
        val interactionSource = remember { MutableInteractionSource() }

        Box(
          modifier = Modifier
            .weight(1f)
            .height(74.dp)
            .semantics { selected = isSelected }
            .clickable(
              interactionSource = interactionSource,
              indication = if (isSelected) null else ripple(bounded = true),
              role = Role.Tab,
              onClick = { onNavigate(item.route) },
            )
            .testTag("nav_${item.route}"),
          contentAlignment = Alignment.Center,
        ) {
          if (activeProgress > 0.02f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val risePx = 10.dp.toPx()
              val animTopY = risePx * (1f - activeProgress)
              val bottomY = h - 2.dp.toPx()
              val padX = 4.dp.toPx()
              val slantPx = 4.5.dp.toPx() * activeProgress
              val corner = 7.dp.toPx()
              val earOut = 5.dp.toPx() * activeProgress

              val leftTopX = padX + slantPx
              val rightTopX = (w - padX - 1.5.dp.toPx()).coerceAtLeast(leftTopX + 24f)
              val rightBottomX = (rightTopX - slantPx).coerceAtLeast(padX + 20f)
              val leftBottomX = padX

              // Folded dark ear tucked behind the top-right edge resting on the bar top line
              if (risePx - animTopY > 2f) {
                val earPath = Path().apply {
                  moveTo(rightTopX - 2.dp.toPx(), animTopY + 0.5.dp.toPx())
                  lineTo(rightTopX + earOut, animTopY + 2.5.dp.toPx())
                  lineTo(rightTopX - 0.5.dp.toPx(), risePx)
                  close()
                }
                drawPath(
                  path = earPath,
                  color = ribbonFoldColor.copy(alpha = activeProgress),
                )
              }

              // Subtle shadow under the raised ribbon card
              val shadowPath = buildSlantedRoundedRectPath(
                tlX = leftTopX,
                tlY = animTopY + 2.dp.toPx(),
                trX = rightTopX,
                trY = animTopY + 2.dp.toPx(),
                brX = rightBottomX,
                brY = bottomY + 1.dp.toPx(),
                blX = leftBottomX,
                blY = bottomY + 1.dp.toPx(),
                radius = corner,
              )
              drawPath(
                path = shadowPath,
                color = Color.Black.copy(alpha = 0.16f * activeProgress),
              )

              // Main 3D gradient ribbon body
              val bodyPath = buildSlantedRoundedRectPath(
                tlX = leftTopX,
                tlY = animTopY,
                trX = rightTopX,
                trY = animTopY,
                brX = rightBottomX,
                brY = bottomY,
                blX = leftBottomX,
                blY = bottomY,
                radius = corner,
              )
              drawPath(
                path = bodyPath,
                brush = Brush.linearGradient(
                  colors = listOf(
                    ribbonTopColor.copy(alpha = activeProgress),
                    ribbonBottomColor.copy(alpha = activeProgress),
                  ),
                  start = Offset(leftTopX, animTopY),
                  end = Offset(rightBottomX, bottomY),
                ),
              )
            }
          }

          // Icon + Label: sits in bottom 64dp when unselected, smoothly lifts slightly inside ribbon when selected
          val topOffsetDp = (10f - 8f * activeProgress).dp
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(top = topOffsetDp, bottom = 4.dp, start = 4.dp, end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
              contentDescription = item.label,
              tint = if (isSelected) onRibbonColor else inactiveColor,
              modifier = Modifier.size(if (isSelected) 24.dp else 22.dp),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = item.label,
              color = if (isSelected) onRibbonColor else inactiveColor,
              fontSize = 11.sp,
              lineHeight = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              maxLines = 1,
              softWrap = false,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

private fun buildSlantedRoundedRectPath(
  tlX: Float,
  tlY: Float,
  trX: Float,
  trY: Float,
  brX: Float,
  brY: Float,
  blX: Float,
  blY: Float,
  radius: Float,
): Path {
  val r = radius.coerceAtLeast(0f)
  return Path().apply {
    moveTo(tlX + r, tlY)
    lineTo(trX - r, trY)
    quadraticTo(trX, trY, trX - (trX - brX) * 0.08f, trY + r)
    lineTo(brX + (trX - brX) * 0.08f, brY - r)
    quadraticTo(brX, brY, brX - r, brY)
    lineTo(blX + r, blY)
    quadraticTo(blX, blY, blX + (tlX - blX) * 0.08f, blY - r)
    lineTo(tlX - (tlX - blX) * 0.08f, tlY + r)
    quadraticTo(tlX, tlY, tlX + r, tlY)
    close()
  }
}
