package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryCatalog
import com.example.data.model.DateFormatUtils
import com.example.data.model.PeriodFilter
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily

@Composable
fun rememberHapticTrigger(enabled: Boolean): () -> Unit {
  val haptic = LocalHapticFeedback.current
  return remember(enabled, haptic) {
    {
      if (enabled) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      }
    }
  }
}

@Composable
fun SensitiveAmountText(
  text: String,
  blurBalances: Boolean,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  color: Color = Color.Unspecified,
  fontWeight: FontWeight? = null,
  textAlign: TextAlign? = null,
  maxLines: Int = 1,
) {
  var revealed by remember(blurBalances) { mutableStateOf(false) }
  val display = if (blurBalances && !revealed) "••••••" else text
  Text(
    text = display,
    fontFamily = HisabMonoFontFamily,
    style = style,
    color = color,
    fontWeight = fontWeight,
    textAlign = textAlign,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
    modifier = if (blurBalances) {
      modifier
        .clip(RoundedCornerShape(6.dp))
        .clickable { revealed = !revealed }
    } else {
      modifier
    },
  )
}

@Composable
fun PeriodFilterStrip(
  selectedPeriod: PeriodFilter,
  selectedMonthKey: String,
  langCode: String,
  onSelectPeriod: (PeriodFilter) -> Unit,
  onShiftMonth: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      val items = listOf(
        PeriodFilter.THIS_MONTH to strings.periodThisMonth,
        PeriodFilter.LAST_MONTH to strings.periodLastMonth,
        PeriodFilter.THIS_QUARTER to strings.periodThisQuarter,
        PeriodFilter.THIS_YEAR to strings.periodThisYear,
        PeriodFilter.CUSTOM_MONTH to strings.periodCustomMonth,
      )
      items.forEach { (period, label) ->
        FilterChip(
          selected = selectedPeriod == period,
          onClick = { onSelectPeriod(period) },
          label = { Text(label, maxLines = 1) },
          modifier = Modifier.testTag("period_chip_${period.id}"),
        )
      }
    }

    if (selectedPeriod == PeriodFilter.CUSTOM_MONTH) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = { onShiftMonth(-1) },
            modifier = Modifier.testTag("period_prev_month_button"),
          ) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
          }
          Text(
            text = DateFormatUtils.formatMonthLabel(selectedMonthKey, langCode),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
          IconButton(
            onClick = { onShiftMonth(1) },
            modifier = Modifier.testTag("period_next_month_button"),
          ) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorSwatchPicker(
  selectedColorHex: Long,
  onSelectColor: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  var hexInput by remember(selectedColorHex) {
    mutableStateOf("#%06X".format(selectedColorHex and 0xFFFFFF))
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CategoryCatalog.colorOptions.forEach { hex ->
        val isSelected = selectedColorHex == hex
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(hex))
            .border(
              width = if (isSelected) 3.dp else 0.dp,
              color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
              shape = CircleShape,
            )
            .clickable { onSelectColor(hex) }
            .testTag("color_swatch_${hex.toString(16)}"),
          contentAlignment = Alignment.Center,
        ) {
          if (isSelected) {
            Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    }

    OutlinedTextField(
      value = hexInput,
      onValueChange = { raw ->
        val clean = raw.uppercase().filter { it == '#' || it in '0'..'9' || it in 'A'..'F' }.take(7)
        hexInput = clean
        val digits = clean.removePrefix("#")
        if (digits.length == 6) {
          digits.toLongOrNull(16)?.let { rgb ->
            onSelectColor(0xFF000000L or rgb)
          }
        }
      },
      label = { Text(strings.customHexHint) },
      leadingIcon = {
        Box(
          modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color(selectedColorHex)),
        )
      },
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("custom_hex_input"),
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconPickerDialog(
  selectedIconKey: String,
  langCode: String,
  onDismiss: () -> Unit,
  onSelectIcon: (String) -> Unit,
) {
  val strings = LocalStrings.current
  var query by remember { mutableStateOf("") }
  var selectedGroupId by remember { mutableStateOf<String?>(null) }

  val filteredGroups = remember(query, selectedGroupId) {
    val q = query.trim().lowercase()
    CategoryCatalog.iconGroups
      .filter { selectedGroupId == null || it.id == selectedGroupId }
      .mapNotNull { group ->
        val matching = if (q.isEmpty()) {
          group.icons
        } else {
          group.icons.filter {
            it.key.contains(q, ignoreCase = true) ||
              it.labelEn.contains(q, ignoreCase = true) ||
              it.labelBn.contains(q, ignoreCase = true) ||
              it.keywords.contains(q, ignoreCase = true)
          }
        }
        if (matching.isEmpty()) null else group.copy(icons = matching)
      }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.chooseIcon) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 430.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text(strings.searchIconsHint) },
          leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.searchIconsHint) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("icon_picker_search"),
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          FilterChip(
            selected = selectedGroupId == null,
            onClick = { selectedGroupId = null },
            label = { Text(strings.allFilterTab) },
          )
          CategoryCatalog.iconGroups.forEach { group ->
            FilterChip(
              selected = selectedGroupId == group.id,
              onClick = { selectedGroupId = if (selectedGroupId == group.id) null else group.id },
              label = { Text(if (langCode == "bn") group.labelBn else group.labelEn) },
            )
          }
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          filteredGroups.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = if (langCode == "bn") group.labelBn else group.labelEn,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
              )
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                group.icons.forEach { icon ->
                  val isSelected = selectedIconKey == icon.key
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                      .minimumInteractiveComponentSize()
                      .size(48.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .clickable {
                        onSelectIcon(icon.key)
                        onDismiss()
                      }
                      .testTag("icon_choice_${icon.key}"),
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = icon.vector,
                        contentDescription = if (langCode == "bn") icon.labelBn else icon.labelEn,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    },
  )
}
