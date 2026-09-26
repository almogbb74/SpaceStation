package com.almog.moonboard.android.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.almog.moonboard.android.R

val IbmPlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_medium, weight = FontWeight.Medium),
    Font(R.font.ibm_plex_mono_bold, weight = FontWeight.Bold),
)

@OptIn(ExperimentalTextApi::class)
val UnboundedExtraBold = FontFamily(
    Font(
        R.font.unbounded,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800)),
    )
)
