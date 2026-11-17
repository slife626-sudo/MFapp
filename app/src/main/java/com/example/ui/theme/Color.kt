package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AppThemePalette(val id: String) {
  EMERALD("emerald"),
  INDIGO("indigo"),
  TERRACOTTA("terracotta"),
  OCEAN("ocean"),
  PLUM("plum");

  companion object {
    fun fromId(id: String?): AppThemePalette =
      entries.firstOrNull { it.id == id } ?: EMERALD
  }
}

enum class ThemeMode(val id: String) {
  SYSTEM("system"),
  LIGHT("light"),
  DARK("dark");

  companion object {
    fun fromId(id: String?): ThemeMode =
      entries.firstOrNull { it.id == id } ?: SYSTEM
  }
}

// Semantic financial feedback colors
val SemanticSafeLight = Color(0xFF0E7C5B)
val SemanticSafeDark = Color(0xFF5CE0B3)
val SemanticWarningLight = Color(0xFFD97706)
val SemanticWarningDark = Color(0xFFFBBF24)
val SemanticDangerLight = Color(0xFFDC2626)
val SemanticDangerDark = Color(0xFFF87171)

// Curated chart palette colors
val ChartPalette = listOf(
  Color(0xFF0E7C5B),
  Color(0xFF2563EB),
  Color(0xFFD97706),
  Color(0xFFDC2626),
  Color(0xFF7C3AED),
  Color(0xFF0891B2),
  Color(0xFFDB2777),
  Color(0xFF4F46E5),
  Color(0xFF65A30D),
  Color(0xFFEA580C),
)

fun paletteSwatchColor(palette: AppThemePalette): Color = when (palette) {
  AppThemePalette.EMERALD -> Color(0xFF0B5D4B)
  AppThemePalette.INDIGO -> Color(0xFF2E3A8C)
  AppThemePalette.TERRACOTTA -> Color(0xFFA8442A)
  AppThemePalette.OCEAN -> Color(0xFF0D5C75)
  AppThemePalette.PLUM -> Color(0xFF6B2D5C)
}

fun lightSchemeFor(palette: AppThemePalette): ColorScheme = when (palette) {
  AppThemePalette.EMERALD -> lightColorScheme(
    primary = Color(0xFF0B5D4B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBDF2DF),
    onPrimaryContainer = Color(0xFF002119),
    secondary = Color(0xFF3E6358),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFEAE0),
    onSecondaryContainer = Color(0xFF0C201A),
    tertiary = Color(0xFFB4690E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE0B8),
    onTertiaryContainer = Color(0xFF311900),
    background = Color(0xFFF6F8F6),
    onBackground = Color(0xFF151C19),
    surface = Color(0xFFF6F8F6),
    onSurface = Color(0xFF151C19),
    surfaceVariant = Color(0xFFE3EBE7),
    onSurfaceVariant = Color(0xFF404A45),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F4F1),
    surfaceContainer = Color(0xFFE8EFEA),
    surfaceContainerHigh = Color(0xFFDFE8E2),
    outline = Color(0xFF707B75),
    outlineVariant = Color(0xFFC1CCC5),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
  )

  AppThemePalette.INDIGO -> lightColorScheme(
    primary = Color(0xFF2E3A8C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE1FF),
    onPrimaryContainer = Color(0xFF06105B),
    secondary = Color(0xFF555A78),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E2F8),
    onSecondaryContainer = Color(0xFF131830),
    tertiary = Color(0xFF0B7A75),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC1F2EE),
    onTertiaryContainer = Color(0xFF00201E),
    background = Color(0xFFF7F7FC),
    onBackground = Color(0xFF171923),
    surface = Color(0xFFF7F7FC),
    onSurface = Color(0xFF171923),
    surfaceVariant = Color(0xFFE4E6F1),
    onSurfaceVariant = Color(0xFF444757),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F2F9),
    surfaceContainer = Color(0xFFE9EBF5),
    surfaceContainerHigh = Color(0xFFE0E3F0),
    outline = Color(0xFF747789),
    outlineVariant = Color(0xFFC4C6D7),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
  )

  AppThemePalette.TERRACOTTA -> lightColorScheme(
    primary = Color(0xFFA8442A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBD1),
    onPrimaryContainer = Color(0xFF3C0800),
    secondary = Color(0xFF77574D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBD0),
    onSecondaryContainer = Color(0xFF2C160E),
    tertiary = Color(0xFF6B5E10),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5E388),
    onTertiaryContainer = Color(0xFF211B00),
    background = Color(0xFFFBF8F6),
    onBackground = Color(0xFF201A18),
    surface = Color(0xFFFBF8F6),
    onSurface = Color(0xFF201A18),
    surfaceVariant = Color(0xFFF2E2DC),
    onSurfaceVariant = Color(0xFF52433E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F1EE),
    surfaceContainer = Color(0xFFF1E9E5),
    surfaceContainerHigh = Color(0xFFEBE1DC),
    outline = Color(0xFF85736D),
    outlineVariant = Color(0xFFD7C2BB),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
  )

  AppThemePalette.OCEAN -> lightColorScheme(
    primary = Color(0xFF0D5C75),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC2ECFF),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF4C616B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFE6F1),
    onSecondaryContainer = Color(0xFF081E27),
    tertiary = Color(0xFF5C5B80),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE2DFFF),
    onTertiaryContainer = Color(0xFF191839),
    background = Color(0xFFF5F9FB),
    onBackground = Color(0xFF141D21),
    surface = Color(0xFFF5F9FB),
    onSurface = Color(0xFF141D21),
    surfaceVariant = Color(0xFFDFE9EE),
    onSurfaceVariant = Color(0xFF404A50),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF4F7),
    surfaceContainer = Color(0xFFE7EFF3),
    surfaceContainerHigh = Color(0xFFDFE9EE),
    outline = Color(0xFF707B81),
    outlineVariant = Color(0xFFBFC9CF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
  )

  AppThemePalette.PLUM -> lightColorScheme(
    primary = Color(0xFF6B2D5C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD8F0),
    onPrimaryContainer = Color(0xFF2B0022),
    secondary = Color(0xFF6E5766),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF8DAEC),
    onSecondaryContainer = Color(0xFF271522),
    tertiary = Color(0xFF815338),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBC7),
    onTertiaryContainer = Color(0xFF321201),
    background = Color(0xFFFAF6F8),
    onBackground = Color(0xFF1F191D),
    surface = Color(0xFFFAF6F8),
    onSurface = Color(0xFF1F191D),
    surfaceVariant = Color(0xFFEEE0E9),
    onSurfaceVariant = Color(0xFF4E434A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5EFF3),
    surfaceContainer = Color(0xFFEFE7EC),
    surfaceContainerHigh = Color(0xFFE9DFE5),
    outline = Color(0xFF80737B),
    outlineVariant = Color(0xFFD1C2CB),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
  )
}

fun darkSchemeFor(palette: AppThemePalette): ColorScheme = when (palette) {
  AppThemePalette.EMERALD -> darkColorScheme(
    primary = Color(0xFF6CDDB8),
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF005140),
    onPrimaryContainer = Color(0xFFBDF2DF),
    secondary = Color(0xFFB3CEC4),
    onSecondary = Color(0xFF1E352D),
    secondaryContainer = Color(0xFF344C43),
    onSecondaryContainer = Color(0xFFCFEAE0),
    tertiary = Color(0xFFFFB966),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF693B00),
    onTertiaryContainer = Color(0xFFFFE0B8),
    background = Color(0xFF0D1412),
    onBackground = Color(0xFFE2EAE5),
    surface = Color(0xFF0D1412),
    onSurface = Color(0xFFE2EAE5),
    surfaceVariant = Color(0xFF2B3632),
    onSurfaceVariant = Color(0xFFBDC9C2),
    surfaceContainerLowest = Color(0xFF090F0D),
    surfaceContainerLow = Color(0xFF141D1A),
    surfaceContainer = Color(0xFF1A2420),
    surfaceContainerHigh = Color(0xFF232F2A),
    outline = Color(0xFF87948D),
    outlineVariant = Color(0xFF3C4943),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
  )

  AppThemePalette.INDIGO -> darkColorScheme(
    primary = Color(0xFFBAC3FF),
    onPrimary = Color(0xFF101C6E),
    primaryContainer = Color(0xFF283486),
    onPrimaryContainer = Color(0xFFDEE1FF),
    secondary = Color(0xFFC4C6DC),
    onSecondary = Color(0xFF2D3042),
    secondaryContainer = Color(0xFF434659),
    onSecondaryContainer = Color(0xFFE0E2F8),
    tertiary = Color(0xFF7BD6D0),
    onTertiary = Color(0xFF003734),
    tertiaryContainer = Color(0xFF00504C),
    onTertiaryContainer = Color(0xFFC1F2EE),
    background = Color(0xFF0E1018),
    onBackground = Color(0xFFE4E5F0),
    surface = Color(0xFF0E1018),
    onSurface = Color(0xFFE4E5F0),
    surfaceVariant = Color(0xFF2D303F),
    onSurfaceVariant = Color(0xFFC2C4D4),
    surfaceContainerLowest = Color(0xFF090B12),
    surfaceContainerLow = Color(0xFF151823),
    surfaceContainer = Color(0xFF1B1E2B),
    surfaceContainerHigh = Color(0xFF242838),
    outline = Color(0xFF8B8E9F),
    outlineVariant = Color(0xFF404457),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
  )

  AppThemePalette.TERRACOTTA -> darkColorScheme(
    primary = Color(0xFFFFB4A1),
    onPrimary = Color(0xFF621400),
    primaryContainer = Color(0xFF882D16),
    onPrimaryContainer = Color(0xFFFFDBD1),
    secondary = Color(0xFFE7BDB1),
    onSecondary = Color(0xFF442A21),
    secondaryContainer = Color(0xFF5D3F36),
    onSecondaryContainer = Color(0xFFFFDBD0),
    tertiary = Color(0xFFD8C66E),
    onTertiary = Color(0xFF393000),
    tertiaryContainer = Color(0xFF524600),
    onTertiaryContainer = Color(0xFFF5E388),
    background = Color(0xFF16110F),
    onBackground = Color(0xFFEDE0DC),
    surface = Color(0xFF16110F),
    onSurface = Color(0xFFEDE0DC),
    surfaceVariant = Color(0xFF3B2E29),
    onSurfaceVariant = Color(0xFFD6C2BC),
    surfaceContainerLowest = Color(0xFF100C0A),
    surfaceContainerLow = Color(0xFF1E1715),
    surfaceContainer = Color(0xFF251D1A),
    surfaceContainerHigh = Color(0xFF302623),
    outline = Color(0xFF9F8D87),
    outlineVariant = Color(0xFF51423D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
  )

  AppThemePalette.OCEAN -> darkColorScheme(
    primary = Color(0xFF7FD1EF),
    onPrimary = Color(0xFF003546),
    primaryContainer = Color(0xFF004D64),
    onPrimaryContainer = Color(0xFFC2ECFF),
    secondary = Color(0xFFB3CAD5),
    onSecondary = Color(0xFF1E333C),
    secondaryContainer = Color(0xFF354A53),
    onSecondaryContainer = Color(0xFFCFE6F1),
    tertiary = Color(0xFFC5C2EE),
    onTertiary = Color(0xFF2D2C4E),
    tertiaryContainer = Color(0xFF444366),
    onTertiaryContainer = Color(0xFFE2DFFF),
    background = Color(0xFF0C1317),
    onBackground = Color(0xFFDFEAEE),
    surface = Color(0xFF0C1317),
    onSurface = Color(0xFFDFEAEE),
    surfaceVariant = Color(0xFF2A363C),
    onSurfaceVariant = Color(0xFFBCC9CF),
    surfaceContainerLowest = Color(0xFF080E11),
    surfaceContainerLow = Color(0xFF131C21),
    surfaceContainer = Color(0xFF192329),
    surfaceContainerHigh = Color(0xFF222E35),
    outline = Color(0xFF86939A),
    outlineVariant = Color(0xFF3D4950),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
  )

  AppThemePalette.PLUM -> darkColorScheme(
    primary = Color(0xFFF9B0E3),
    onPrimary = Color(0xFF4E1441),
    primaryContainer = Color(0xFF682A58),
    onPrimaryContainer = Color(0xFFFFD8F0),
    secondary = Color(0xFFDBC0D1),
    onSecondary = Color(0xFF3D2A37),
    secondaryContainer = Color(0xFF55404E),
    onSecondaryContainer = Color(0xFFF8DAEC),
    tertiary = Color(0xFFF6B898),
    onTertiary = Color(0xFF4C260F),
    tertiaryContainer = Color(0xFF663B23),
    onTertiaryContainer = Color(0xFFFFDBC7),
    background = Color(0xFF151014),
    onBackground = Color(0xFFECE0E7),
    surface = Color(0xFF151014),
    onSurface = Color(0xFFECE0E7),
    surfaceVariant = Color(0xFF392D34),
    onSurfaceVariant = Color(0xFFD0C1CA),
    surfaceContainerLowest = Color(0xFF0F0B0E),
    surfaceContainerLow = Color(0xFF1D161B),
    surfaceContainer = Color(0xFF241C21),
    surfaceContainerHigh = Color(0xFF2F252C),
    outline = Color(0xFF998B93),
    outlineVariant = Color(0xFF4D4048),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
  )
}
