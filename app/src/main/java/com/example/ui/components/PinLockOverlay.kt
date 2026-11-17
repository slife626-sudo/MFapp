package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.theme.HisabMonoFontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PinLockOverlay(
  onVerifyPin: (String) -> Boolean,
) {
  val strings = LocalStrings.current
  val scope = rememberCoroutineScope()
  var enteredPin by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }
  val shakeOffset = remember { Animatable(0f) }

  fun submitDigit(digit: String) {
    if (enteredPin.length >= 4) return
    isError = false
    val next = enteredPin + digit
    enteredPin = next
    if (next.length == 4) {
      val ok = onVerifyPin(next)
      if (!ok) {
        isError = true
        enteredPin = ""
        scope.launch {
          for (delta in listOf(-18f, 18f, -12f, 12f, -6f, 0f)) {
            shakeOffset.animateTo(delta, tween(45))
          }
        }
      }
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Lock,
          contentDescription = strings.enterPinTitle,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(30.dp),
        )
      }

      Spacer(Modifier.height(16.dp))
      Text(
        text = strings.appName,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(Modifier.height(6.dp))
      Text(
        text = if (isError) strings.wrongPin else strings.enterPinTitle,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )

      Spacer(Modifier.height(24.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
      ) {
        repeat(4) { index ->
          val filled = index < enteredPin.length
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(
                when {
                  isError -> MaterialTheme.colorScheme.error
                  filled -> MaterialTheme.colorScheme.primary
                  else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
              )
              .border(
                width = 1.5.dp,
                color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape,
              )
          )
        }
      }

      Spacer(Modifier.height(32.dp))

      PinKeypad(
        onDigit = ::submitDigit,
        onBackspace = {
          if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
          }
        },
        modifier = Modifier.widthIn(max = 300.dp),
      )
    }
  }
}

@Composable
fun SetupPinDialog(
  onDismiss: () -> Unit,
  onPinConfirmed: (String) -> Unit,
) {
  val strings = LocalStrings.current
  var firstPin by remember { mutableStateOf<String?>(null) }
  var currentInput by remember { mutableStateOf("") }
  var mismatchError by remember { mutableStateOf(false) }

  fun onDigit(digit: String) {
    if (currentInput.length >= 4) return
    mismatchError = false
    val next = currentInput + digit
    currentInput = next
    if (next.length == 4) {
      if (firstPin == null) {
        firstPin = next
        currentInput = ""
      } else if (firstPin == next) {
        onPinConfirmed(next)
      } else {
        firstPin = null
        currentInput = ""
        mismatchError = true
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (firstPin == null) strings.createPinTitle else strings.confirmPinTitle,
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (mismatchError) {
          Text(
            text = strings.pinMismatch,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp),
          )
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.padding(vertical = 12.dp),
        ) {
          repeat(4) { idx ->
            val filled = idx < currentInput.length
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                  if (filled) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
          }
        }
        Spacer(Modifier.height(12.dp))
        PinKeypad(
          onDigit = ::onDigit,
          onBackspace = {
            if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1)
          },
          modifier = Modifier.widthIn(max = 260.dp),
        )
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    },
  )
}

@Composable
private fun PinKeypad(
  onDigit: (String) -> Unit,
  onBackspace: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rows = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("", "0", "DEL"),
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    rows.forEach { row ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        row.forEach { key ->
          when (key) {
            "" -> Spacer(Modifier.size(64.dp))
            "DEL" -> Box(
              modifier = Modifier
                .size(64.dp)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickable(onClick = onBackspace)
                .testTag("pin_key_backspace"),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = "Backspace",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            else -> Surface(
              modifier = Modifier
                .size(64.dp)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickable { onDigit(key) }
                .testTag("pin_key_$key"),
              shape = RoundedCornerShape(32.dp),
              color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = key,
                  fontFamily = HisabMonoFontFamily,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }
    }
  }
}
